/*******************************************************************************
 * Copyright (c) 2013 - Xdin AB
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Erik Johansson
 ******************************************************************************/


package org.eclipse.cdt.internal.ui.refactoring.createmethod;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class CreateMethodRefactoringWizard extends RefactoringWizard {

	public CreateMethodRefactoringWizard(Refactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(Messages.CreateMethodRefactoringWizard_PageTitle);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}
	
	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();
		((CreateMethodRefactoring) getRefactoring()).shouldOpenLocation(true);
		return result;
	}

	@Override
	protected void addUserInputPages() {	
		CreateMethodRefactoring ref = (CreateMethodRefactoring) getRefactoring();
		if (ref.getNumberOfParameters() < 1)
			return;
		else		
			addPage(new CreateMethodRefactoringWizardPage());
	}
}
