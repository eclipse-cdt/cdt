/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinelocalvariable;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * The wizard page for Extract Local Variable refactoring, creates the UI page.
 * 
 * @since 6.5
 */
public class InlineLocalVariableWizard extends RefactoringWizard {

	public InlineLocalVariableWizard(InlineLocalVariableRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(Messages.InlineLocalVariable);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	@Override
	protected void addUserInputPages() {}

}
