/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.filters.actions;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.filters.SystemFilterDialogInterface;
import org.eclipse.rse.ui.filters.dialogs.ISystemFilterWizard;
import org.eclipse.rse.ui.filters.dialogs.SystemFilterWizardDialog;
import org.eclipse.swt.widgets.Shell;


public abstract class SystemFilterAbstractFilterWizardAction
	extends SystemFilterAbstractFilterAction 
{


    
	/**
	 * Constructor for SystemFilterAbstactFilterWizardAction
	 */
	public SystemFilterAbstractFilterWizardAction(Shell parent, String title) 
	{
		super(parent, title);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);		
	}

	/**
	 * Constructor for SystemFilterAbstactFilterWizardAction
	 */
	public SystemFilterAbstractFilterWizardAction(Shell parent, String label, String tooltip) 
	{
		super(parent, label, tooltip);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);		
	}

	/**
	 * @see SystemFilterAbstractFilterAction#doOKprocessing(Object)
	 */
	public void doOKprocessing(Object dlgValue) 
	{
	}

	/**
	 * @see SystemFilterAbstractFilterAction#getDialogValue(Dialog)
	 */
	protected Object getDialogValue(Dialog dlg) 
	{
		return null;
	}

	/**
	 * @see SystemFilterAbstractFilterAction#createFilterDialog(Shell)
	 */
	public SystemFilterDialogInterface createFilterDialog(Shell parent) 
	{
		ISystemFilterWizard newWizard = getFilterWizard();	
		  		
	    SystemFilterDialogInterface dialog = 
	        new SystemFilterWizardDialog(parent, newWizard);	    
	        
	    return dialog;
	}

	/**
	 * Return the wizard so we can customize it prior to showing it.
	 */
	public abstract ISystemFilterWizard getFilterWizard();

}