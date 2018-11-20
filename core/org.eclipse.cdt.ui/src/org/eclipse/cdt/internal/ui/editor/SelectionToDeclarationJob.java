/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Based on class OpenDeclarationsJob
 * @author Alvaro Sanchez-Leon
 * @since 5.6
 */
public class SelectionToDeclarationJob extends Job implements ASTRunnable {
	private enum NameKind {
		REFERENCE, DECLARATION, USING_DECL, DEFINITION
	}

	private final ITranslationUnit fTranslationUnit;
	private IIndex fIndex;
	private final ITextSelection fTextSelection;
	private IFunctionDeclaration[] fResolvedFunctions;

	public SelectionToDeclarationJob(ITextEditor editor, ITextSelection textSelection) throws CoreException {
		super(CEditorMessages.OpenDeclarations_dialog_title);

		if (!(editor instanceof CEditor)) {
			IStatus status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, ICStatusConstants.INTERNAL_ERROR,
					"Action not supportted outside the context of the C Editor", null); //$NON-NLS-1$
			throw new CoreException(status);
		}

		fTranslationUnit = ((CEditor) editor).getInputCElement();
		fTextSelection = textSelection;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			return resolve(monitor);
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	IStatus resolve(IProgressMonitor monitor) throws CoreException {
		assert fIndex == null;
		if (fIndex != null)
			return Status.CANCEL_STATUS;

		fIndex = CCorePlugin.getIndexManager().getIndex(fTranslationUnit.getCProject(), IIndexManager.ADD_DEPENDENCIES
				| IIndexManager.ADD_DEPENDENT | IIndexManager.ADD_EXTENSION_FRAGMENTS_NAVIGATION);

		try {
			fIndex.acquireReadLock();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}

		try {
			return ASTProvider.getASTProvider().runOnAST(fTranslationUnit, ASTProvider.WAIT_NO, monitor, this);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	@Override
	public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {

		// initialize to empty results
		fResolvedFunctions = new IFunctionDeclaration[0];

		if (ast == null) {
			return Status.OK_STATUS;
		}

		int selectionStart = fTextSelection.getOffset();
		int selectionLength = fTextSelection.getLength();

		final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);

		IASTName sourceName = nodeSelector.findEnclosingName(selectionStart, selectionLength);
		if (sourceName == null) {
			IStatus status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,
					CEditorMessages.StepIntoSelection_unable_to_resolve_name);
			throw new CoreException(status);
		}

		IName[] implicitTargets = findImplicitTargets(ast, nodeSelector, selectionStart, selectionLength);

		NameKind kind = getNameKind(sourceName);
		IBinding b = sourceName.resolveBinding();
		IBinding[] bindings = new IBinding[] { b };
		if (b instanceof IProblemBinding) {
			IBinding[] candidateBindings = ((IProblemBinding) b).getCandidateBindings();
			if (candidateBindings.length != 0) {
				bindings = candidateBindings;
			}
		} else if (kind == NameKind.DEFINITION && b instanceof IType) {
			// No resolution of type definitions.
			return Status.OK_STATUS;
		}

		IName[] targets = IName.EMPTY_ARRAY;
		String filename = ast.getFilePath();
		for (IBinding binding : bindings) {
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

		final ArrayList<IFunctionDeclaration> functionElements = new ArrayList<>();
		filterToFunctions(fTranslationUnit.getCProject(), fIndex, targets, functionElements);

		// save the resolved function declarations
		fResolvedFunctions = functionElements.toArray(new IFunctionDeclaration[functionElements.size()]);

		return Status.OK_STATUS;
	}

	/**
	 * This is the method used to retrieve the results from this job
	 * @return The function(s) matching the selection
	 */
	public IFunctionDeclaration[] getSelectedFunctions() {
		return fResolvedFunctions;
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
			declNames = findDefinitions(index, ast, kind, binding);
		}

		if (declNames.length == 0) {
			if (kind == NameKind.DEFINITION) {
				declNames = findDefinitions(index, ast, kind, binding);
			} else {
				declNames = findDeclarations(index, ast, binding);
			}
		}
		return declNames;
	}

	private IName[] findDefinitions(IIndex index, IASTTranslationUnit ast, NameKind kind, IBinding binding)
			throws CoreException {
		List<IASTName> declNames = new ArrayList<>();
		declNames.addAll(Arrays.asList(ast.getDefinitionsInAST(binding)));
		for (Iterator<IASTName> i = declNames.iterator(); i.hasNext();) {
			IASTName name = i.next();
			final IBinding b2 = name.resolveBinding();
			if (b2 instanceof ICPPUsingDeclaration) {
				i.remove();
			}
			if (binding != b2 && binding instanceof ICPPSpecialization) {
				// Make sure binding specializes b2 so that for instance we do
				// not navigate from
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
		}
		if (!declNames.isEmpty()) {
			return declNames.toArray(new IASTName[declNames.size()]);
		}

		// 2. Try definition in index.
		return index.findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
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
		// 'using' declarations are considered only when there are no other
		// declarations.
		if (declNames.length == 0 && usingDeclarations != null) {
			declNames = usingDeclarations.toArray(new IName[usingDeclarations.size()]);
		}
		return declNames;
	}

	/**
	 * Returns definitions of bindings referenced by implicit name at the given location.
	 */
	private IName[] findImplicitTargets(IASTTranslationUnit ast, IASTNodeSelector nodeSelector, int offset, int length)
			throws CoreException {
		IName[] definitions = IName.EMPTY_ARRAY;
		IASTName firstName = nodeSelector.findEnclosingImplicitName(offset, length);
		if (firstName != null) {
			IASTImplicitNameOwner owner = (IASTImplicitNameOwner) firstName.getParent();
			for (IASTImplicitName name : owner.getImplicitNames()) {
				if (((ASTNode) name).getOffset() == ((ASTNode) firstName).getOffset()) {
					IBinding binding = name.resolveBinding(); // Guaranteed to
																// resolve.
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
		return loc1.getFileName().equals(loc2.getFileName())
				&& max(loc1.getNodeOffset(), loc2.getNodeOffset()) < min(loc1.getNodeOffset() + loc1.getNodeLength(),
						loc2.getNodeOffset() + loc2.getNodeLength());
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

	private void filterToFunctions(ICProject project, IIndex index, IName[] declNames,
			List<IFunctionDeclaration> functionElements) {
		for (IName declName : declNames) {
			try {
				ICElement elem = getCElementForName(project, index, declName);
				if (elem instanceof IFunctionDeclaration) {
					functionElements.add((IFunctionDeclaration) elem);
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
			IBinding binding = astName.resolveBinding();
			if (binding != null) {
				ITranslationUnit tu = IndexUI.getTranslationUnit(astName);
				if (tu != null) {
					if (tu instanceof IWorkingCopy)
						tu = ((IWorkingCopy) tu).getOriginalElement();
					IASTFileLocation loc = astName.getFileLocation();
					IRegion region = new Region(loc.getNodeOffset(), loc.getNodeLength());
					return CElementHandleFactory.create(tu, binding, astName.isDefinition(), region, 0);
				}
			}
			return null;
		}
		return null;
	}

}
