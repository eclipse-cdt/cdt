/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.hidemethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.AccessContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author Guido Zgraggen IFS
 */
public class HideMethodRefactoring extends CRefactoring {
	public static final String ID = "org.eclipse.cdt.internal.ui.refactoring.hidemethod.HideMethodRefactoring"; //$NON-NLS-1$

	private IASTName methodName;
	private IASTDeclaration methodDeclaration;

	public HideMethodRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		name = Messages.HideMethodRefactoring_HIDE_METHOD;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		try {
			super.checkInitialConditions(sm.newChild(8));

			if (initStatus.hasFatalError()) {
				return initStatus;
			}

			if (isProgressMonitorCanceled(sm, initStatus))
				return initStatus;

			List<IASTName> names = findAllMarkedNames();
			if (names.isEmpty()) {
				initStatus.addFatalError(Messages.HideMethodRefactoring_NoNameSelected);
				return initStatus;
			}
			IASTName name = names.get(names.size() - 1);

			methodName = DefinitionFinder.getMemberDeclaration(name, refactoringContext, sm.newChild(1));
			if (methodName == null) {
				initStatus.addFatalError(Messages.HideMethodRefactoring_NoMethodNameSelected);
				return initStatus;
			}

			IASTDeclarator decl = (IASTDeclarator) methodName.getParent();
			decl = CPPVisitor.findOutermostDeclarator(decl);
			methodDeclaration = (IASTDeclaration) decl.getParent();
			if (methodDeclaration == null
					|| !(methodDeclaration.getParent() instanceof ICPPASTCompositeTypeSpecifier)) {
				initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods);
				return initStatus;
			}

