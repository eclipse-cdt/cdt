/*******************************************************************************
 * Copyright (c) 2009, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.hidemethod;

import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author Emanuel Graf IFS
 */
public class HideMethodRefactoringDescriptor extends CRefactoringDescriptor {

	public HideMethodRefactoringDescriptor(String project, String description, String comment,
			Map<String, String> arguments) {
		super(HideMethodRefactoring.ID, project, description, comment, RefactoringDescriptor.STRUCTURAL_CHANGE,
				arguments);
	}

	@Override
	public CRefactoring createRefactoring(RefactoringStatus status) throws CoreException {
		ISelection selection = getSelection();
		ICProject proj = getCProject();
		return new HideMethodRefactoring(getTranslationUnit(), selection, proj);
	}
}
