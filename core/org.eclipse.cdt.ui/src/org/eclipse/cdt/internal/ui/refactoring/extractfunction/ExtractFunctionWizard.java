/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
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
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ExtractFunctionWizard extends RefactoringWizard {
	public ExtractFunctionWizard(ExtractFunctionRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(Messages.ExtractFunctionRefactoring_ExtractFunction);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	@Override
	protected void addUserInputPages() {
		addPage(new ExtractFunctionInputPage());
	}
}
