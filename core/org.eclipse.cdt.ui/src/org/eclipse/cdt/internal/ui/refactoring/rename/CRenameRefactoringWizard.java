/*******************************************************************************
 * Copyright (c) 2004, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
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
        CRenameRefactoringInputPage page= new CRenameRefactoringInputPage();
        addPage(page);
    }
}
