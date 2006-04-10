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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.comm.SystemCommunicationsDaemon;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


/**
 * Action to start and stop RSE communications daemon
 */
public class SystemStartCommunicationsDaemonAction extends SystemBaseAction implements IAction 
{

	/**
	 * Constructor
	 * 
	 * @param shell
	 */
	public SystemStartCommunicationsDaemonAction(Shell shell)
	{
		super(SystemResources.ACTION_DAEMON_STOP_LABEL,
			  SystemResources.ACTION_DAEMON_STOP_TOOLTIP,
			  shell);
			  	
		setHelp(SystemPlugin.HELPPREFIX + "dmna0000");
					  	
		if (!SystemCommunicationsDaemon.isAutoStart() || !SystemCommunicationsDaemon.getInstance().isRunning())
		{
			setActionLabelToStart();
		}

		SystemCommunicationsDaemon.setAction(this); 
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		SystemCommunicationsDaemon daemon = SystemCommunicationsDaemon.getInstance();
		if (daemon.isRunning())
		{
			daemon.stopDaemon();
			
			// Change the menu label for the action
			setActionLabelToStart();
		}
		else 
		{
			daemon.startDaemon();
			// Change the menu label for the action
			setActionLabelToStop();
		}
	}

	/**
	 * Helper method for changing the action text and tooltip to start
	 */
	public void setActionLabelToStart()
	{
		setText(SystemResources.ACTION_DAEMON_START_LABEL);
		setToolTipText(SystemResources.ACTION_DAEMON_START_TOOLTIP);
	}

	/**
	 * Helper method for changing the action text and tooltip to stop
	 */
	public void setActionLabelToStop()
	{
		setText(SystemResources.ACTION_DAEMON_STOP_LABEL);
		setToolTipText(SystemResources.ACTION_DAEMON_STOP_TOOLTIP);	
	}

}