			if (isProgressMonitorCanceled(sm, initStatus))
				return initStatus;
			if (methodDeclaration instanceof IASTFunctionDefinition) {
				IASTDeclarator declarator = ((IASTFunctionDefinition) methodDeclaration).getDeclarator();
				if (ASTQueries.findInnermostDeclarator(declarator).getName().getRawSignature()
						.equals(name.getRawSignature())) {
					if (!(declarator instanceof IASTFunctionDeclarator)) {
						initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods);
						return initStatus;
					}
				}
			} else if (methodDeclaration instanceof IASTSimpleDeclaration) {
				for (IASTDeclarator declarator : ((IASTSimpleDeclaration) methodDeclaration).getDeclarators()) {
					if (declarator.getName().getRawSignature().equals(name.getRawSignature())) {
						if (!(declarator instanceof IASTFunctionDeclarator)) {
							initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods);
							return initStatus;
						}
					}
				}
			} else {
				initStatus.addFatalError(Messages.HideMethodRefactoring_CanOnlyHideMethods);
				return initStatus;
			}

			IASTCompositeTypeSpecifier classNode = ASTQueries.findAncestorWithType(methodName,
					IASTCompositeTypeSpecifier.class);
			if (classNode == null) {
				initStatus.addError(Messages.HideMethodRefactoring_EnclosingClassNotFound);
			}

			if (checkIfPrivate(classNode, methodDeclaration)) {
				initStatus.addError(Messages.HideMethodRefactoring_IsAlreadyPrivate);
			}
			return initStatus;
		} finally {
			sm.done();
		}
	}

	private boolean checkIfPrivate(IASTCompositeTypeSpecifier classNode, IASTDeclaration decl) {
		IASTDeclaration[] members = classNode.getMembers();
		int currentVisibility = ICPPASTVisibilityLabel.v_private;
		if (IASTCompositeTypeSpecifier.k_struct == classNode.getKey()) {
			currentVisibility = ICPPASTVisibilityLabel.v_public;
		}
		for (IASTDeclaration declaration : members) {
			if (declaration instanceof ICPPASTVisibilityLabel) {
				currentVisibility = ((ICPPASTVisibilityLabel) declaration).getVisibility();
			}

			if (declaration != null) {
				if (decl == declaration) {
					break;
				}
			}
		}
		if (ICPPASTVisibilityLabel.v_private == currentVisibility) {
			return true;
		}
		return false;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext checkContext)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		try {
			RefactoringStatus status = new RefactoringStatus();
			IIndex index = getIndex();
			IIndexBinding methodBinding = index.adaptBinding(methodName.resolveBinding());
			if (methodBinding == null)
				return null;
			List<IASTName> references = new ArrayList<>();
			Set<String> searchedFiles = new HashSet<>();
			IEditorPart[] dirtyEditors = EditorUtility.getDirtyEditors(true);
			SubMonitor loopProgress = sm.newChild(3).setWorkRemaining(dirtyEditors.length);
			for (IEditorPart editor : dirtyEditors) {
				if (sm.isCanceled()) {
					throw new OperationCanceledException();
				}
				IEditorInput editorInput = editor.getEditorInput();
				if (editorInput instanceof ITranslationUnitEditorInput) {
					ITranslationUnit tu = CModelUtil
							.toWorkingCopy(((ITranslationUnitEditorInput) editorInput).getTranslationUnit());
					searchedFiles.add(tu.getLocation().toOSString());
					IASTTranslationUnit ast = getAST(tu, loopProgress.newChild(1));
					for (IASTName reference : ast.getReferences(methodBinding)) {
						if (!AccessContext.isAccessible(methodBinding, ICPPMember.v_private, reference)) {
							status.addWarning(Messages.HideMethodRefactoring_HasExternalReferences);
							return status;
						}
					}
				}
			}

			IIndexName[] referencesFromIndex = index.findReferences(methodBinding);
			int remainingCount = referencesFromIndex.length;
			loopProgress = sm.newChild(6).setWorkRemaining(remainingCount);
			for (IIndexName name : referencesFromIndex) {
				if (sm.isCanceled()) {
					throw new OperationCanceledException();
				}
				ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(name.getFile().getLocation(), null);
				if (searchedFiles.add(tu.getLocation().toOSString())) {
					IASTTranslationUnit ast = getAST(tu, loopProgress.newChild(1));
					for (IASTName reference : ast.getReferences(methodBinding)) {
						if (!AccessContext.isAccessible(methodBinding, ICPPMember.v_private, reference)) {
							status.addWarning(Messages.HideMethodRefactoring_HasExternalReferences);
							return status;
						}
					}
					ArrayUtil.addAll(references, ast.getReferences(methodBinding));
				}
				loopProgress.setWorkRemaining(--remainingCount);
			}

			return status;
		} finally {
			sm.done();
		}
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		ASTRewrite rewriter = collector.rewriterForTranslationUnit(methodName.getTranslationUnit());
		TextEditGroup editGroup = new TextEditGroup(
				Messages.HideMethodRefactoring_FILE_CHANGE_TEXT + methodName.getRawSignature());

		ICPPASTCompositeTypeSpecifier classDefinition = (ICPPASTCompositeTypeSpecifier) methodDeclaration.getParent();
		ClassMemberInserter.createChange(classDefinition, VisibilityEnum.v_private, methodDeclaration, false,
				collector);

		rewriter.remove(methodDeclaration, editGroup);
	}

	private List<IASTName> findAllMarkedNames() throws OperationCanceledException, CoreException {
		final ArrayList<IASTName> namesVector = new ArrayList<>();

		IASTTranslationUnit ast = getAST(tu, null);
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name.isPartOfTranslationUnitFile()
						&& SelectionHelper.doesNodeOverlapWithRegion(name, selectedRegion)) {
					if (!(name instanceof ICPPASTQualifiedName)) {
						namesVector.add(name);
					}
				}
				return super.visit(name);
			}
		});
		return namesVector;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		Map<String, String> arguments = getArgumentMap();
		RefactoringDescriptor desc = new HideMethodRefactoringDescriptor(project.getProject().getName(),
				"Hide Method Refactoring", "Hide Method " + methodName.getRawSignature(), arguments); //$NON-NLS-1$//$NON-NLS-2$
		return desc;
	}

	private Map<String, String> getArgumentMap() {
		Map<String, String> arguments = new HashMap<>();
		arguments.put(CRefactoringDescriptor.FILE_NAME, tu.getLocationURI().toString());
		arguments.put(CRefactoringDescriptor.SELECTION, selectedRegion.getOffset() + "," + selectedRegion.getLength()); //$NON-NLS-1$
		return arguments;
	}
}
