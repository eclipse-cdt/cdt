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
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


/**
 * Action for switching RSE Connections offline
 * 
 * @author yantzi
 * @since Artemis 6.0
 */
public class SystemWorkOfflineAction extends SystemBaseAction 
{
	/**
	 * Constructor
	 * 
	 * @param shell
	 */
	public SystemWorkOfflineAction(Shell shell) {
		super(SystemResources.RESID_OFFLINE_WORKOFFLINE_LABEL, SystemResources.RESID_OFFLINE_WORKOFFLINE_TOOLTIP, shell);
		allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
		setHelp(RSEUIPlugin.HELPPREFIX+"wofa0000");
	}

	/**
	 * Override of parent. Called when testing if action should be enabled base on current
	 *  selection. We check the selected object is one of our subsystems, and if we are
	 *  currently connected.
	 */
	public boolean checkObjectType(Object obj) 
	{
		if (obj instanceof IHost)
		  return true;
		else 
		  return false;
	}
	
	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run()	
	{		  
		IHost conn = (IHost)getFirstSelection();
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry(); 
	
		if (conn.isOffline())
		{
			// offline going online
			setChecked(false);
			sr.setHostOffline(conn, false);
		}
		else
		{
			// these need to be set before calling disconnect so the iSeires subsystems know not
			// to collapse 
			sr.setHostOffline(conn, true);
			setChecked(true);
						
			// online going offline, disconnect all subsystems
			ISubSystem[] subsystems = sr.getSubSystems(conn);
			if (subsystems != null)
			{
				boolean cancelled = false;				
				for (int i = 0; i < subsystems.length && !cancelled; i++)
				{
					try 
					{
						subsystems[i].disconnect(getShell(), false);
					} catch (InterruptedException e) {
						// user cancelled disconnect
						cancelled = true;
					} catch (Exception e) {
						SystemBasePlugin.logError("SystemWorkOfflineAction.run", e);
					}
				}
			}
			
			// check that everything was disconnedted okay and this is not the local connection
			if(sr.isAnySubSystemConnected(conn) && !IRSESystemType.SYSTEMTYPE_LOCAL.equals(conn.getSystemType()))
			{
				// backout changes, likely because user cancelled the disconnect
				setChecked(false);
				sr.setHostOffline(conn, false);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.actions.SystemBaseAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection))
		{
			setChecked(((IHost) selection.getFirstElement()).isOffline());
			return true;
		}
		
		return false;
	}


}