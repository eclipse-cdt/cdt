/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinelocalvariable;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;

/**
 * @since 6.5
 */
public class InlineLocalVariableRefactoringDescriptor extends CRefactoringDescriptor {
	static protected final String NAME = "name"; //$NON-NLS-1$

	public InlineLocalVariableRefactoringDescriptor(String project, String description, String comment,
			Map<String, String> arguments) {
		super(InlineLocalVariableRefactoring.ID, project, description, comment, RefactoringDescriptor.MULTI_CHANGE,
				arguments);
	}

	@Override
	public CRefactoring createRefactoring(RefactoringStatus status) throws CoreException {
		ISelection selection = getSelection();
		ICProject proj = getCProject();
		InlineLocalVariableRefactoring refactoring = new InlineLocalVariableRefactoring(getTranslationUnit(), selection,
				proj);
		refactoring.getRefactoringInfo().setName(arguments.get(NAME));
		return refactoring;
	}
}
