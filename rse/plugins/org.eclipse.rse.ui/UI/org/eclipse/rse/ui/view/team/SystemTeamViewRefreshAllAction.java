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
 * Michael Berger (IBM) - 146339 Added refresh action graphic.
 ********************************************************************************/

package org.eclipse.rse.ui.view.team;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to refresh the entire System Team tree view
 */
public class SystemTeamViewRefreshAllAction extends SystemBaseAction 
                                 //
{
	private SystemTeamViewPart teamView;
	
	/**
	 * Constructor for SystemRefreshAllAction
	 */
	public SystemTeamViewRefreshAllAction(Shell parent, SystemTeamViewPart teamView) 
	{
		super(SystemResources.ACTION_REFRESH_ALL_LABEL,SystemResources.ACTION_REFRESH_ALL_TOOLTIP,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_REFRESH_ID),
		      parent);
		this.teamView = teamView;
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_BUILD);        
        setSelectionSensitive(false);
        
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0009");
	}

	/**
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		return enable;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		try {
		SystemResourceManager.getRemoteSystemsProject().refreshLocal(IResource.DEPTH_INFINITE, null);		
		} catch (Exception exc) {}
		
		SystemTeamView teamViewer = (SystemTeamView)teamView.getTreeViewer();
		teamViewer.refresh();
		//System.out.println("Running refresh all");
	}		
}