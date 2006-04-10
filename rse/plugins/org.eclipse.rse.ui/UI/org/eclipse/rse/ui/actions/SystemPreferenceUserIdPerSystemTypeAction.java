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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.SystemType;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.ui.dialogs.SystemUserIdPerSystemTypeDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * A selectable system type overall default userId action.
 */
public class SystemPreferenceUserIdPerSystemTypeAction extends SystemBaseDialogAction 
                                 
{	
	
	private SystemType systemType;
	
	/**
	 * Constructor
	 */
	public SystemPreferenceUserIdPerSystemTypeAction(Shell parent, SystemType systemType) 
	{
		super(systemType.getName()+"...",null,parent);
		this.systemType = systemType;
        setSelectionSensitive(false);
        
		setHelp(SystemPlugin.HELPPREFIX+"actn0010");
	}

	/**
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		return enable;
	}

    /*
	 * Override of parent
	 * @see #run()
	 */
	protected Dialog createDialog(Shell parent)
	{
		return new SystemUserIdPerSystemTypeDialog(parent, systemType);
	}
	
	/**
	 * Required by parent. We use it to return the userId. Note the actual update is done!
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		String userId = null;
		SystemUserIdPerSystemTypeDialog uidDlg = (SystemUserIdPerSystemTypeDialog)dlg;
		if (!uidDlg.wasCancelled())
	    {
		   userId = uidDlg.getUserId();
		   SystemPreferencesManager.getPreferencesManager().setDefaultUserId(systemType.getName(), userId);
		   SystemPlugin.getTheSystemRegistry().fireEvent(ISystemResourceChangeEvents.PROPERTYSHEET_UPDATE_EVENT);
		}
		return userId;					
	}

}