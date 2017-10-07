/******************************************************************************* 
 * Copyright (c) 2017 Pavel Marek 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html   
 *  
 * Contributors:  
 *      Pavel Marek - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.ui.CUIPlugin;

public class OverrideMethodsWizard extends RefactoringWizard {
	private OverrideMethodsRefactoring refactoring;
	
	
	public OverrideMethodsWizard(OverrideMethodsRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		this.refactoring= refactoring;
		setDefaultPageTitle(Messages.OverrideMethodsInputPage_Name);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	@Override
	protected void addUserInputPages() {
		addPage(new OverrideMethodsInputPage(refactoring));
	}

}
