/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect() 
 * Martin Oberhuber (Wind River) - [216266] Consider stateless subsystems (supportsSubSystemConnect==false)
 * David McKnight   (IBM)        - [237970]  Subsystem.connect( ) fails for substituting host name when isOffline( ) is true
 *******************************************************************************/

package org.eclipse.rse.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * This is the action for connecting all subsystems for a given connection.
 */
public class SystemConnectAllSubSystemsAction extends SystemBaseAction
{
	public class ConnectAllJob extends Job
	{
		private IHost _connection;
		public ConnectAllJob(IHost connection)
		{
			super(SystemResources.ACTION_CONNECT_ALL_LABEL);
			_connection = connection;
		}
		
		public IStatus run(IProgressMonitor monitor)
		{
		    List failedSystems = new ArrayList();
			try 
			{
			    //forced instantiation of all subsystems
			    ISubSystem[] subsystems = _connection.getSubSystems();
			    for (int i = 0; i < subsystems.length; i++)
			    {
			        ISubSystem subsystem = subsystems[i];
			        IConnectorService system = subsystem.getConnectorService();
			        if (!subsystem.isConnected()
			          && subsystem.getSubSystemConfiguration().supportsSubSystemConnect()
			          && !failedSystems.contains(system))
			        {
			            try
			            {
			                subsystem.connect(monitor, false);
			            }
						catch (SystemMessageException e) {
							//TODO should we collect all messages and just show one dialog with a MultiStatus?
			                failedSystems.add(system);
							SystemMessageDialog.displayMessage(e);
						}
						catch (Exception e) {
			                failedSystems.add(system);
			                if ((e instanceof InterruptedException) || (e instanceof OperationCanceledException)) {
				                // if the user was prompted for password and cancelled
				                // or if the connect was interrupted for some other reason
				                // we don't attempt to connect the other subsystems
								break;
			                }
							SystemBasePlugin.logError(
									e.getLocalizedMessage()!=null ? e.getLocalizedMessage() : e.getClass().getName(),
									e);
						}
			        }
			    }
			} 
			catch (Exception exc) 
			{
			} 	// msg already shown	
			if (failedSystems.size() > 0)
			{
				return Status.CANCEL_STATUS;
			}
			else
			{
				return Status.OK_STATUS;
			}
		}
		
	}
	
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
	    sr = RSECorePlugin.getTheSystemRegistry();
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
	    		!((IHost)obj).isOffline() &&
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

		IHost conn = (IHost)getFirstSelection();
		ConnectAllJob job = new ConnectAllJob(conn);
		job.schedule();
	}
}
