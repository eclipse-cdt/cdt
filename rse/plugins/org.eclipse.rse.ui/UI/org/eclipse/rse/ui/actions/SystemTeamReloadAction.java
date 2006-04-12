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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemResourceListener;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to refresh the entire Remote Systems Explorer tree view,
 * by reloading it from disk. This is to be done after the user does a synchronization
 * with the repository.
 */
public class SystemTeamReloadAction extends SystemBaseAction 
                                 
{
	
	//private SystemProfile prevProfile = null;
	
	/**
	 * Constructor 
	 */
	public SystemTeamReloadAction(Shell parent) 
	{
		super(SystemResources.ACTION_TEAM_RELOAD_LABEL,SystemResources.ACTION_TEAM_RELOAD_TOOLTIP,
		      parent);
        allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_BUILD);        
        //setSelectionSensitive(false);        
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0009");
	}

	/**
	 * Selection has been changed. Decide to enable or not.
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = SystemResourceListener.changesPending();
		return enable;
	}

	/**
	 * This is the method called when the user selects this action to run.
	 */
	public void run() 
	{
		//SystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		SystemMessage confirmMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONFIRM_RELOADRSE);
		SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), confirmMsg);
		boolean ok = msgDlg.openQuestionNoException();
		if (ok)
		{
			SystemResourceListener.reloadRSE();
		}
	}		
}