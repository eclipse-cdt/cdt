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

import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;



/**
 * This is the action for clearing in-memory userId and password cache 
 */
public class SystemClearPasswordAction extends SystemBaseAction
								implements  ISystemMessages
{
	
	/**
	 * Constructor.
	 * @param shell  Shell of parent window, used as the parent for the dialog.
	 *               Can be null, but be sure to call setParent before the action is used (ie, run).
	 */
	public SystemClearPasswordAction(Shell shell) 
	{
		super(SystemResources.ACTION_CLEARPASSWORD_LABEL, SystemResources.ACTION_CLEARPASSWORD_TOOLTIP, shell);
		allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0049");
	}
	
	/**
	 * Override of parent. 
	 * Called when testing if an action should be enabled based on the current selection.
	 * The clear password action can be enabled if the selected object is a subsystem
	 * that is not connected and has a password that is saved.
	 * @return true if the clear password action can be enabled.
	 */
	public boolean checkObjectType(Object obj) {
		boolean result = false;
		if (obj instanceof ISubSystem) {
			ISubSystem subsystem = (ISubSystem) obj;
			IConnectorService cs = subsystem.getConnectorService();
			result = !cs.isConnected() && cs.isPasswordCached(true);
		}
		return result;
	}
	
	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run()
	{
		ISubSystem ss = (ISubSystem)getFirstSelection();
		try {
			IConnectorService system = ss.getConnectorService();
			
			// get the user id
			String userId = system.getUserId();
			
			// clear userid/password from memory and fire event
			//DKM and disk now
			system.clearPasswordCache(true);
			RSEUIPlugin.getTheSystemRegistry().fireEvent(new SystemResourceChangeEvent(ss, 
					ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, 
					ss.getHost()));
			
			/* DKM - this is now done in clearPasswordCache
			 * 
			// now get rid of userid/password from disk
			String systemType = ss.getSystem().getSystemType();
			String hostName = system.getHostName();
			PasswordPersistenceManager.getInstance().remove(systemType, hostName, userId);
			*/
		}
		catch (Exception exc) {
			// msg already shown
		}
	}
}