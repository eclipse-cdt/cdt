/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.cdt.internal.corext.refactoring.base.Refactoring;

/**
 * An abstract superclass for all wizard pages added to a refactoring wizard. The
 * class provides access to the refactoring and the refactoring wizard.
 * 
 * @see RefactoringWizard
 */
public abstract class RefactoringWizardPage extends WizardPage {

	public static final String REFACTORING_SETTINGS= "org.eclipse.cdt.ui.refactoring"; //$NON-NLS-1$

	/**
	 * Creates a new refactoring wizard page.
	 * 
	 * @param name the page's name.
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	protected RefactoringWizardPage(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizardPage.
	 */
	public void setWizard(IWizard newWizard) {
		Assert.isTrue(newWizard instanceof RefactoringWizard);
		super.setWizard(newWizard);
	}

	/**
	 * Returns the refactoring used by the wizard to which this page belongs.
	 * Returns <code>null</code> if the page isn't added to any wizard yet.
	 * 
	 * @return the refactoring associated with this refactoring wizard page
	 */
	protected Refactoring getRefactoring() {
		RefactoringWizard wizard= getRefactoringWizard();
		if (wizard == null)
			return null;
		return wizard.getRefactoring();
	}
	
	/**
	 * Returns the page's refactoring wizard.
	 * 
	 * @return the page's refactoring wizard
	 */
	protected RefactoringWizard getRefactoringWizard() {
		return (RefactoringWizard)getWizard();
	}
	
	/**
	 * The user has pressed the finish button. Perform the page specific finish
	 * action. 
	 * 
	 * @return <code>true</code> if finish operation ended without errors.
	 * 	Otherwise <code>false</code> is returned.
	 */
	protected boolean performFinish() {
		return true;
	}
	
	/**
	 * Returns the refactoring dialog settings.
	 * 
	 * @return the refactoring dialog settings.
	 */
	protected IDialogSettings getRefactoringSettings() {
		IDialogSettings settings= getDialogSettings();
		if (settings == null)
			return null;
		IDialogSettings result= settings.getSection(REFACTORING_SETTINGS);
		if (result == null) {
			result= new DialogSettings(REFACTORING_SETTINGS);
			settings.addSection(result); 
		}
		return result;
	}
}
