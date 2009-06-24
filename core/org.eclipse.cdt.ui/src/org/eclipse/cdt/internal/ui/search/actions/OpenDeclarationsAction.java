/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
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
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

/**
 * Navigates to the definition of a name, or to the declaration if invoked on the definition.
 */
public class OpenDeclarationsAction extends SelectionParseAction implements ASTRunnable {
	public static boolean sIsJUnitTest = false;	

	private enum NameKind { REFERENCE, DECLARATION, USING_DECL, DEFINITION }

	private class WrapperJob extends Job {
		WrapperJob() {
			super(CEditorMessages.OpenDeclarations_dialog_title); 
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				return performNavigation(monitor);
			} catch (CoreException e) {
				return e.getStatus();
			}
		}		
	}
	
	
	ITextSelection fTextSelection;
	private String fSelectedText;
	private ITranslationUnit fWorkingCopy;
	private IIndex fIndex;
	private IProgressMonitor fMonitor;

	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		super(editor);
		setText(CEditorMessages.OpenDeclarations_label); 
		setToolTipText(CEditorMessages.OpenDeclarations_tooltip); 
		setDescription(CEditorMessages.OpenDeclarations_description); 
	}

	protected IStatus performNavigation(IProgressMonitor monitor) throws CoreException {
		clearStatusLine();

		fMonitor= monitor;
		ICElement celem= fEditor.getInputCElement();
		if (!(celem instanceof ITranslationUnit)) 
			return Status.CANCEL_STATUS;

		fWorkingCopy= (ITranslationUnit) celem;
		fIndex= CCorePlugin.getIndexManager().getIndex(fWorkingCopy.getCProject(),
				IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

		try {
			fIndex.acquireReadLock();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}

		try {
			return ASTProvider.getASTProvider().runOnAST(fWorkingCopy, ASTProvider.WAIT_ACTIVE_ONLY, monitor, this);
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
		if (sourceName == null) {
			IASTName implicit = nodeSelector.findEnclosingImplicitName(selectionStart, selectionLength);
			if (implicit != null) {
				IASTImplicitNameOwner owner = (IASTImplicitNameOwner) implicit.getParent();
				IASTImplicitName[] implicits = owner.getImplicitNames();
				// There may be more than one name in the same spot
				if (implicits.length > 0) {
					List<IName> allNames = new ArrayList<IName>();
					for (IASTImplicitName name : implicits) {
						if (((ASTNode) name).getOffset() == ((ASTNode) implicit).getOffset()) {
							IBinding binding = name.resolveBinding(); // guaranteed to resolve
							IName[] declNames = findDeclNames(ast, NameKind.REFERENCE, binding);
							allNames.addAll(Arrays.asList(declNames));
						}
					}
					if (navigateViaCElements(fWorkingCopy.getCProject(), fIndex, allNames.toArray(new IName[0])))
						return Status.OK_STATUS;
				}
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
			IName[] declNames = null;
			String filename = ast.getFilePath();
			for (IBinding binding : bindings) {
				if (binding != null && !(binding instanceof IProblemBinding)) {
					IName[] names = findDeclNames(ast, kind, binding);
					for (int i = 0; i < names.length; i++) {
						if (names[i] instanceof IIndexName &&
								filename.equals(((IIndexName) names[i]).getFileLocation().getFileName())) {
							// Exclude index names from the current file.
							names[i] = null;
						} else if (isSameName(names[i], sourceName)) {
							// Exclude the current location.
							names[i] = null;
						} else if (binding instanceof IParameter) {
							if (!isInSameFunction(sourceName, names[i])) {
								names[i] = null;
							}
						} else if (binding instanceof ICPPTemplateParameter) {
							if (!isInSameTemplate(sourceName, names[i])) {
								names[i] = null;
							}
						}
					}
					compact(names);
					if (declNames == null) {
						declNames = names;
					} else {
						declNames = (IName[]) ArrayUtil.addAll(IName.class, declNames, names);
					}
				}
			}
			declNames = (IName[]) ArrayUtil.removeNulls(IName.class, declNames);

			if (navigateViaCElements(fWorkingCopy.getCProject(), fIndex, declNames)) {
				found= true;
			} else {
				// Leave old method as fallback for local variables, parameters and 
				// everything else not covered by ICElementHandle.
				found = navigateOneLocation(declNames);
			}
			if (!found && !navigationFallBack(ast, sourceName, kind)) {
				reportSymbolLookupFailure(new String(sourceName.toCharArray()));
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
			reportSelectionMatchFailure();
		}
		return Status.OK_STATUS; 
	}

	/**
	 * Compacts an array by moving all <code>null</code> elements to the end.
	 * @param array
	 */
	private void compact(Object[] array) {
		for (int i = 0, j = 0; i < array.length; i++) {
			if (array[i] != null) {
				if (i != j) {
					array[j] = array[i];
					array[i] = null;
				}
				j++;
			}
		}
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
	
	private static boolean isInSameFunction(IASTName name1, IName name2) {
		IASTDeclaration decl1 = getEnclosingDeclaration(name1);
		IASTDeclaration decl2 = name2 instanceof IASTName ? getEnclosingDeclaration((IASTName) name2) : null;
		return decl1 != null && decl1.equals(decl2) || decl1 == null && decl2 == null;
	}

	private static IASTDeclaration getEnclosingDeclaration(IASTNode node) {
		while (node != null && !(node instanceof IASTDeclaration)) {
			node= node.getParent();
		}
		return (IASTDeclaration) node;
	}

	private static boolean isInSameTemplate(IASTName name1, IName name2) {
		IASTDeclaration decl1 = getEnclosingTemplateDeclaration(name1);
		IASTDeclaration decl2 = name2 instanceof IASTName ?
				getEnclosingTemplateDeclaration((IASTName) name2) : null;
		return decl1 != null && decl1.equals(decl2) || decl1 == null && decl2 == null;
	}

	private static IASTDeclaration getEnclosingTemplateDeclaration(IASTNode node) {
		while (node != null && !(node instanceof ICPPASTTemplateDeclaration)) {
			node= node.getParent();
		}
		return (IASTDeclaration) node;
	}

	private IName[] findDeclNames(IASTTranslationUnit ast, NameKind kind, IBinding binding) throws CoreException {
		IName[] declNames = findNames(fIndex, ast, kind, binding);
		if (declNames.length == 0) {
			if (binding instanceof ICPPSpecialization) {
				// bug 207320, handle template instances
				IBinding specialized= ((ICPPSpecialization) binding).getSpecializedBinding();
				if (specialized != null && !(specialized instanceof IProblemBinding)) {
					declNames = findNames(fIndex, ast, NameKind.DEFINITION, specialized);
				}
			} else if (binding instanceof ICPPMethod) {
				// bug 86829, handle implicit methods.
				ICPPMethod method= (ICPPMethod) binding;
				if (method.isImplicit()) {
					try {
						IBinding clsBinding= method.getClassOwner();
						if (clsBinding != null && !(clsBinding instanceof IProblemBinding)) {
							declNames= findNames(fIndex, ast, NameKind.REFERENCE, clsBinding);
						}
					} catch (DOMException e) {
						// don't log problem bindings.
					}
				}
			}
		}
		return declNames;
	}

	private boolean navigationFallBack(IASTTranslationUnit ast, IASTName sourceName, NameKind kind) {
		// bug 102643, as a fall-back we look up the selected word in the index
		if (fSelectedText != null && fSelectedText.length() > 0) {
			try {
				final ICProject project = fWorkingCopy.getCProject();
				final char[] name = fSelectedText.toCharArray();
				List<ICElement> elems= new ArrayList<ICElement>();
								
				// bug 252549, search for names in the AST first
				Set<IBinding> bindings= new HashSet<IBinding>();
				Set<IBinding> ignoreIndexBindings= new HashSet<IBinding>();
				ASTNameCollector nc= new ASTNameCollector(fSelectedText);
				ast.accept(nc);
				IASTName[] candidates= nc.getNames();
				for (IASTName astName : candidates) {
					try {
						IBinding b= astName.resolveBinding();
						if (b != null && !(b instanceof IProblemBinding)) {
							if (bindings.add(b)) {
								ignoreIndexBindings.add(fIndex.adaptBinding(b));
							}
						}
					} catch (RuntimeException e) {
						CUIPlugin.log(e);
					}
				}
				
				// Search the index, also
				final IndexFilter filter = IndexFilter.getDeclaredBindingFilter(ast.getLinkage().getLinkageID(), false);
				final IIndexBinding[] idxBindings = fIndex.findBindings(name, false, filter, fMonitor);
				for (IIndexBinding idxBinding : idxBindings) {
					if (!ignoreIndexBindings.contains(idxBinding)) {
						bindings.add(idxBinding);
					}
				}
				
				// Search for a macro in the index
				IIndexMacro[] macros= fIndex.findMacros(name, filter, fMonitor);
				for (IIndexMacro macro : macros) {
					ICElement elem= IndexUI.getCElementForMacro(project, fIndex, macro);
					if (elem != null) {
						elems.add(elem);
					}
				}

				String[] sourceQualifiedName= sourceName != null ?
						CPPVisitor.getQualifiedName(sourceName.resolveBinding()) :
					    new String[] { fSelectedText };
				// Convert bindings to CElements
				for (IBinding binding : bindings) {
					String[] qualifiedName = CPPVisitor.getQualifiedName(binding);
					if (!Arrays.equals(qualifiedName, sourceQualifiedName)) {
						continue;
					}
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

	private boolean isSameName(IName n1, IName n2) {
		IASTFileLocation loc1 = n1.getFileLocation();
		IASTFileLocation loc2 = n2.getFileLocation();
		return loc1.getFileName().equals(loc2.getFileName()) &&
				loc1.getNodeOffset() == loc2.getNodeOffset() &&
				loc1.getNodeLength() == loc2.getNodeLength();
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
						open(path, 0, 0);
					} catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}
			});
		} else {
			reportIncludeLookupFailure(new String(incStmt.getName().toCharArray()));
		}
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
					open(path, offset, length);
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		});
		return true;
	}

	private boolean navigateViaCElements(ICProject project, IIndex index, IName[] declNames) {
		final ArrayList<ICElement> elements= new ArrayList<ICElement>();
		convertToCElements(project, index, declNames, elements);
		return navigateCElements(elements);
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
	
	private boolean navigateCElements(final List<ICElement> elements) {
		if (elements.isEmpty()) {
			return false;
		}

		runInUIThread(new Runnable() {
			public void run() {
				ISourceReference target= null;
				if (elements.size() == 1) {
					target= (ISourceReference) elements.get(0);
				} else {
					if (sIsJUnitTest) {
						throw new RuntimeException("ambiguous input: " + elements.size()); //$NON-NLS-1$
					}
					ICElement[] elemArray= elements.toArray(new ICElement[elements.size()]);
					target = (ISourceReference) OpenActionUtil.selectCElement(elemArray, getSite().getShell(),
							CEditorMessages.OpenDeclarationsAction_dialog_title, CEditorMessages.OpenDeclarationsAction_selectMessage, 
							CElementBaseLabels.ALL_DEFAULT | CElementBaseLabels.ALL_FULLY_QUALIFIED | CElementBaseLabels.MF_POST_FILE_QUALIFIED, 0);
				}
				if (target != null) {
					ITranslationUnit tu= target.getTranslationUnit();
					ISourceRange sourceRange;
					try {
						sourceRange = target.getSourceRange();
						if (tu != null && sourceRange != null) {
							open(tu.getLocation(), sourceRange.getIdStartPos(), sourceRange.getIdLength());
						}
					} catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}
			}
		});
		return true;
	}

	private ICElementHandle getCElementForName(ICProject project, IIndex index, IName declName) 
	throws CoreException {
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
			if (name.resolveBinding() instanceof ICPPUsingDeclaration) {
				i.remove();
			}
		}
		if (!declNames.isEmpty()) {
			return declNames.toArray(new IASTName[declNames.size()]);
		}

		// 2. Try definition in index
		return index.findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
	}

	private IName[] findDeclarations(IIndex index, IASTTranslationUnit ast,
			IBinding binding) throws CoreException {
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

	@Override
	public void run() {
		computeSelectedWord();
		if (fTextSelection != null) {
			new WrapperJob().schedule();
		}
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().asyncExec(runnable);
		}
	}

	/**
	 * For the purpose of regression testing.
	 * @since 4.0
	 */
	public void runSync() throws CoreException {
		computeSelectedWord();
		if (fTextSelection != null) {
			performNavigation(new NullProgressMonitor());
		}
	}

	private void computeSelectedWord() {
		fTextSelection = getSelectedStringFromEditor();
		fSelectedText= null;
		if (fTextSelection != null) {
			if (fTextSelection.getLength() > 0) {
				fSelectedText= fTextSelection.getText();
			} else {
				IDocument document= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
				IRegion reg= CWordFinder.findWord(document, fTextSelection.getOffset());
				if (reg != null && reg.getLength() > 0) {
					try {
						fSelectedText= document.get(reg.getOffset(), reg.getLength());
					} catch (BadLocationException e) {
						CUIPlugin.log(e);
					}
				}
			}
		}
	}
}
