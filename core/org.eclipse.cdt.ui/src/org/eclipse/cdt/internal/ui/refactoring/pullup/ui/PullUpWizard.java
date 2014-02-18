/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup.ui;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpRefactoring;

public class PullUpWizard extends RefactoringWizard {

	public PullUpWizard(PullUpRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	
	
	@Override
	protected void addUserInputPages() {
		final PullUpRefactoring refactoring = (PullUpRefactoring) this.getRefactoring();
		addPage(PullUpMemberPageControl.createWizardPage(
				Messages.PullUpRefactoring_selectMemberPage, 
				refactoring.getInformation()));
		addPage(RemoveFromClassControl.createWizardPage(
				Messages.PullUpRefactoring_selectRemovePage, 
				refactoring.getInformation()));
	}
}
