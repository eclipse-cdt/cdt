/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.cdt.core.dom.ast.DOMException;
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
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.ui.text.SharedASTJob;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.corext.codemanipulation.AddIncludesOperation;

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
	private String[] fIncludePath;
	private IRequiredInclude[] fRequiredIncludes;
	private String[] fUsingDeclarations;

	public AddIncludeOnSelectionAction(ITextEditor editor) {
		super(CEditorMessages.getBundleForConstructedKeys(), "AddIncludeOnSelection.", editor); //$NON-NLS-1$

		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(this,
				ICHelpContextIds.ADD_INCLUDE_ON_SELECTION_ACTION);
	}

	private void insertInclude(IRequiredInclude[] includes, String[] usings, int beforeOffset) {
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
        IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(fProject);
        fIncludePath = null;
        if (provider != null) {
            IScannerInfo info = provider.getScannerInformation(fTu.getResource());
            if (info != null) {
                fIncludePath = info.getIncludePaths();
            }
        }
        if (fIncludePath == null) {
        	fIncludePath = new String[0];
        }

		try {
			final ISelection selection= getTextEditor().getSelectionProvider().getSelection();
			if (selection.isEmpty() || !(selection instanceof ITextSelection)) {
				return;
			}
			if (!validateEditorInputState()) {
				return;
			}

			final String[] lookupName = new String[1];

			SharedASTJob job = new SharedASTJob(CEditorMessages.AddIncludeOnSelection_label, fTu) {
				@Override
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
					deduceInclude((ITextSelection) selection, ast, lookupName);
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			job.join();

			if (fRequiredIncludes == null || fRequiredIncludes.length == 0 && lookupName[0].length() > 0) {
				// Try contribution from plugins.
				IFunctionSummary fs = findContribution(lookupName[0]);
				if (fs != null) {
					fRequiredIncludes = fs.getIncludes();
					String ns = fs.getNamespace();
					if (ns != null && ns.length() > 0) {
						fUsingDeclarations = new String[] { fs.getNamespace() };
					}
				}

			}
			if (fRequiredIncludes != null && fRequiredIncludes.length >= 0) {
				insertInclude(fRequiredIncludes, fUsingDeclarations, ((ITextSelection) selection).getOffset());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Extract the includes for the given selection.  This can be both used to perform
	 * the work as well as being invoked when there is a change.
	 * @param index 
	 */
	private void deduceInclude(ITextSelection selection, IASTTranslationUnit ast, String[] lookupName)
			throws CoreException {
		IASTNodeSelector selector = ast.getNodeSelector(fTu.getLocation().toOSString());
		IASTName name = selector.findEnclosingName(selection.getOffset(), selection.getLength());
		if (name == null) {
			return;
		}
		char[] nameChars = name.toCharArray();
		lookupName[0] = new String(nameChars);
		IBinding binding = name.resolveBinding();
		try {
			if (binding instanceof ICPPVariable) {
				IType type = ((ICPPVariable) binding).getType();
				type = SemanticUtil.getNestedType(type,
						SemanticUtil.ALLCVQ | SemanticUtil.PTR | SemanticUtil.ARRAY | SemanticUtil.REF);
				if (type instanceof IBinding) {
					binding = (IBinding) type;
					nameChars = binding.getNameCharArray();
				}
			}
		} catch (DOMException e) {
			CUIPlugin.log(e);
		}
		if (nameChars.length == 0) {
			return;
		}

		final Map<String, IncludeCandidate> candidatesMap= new HashMap<String, IncludeCandidate>();
		final IIndex index = ast.getIndex();
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
				try {
					indexBinding = indexBinding.getOwner();
				} catch (DOMException e) {
					continue;
				}
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

		fRequiredIncludes = null;
		fUsingDeclarations = null;
		if (candidates.size() == 1) {
			IncludeCandidate candidate = candidates.get(0);
			fRequiredIncludes = new IRequiredInclude[] { candidate.getInclude() };
			IIndexBinding indexBinding = candidate.getBinding();

			if (indexBinding instanceof ICPPBinding && !(indexBinding instanceof IIndexMacro)) {
				// Decide what 'using' declaration, if any, should be added along with the include.
				String usingDeclaration = deduceUsingDeclaration(binding, indexBinding, ast);
				if (usingDeclaration != null)
					fUsingDeclarations = new String[] { usingDeclaration };
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
			IRequiredInclude include = getRequiredInclude(representativeFile, index);
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
		IASTName[] names;
		if (name instanceof ICPPASTQualifiedName) {
			names = ((ICPPASTQualifiedName) name).getNames();
		} else {
			names = new IASTName[] { name };
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
		try {
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
		} catch (DOMException e) {
			CUIPlugin.log(e);
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
			// TODO(sprigogin): Change to ArrayDeque when Java 5 support is no longer needed.
			LinkedList<IIndexFile> front = new LinkedList<IIndexFile>();
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
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ICHelpInvocationContext context = new ICHelpInvocationContext() {
					public IProject getProject() {
						return fProject;
					}

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
	private IRequiredInclude getRequiredInclude(IIndexFile file, IIndex index) throws CoreException {
		IIndexInclude[] includes;
		includes = index.findIncludedBy(file);
		if (includes.length > 0) {
			// Let the existing includes vote. To be eligible to vote, an include
			// has to be resolvable in the context of the current translation unit.
			int systemIncludeVotes = 0;
			String[] ballotBox = new String[includes.length];
			int k = 0;
			for (IIndexInclude include : includes) {
				if (isResolvable(include)) {
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
				return new RequiredInclude(winner, systemIncludeVotes * 2 >= k);
			}
		}

		// The file has never been included before.
		URI targetUri = file.getLocation().getURI();
        IPath targetLocation = PathUtil.getCanonicalPath(new Path(targetUri.getPath()));
    	IPath sourceLocation = PathUtil.getCanonicalPath(fTu.getResource().getLocation());
        boolean isSystemIncludePath = false;

        IPath path = PathUtil.makeRelativePathToIncludes(targetLocation, fIncludePath);
        if (path != null && ResourceLookup.findFilesForLocationURI(targetUri).length == 0) {
        	// A header file in the include path but outside the workspace is included with angle brackets.
            isSystemIncludePath = true;
        }
        if (path == null) {
        	IPath sourceDirectory = sourceLocation.removeLastSegments(1);
        	if (PathUtil.isPrefix(sourceDirectory, targetLocation)) {
        		path = targetLocation.removeFirstSegments(sourceDirectory.segmentCount());
        	} else {
        		path = targetLocation;
        	}
        	if (targetLocation.getDevice() != null &&
        			targetLocation.getDevice().equalsIgnoreCase(sourceDirectory.getDevice())) {
        		path = path.setDevice(null);
        	}
        	if (path.isAbsolute() && path.getDevice() == null &&
        			ResourceLookup.findFilesForLocationURI(targetUri).length != 0) {
        		// The file is inside workspace. Include with a relative path.
        		path = PathUtil.makeRelativePath(path, sourceDirectory);
        	}
        }
    	return new RequiredInclude(path.toString(), isSystemIncludePath);
    }

	/**
	 * Returns <code>true</code> if the given include can be resolved in the context of
	 * the current translation unit.
	 */
	private boolean isResolvable(IIndexInclude include) {
		try {
			File target = new File(include.getIncludesLocation().getURI().getPath());
			String includeName = include.getFullName();
			for (String dir : fIncludePath) {
				if (target.equals(new File(dir, includeName))) {
					return true;
				}
			}
			if (include.isSystemInclude()) {
				return false;
			}
			String directory = new File(fTu.getLocationURI().getPath()).getParent();
			return target.equals(new File(directory, includeName).getCanonicalFile());
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return false;
		} catch (IOException e) {
			CUIPlugin.log(e);
			return false;
		}
	}

	/**
	 * Get the fully qualified name for a given index binding.
	 * @param binding
	 * @return binding's fully qualified name
	 * @throws CoreException
	 */
	private static String getBindingQualifiedName(IIndexBinding binding) throws CoreException {
		String[] qname= binding.getQualifiedName();
		return CPPVisitor.renderQualifiedName(qname);
	}

	/**
	 * To be used by ElementListSelectionDialog for user to choose which declarations/
	 * definitions for "add include" when there are more than one to choose from.  
	 */
	private static class IncludeCandidate {
		private final IIndexBinding binding;
		private final IRequiredInclude include;
		private final String label;

		public IncludeCandidate(IIndexBinding binding, IRequiredInclude include) throws CoreException {
			this.binding = binding;
			this.include = include;
			this.label = getBindingQualifiedName(binding) + " - " + include.toString(); //$NON-NLS-1$
		}

		public IIndexBinding getBinding() {
			return binding;
		}

		public IRequiredInclude getInclude() {
			return include;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static class RequiredInclude implements IRequiredInclude {
		final String includeName;
		final boolean isSystem;

		RequiredInclude(String includeName, boolean isSystem) {
			this.includeName = includeName;
			this.isSystem = isSystem;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.IRequiredInclude#getIncludeName()
		 */
		public String getIncludeName() {
			return includeName;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.IRequiredInclude#isStandard()
		 */
		public boolean isStandard() {
			return isSystem;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(includeName.length() + 2);
			buf.append(isSystem ? '<' : '"');
			buf.append(includeName);
			buf.append(isSystem ? '>' : '"');
			return buf.toString();
		}
	}
}
