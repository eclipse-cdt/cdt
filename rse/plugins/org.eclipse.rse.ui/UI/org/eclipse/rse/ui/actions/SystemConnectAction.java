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

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;

//import org.eclipse.rse.core.ui.SystemMessage;


/**
 * This is the action for connecting to the remote subsystem
 */
public class SystemConnectAction extends SystemBaseAction
								implements  ISystemMessages
{
	/**
	 * Constructor.
	 * @param shell  Shell of parent window, used as the parent for the dialog.
	 *               Can be null, but be sure to call setParent before the action is used (ie, run).
 	 */
	public SystemConnectAction(Shell shell) 
	{
	  super(SystemResources.ACTION_CONNECT_LABEL,SystemResources.ACTION_CONNECT_TOOLTIP, shell);
	  allowOnMultipleSelection(false);
	  setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
    	setHelp(RSEUIPlugin.HELPPREFIX+"actn0047");
	}
	/**
	 * Override of parent. Called when testing if action should be enabled base on current
	 *  selection. We check the selected object is one of our subsystems, and we are not
	 *  already connected.
	 */
	public boolean checkObjectType(Object obj) 
	{
		if ( !(obj instanceof ISubSystem) ||
		     ((ISubSystem)obj).getConnectorService().isConnected() )
		  return false;
		else 
		  return true;
	}
	
	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run()
	{
		ISubSystem ss = (ISubSystem)getFirstSelection();
		try {
			if (ss.getHost().getSystemType().equals(IRSESystemType.SYSTEMTYPE_WINDOWS))
				ss.connect(getShell());
			else	
			  	ss.connect(getShell(), true);
		} catch (Exception exc) {} // msg already shown
	}
}