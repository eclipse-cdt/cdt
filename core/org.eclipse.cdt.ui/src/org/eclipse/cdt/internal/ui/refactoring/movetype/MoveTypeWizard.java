/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Ball (Google) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.movetype;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * The wizard page for Move Type refactoring, creates the UI page.
 */
public class MoveTypeWizard extends RefactoringWizard {

	public MoveTypeWizard(MoveTypeRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(Messages.MoveType);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	@Override
	protected void addUserInputPages() {}

}
