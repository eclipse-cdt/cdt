/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.reducer;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.ui.CUIPlugin;

public class RemoveUnusedDeclarationsWizard extends RefactoringWizard {
	public RemoveUnusedDeclarationsWizard(RemoveUnusedDeclarationsRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(Messages.RemoveUnusedDeclarationsRefactoring_RemoveUnusedDeclarations);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	@Override
	protected void addUserInputPages() {
	}
}

