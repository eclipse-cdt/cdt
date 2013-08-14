/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import static org.eclipse.cdt.core.index.IndexLocationFactory.getAbsolutePath;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.ui.text.SharedASTJob;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.corext.codemanipulation.AddIncludesOperation;
import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.internal.corext.codemanipulation.InclusionContext;

import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

/**
 * Adds an include statement and, optionally, a 'using' declaration for the currently
 * selected name.
 */
public class AddIncludeOnSelectionAction extends TextEditorAction {
	public static boolean sIsJUnitTest = false;

	private ITranslationUnit fTu;
	private IProject fProject;
	private final List<IncludeInfo> fRequiredIncludes = new ArrayList<IncludeInfo>();
	private final List<String> fUsingDeclarations = new ArrayList<String>();
	protected InclusionContext fContext;

	public AddIncludeOnSelectionAction(ITextEditor editor) {
		super(CEditorMessages.getBundleForConstructedKeys(), "AddIncludeOnSelection.", editor); //$NON-NLS-1$

		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(this,
				ICHelpContextIds.ADD_INCLUDE_ON_SELECTION_ACTION);
	}

	private void insertInclude(List<IncludeInfo> includes, List<String> usings, int beforeOffset) {
		AddIncludesOperation op= new AddIncludesOperation(fTu, beforeOffset, includes, usings);
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(
					PlatformUI.getWorkbench().getProgressService(),
					new WorkbenchRunnableAdapter(op), op.getSchedulingRule());
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), CEditorMessages.AddIncludeOnSelection_error_title,
					CEditorMessages.AddIncludeOnSelection_insertion_failed); 
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}
	}

	private static ITranslationUnit getTranslationUnit(ITextEditor editor) {
		if (editor == null) {
			return null;
		}
		return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
	}

	private Shell getShell() {
		return getTextEditor().getSite().getShell();
	}

	@Override
	public void run() {
		fTu = getTranslationUnit(getTextEditor());
		if (fTu == null) {
			return;
		}
		fProject = fTu.getCProject().getProject();

		try {
			final ISelection selection= getTextEditor().getSelectionProvider().getSelection();
			if (selection.isEmpty() || !(selection instanceof ITextSelection)) {
				return;
			}
			if (!validateEditorInputState()) {
				return;
			}

			final String[] lookupName = new String[1];
			final IIndex index= CCorePlugin.getIndexManager().getIndex(fTu.getCProject(), IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT);
			SharedASTJob job = new SharedASTJob(CEditorMessages.AddIncludeOnSelection_label, fTu) {
				@Override
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
					deduceInclude((ITextSelection) selection, index, ast, lookupName);
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			job.join();

			if (fRequiredIncludes.isEmpty() && lookupName[0].length() > 0) {
				// Try contribution from plug-ins.
				IFunctionSummary fs = findContribution(lookupName[0]);
				if (fs != null) {
					IRequiredInclude[] functionIncludes = fs.getIncludes();
					if (functionIncludes != null) {
						for (IRequiredInclude include : functionIncludes) {
							fRequiredIncludes.add(new IncludeInfo(include.getIncludeName(), include.isStandard()));
						}
					}
					String ns = fs.getNamespace();
					if (ns != null && ns.length() > 0) {
						fUsingDeclarations.add(fs.getNamespace());
					}
				}

			}
			if (!fRequiredIncludes.isEmpty()) {
				insertInclude(fRequiredIncludes, fUsingDeclarations, ((ITextSelection) selection).getOffset());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (CoreException e) {
			CUIPlugin.log("Cannot perform 'Add Include'", e); //$NON-NLS-1$
		}
	}

	/**
	 * Extracts the includes for the given selection.  This can be both used to perform
	 * the work as well as being invoked when there is a change.
	 * @param selection a text selection.
	 * @param ast an AST.
	 * @param lookupName a one-element array used to return the selected name.
	 */
	private void deduceInclude(ITextSelection selection, IIndex index, IASTTranslationUnit ast, String[] lookupName)
			throws CoreException {
		fContext = new InclusionContext(fTu);
		IASTNodeSelector selector = ast.getNodeSelector(fTu.getLocation().toOSString());
		IASTName name = selector.findEnclosingName(selection.getOffset(), selection.getLength());
		if (name == null) {
			return;
		}
		char[] nameChars = name.toCharArray();
		lookupName[0] = new String(nameChars);
		IBinding binding = name.resolveBinding();
		if (binding instanceof ICPPVariable) {
			IType type = ((ICPPVariable) binding).getType();
			type = SemanticUtil.getNestedType(type,
					SemanticUtil.ALLCVQ | SemanticUtil.PTR | SemanticUtil.ARRAY | SemanticUtil.REF);
			if (type instanceof IBinding) {
				binding = (IBinding) type;
				nameChars = binding.getNameCharArray();
			}
		}
		if (nameChars.length == 0) {
			return;
		}

		final Map<String, IncludeCandidate> candidatesMap= new HashMap<String, IncludeCandidate>();
		final IndexFilter filter = IndexFilter.getDeclaredBindingFilter(ast.getLinkage().getLinkageID(), false);
		
		List<IIndexBinding> bindings = new ArrayList<IIndexBinding>();
		IIndexBinding adaptedBinding= index.adaptBinding(binding);
		if (adaptedBinding == null) {
			bindings.addAll(Arrays.asList(index.findBindings(nameChars, false, filter, new NullProgressMonitor())));
		} else {
			bindings.add(adaptedBinding);
			while (adaptedBinding instanceof ICPPSpecialization) {
				adaptedBinding= index.adaptBinding(((ICPPSpecialization) adaptedBinding).getSpecializedBinding());
				if (adaptedBinding != null) {
					bindings.add(adaptedBinding);
				}
			}
		}

		for (IIndexBinding indexBinding : bindings) {
			// Replace ctor with the class itself.
			if (indexBinding instanceof ICPPConstructor) {
				indexBinding = indexBinding.getOwner();
			}
			IIndexName[] definitions= null;
			// class, struct, union, enum-type, enum-item
			if (indexBinding instanceof ICompositeType || indexBinding instanceof IEnumeration || indexBinding instanceof IEnumerator) {
				definitions= index.findDefinitions(indexBinding);
			} else if (indexBinding instanceof ITypedef || (indexBinding instanceof IFunction)) {
				definitions = index.findDeclarations(indexBinding);
			}
			if (definitions != null) {
				for (IIndexName definition : definitions) {
					considerForInclusion(definition, indexBinding, index, candidatesMap);
				}
				if (definitions.length > 0 && adaptedBinding != null) 
					break;
			}
		}
		IIndexMacro[] macros = index.findMacros(nameChars, filter, new NullProgressMonitor());
		for (IIndexMacro macro : macros) {
			IIndexName definition = macro.getDefinition();
			considerForInclusion(definition, macro, index, candidatesMap);
		}

		final ArrayList<IncludeCandidate> candidates = new ArrayList<IncludeCandidate>(candidatesMap.values());
		if (candidates.size() > 1) {
			if (sIsJUnitTest) {
				throw new RuntimeException("ambiguous input"); //$NON-NLS-1$
			}
			runInUIThread(new Runnable() {
				@Override
				public void run() {
					ElementListSelectionDialog dialog=
						new ElementListSelectionDialog(getShell(), new LabelProvider());
					dialog.setElements(candidates.toArray());
					dialog.setTitle(CEditorMessages.AddIncludeOnSelection_label); 
					dialog.setMessage(CEditorMessages.AddIncludeOnSelection_description); 
					if (dialog.open() == Window.OK) {
						candidates.clear();
						candidates.add((IncludeCandidate) dialog.getFirstResult());
					}
				}
			});
		}

		fRequiredIncludes.clear();
		fUsingDeclarations.clear();
		if (candidates.size() == 1) {
			IncludeCandidate candidate = candidates.get(0);
			fRequiredIncludes.add(candidate.getInclude());
			IIndexBinding indexBinding = candidate.getBinding();

			if (indexBinding instanceof ICPPBinding && !(indexBinding instanceof IIndexMacro)) {
				// Decide what 'using' declaration, if any, should be added along with the include.
				String usingDeclaration = deduceUsingDeclaration(binding, indexBinding, ast);
				if (usingDeclaration != null)
					fUsingDeclarations.add(usingDeclaration);
			}
		}
	}

	/**
	 * Adds an include candidate to the <code>candidates</code> map if the file containing
	 * the definition is suitable for inclusion.
	 */
	private void considerForInclusion(IIndexName definition, IIndexBinding binding,
			IIndex index, Map<String, IncludeCandidate> candidates) throws CoreException {
		if (definition == null) {
			return;
		}
		IIndexFile file = definition.getFile();
		// Consider the file for inclusion only if it is not a source file,
		// or a source file that was already included by some other file. 
		if (!isSource(getPath(file)) || index.findIncludedBy(file, 0).length > 0) {
			IIndexFile representativeFile = getRepresentativeFile(file, index);
			IncludeInfo include = getRequiredInclude(representativeFile, index);
			if (include != null) {
				IncludeCandidate candidate = new IncludeCandidate(binding, include);
				if (!candidates.containsKey(candidate.toString())) {
					candidates.put(candidate.toString(), candidate);
				}
			}
		}
	}

	private String deduceUsingDeclaration(IBinding source, IBinding target, IASTTranslationUnit ast) {
		if (source.equals(target)) {
			return null;  // No using declaration is needed.
		}
		ArrayList<String> targetChain = getUsingChain(target);
		if (targetChain.size() <= 1) {
			return null;  // Target is not in a namespace
		}

		// Check if any of the existing using declarations and directives matches
		// the target.
		final IASTDeclaration[] declarations= ast.getDeclarations(false);
		for (IASTDeclaration declaration : declarations) {
			if (declaration.isPartOfTranslationUnitFile()) {
				IASTName name = null;
				if (declaration instanceof ICPPASTUsingDeclaration) {
					name = ((ICPPASTUsingDeclaration) declaration).getName();
					if (match(name, targetChain, false)) {
						return null;
					}
				} else if (declaration instanceof ICPPASTUsingDirective) {
					name = ((ICPPASTUsingDirective) declaration).getQualifiedName();
					if (match(name, targetChain, true)) {
						return null;
					}
				}
			}
		}

		ArrayList<String> sourceChain = getUsingChain(source);
		if (sourceChain.size() >= targetChain.size()) {
			int j = targetChain.size();
			for (int i = sourceChain.size(); --j >= 1 && --i >= 1;) {
				if (!sourceChain.get(i).equals(targetChain.get(j))) {
					break;
				}
			}
			if (j <= 0) {
				return null;  // Source is in the target's namespace
			}
		}
		StringBuilder buf = new StringBuilder();
		for (int i = targetChain.size(); --i >= 0;) {
			if (buf.length() > 0) {
				buf.append("::"); //$NON-NLS-1$
			}
			buf.append(targetChain.get(i));
		}
		return buf.toString();
	}

	private boolean match(IASTName name, ArrayList<String> usingChain, boolean excludeLast) {
		ICPPASTNameSpecifier[] names;
		if (name instanceof ICPPASTQualifiedName) {
			// OK to use getNames() here. 'name' comes from a namespace-scope
			// using-declaration or using-directive, which cannot contain
			// decltype-specifiers.
			names = ((ICPPASTQualifiedName) name).getAllSegments();
		} else {
			names = new ICPPASTNameSpecifier[] { (ICPPASTName) name };
		}
		if (names.length != usingChain.size() - (excludeLast ? 1 : 0)) {
			return false;
		}
		for (int i = 0; i < names.length; i++) {
			if (!names[i].toString().equals(usingChain.get(usingChain.size() - 1 - i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns components of the qualified name in reverse order.
	 * For ns1::ns2::Name, e.g., it returns [Name, ns2, ns1].
	 */
	private ArrayList<String> getUsingChain(IBinding binding) {
		ArrayList<String> chain = new ArrayList<String>(4);
		for (; binding != null; binding = binding.getOwner()) {
			String name = binding.getName();
			if (binding instanceof ICPPNamespace) {
				if (name.length() == 0) {
					continue;
				}
			} else {
				chain.clear();
			}
			chain.add(name);
		}
		return chain;
	}

	/**
	 * Given a header file, decides if this header file should be included directly or
	 * through another header file. For example, <code>bits/stl_map.h</code> is not supposed
	 * to be included directly, but should be represented by <code>map</code>.
	 * @return the header file to include.
	 */
	private IIndexFile getRepresentativeFile(IIndexFile headerFile, IIndex index) {
		try {
			if (isWorkspaceFile(headerFile.getLocation().getURI())) {
				return headerFile;
			}
			ArrayDeque<IIndexFile> front = new ArrayDeque<IIndexFile>();
			front.add(headerFile);
			HashSet<IIndexFile> processed = new HashSet<IIndexFile>();
			processed.add(headerFile);
			while (!front.isEmpty()) {
				IIndexFile file = front.remove();
				// A header without an extension is a good candidate for inclusion into a C++ source file.
				if (fTu.isCXXLanguage() && !hasExtension(getPath(file))) {
					return file;
				}
				IIndexInclude[] includes = index.findIncludedBy(file, 0);
				for (IIndexInclude include : includes) {
					IIndexFile includer = include.getIncludedBy();
					if (!processed.contains(includer)) {
						URI uri = includer.getLocation().getURI();
						if (isSource(uri.getPath()) || isWorkspaceFile(uri)) {
							return file;
						}
						front.add(includer);
						processed.add(includer);
					}
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return headerFile;
	}

	private boolean isWorkspaceFile(URI uri) {
		for (IFile file : ResourceLookup.findFilesForLocationURI(uri)) {
			if (file.exists()) {
				return true;
			}
		}
		return false;
	}

	private boolean hasExtension(String path) {
		return path.indexOf('.', path.lastIndexOf('/') + 1) >= 0;
	}

	private IFunctionSummary findContribution(final String name) {
		final IFunctionSummary[] fs = new IFunctionSummary[1];
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ICHelpInvocationContext context = new ICHelpInvocationContext() {
					@Override
					public IProject getProject() {
						return fProject;
					}

					@Override
					public ITranslationUnit getTranslationUnit() {
						return fTu;
					}
				};

				fs[0] = CHelpProviderManager.getDefault().getFunctionInfo(context, name);
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(op);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), CEditorMessages.AddIncludeOnSelection_error_title,
					CEditorMessages.AddIncludeOnSelection_help_provider_error); 
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}
		return fs[0];
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}

	@Override
	public void update() {
		ITextEditor editor = getTextEditor();
		setEnabled(editor != null && getTranslationUnit(editor) != null);
	}

	/**
	 * Checks if a file is a source file (.c, .cpp, .cc, etc). Header files are not considered source files.
	 * @return Returns <code>true</code> if the the file is a source file.
	 */
	private boolean isSource(String filename) {
		IContentType ct= CCorePlugin.getContentType(fProject, filename);
		if (ct != null) {
			String id = ct.getId();
			if (CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id) || CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id)) {
				return true;
			}
		}
		return false;
	}

	private static String getPath(IIndexFile file) throws CoreException {
		return file.getLocation().getURI().getPath();
	}

	/**
	 * Returns the RequiredInclude object to be added to the include list
	 * @param path - the full path of the file to include
	 * @return the required include
	 * @throws CoreException 
	 */
	private IncludeInfo getRequiredInclude(IIndexFile file, IIndex index) throws CoreException {
		IIndexInclude[] includes = index.findIncludedBy(file);
		if (includes.length > 0) {
			// Let the existing includes vote. To be eligible to vote, an include
			// has to be resolvable in the context of the current translation unit.
			int systemIncludeVotes = 0;
			String[] ballotBox = new String[includes.length];
			int k = 0;
			for (IIndexInclude include : includes) {
				if (isResolvableInCurrentContext(include)) {
					ballotBox[k++] = include.getFullName();
					if (include.isSystemInclude()) {
						systemIncludeVotes++;
					}
				}
			}
			if (k != 0) {
				Arrays.sort(ballotBox, 0, k);
				String contender = ballotBox[0];
				int votes = 1;
				String winner = contender;
				int winnerVotes = votes;
				for (int i = 1; i < k; i++) {
					if (!ballotBox[i].equals(contender)) {
						contender = ballotBox[i]; 
						votes = 1;
					}
					votes++;
					if (votes > winnerVotes) {
						winner = contender;
						winnerVotes = votes;
					}
				}
				return new IncludeInfo(winner, systemIncludeVotes * 2 >= k);
			}
		}

		// The file has never been included before.
        IPath targetLocation = getAbsolutePath(file.getLocation());
        return fContext.getIncludeForHeaderFile(targetLocation);
    }

	/**
	 * Returns {@code true} if the given include can be resolved in the context of
	 * the current translation unit.
	 */
	private boolean isResolvableInCurrentContext(IIndexInclude include) {
		try {
			IncludeInfo includeInfo = new IncludeInfo(include.getFullName(), include.isSystemInclude());
			return fContext.resolveInclude(includeInfo) != null;
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return false;
		}
	}

	/**
	 * Returns the fully qualified name for a given index binding.
	 * @param binding
	 * @return binding's fully qualified name
	 * @throws CoreException
	 */
	private static String getBindingQualifiedName(IIndexBinding binding) throws CoreException {
		String[] qname= CPPVisitor.getQualifiedName(binding);
		StringBuilder result = new StringBuilder();
		boolean needSep= false;
		for (String element : qname) {
			if (needSep)
				result.append(Keywords.cpCOLONCOLON);
			result.append(element);  
			needSep= true;
		}
		return result.toString();
	}

	/**
	 * To be used by ElementListSelectionDialog for user to choose which declarations/
	 * definitions for "add include" when there are more than one to choose from.  
	 */
	private static class IncludeCandidate {
		private final IIndexBinding binding;
		private final IncludeInfo include;
		private final String label;

		public IncludeCandidate(IIndexBinding binding, IncludeInfo include) throws CoreException {
			this.binding = binding;
			this.include = include;
			this.label = getBindingQualifiedName(binding) + " - " + include.toString(); //$NON-NLS-1$
		}

		public IIndexBinding getBinding() {
			return binding;
		}

		public IncludeInfo getInclude() {
			return include;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
