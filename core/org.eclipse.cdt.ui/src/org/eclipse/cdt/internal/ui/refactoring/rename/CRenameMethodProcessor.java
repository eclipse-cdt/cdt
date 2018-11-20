/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc. and others.
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
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

/**
 * Rename processor for methods.
 */
public class CRenameMethodProcessor extends CRenameGlobalProcessor {
	public CRenameMethodProcessor(CRenameProcessor processor, String kind, boolean isVirtual) {
		super(processor, kind);
		if (isVirtual)
			setAvailableOptions(getAvailableOptions() | CRefactory.OPTION_DO_VIRTUAL);
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws OperationCanceledException, CoreException {
		CRefactoringArgument argument = getArgument();
		IBinding binding = argument.getBinding();
		if (binding instanceof ICPPConstructor) {
			return RefactoringStatus
					.createFatalErrorStatus(RenameMessages.CRenameMethodProcessor_fatalError_renameConstructor);
		}
		String identifier = argument.getName();
		if (identifier.startsWith("~")) { //$NON-NLS-1$
			return RefactoringStatus
					.createFatalErrorStatus(RenameMessages.CRenameMethodProcessor_fatalError_renameDestructor);
		}
		if (identifier.startsWith("operator") && //$NON-NLS-1$
				identifier.length() > 8 && !CRefactoringUtils.isIdentifierChar(identifier.charAt(8))) {
			return RefactoringStatus
					.createFatalErrorStatus(RenameMessages.CRenameMethodProcessor_fatalError_renameOperator);
		}
		return super.checkInitialConditions(pm);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor, CheckConditionsContext context)
			throws OperationCanceledException, CoreException {
		CRefactoringArgument argument = getArgument();
		RefactoringStatus result = new RefactoringStatus();
		IScope scope = argument.getScope();
		if (scope != null) {
			IASTNode node = ASTInternal.getPhysicalNodeOfScope(scope);
			if (node instanceof IASTCompositeTypeSpecifier) {
				IASTCompositeTypeSpecifier se = (IASTCompositeTypeSpecifier) node;
				IASTName name = ASTManager.getSimpleName(se.getName());
				if (getReplacementText().equals(name.toString())) {
					return RefactoringStatus.createFatalErrorStatus(
							RenameMessages.CRenameMethodProcessor_fatalError_renameToConstructor);
				}
				if (getReplacementText().startsWith("~")) { //$NON-NLS-1$
					return RefactoringStatus.createFatalErrorStatus(
							RenameMessages.CRenameMethodProcessor_fatalError_renameToDestructor);
				}
				if (!CRefactoringUtils.checkIdentifier(getReplacementText())) {
					result.merge(RefactoringStatus
							.createErrorStatus(RenameMessages.CRenameMethodProcessor_warning_illegalCharacters));
				}
			}
		}
		if (argument.getArgumentKind() == CRefactory.ARGUMENT_VIRTUAL_METHOD
				&& (getSelectedOptions() & CRefactory.OPTION_DO_VIRTUAL) == 0) {
			result.merge(
					RefactoringStatus.createWarningStatus(RenameMessages.CRenameMethodProcessor_warning_renameVirtual));
		}

		result.merge(super.checkFinalConditions(monitor, context));
		return result;
	}

	@Override
	protected IBinding[] getBindingsToBeRenamed(RefactoringStatus status) {
		if ((getSelectedOptions() & CRefactory.OPTION_DO_VIRTUAL) == 0) {
			return super.getBindingsToBeRenamed(status);
		}

		CRefactoringArgument argument = getArgument();
		IBinding binding = argument.getBinding();
		IASTNode node = ASTInternal.getPhysicalNodeOfScope(argument.getScope());
		ArrayList<IBinding> bindings = new ArrayList<>();
		if (binding != null) {
			recordRename(binding);
			bindings.add(binding);
		}
		if (binding instanceof ICPPMethod) {
			ICPPMethod m = (ICPPMethod) binding;
			try {
				CPPSemantics.pushLookupPoint(node);
				IBinding[] bs = ClassTypeHelper.findOverridden(m);
				bindings.addAll(Arrays.asList(bs));
				bs = ClassTypeHelper.findOverriders(getIndex(), m);
				bindings.addAll(Arrays.asList(bs));
			} catch (CoreException e) {
				status.addError(e.getMessage());
			} finally {
				CPPSemantics.popLookupPoint();
			}
		}
		return bindings.toArray(new IBinding[bindings.size()]);
	}
}
