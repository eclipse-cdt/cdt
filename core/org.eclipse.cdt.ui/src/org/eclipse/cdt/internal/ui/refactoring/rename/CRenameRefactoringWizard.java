/*******************************************************************************
 * Copyright (c) 2004, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Markus Schorn - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Refactoring Wizard adding the input page.
 */
public class CRenameRefactoringWizard extends RefactoringWizard {

	public CRenameRefactoringWizard(CRenameRefactoring r) {
		super(r, DIALOG_BASED_USER_INTERFACE);
	}

	// overrider
	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(getRefactoring().getName());
		CRenameRefactoringInputPage page = new CRenameRefactoringInputPage();
		addPage(page);
	}
}
