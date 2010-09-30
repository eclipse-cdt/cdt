/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *	  Sergey Prigogin (Google)
******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNameCollector;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

class OpenDeclarationsJob extends Job implements ASTRunnable {

	private enum NameKind { REFERENCE, DECLARATION, USING_DECL, DEFINITION }

	private final SelectionParseAction fAction;
	private IProgressMonitor fMonitor;
	private final ITranslationUnit fTranslationUnit;
	private IIndex fIndex;
	private final ITextSelection fTextSelection;
	private final String fSelectedText;

	OpenDeclarationsJob(SelectionParseAction action, ITranslationUnit editorInput, ITextSelection textSelection, String text) {
		super(CEditorMessages.OpenDeclarations_dialog_title);
		fAction= action;
		fTranslationUnit= editorInput;
		fTextSelection= textSelection;
		fSelectedText= text;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			return performNavigation(monitor);
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	IStatus performNavigation(IProgressMonitor monitor) throws CoreException {
		fAction.clearStatusLine();

		assert fIndex == null;
		if (fIndex != null)
			return Status.CANCEL_STATUS;

		fMonitor= monitor;
		fIndex= CCorePlugin.getIndexManager().getIndex(fTranslationUnit.getCProject(),
				IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

		try {
			fIndex.acquireReadLock();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}

		try {
			return ASTProvider.getASTProvider().runOnAST(fTranslationUnit, ASTProvider.WAIT_ACTIVE_ONLY, monitor, this);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
		if (ast == null) {
			return Status.OK_STATUS;
		}
		int selectionStart = fTextSelection.getOffset();
		int selectionLength = fTextSelection.getLength();

		final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);

		IASTName sourceName= nodeSelector.findEnclosingName(selectionStart, selectionLength);
		IName[] implicitTargets = findImplicitTargets(ast, nodeSelector, selectionStart, selectionLength);
		if (sourceName == null) {
			if (implicitTargets.length > 0) {
				if (navigateViaCElements(fTranslationUnit.getCProject(), fIndex, implicitTargets))
					return Status.OK_STATUS;
			}
		} else {
			boolean found= false;
			final IASTNode parent = sourceName.getParent();
			if (parent instanceof IASTPreprocessorIncludeStatement) {
				openInclude(((IASTPreprocessorIncludeStatement) parent));
				return Status.OK_STATUS;
			}
			NameKind kind = getNameKind(sourceName);
			IBinding b = sourceName.resolveBinding();
			IBinding[] bindings = new IBinding[] { b };
			if (b instanceof IProblemBinding) {
				IBinding[] candidateBindings = ((IProblemBinding) b).getCandidateBindings();
				if (candidateBindings.length != 0) {
					bindings = candidateBindings;
				}
			} else if (kind == NameKind.DEFINITION && b instanceof IType) {
				// Don't navigate away from a type definition.
				// Select the name at the current location instead.
				navigateToName(sourceName);
				return Status.OK_STATUS;
			}
			IName[] targets = IName.EMPTY_ARRAY;
			String filename = ast.getFilePath();
			for (IBinding binding : bindings) {
				if (binding != null && !(binding instanceof IProblemBinding)) {
					IName[] names = findDeclNames(ast, kind, binding);
					for (final IName name : names) {
						if (name != null) {
							if (name instanceof IIndexName &&
									filename.equals(((IIndexName) name).getFileLocation().getFileName())) {
								// Exclude index names from the current file.
							} else if (areOverlappingNames(name, sourceName)) {
								// Exclude the current location.
							} else if (binding instanceof IParameter) {
								if (isInSameFunction(sourceName, name)) {
									targets = ArrayUtil.append(targets, name);
								}
							} else if (binding instanceof ICPPTemplateParameter) {
								if (isInSameTemplate(sourceName, name)) {
									targets = ArrayUtil.append(targets, name);
								}
							} else {
								targets = ArrayUtil.append(targets, name);
							}
						}
					}
				}
			}
			targets = ArrayUtil.trim(ArrayUtil.addAll(targets, implicitTargets));
			if (navigateViaCElements(fTranslationUnit.getCProject(), fIndex, targets)) {
				found= true;
			} else {
				// Leave old method as fallback for local variables, parameters and
				// everything else not covered by ICElementHandle.
				found = navigateOneLocation(targets);
			}
			if (!found && !navigationFallBack(ast, sourceName, kind)) {
				fAction.reportSymbolLookupFailure(new String(sourceName.toCharArray()));
			}
			return Status.OK_STATUS;
		}

		// No enclosing name, check if we're in an include statement
		IASTNode node= nodeSelector.findEnclosingNode(selectionStart, selectionLength);
		if (node instanceof IASTPreprocessorIncludeStatement) {
			openInclude((IASTPreprocessorIncludeStatement) node);
			return Status.OK_STATUS;
		} else if (node instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			IASTPreprocessorFunctionStyleMacroDefinition mdef= (IASTPreprocessorFunctionStyleMacroDefinition) node;
			for (IASTFunctionStyleMacroParameter par: mdef.getParameters()) {
				String parName= par.getParameter();
				if (parName.equals(fSelectedText)) {
					if (navigateToLocation(par.getFileLocation())) {
						return Status.OK_STATUS;
					}
				}
			}
		}
		if (!navigationFallBack(ast, null, NameKind.REFERENCE)) {
			fAction.reportSelectionMatchFailure();
		}
		return Status.OK_STATUS;
	}

	private IName[] findDeclNames(IASTTranslationUnit ast, NameKind kind, IBinding binding) throws CoreException {
		IName[] declNames = findNames(fIndex, ast, kind, binding);
		// Bug 207320, handle template instances.
		while (declNames.length == 0 && binding instanceof ICPPSpecialization) {
			binding = ((ICPPSpecialization) binding).getSpecializedBinding();
			if (binding != null && !(binding instanceof IProblemBinding)) {
				declNames = findNames(fIndex, ast, NameKind.DEFINITION, binding);
			}
		}
		if (declNames.length == 0 && binding instanceof ICPPMethod) {
			// Bug 86829, handle implicit methods.
			ICPPMethod method= (ICPPMethod) binding;
			if (method.isImplicit()) {
				IBinding clsBinding= method.getClassOwner();
				if (clsBinding != null && !(clsBinding instanceof IProblemBinding)) {
					declNames= findNames(fIndex, ast, NameKind.REFERENCE, clsBinding);
				}
			}
		}
		return declNames;
	}

	private IName[] findNames(IIndex index, IASTTranslationUnit ast, NameKind kind, IBinding binding) throws CoreException {
		IName[] declNames;
		if (kind == NameKind.DEFINITION) {
			declNames= findDeclarations(index, ast, binding);
		} else {
			declNames= findDefinitions(index, ast, kind, binding);
		}

		if (declNames.length == 0) {
			if (kind == NameKind.DEFINITION) {
				declNames= findDefinitions(index, ast, kind, binding);
			} else {
				declNames= findDeclarations(index, ast, binding);
			}
		}
		return declNames;
	}

	private IName[] findDefinitions(IIndex index, IASTTranslationUnit ast, NameKind kind, IBinding binding) throws CoreException {
		List<IASTName> declNames= new ArrayList<IASTName>();
		declNames.addAll(Arrays.asList(ast.getDefinitionsInAST(binding)));
		for (Iterator<IASTName> i = declNames.iterator(); i.hasNext();) {
			IASTName name= i.next();
			final IBinding b2 = name.resolveBinding();
			if (b2 instanceof ICPPUsingDeclaration) {
				i.remove();
			}
			if (binding != b2 && binding instanceof ICPPSpecialization) {
				// Make sure binding specializes b2 so that for instance we do not navigate from
				// one partial specialization to another.
				IBinding spec= binding;
				while (spec instanceof ICPPSpecialization) {
					spec= ((ICPPSpecialization) spec).getSpecializedBinding();
					if (spec == b2)
						break;
				}
				if (!(spec instanceof ICPPSpecialization)) {
					i.remove();
				}
			}
		}
		if (!declNames.isEmpty()) {
			return declNames.toArray(new IASTName[declNames.size()]);
		}

		// 2. Try definition in index.
		return index.findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
	}

	private IName[] findDeclarations(IIndex index, IASTTranslationUnit ast, IBinding binding) throws CoreException {
		IName[] declNames= ast.getDeclarationsInAST(binding);
		for (int i = 0; i < declNames.length; i++) {
			IName name = declNames[i];
			if (name.isDefinition())
				declNames[i]= null;
		}
		declNames= (IName[]) ArrayUtil.removeNulls(IName.class, declNames);
		if (declNames.length == 0) {
			declNames= index.findNames(binding, IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		}
		return declNames;
	}

	/**
	 * Returns definitions of bindings referenced by implicit name at the given location.
	 */
	private IName[] findImplicitTargets(IASTTranslationUnit ast, IASTNodeSelector nodeSelector,
			int offset, int length) throws CoreException {
		IName[] definitions = IName.EMPTY_ARRAY;
		IASTName firstName = nodeSelector.findEnclosingImplicitName(offset, length);
		if (firstName != null) {
			IASTImplicitNameOwner owner = (IASTImplicitNameOwner) firstName.getParent();
			for (IASTImplicitName name : owner.getImplicitNames()) {
				if (((ASTNode) name).getOffset() == ((ASTNode) firstName).getOffset()) {
					IBinding binding = name.resolveBinding(); // Guaranteed to resolve.
					IName[] declNames = findDeclNames(ast, NameKind.REFERENCE, binding);
					definitions = ArrayUtil.addAll(definitions, declNames);
				}
			}
		}
		return ArrayUtil.trim(definitions);
	}

	private static NameKind getNameKind(IName name) {
		if (name.isDefinition()) {
			if (getBinding(name) instanceof ICPPUsingDeclaration) {
				return NameKind.USING_DECL;
			} else {
				return NameKind.DEFINITION;
			}
		} else if (name.isDeclaration()) {
			return NameKind.DECLARATION;
		}
		return NameKind.REFERENCE;
	}

	private static IBinding getBinding(IName name) {
		if (name instanceof IASTName) {
			return ((IASTName) name).resolveBinding();
		} else if (name instanceof IIndexFragmentName) {
			try {
				return ((IIndexFragmentName) name).getBinding();
			} catch (CoreException e) {
				// Fall through to return null.
			}
		}
		return null;
	}

	private boolean areOverlappingNames(IName n1, IName n2) {
		if (n1 == n2)
			return true;

		IASTFileLocation loc1 = n1.getFileLocation();
		IASTFileLocation loc2 = n2.getFileLocation();
		if (loc1 == null || loc2 == null)
			return false;
		return loc1.getFileName().equals(loc2.getFileName()) &&
				max(loc1.getNodeOffset(), loc2.getNodeOffset()) <
				min(loc1.getNodeOffset() + loc1.getNodeLength(), loc2.getNodeOffset() + loc2.getNodeLength());
	}

	private static boolean isInSameFunction(IASTName refName, IName funcDeclName) {
		if (funcDeclName instanceof IASTName) {
			IASTDeclaration fdef = getEnclosingFunctionDefinition((IASTNode) funcDeclName);
			return fdef != null && fdef.contains(refName);
		} 
		return false;
	}

	private static IASTDeclaration getEnclosingFunctionDefinition(IASTNode node) {
		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node= node.getParent();
		}
		return (IASTDeclaration) node;
	}

	private static boolean isInSameTemplate(IASTName refName, IName templateDeclName) {
		if (templateDeclName instanceof IASTName) {
			IASTDeclaration template = getEnclosingTemplateDeclaration(refName);
			return template != null && template.contains(refName);
		} 
		return false;
	}

	private static IASTDeclaration getEnclosingTemplateDeclaration(IASTNode node) {
		while (node != null && !(node instanceof ICPPASTTemplateDeclaration)) {
			node= node.getParent();
		}
		return (IASTDeclaration) node;
	}

	private void convertToCElements(ICProject project, IIndex index, IName[] declNames, List<ICElement> elements) {
		for (IName declName : declNames) {
			try {
				ICElement elem = getCElementForName(project, index, declName);
				if (elem instanceof ISourceReference) {
					elements.add(elem);
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
	}

	private ICElementHandle getCElementForName(ICProject project, IIndex index, IName declName) throws CoreException {
		if (declName instanceof IIndexName) {
			return IndexUI.getCElementForName(project, index, (IIndexName) declName);
		}
		if (declName instanceof IASTName) {
			IASTName astName = (IASTName) declName;
			IBinding binding= astName.resolveBinding();
			if (binding != null) {
				ITranslationUnit tu= IndexUI.getTranslationUnit(project, astName);
				if (tu != null) {
					IASTFileLocation loc= astName.getFileLocation();
					IRegion region= new Region(loc.getNodeOffset(), loc.getNodeLength());
					return CElementHandleFactory.create(tu, binding, astName.isDefinition(), region, 0);
				}
			}
			return null;
		}
		return null;
	}

	private boolean navigateViaCElements(ICProject project, IIndex index, IName[] declNames) {
		final ArrayList<ICElement> elements= new ArrayList<ICElement>();
		convertToCElements(project, index, declNames, elements);
		return navigateCElements(elements);
	}

	private boolean navigateCElements(List<ICElement> elements) {
		if (elements.isEmpty()) {
			return false;
		}

		final List<ICElement> uniqueElements;
		if (elements.size() < 2) {
			uniqueElements= elements;
		} else {
			// Make sure only one element per location is proposed
			Set<String> sigs= new HashSet<String>();
			sigs.add(null);
			
			uniqueElements= new ArrayList<ICElement>();
			for (ICElement elem : elements) {
				if (sigs.add(getLocationSignature((ISourceReference) elem))) {
					uniqueElements.add(elem);
				}
			}
		}
		
		runInUIThread(new Runnable() {
			public void run() {
				ISourceReference target= null;
				if (uniqueElements.size() == 1) {
					target= (ISourceReference) uniqueElements.get(0);
				} else {
					if (OpenDeclarationsAction.sIsJUnitTest) {
						throw new RuntimeException("ambiguous input: " + uniqueElements.size()); //$NON-NLS-1$
					}
					ICElement[] elemArray= uniqueElements.toArray(new ICElement[uniqueElements.size()]);
					target = (ISourceReference) OpenActionUtil.selectCElement(elemArray, fAction.getSite().getShell(),
							CEditorMessages.OpenDeclarationsAction_dialog_title, CEditorMessages.OpenDeclarationsAction_selectMessage,
							CElementLabels.ALL_DEFAULT | CElementLabels.ALL_FULLY_QUALIFIED | CElementLabels.MF_POST_FILE_QUALIFIED, 0);
				}
				if (target != null) {
					ITranslationUnit tu= target.getTranslationUnit();
					ISourceRange sourceRange;
					try {
						sourceRange = target.getSourceRange();
						if (tu != null && sourceRange != null) {
							fAction.open(tu, sourceRange.getIdStartPos(), sourceRange.getIdLength());
						}
					} catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}
			}
		});
		return true;
	}

	private String getLocationSignature(ISourceReference elem) {
		ITranslationUnit tu= elem.getTranslationUnit();
		ISourceRange sourceRange;
		try {
			sourceRange = elem.getSourceRange();
			if (tu != null && sourceRange != null) {
				return tu.getPath().toString() + IPath.SEPARATOR + sourceRange.getIdStartPos() + IPath.SEPARATOR + sourceRange.getIdLength();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return null;
	}

	private boolean navigateOneLocation(IName[] names) {
		for (IName name : names) {
			if (navigateToName(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean navigateToName(IName name) {
		return navigateToLocation(name.getFileLocation());
	}

	private boolean navigateToLocation(IASTFileLocation fileloc) {
		if (fileloc == null) {
			return false;
		}
		final IPath path = new Path(fileloc.getFileName());
		final int offset = fileloc.getNodeOffset();
		final int length = fileloc.getNodeLength();

		runInUIThread(new Runnable() {
			public void run() {
				try {
					fAction.open(path, offset, length);
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		});
		return true;
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().asyncExec(runnable);
		}
	}

	private void openInclude(IASTPreprocessorIncludeStatement incStmt) {
		String name = null;
		if (incStmt.isResolved())
			name = incStmt.getPath();

		if (name != null) {
			final IPath path = new Path(name);
			runInUIThread(new Runnable() {
				public void run() {
					try {
						fAction.open(path, 0, 0);
					} catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}
			});
		} else {
			fAction.reportIncludeLookupFailure(new String(incStmt.getName().toCharArray()));
		}
	}

	private boolean navigationFallBack(IASTTranslationUnit ast, IASTName sourceName, NameKind kind) {
		// Bug 102643, as a fall-back we look up the selected word in the index.
		if (fSelectedText != null && fSelectedText.length() > 0) {
			try {
				final ICProject project = fTranslationUnit.getCProject();
				final char[] name = fSelectedText.toCharArray();
				List<ICElement> elems= new ArrayList<ICElement>();
	
				// Bug 252549, search for names in the AST first.
				Set<IBinding> primaryBindings= new HashSet<IBinding>();
				ASTNameCollector nc= new ASTNameCollector(fSelectedText);
				ast.accept(nc);
				IASTName[] candidates= nc.getNames();
				for (IASTName astName : candidates) {
					try {
						IBinding b= astName.resolveBinding();
						if (b != null && !(b instanceof IProblemBinding)) {
							primaryBindings.add(b);
						}
					} catch (RuntimeException e) {
						CUIPlugin.log(e);
					}
				}

				// Search the index, also.
				final IndexFilter filter = IndexFilter.getDeclaredBindingFilter(ast.getLinkage().getLinkageID(), false);
				final IIndexBinding[] idxBindings = fIndex.findBindings(name, false, filter, fMonitor);
				for (IIndexBinding idxBinding : idxBindings) {
					primaryBindings.add(idxBinding);
				}

				// Search for a macro in the index.
				IIndexMacro[] macros= fIndex.findMacros(name, filter, fMonitor);
				for (IIndexMacro macro : macros) {
					ICElement elem= IndexUI.getCElementForMacro(project, fIndex, macro);
					if (elem != null) {
						elems.add(elem);
					}
				}

				Collection<IBinding> secondaryBindings;
				if (ast instanceof ICPPASTTranslationUnit) {
					secondaryBindings= cppRemoveSecondaryBindings(primaryBindings, sourceName);
				} else {
					secondaryBindings= defaultRemoveSecondaryBindings(primaryBindings, sourceName);
				}

				// Convert bindings to CElements.
				Collection<IBinding> bs= primaryBindings;
				for (int k = 0; k < 2; k++) {
					for (IBinding binding : bs) {
						IName[] names = findNames(fIndex, ast, kind, binding);
						// Exclude names of the same kind.
						for (int i = 0; i < names.length; i++) {
							if (getNameKind(names[i]) == kind) {
								names[i] = null;
							}
						}
						names = (IName[]) ArrayUtil.removeNulls(IName.class, names);
						convertToCElements(project, fIndex, names, elems);
					}
					// In case we did not find anything, consider the secondary bindings.
					if (!elems.isEmpty())
						break;
					bs= secondaryBindings;
				}
				if (navigateCElements(elems)) {
					return true;
				}
				if (sourceName != null && sourceName.isDeclaration()) {
					// Select the name at the current location as the last resort.
					return navigateToName(sourceName);
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
		return false;
	}

	private Collection<IBinding> defaultRemoveSecondaryBindings(Set<IBinding> primaryBindings, IASTName sourceName) {
		if (sourceName != null) {
			IBinding b= sourceName.resolveBinding();
			if (b != null && ! (b instanceof IProblemBinding)) {
				try {
					for (Iterator<IBinding> iterator = primaryBindings.iterator(); iterator.hasNext();) {
						if (!checkOwnerNames(b, iterator.next()))
							iterator.remove();
					}
				} catch (DOMException e) {
					// Ignore
				}
			}
		}
		return Collections.emptyList();
	}

	private boolean checkOwnerNames(IBinding b1, IBinding b2) throws DOMException {
		IBinding o1 = b1.getOwner();
		IBinding o2 = b2.getOwner();
		if (o1 == o2)
			return true;

		if (o1 == null || o2 == null)
			return false;

		if (!CharArrayUtils.equals(o1.getNameCharArray(), o2.getNameCharArray()))
			return false;

		return checkOwnerNames(o1, o2);
	}

	private Collection<IBinding> cppRemoveSecondaryBindings(Set<IBinding> primaryBindings, IASTName sourceName) {
		List<IBinding> result= new ArrayList<IBinding>();
		String[] sourceQualifiedName= null;
		int funcArgCount= -1;
		if (sourceName != null) {
			final IBinding binding = sourceName.resolveBinding();
			if (binding != null) {
				sourceQualifiedName= CPPVisitor.getQualifiedName(binding);
				if (binding instanceof ICPPUnknownBinding) {
					LookupData data= CPPSemantics.createLookupData(sourceName);
					if (data.isFunctionCall()) {
						funcArgCount= data.getFunctionArgumentCount();
					}
				}
			}
		}

		for (Iterator<IBinding> iterator = primaryBindings.iterator(); iterator.hasNext();) {
			IBinding binding = iterator.next();
			if (sourceQualifiedName != null) {
				String[] qualifiedName = CPPVisitor.getQualifiedName(binding);
				if (!Arrays.equals(qualifiedName, sourceQualifiedName)) {
					iterator.remove();
					continue;
				}
			}
			if (funcArgCount != -1) {
				// For c++ we can check the number of parameters.
				if (binding instanceof ICPPFunction) {
					ICPPFunction f= (ICPPFunction) binding;
					try {
						if (f.getRequiredArgumentCount() > funcArgCount) {
							iterator.remove();
							result.add(binding);
							continue;
						}
						if (!f.takesVarArgs() && !f.hasParameterPack()) {
							final IType[] parameterTypes = f.getType().getParameterTypes();
							int maxArgs= parameterTypes.length;
							if (maxArgs == 1 && SemanticUtil.isVoidType(parameterTypes[0])) {
								maxArgs= 0;
							}
							if (maxArgs < funcArgCount) {
								iterator.remove();
								result.add(binding);
								continue;
							}
						}
					} catch (DOMException e) {
						// Ignore problem bindings.
						continue;
					}
				}
			}
		}

		return result;
	}
}