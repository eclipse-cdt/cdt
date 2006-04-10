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

package org.eclipse.rse.files.ui.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.wizards.SystemNewFolderWizard;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.actions.SystemBaseWizardAction;
import org.eclipse.swt.widgets.Shell;



/**
 * An action for prompting the user with a wizard, for creating a new folder
 */
public class SystemNewFolderAction extends SystemBaseWizardAction
{

	/**
	 * Constructor when you want to use the default label, tooltip and image
	 */
	public SystemNewFolderAction(Shell parent) 
	{
		this(FileResources.ACTION_NEWFOLDER_LABEL, 
				FileResources.ACTION_NEWFOLDER_TOOLTIP,
		     SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFOLDER_ID), 
		     parent);
	}
	
	/**
	 * Constructor when you want to supply your own label, tooltip and image
	 */	
    public SystemNewFolderAction(String text, String tooltip, ImageDescriptor image, Shell parent)
    {
   	     super(text, tooltip, image, parent);
		 setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);
		 allowOnMultipleSelection(false);
    }	

	/**
	 * Override of parent to create the wizard
	 */
    protected  IWizard createWizard()
    {
   	     SystemNewFolderWizard newFolderWizard  = new SystemNewFolderWizard();
   	     return newFolderWizard;
    }
   
}