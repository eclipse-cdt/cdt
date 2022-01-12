/*******************************************************************************
 * Copyright (c) 2017 Pavel Marek
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Pavel Marek - initial API and implementation
 *      Marco Stornelli - Improvements
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import java.util.List;

import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.ImplementMethodRefactoring;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

/**
 * This adds "Override Methods" functionality to Source context menu
 * located both in main menu and as a pop-up menu in editor. User can select
 * which virtual methods overrides from which classes should be generated.
 * This code generation feature is supposed to be triggered mostly in header
 * files. Every method is added under corresponding visibility label, in case
 * when no labels are found, they are generated in order set in preferences.
 * <p>
 * Code of this contribution is inspired from "Generate getters and setters"
 * code generation and "Extract constant" refactoring.
 * </p>
 *
 * Steps of this refactoring are:
 * 1) Initial conditions checking.
 * The initial conditions are satisfied when
 * the selection (cursor) is located inside a class definition, this class has
 * some base classes, and there is at least one virtual method to override.
 *   During this step the {@link VirtualMethodASTVisitor} traverses the AST for
 * the current file and finds the class that is selected. The binding for this
 * class is resolved and from this binding all the informations about base
 * classes and their virtual methods are gathered inside {@link VirtualMethodContainer}.
 *
 * 2) Method selection (dialog with user).
 * {@link OverrideMethodsInputPage} represents the only <code>WizardInputPage</code>
 * that this code generation consists of. This wizard looks similar to the wizard
 * from "Generate getters and setters" - there is a <code>CheckBoxTreeView</code>
 * where parent nodes represent base classes and children nodes represent their
 * virtual methods.
 *   When one of items (virtual methods) is checked, the corresponding method
 * is saved to {@link VirtualMethodPrintData}.
 *
 * 3) Collection of all changes.
 * This step is handled just by {@link VirtualMethodPrintData} that adds
 * selected methods inside class (rewrites the corresponding AST with the help
 * of {@link org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter}).
 *
 * @author Pavel Marek
 */
public class OverrideMethodsRefactoring extends CRefactoring {
	private VirtualMethodsASTVisitor fVirtualMethodVisitor;
	private OverrideOptions fOptions;
	private VirtualMethodContainer fMethodContainer;
	private VirtualMethodPrintData fPrintData = new VirtualMethodPrintData();
	private ImplementMethodRefactoring fImplementMethodRefactoring;

	public VirtualMethodPrintData getPrintData() {
		return fPrintData;
	}

	public VirtualMethodContainer getMethodContainer() {
		return fMethodContainer;
	}

	public OverrideMethodsRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);

		fOptions = new OverrideOptions(project);
		fMethodContainer = new VirtualMethodContainer(fOptions);
		fVirtualMethodVisitor = new VirtualMethodsASTVisitor((ITextSelection) selection, tu.getFile().getName(),
				fMethodContainer, TypeUtil.getFullyQualifiedName(element).getFullyQualifiedName());
		fImplementMethodRefactoring = new ImplementMethodRefactoring(element, selection, project);
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		return null;
	}

	/**
	 * Called when preview button is pushed, that means that there is at least
	 * one modification.
	 */
	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		List<IASTSimpleDeclaration> methods = fPrintData.rewriteAST(refactoringContext, collector,
				fVirtualMethodVisitor);
		fImplementMethodRefactoring.setContext(refactoringContext);
		fImplementMethodRefactoring.collectModifications(pm, collector, methods, fPrintData.getParentOffset());
	}

	/**
	 * Removes already overridden methods from VirtualMethodContainer.
	 * This method should be called after fVirtualMethodVisitor visited the ast,
	 * ie. fMethodContainer was filled.
	 */
	private void removeOverridenMethods() {
		ICPPClassType classType = fVirtualMethodVisitor.getClassBinding();

		// Remove all methods that are declared in this class.
		for (ICPPMethod method : classType.getDeclaredMethods()) {
			fMethodContainer.remove(method);
		}
	}

	/**
	 * Checks whether selection is inside class.
	 * Also initializes fMethodContainer.
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor subMonitor = SubMonitor.convert(pm, 5);
		RefactoringStatus status = super.checkInitialConditions(pm);
		IASTTranslationUnit ast = getAST(tu, subMonitor.split(1));

		// Find the class inside which has selection inside it.
		fVirtualMethodVisitor.visitAst(ast);
		subMonitor.worked(3);

		if (fVirtualMethodVisitor.getClassNode() == null) {
			status.addFatalError(Messages.OverrideMethodsRefactoring_SelNotInClass);
			return status;
		}

		// Discard methods that are already overridden from fMethodContainer.
		removeOverridenMethods();

		if (fMethodContainer.isEmpty()) {
			status.addFatalError(Messages.OverrideMethodsRefactoring_NoMethods);
			return status;
		}

		return status;
	}

	@Override
	protected RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		fImplementMethodRefactoring.finalConditions(checkContext);
		return result;
	}

	public OverrideOptions getOptions() {
		return fOptions;
	}
}
