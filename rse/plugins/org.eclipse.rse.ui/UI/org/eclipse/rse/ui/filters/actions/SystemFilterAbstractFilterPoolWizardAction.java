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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogInterface;
import org.eclipse.rse.ui.filters.dialogs.SystemFilterPoolWizardDialog;
import org.eclipse.rse.ui.filters.dialogs.SystemFilterPoolWizardInterface;
import org.eclipse.swt.widgets.Shell;


public abstract class SystemFilterAbstractFilterPoolWizardAction
	extends SystemFilterAbstractFilterPoolAction 
{



	/**
	 * Constructor for SystemFilterAbstactFilterPoolWizardAction
	 */
	public SystemFilterAbstractFilterPoolWizardAction(Shell parent, String title) 
	{
		super(parent, title);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);		
	}

	

	/**
	 * Constructor for SystemFilterAbstactFilterPoolWizardAction
	 */
	public SystemFilterAbstractFilterPoolWizardAction(Shell parent, ImageDescriptor image,
	                                                  String label, String tooltip) 
	{
		super(parent, image, label, tooltip);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);		
	}
	
	/**
	 * @see SystemFilterAbstractFilterPoolAction#doOKprocessing(Object)
	 */
	public void doOKprocessing(Object dlgValue) 
	{
	}

	/**
	 * @see SystemFilterAbstractFilterPoolAction#getDialogValue(Dialog)
	 */
	protected Object getDialogValue(Dialog dlg) 
	{
		return null;
	}

	/**
	 * @see SystemFilterAbstractFilterPoolAction#createFilterPoolDialog(Shell)
	 */
	public SystemFilterPoolDialogInterface createFilterPoolDialog(Shell parent) 
	{
		SystemFilterPoolWizardInterface newWizard = getFilterPoolWizard();		    
	    SystemFilterPoolDialogInterface dialog = 
	        new SystemFilterPoolWizardDialog(parent, newWizard);	    
	    return dialog;
	}

	/**
	 * Return the wizard so we can customize it prior to showing it.
	 */
	public abstract SystemFilterPoolWizardInterface getFilterPoolWizard();

}