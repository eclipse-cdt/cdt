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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


//import com.ibm.etools.systems.*;
/**
 * This is the action for connecting all subsystems for a given connection.
 */
public class SystemConnectAllSubSystemsAction extends SystemBaseAction
								   implements  ISystemMessages
{
	
	private ISystemRegistry sr = null;
	/**
	 * Constructor.
	 * @param shell  Shell of parent window, used as the parent for the dialog.
	 *               Can be null, but be sure to call setParent before the action is used (ie, run).
	 */
	public SystemConnectAllSubSystemsAction(Shell shell)
	{
	    super(SystemResources.ACTION_CONNECT_ALL_LABEL,SystemResources.ACTION_CONNECT_ALL_TOOLTIP, shell);
	    allowOnMultipleSelection(false);
	    setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
	    sr = RSEUIPlugin.getTheSystemRegistry();
  	    //setHelp(RSEUIPlugin.HELPPREFIX+"actn0022");
	}
	/**
	 * Override of parent. Called when testing if action should be enabled base on current
	 *  selection. We check the selected object is one of our subsystems, and if we are
	 *  currently connected.
	 */
	public boolean checkObjectType(Object obj) 
	{
	    if ((obj instanceof IHost) &&
	            !sr.areAllSubSystemsConnected((IHost)obj))
	    {
	        return true;
	    }
		else 
		{
		  return false;
		}
	}
	
	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run()	
	{		  
	    List failedSystems = new ArrayList();
		IHost conn = (IHost)getFirstSelection();
		try 
		{
		    Shell shell = getShell();
		    ISubSystem[] subsystems = conn.getSubSystems();
		    for (int i = 0; i < subsystems.length; i++)
		    {
		        ISubSystem subsystem = subsystems[i];
		        IConnectorService system = subsystem.getConnectorService();
		        if (!subsystem.isConnected() && !failedSystems.contains(system))
		        {
		            try
		            {
		                subsystem.connect(shell, false);
		            }
		            catch (Exception e) 
		            {	
		                failedSystems.add(system);
		                
		                // if the user was prompted for password and cancelled
		                // or if the connect was interrupted for some other reason
		                // we don't attempt to connect the other subsystems
		                if (e instanceof InterruptedException) {
		                	break;
		                }
		            }// msg already shown	
		        }
		    }
		} 
		catch (Exception exc) 
		{
		} 	// msg already shown	
	}
}