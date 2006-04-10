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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemResources;


/**
 * A cascading menu action for "Remote Servers->". The actions contributed to the
 *  menu must implement the ISystemRemoteServerAction interface.
 * @see org.eclipse.rse.ui.actions.ISystemRemoteServerAction
 */
public class SystemCascadingRemoteServersAction extends SystemBaseSubMenuAction implements  IMenuListener
{
	
	/**
	 * Constructor 
	 */
	public SystemCascadingRemoteServersAction()
	{
		super(SystemResources.ACTION_CASCADING_REMOTESERVERS_LABEL, SystemResources.ACTION_CASCADING_REMOTESERVERS_TOOLTIP, null);
		setMenuID(ISystemContextMenuConstants.MENU_STARTSERVER);
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(false);
        setHelp(SystemPlugin.HELPPREFIX+"actnsrsv");
	}

	/**
	 * @see SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager menu)
	{
		// we don't populate it. SystemView populates it by calling each adapter and letting them populate it.
		menu.addMenuListener(this);
		//System.out.println("in populateSubMenu");
		return menu;
	}

	/**
	 * Called when submenu is about to show
	 */
	public void menuAboutToShow(IMenuManager subMenu)
	{
		//System.out.println("menuAboutToShow");
		IStructuredSelection selection = getSelection();
		if( selection == null )
		{
			subMenu.add(new SystemBaseAction("Programming error. Selection is null! ", null));
			return;
		} // end if(nothing is selected)
		Object firstSelection = selection.getFirstElement();
		IHost conn = null;
		if (firstSelection instanceof IHost)
			conn = (IHost)firstSelection;
		else if (firstSelection instanceof ISubSystem)
			conn = ((ISubSystem)firstSelection).getHost();
		
		// decide whether or not to enable/disable each entry, by letting it decide...
		IAction[] actions = getActions();
		//System.out.println("...how many actions? "+actions.length);		
		//System.out.println("...connection null ? "+(conn==null));
		for (int idx=0; idx<actions.length; idx++)
		{
			if (actions[idx] instanceof SystemCascadingRemoteServerBaseAction)
			{
				SystemCascadingRemoteServerBaseAction action = (SystemCascadingRemoteServerBaseAction)actions[idx];
				action.setHost(conn);
				if (conn.isOffline())
					action.setEnabled(false);
				else
				 	action.setEnabled(action.shouldEnable(conn));
			}
		}
	}
}