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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;

import org.eclipse.cdt.internal.corext.refactoring.IRefactoringProcessor;
import org.eclipse.cdt.internal.corext.refactoring.base.Refactoring;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringStarter;

/**
 * Opens the user interface for a given refactoring.
 */
public class UserInterfaceStarter {
	
	protected static final String WIZARD= "wizard"; //$NON-NLS-1$
	
	private IConfigurationElement fConfigElement;
	
	/**
	 * Opens the user interface for the given refactoring. The provided
	 * shell should be used as a parent shell.
	 * 
	 * @param refactoring the refactoring for which the user interface
	 *  should be opened
	 * @param parent the parent shell to be used
	 * 
	 * @exception CoreException if the user interface can't be activated
	 */
	public static void run(Refactoring refactoring, Shell parent) throws CoreException {
		run(refactoring, parent, true);
	}
	
	/**
	 * Opens the user interface for the given refactoring. The provided
	 * shell should be used as a parent shell.
	 * 
	 * @param refactoring the refactoring for which the user interface
	 *  should be opened
	 * @param parent the parent shell to be used
	 * @param forceSave <code>true<code> if saving is needed before
	 *  executing the refactoring
	 * 
	 * @exception CoreException if the user interface can't be activated
	 */
	public static void run(Refactoring refactoring, Shell parent, boolean forceSave) throws CoreException {
		IRefactoringProcessor processor= (IRefactoringProcessor)refactoring.getAdapter(IRefactoringProcessor.class);
		// TODO this should change. Either IRefactoring models Refactoring API. 
		Assert.isNotNull(processor);
		UserInterfaceStarter starter= new UserInterfaceStarter();
		if(starter != null) {
			starter.activate(refactoring, parent, forceSave);
		} else 
		{	
		MessageDialog.openInformation(parent, 
			refactoring.getName(), 
			RefactoringMessages.getString("UserInterfaceStarter.No_ui_found")); //$NON-NLS-1$
		}
	}
		
	protected void activate(Refactoring refactoring, Shell parent, boolean save) throws CoreException {
		RenameElementWizard wizard= new RenameElementWizard();	
		wizard.initialize(refactoring);	
		new RefactoringStarter().activate(refactoring, wizard, parent, wizard.getDefaultPageTitle(), save);
	}
}
