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
package org.eclipse.cdt.internal.ui.refactoring.pushdown.ui;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import org.eclipse.cdt.internal.ui.refactoring.pullup.PushDownRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.Messages;

public class PushDownWizard extends RefactoringWizard {

	
	public PushDownWizard(PushDownRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
	}
	
	
	
	@Override
	public boolean canFinish() {
		final PushDownRefactoring refactoring = (PushDownRefactoring) this.getRefactoring();
		return super.canFinish() && !refactoring.getInformation().getSelectedMembers().isEmpty();
	}
	
	
	
	@Override
	protected void addUserInputPages() {
		final PushDownRefactoring refactoring = (PushDownRefactoring) this.getRefactoring();
		this.addPage(PushDownMemberPageControl.createWizardPage(
				Messages.PushDownRefactoring_selectMemberPage, 
				refactoring.getInformation()));
		
		this.addPage(PushDownTargetClassControl.createWizardPage(
				Messages.PushDownRefactoring_selectTargetsPage, 
				refactoring.getInformation()));
	}

}
