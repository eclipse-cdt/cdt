/*******************************************************************************
 * Copyright (c) 2009, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *	   Sergey Prigogin (Google)
 *     Nathan Ridge
******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.PTR;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNameCollector;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IStructureDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.HeuristicResolver;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.editor.CElementIncludeResolver;
import org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction.ITargetDisambiguator;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.ui.CUIPlugin;
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

class OpenDeclarationsJob extends Job implements ASTRunnable {
	private enum NameKind {
		REFERENCE, DECLARATION, USING_DECL, DEFINITION
	}

	private final SelectionParseAction fAction;
	private IProgressMonitor fMonitor;
	private final ITranslationUnit fTranslationUnit;
	private IIndex fIndex;
	private final ITextSelection fTextSelection;
	private final String fSelectedText;
	private final ITargetDisambiguator fTargetDisambiguator;

	OpenDeclarationsJob(SelectionParseAction action, ITranslationUnit editorInput, ITextSelection textSelection,
			String text, ITargetDisambiguator targetDisambiguator) {
		super(CEditorMessages.OpenDeclarations_dialog_title);
		fAction = action;
		fTranslationUnit = editorInput;
		fTextSelection = textSelection;
		fSelectedText = text;
		fTargetDisambiguator = targetDisambiguator;
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

		fMonitor = monitor;
		fIndex = CCorePlugin.getIndexManager().getIndex(fTranslationUnit.getCProject(), IIndexManager.ADD_DEPENDENCIES
				| IIndexManager.ADD_DEPENDENT | IIndexManager.ADD_EXTENSION_FRAGMENTS_NAVIGATION);

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

	// Navigation without an AST. Used for assembly files which do not have an AST.
	// TODO(nathanridge): Can we avoid using ASTProvider altogether in this case?
	private IStatus navigateWithoutAst() {
		try {
			ICElement element = SelectionConverter.getElementAtOffset(fTranslationUnit, fTextSelection);
			if (element instanceof IInclude) {
				// If the cursor is over an include, open the referenced file.
				IInclude include = (IInclude) element;
				List<IPath> paths = CElementIncludeResolver.resolveInclude(include);
				openInclude(paths, include.getIncludeName());
				return Status.OK_STATUS;
			} else {
				// Otherwise, lookup the selected word in the index.
				// Without a semantic model for the assembly code, this is the best we can do.
				if (fSelectedText != null && fSelectedText.length() > 0) {
					final ICProject project = fTranslationUnit.getCProject();
					final char[] name = fSelectedText.toCharArray();
					List<ICElement> elems = new ArrayList<>();

					// Search for an element in the assembly file.
					// Some things in assembly files like macro definitions are
					// modelled in the C model, so those will be found here.
					fTranslationUnit.accept(element1 -> {
						if (element1.getElementName().equals(fSelectedText)) {
							elems.add(element1);
						}
						return true;
					});

					// Search for a binding in the index.
					final IndexFilter filter = IndexFilter.ALL;
					final IIndexBinding[] bindings = fIndex.findBindings(name, false, filter, fMonitor);
					for (IIndexBinding binding : bindings) {
						// Convert bindings to CElements.
						IName[] declNames = fIndex.findNames(binding, IIndex.FIND_DECLARATIONS | IIndex.FIND_DEFINITIONS
								| IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
						convertToCElements(project, fIndex, declNames, elems);
					}

					// Search for a macro in the index.
					IIndexMacro[] macros = fIndex.findMacros(name, filter, fMonitor);
					for (IIndexMacro macro : macros) {
						ICElement elem = IndexUI.getCElementForMacro(project, fIndex, macro);
						if (elem != null) {
							elems.add(elem);
						}
					}

					if (!navigateCElements(elems)) {
						fAction.reportSymbolLookupFailure(fSelectedText);
					}
					return Status.OK_STATUS;
				}
			}
		} catch (CModelException e) {
		} catch (CoreException e) {
		}
		fAction.reportSelectionMatchFailure();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
		if (ast == null) {
			return navigateWithoutAst();
		}
		int selectionStart = fTextSelection.getOffset();
		int selectionLength = fTextSelection.getLength();

		final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);

		IASTName sourceName = nodeSelector.findEnclosingName(selectionStart, selectionLength);
		IName[] implicitTargets = findImplicitTargets(ast, nodeSelector, selectionStart, selectionLength);
		if (sourceName == null || SemanticUtil.isAutoOrDecltype(fSelectedText)) {
			if (navigateViaCElements(fTranslationUnit.getCProject(), fIndex, implicitTargets))
				return Status.OK_STATUS;
		} else {
			CPPSemantics.pushLookupPoint(sourceName);
			try {
				boolean found = false;
				final IASTNode parent = sourceName.getParent();
				if (parent instanceof IASTPreprocessorIncludeStatement) {
					openInclude(((IASTPreprocessorIncludeStatement) parent));
					return Status.OK_STATUS;
				} else if (parent instanceof ICPPASTTemplateId) {
					sourceName = (IASTName) parent;
				}
				NameKind kind = getNameKind(sourceName);
				IBinding b = sourceName.resolveBinding();
				IBinding[] bindings = new IBinding[] { b };
				if (b instanceof IProblemBinding) {
					IBinding[] candidateBindings = ((IProblemBinding) b).getCandidateBindings();
					if (candidateBindings.length != 0) {
						bindings = candidateBindings;
					}
				} else if (kind == NameKind.DEFINITION && b instanceof IType && !(b instanceof ICPPClassTemplate)) {
					// Don't navigate away from a type definition.
					// Select the name at the current location instead.
					// However, for a class template, it's useful to navigate to
					// a forward declaration, as it may contain definitions of
					// default arguments, while the definition may not.
					navigateToName(sourceName);
					return Status.OK_STATUS;
				}
				IName[] targets = IName.EMPTY_ARRAY;
				String filename = ast.getFilePath();
				for (int i = 0; i < bindings.length; ++i) {
					IBinding binding = bindings[i];
					if (binding instanceof ICPPUnknownBinding) {
						// We're not going to find declarations for an unknown binding.
						// To try to do something useful anyways, we try to heuristically
						// resolve the unknown binding to one or more concrete bindings,
						// and use those instead.
						IBinding[] resolved = HeuristicResolver.resolveUnknownBinding((ICPPUnknownBinding) binding);
						if (resolved.length > 0) {
							bindings = ArrayUtil.addAll(bindings, resolved);
							continue;
						}
					}
					if (binding instanceof ICPPUsingDeclaration) {
						// Skip using-declaration bindings. Their delegates will be among the implicit targets.
						continue;
					}
					if (binding != null && !(binding instanceof IProblemBinding)) {
						IName[] names = findDeclNames(ast, kind, binding);
						for (final IName name : names) {
							if (name != null) {
								if (name instanceof IIndexName
										&& filename.equals(((IIndexName) name).getFileLocation().getFileName())) {
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
					found = true;
				} else {
					// Leave old method as fallback for local variables, parameters and
					// everything else not covered by ICElementHandle.
					found = navigateOneLocation(ast, targets);
				}
				if (!found && !navigationFallBack(ast, sourceName, kind)) {
					fAction.reportSymbolLookupFailure(new String(sourceName.toCharArray()));
				}
				return Status.OK_STATUS;
			} finally {
				CPPSemantics.popLookupPoint();
			}
		}

		// No enclosing name, check if we're in an include statement
		IASTNode node = nodeSelector.findEnclosingNode(selectionStart, selectionLength);
		if (node instanceof IASTPreprocessorIncludeStatement) {
			openInclude((IASTPreprocessorIncludeStatement) node);
			return Status.OK_STATUS;
		} else if (node instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			IASTPreprocessorFunctionStyleMacroDefinition mdef = (IASTPreprocessorFunctionStyleMacroDefinition) node;
			for (IASTFunctionStyleMacroParameter par : mdef.getParameters()) {
				String parName = par.getParameter();
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
		if (binding instanceof ICPPAliasTemplateInstance) {
			binding = ((ICPPAliasTemplateInstance) binding).getTemplateDefinition();
		}
		IName[] declNames = findNames(fIndex, ast, kind, binding);
		// Bug 207320, handle template instances.
		while (declNames.length == 0 && binding instanceof ICPPSpecialization) {
			binding = ((ICPPSpecialization) binding).getSpecializedBinding();
			if (binding != null && !(binding instanceof IProblemBinding)) {
				declNames = findNames(fIndex, ast, NameKind.REFERENCE, binding);
			}
		}
		if (declNames.length == 0 && binding instanceof ICPPMethod) {
			// Bug 86829, handle implicit methods.
			ICPPMethod method = (ICPPMethod) binding;
			if (method.isImplicit()) {
				IBinding clsBinding = method.getClassOwner();
				if (clsBinding != null && !(clsBinding instanceof IProblemBinding)) {
					declNames = findNames(fIndex, ast, NameKind.REFERENCE, clsBinding);
				}
			}
		}
		return declNames;
	}

	private IName[] findNames(IIndex index, IASTTranslationUnit ast, NameKind kind, IBinding binding)
			throws CoreException {
		IName[] declNames;
		if (kind == NameKind.DEFINITION) {
			declNames = findDeclarations(index, ast, binding);
		} else {
			declNames = findDefinitions(index, ast, binding);
		}

		if (declNames.length == 0) {
			if (kind == NameKind.DEFINITION) {
				declNames = findDefinitions(index, ast, binding);
			} else {
				declNames = findDeclarations(index, ast, binding);
			}
		}
		return declNames;
	}

	private IName[] findDefinitions(IIndex index, IASTTranslationUnit ast, IBinding binding) throws CoreException {
		// The priority of matches are as follows:
		//  - If there are exact AST matches, those are returned.
		//  - Otherwise, if there are exact index matches, those are returned.
		//  - Otherwise, permissive matches from the AST and index, if any, are
		//    combined and returned.
		List<IASTName> exactAstMatches = new ArrayList<>();
		List<IName> permissiveMatches = new ArrayList<>();
		exactAstMatches.addAll(Arrays.asList(ast.getDefinitionsInAST(binding, /* permissive = */ true)));
		for (Iterator<IASTName> i = exactAstMatches.iterator(); i.hasNext();) {
			IASTName name = i.next();
			final IBinding b2 = name.resolveBinding();
			if (b2 instanceof ICPPUsingDeclaration) {
				i.remove();
			}
			if (binding != b2 && binding instanceof ICPPSpecialization) {
				// Make sure binding specializes b2 so that for instance we do not navigate from
				// one partial specialization to another.
				IBinding spec = binding;
				while (spec instanceof ICPPSpecialization) {
					spec = ((ICPPSpecialization) spec).getSpecializedBinding();
					if (spec == b2)
						break;
				}
				if (!(spec instanceof ICPPSpecialization)) {
					i.remove();
				}
			}
			if (b2 instanceof IProblemBinding) {
				permissiveMatches.add(name);
				i.remove();
			}
		}
		if (!exactAstMatches.isEmpty()) {
			return exactAstMatches.toArray(new IASTName[exactAstMatches.size()]);
		}

		// Try definition in index.
		IName[] indexMatches = index.findNames(binding,
				IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES | IIndex.FIND_POTENTIAL_MATCHES);
		List<IName> exactIndexMatches = new ArrayList<>();
		exactIndexMatches.addAll(Arrays.asList(indexMatches));
		for (Iterator<IName> i = exactIndexMatches.iterator(); i.hasNext();) {
			IName name = i.next();
			if (name instanceof IIndexName && ((IIndexName) name).isPotentialMatch()) {
				permissiveMatches.add(name);
				i.remove();
			}
		}
		if (!exactIndexMatches.isEmpty()) {
			return exactIndexMatches.toArray(new IName[exactIndexMatches.size()]);
		}

		if (!permissiveMatches.isEmpty()) {
			return permissiveMatches.toArray(new IName[permissiveMatches.size()]);
		}

		return IName.EMPTY_ARRAY;
	}

	private IName[] findDeclarations(IIndex index, IASTTranslationUnit ast, IBinding binding) throws CoreException {
		IASTName[] astNames = ast.getDeclarationsInAST(binding);
		ArrayList<IASTName> usingDeclarations = null;
		for (int i = 0; i < astNames.length; i++) {
			IASTName name = astNames[i];
			if (name.isDefinition()) {
				astNames[i] = null;
			} else if (ASTQueries.findAncestorWithType(name, ICPPASTUsingDeclaration.class) != null) {
				if (usingDeclarations == null)
					usingDeclarations = new ArrayList<>(1);
				usingDeclarations.add(name);
				astNames[i] = null;
			}
		}
		IName[] declNames = ArrayUtil.removeNulls(astNames);
		if (declNames.length == 0) {
			declNames = index.findNames(binding, IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		}
		// 'using' declarations are considered only when there are no other declarations.
		if (declNames.length == 0 && usingDeclarations != null) {
			declNames = usingDeclarations.toArray(new IName[usingDeclarations.size()]);
		}
		return declNames;
	}

	/**
	 * Returns definitions of bindings referenced by implicit name at the given location.
	 *
	 * Also, if the given location is over the 'auto' in a variable declaration, the
	 * variable's type is opened.
	 */
	private IName[] findImplicitTargets(IASTTranslationUnit ast, IASTNodeSelector nodeSelector, int offset, int length)
			throws CoreException {
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
		} else if (SemanticUtil.isAutoOrDecltype(fSelectedText)) {
			IASTNode enclosingNode = nodeSelector.findEnclosingNode(offset, length);
			try {
				CPPSemantics.pushLookupPoint(enclosingNode);
				IType type = CPPSemantics.resolveDecltypeOrAutoType(enclosingNode);
				if (type instanceof ICPPUnknownType) {
					IType hType = HeuristicResolver.resolveUnknownType((ICPPUnknownType) type);
					if (hType != null)
						type = hType;
				}
				// Strip qualifiers, references, and pointers, but NOT
				// typedefs, since for typedefs we want to refer to the
				// typedef declaration.
				type = SemanticUtil.getNestedType(type, CVTYPE | REF | PTR);
				if (type instanceof IBinding) {
					IName[] declNames = findDeclNames(ast, NameKind.REFERENCE, (IBinding) type);
					definitions = ArrayUtil.addAll(definitions, declNames);
				}
			} finally {
				CPPSemantics.popLookupPoint();
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

	private static boolean areOverlappingNames(IName n1, IName n2) {
		if (n1 == n2)
			return true;

		IASTFileLocation loc1 = getFileLocation(n1);
		IASTFileLocation loc2 = getFileLocation(n2);
		if (loc1 == null || loc2 == null)
			return false;
		return loc1.getFileName().equals(loc2.getFileName())
				&& max(loc1.getNodeOffset(), loc2.getNodeOffset()) < min(loc1.getNodeOffset() + loc1.getNodeLength(),
						loc2.getNodeOffset() + loc2.getNodeLength());
	}

	private static IASTFileLocation getFileLocation(IName name) {
		IASTFileLocation fileLocation = name.getFileLocation();
		if (name instanceof IASTName) {
			IASTName astName = (IASTName) name;
			IASTImageLocation imageLocation = astName.getImageLocation();
			if (imageLocation != null && imageLocation.getLocationKind() != IASTImageLocation.MACRO_DEFINITION
					&& astName.getTranslationUnit().getFilePath().equals(fileLocation.getFileName())) {
				fileLocation = imageLocation;
			}
		}
		return fileLocation;
	}

	private static boolean isInSameFunction(IASTName refName, IName funcDeclName) {
		if (funcDeclName instanceof IASTName) {
			IASTDeclaration fdecl = getEnclosingFunctionDeclaration((IASTNode) funcDeclName);
			return fdecl != null && fdecl.contains(refName);
		}
		return false;
	}

	private static boolean isFunctionDeclaration(IASTNode node) {
		if (node instanceof IASTFunctionDefinition) {
			return true;
		}
		if (node instanceof IASTSimpleDeclaration) {
			IASTDeclarator[] declarators = ((IASTSimpleDeclaration) node).getDeclarators();
			return declarators.length == 1 && declarators[0] instanceof IASTFunctionDeclarator;
		}
		return false;
	}

	private static IASTDeclaration getEnclosingFunctionDeclaration(IASTNode node) {
		while (node != null && !isFunctionDeclaration(node)) {
			node = node.getParent();
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
			node = node.getParent();
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
			IIndexName indexName = (IIndexName) declName;
			ITranslationUnit tu = IndexUI.getTranslationUnit(project, indexName);
			if (tu != null) {
				// If the file containing the target name is accessible via multiple
				// workspace paths, choose the one that most closely matches the
				// workspace path of the file originating the action.
				tu = IndexUI.getPreferredTranslationUnit(tu, fTranslationUnit);

				return IndexUI.getCElementForName(tu, index, indexName);
			}
			return null;
		}
		if (declName instanceof IASTName) {
			IASTName astName = (IASTName) declName;
			IBinding binding = astName.resolveBinding();
			if (binding != null) {
				ITranslationUnit tu = IndexUI.getTranslationUnit(astName);
				if (tu != null) {
					if (tu instanceof IWorkingCopy)
						tu = ((IWorkingCopy) tu).getOriginalElement();
					IASTFileLocation loc = getFileLocation(astName);
					IRegion region = new Region(loc.getNodeOffset(), loc.getNodeLength());
					return CElementHandleFactory.create(tu, binding, astName.isDefinition(), region, 0);
				}
			}
			return null;
		}
		return null;
	}

	private boolean navigateViaCElements(ICProject project, IIndex index, IName[] declNames) {
		final ArrayList<ICElement> elements = new ArrayList<>();
		convertToCElements(project, index, declNames, elements);
		return navigateCElements(elements);
	}

	private boolean navigateCElements(List<ICElement> elements) {
		if (elements.isEmpty()) {
			return false;
		}

		final List<ICElement> uniqueElements;
		if (elements.size() < 2) {
			uniqueElements = elements;
		} else {
			// Make sure only one element per location is proposed
			Set<String> sigs = new HashSet<>();
			sigs.add(null);

			uniqueElements = new ArrayList<>();
			for (ICElement elem : elements) {
				if (sigs.add(getLocationSignature((ISourceReference) elem))) {
					uniqueElements.add(elem);
				}
			}
		}

		runInUIThread(new Runnable() {
			@Override
			public void run() {
				ISourceReference target = null;
				if (uniqueElements.size() == 1) {
					target = (ISourceReference) uniqueElements.get(0);
				} else {
					if (uniqueElements.size() == 2) {
						final ICElement e0 = uniqueElements.get(0);
						final ICElement e1 = uniqueElements.get(1);
						// Prefer a method of a class, to the class itself.
						if (isMethodOfClass(e1, e0)) {
							target = (ISourceReference) e1;
						} else if (isMethodOfClass(e0, e1)) {
							target = (ISourceReference) e0;
						}
					}
					if (target == null) {
						if (OpenDeclarationsAction.sDisallowAmbiguousInput) {
							throw new RuntimeException("ambiguous input: " + uniqueElements.size()); //$NON-NLS-1$
						}
						ICElement[] elemArray = uniqueElements.toArray(new ICElement[uniqueElements.size()]);
						target = (ISourceReference) fTargetDisambiguator.disambiguateTargets(elemArray, fAction);
					}
				}
				if (target != null) {
					ITranslationUnit tu = target.getTranslationUnit();
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

			private boolean isMethodOfClass(ICElement method, ICElement clazz) {
				return method instanceof IMethodDeclaration && clazz instanceof IStructureDeclaration
						&& method.getParent() != null && method.getParent().equals(clazz);
			}
		});
		return true;
	}

	private String getLocationSignature(ISourceReference elem) {
		ITranslationUnit tu = elem.getTranslationUnit();
		ISourceRange sourceRange;
		try {
			sourceRange = elem.getSourceRange();
			if (tu != null && sourceRange != null) {
				return tu.getPath().toString() + IPath.SEPARATOR + sourceRange.getIdStartPos() + IPath.SEPARATOR
						+ sourceRange.getIdLength();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return null;
	}

	private IName[] filterNamesByIndexFileSet(IASTTranslationUnit ast, IName[] names) {
		IIndexFileSet indexFileSet = ast.getIndexFileSet();
		if (indexFileSet == null) {
			return names;
		}
		IName[] result = IName.EMPTY_ARRAY;
		for (IName name : names) {
			if (name instanceof IIndexName) {
				try {
					if (!indexFileSet.contains(((IIndexName) name).getFile()))
						continue;
				} catch (CoreException e) {
				}
			}
			result = ArrayUtil.append(result, name);
		}
		return result;
	}

	private boolean navigateOneLocation(IASTTranslationUnit ast, IName[] names) {
		// If there is more than one name, try to filter out
		// ones defined in a file not in the AST's index file set.
		if (names.length > 1) {
			IName[] filteredNames = filterNamesByIndexFileSet(ast, names);
			if (filteredNames.length > 0) {
				names = filteredNames;
			}
		}
		for (IName name : names) {
			if (navigateToName(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean navigateToName(IName name) {
		return navigateToLocation(getFileLocation(name));
	}

	private boolean navigateToLocation(IASTFileLocation fileloc) {
		if (fileloc == null) {
			return false;
		}
		final IPath path = new Path(fileloc.getFileName());
		final int offset = fileloc.getNodeOffset();
		final int length = fileloc.getNodeLength();

		runInUIThread(() -> {
			try {
				fAction.open(path, offset, length);
			} catch (CoreException e) {
				CUIPlugin.log(e);
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

	private void openInclude(IPath path) {
		runInUIThread(() -> {
			try {
				fAction.open(path, 0, 0);
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		});
	}

	private void openInclude(IASTPreprocessorIncludeStatement incStmt) {
		String name = null;
		if (incStmt.isResolved())
			name = incStmt.getPath();

		List<IPath> paths = null;
		if (name != null) {
			paths = new ArrayList<>();
			paths.add(new Path(name));
		} else if (!incStmt.isActive()) {
			// Includes inside inactive preprocessor branches will not be resolved in the AST.
			// For these, attempt resolving the include via the C model as a fallback.
			try {
				ICElement element = SelectionConverter.getElementAtOffset(fTranslationUnit, fTextSelection);
				if (element instanceof IInclude) {
					paths = CElementIncludeResolver.resolveInclude((IInclude) element);
				}
			} catch (CModelException e) {
			} catch (CoreException e) {
			}
		}

		openInclude(paths, new String(incStmt.getName().toCharArray()));
	}

	private void openInclude(List<IPath> paths, String includeName) {
		if (paths == null || paths.isEmpty()) {
			fAction.reportIncludeLookupFailure(includeName);
		} else if (paths.size() == 1) {
			openInclude(paths.get(0));
		} else {
			runInUIThread(() -> {
				IPath selected = OpenActionUtil.selectPath(paths, CEditorMessages.OpenDeclarationsAction_dialog_title,
						CEditorMessages.OpenDeclarationsAction_selectMessage);
				if (selected != null) {
					openInclude(selected);
				}
			});
		}
	}

	private boolean navigationFallBack(IASTTranslationUnit ast, IASTName sourceName, NameKind kind) {
		// Bug 102643, as a fall-back we look up the selected word in the index.
		if (fSelectedText != null && fSelectedText.length() > 0) {
			try {
				final ICProject project = fTranslationUnit.getCProject();
				final char[] name = fSelectedText.toCharArray();
				List<ICElement> elems = new ArrayList<>();

				// Bug 252549, search for names in the AST first.
				Set<IBinding> primaryBindings = new HashSet<>();
				ASTNameCollector nc = new ASTNameCollector(fSelectedText);
				ast.accept(nc);
				IASTName[] candidates = nc.getNames();
				for (IASTName astName : candidates) {
					try {
						IBinding b = astName.resolveBinding();
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
				IIndexMacro[] macros = fIndex.findMacros(name, filter, fMonitor);
				for (IIndexMacro macro : macros) {
					ICElement elem = IndexUI.getCElementForMacro(project, fIndex, macro);
					if (elem != null) {
						elems.add(elem);
					}
				}

				Collection<IBinding> secondaryBindings;
				if (ast instanceof ICPPASTTranslationUnit) {
					secondaryBindings = cppRemoveSecondaryBindings(primaryBindings, sourceName);
				} else {
					secondaryBindings = defaultRemoveSecondaryBindings(primaryBindings, sourceName);
				}

				// Convert bindings to CElements.
				Collection<IBinding> bs = primaryBindings;
				for (int k = 0; k < 2; k++) {
					for (IBinding binding : bs) {
						IName[] names = findNames(fIndex, ast, kind, binding);
						// Exclude names of the same kind.
						for (int i = 0; i < names.length; i++) {
							if (getNameKind(names[i]) == kind) {
								names[i] = null;
							}
						}
						names = ArrayUtil.removeNulls(IName.class, names);
						convertToCElements(project, fIndex, names, elems);
					}
					// In case we did not find anything, consider the secondary bindings.
					if (!elems.isEmpty())
						break;
					bs = secondaryBindings;
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
			IBinding b = sourceName.resolveBinding();
			if (b != null && !(b instanceof IProblemBinding)) {
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
		List<IBinding> result = new ArrayList<>();
		String[] sourceQualifiedName = null;
		int funcArgCount = -1;
		if (sourceName != null) {
			final IBinding binding = sourceName.resolveBinding();
			if (binding != null) {
				sourceQualifiedName = CPPVisitor.getQualifiedName(binding);
				if (binding instanceof ICPPUnknownBinding) {
					LookupData data = CPPSemantics.createLookupData(sourceName);
					if (data.isFunctionCall()) {
						funcArgCount = data.getFunctionArgumentCount();
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
			if (funcArgCount >= 0) {
				// For C++ we can check the number of parameters.
				if (binding instanceof ICPPFunction) {
					ICPPFunction f = (ICPPFunction) binding;
					if (f.getRequiredArgumentCount() > funcArgCount) {
						iterator.remove();
						result.add(binding);
						continue;
					}
					if (!f.takesVarArgs() && !f.hasParameterPack()) {
						final IType[] parameterTypes = f.getType().getParameterTypes();
						int maxArgs = parameterTypes.length;
						if (maxArgs == 1 && SemanticUtil.isVoidType(parameterTypes[0])) {
							maxArgs = 0;
						}
						if (maxArgs < funcArgCount) {
							iterator.remove();
							result.add(binding);
							continue;
						}
					}
				}
			}
		}

		return result;
	}
}
