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

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


//import com.ibm.etools.systems.*;
/**
 * This is the action forconnecting all subsystems for a given connection.
 */
public class SystemDisconnectAllSubSystemsAction extends SystemBaseAction
								   implements  ISystemMessages
{
	
	private ISystemRegistry sr = null;
	/**
	 * Constructor.
	 * @param shell  Shell of parent window, used as the parent for the dialog.
	 *               Can be null, but be sure to call setParent before the action is used (ie, run).
	 */
	public SystemDisconnectAllSubSystemsAction(Shell shell)
	{
	    super(SystemResources.ACTION_DISCONNECTALLSUBSYSTEMS_LABEL, SystemResources.ACTION_DISCONNECTALLSUBSYSTEMS_TOOLTIP, shell);
	    allowOnMultipleSelection(false);
	    setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
	    sr = SystemPlugin.getTheSystemRegistry();
	    // TODO help for connect all
  	    //setHelp(SystemPlugin.HELPPREFIX+"actn0022");
	}
	/**
	 * Override of parent. Called when testing if action should be enabled base on current
	 *  selection. We check the selected object is one of our subsystems, and if we are
	 *  currently connected.
	 */
	public boolean checkObjectType(Object obj) 
	{
		if ( !(obj instanceof IHost) ||
		     !(sr.isAnySubSystemConnected((IHost)obj) ))
		  return false;
		else 
		  return true;
	}
	
	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run()	
	{		  
		IHost conn = (IHost)getFirstSelection();
		try {
		  sr.disconnectAllSubSystems(conn);
		} catch (Exception exc) {} // msg already shown		
	}
}