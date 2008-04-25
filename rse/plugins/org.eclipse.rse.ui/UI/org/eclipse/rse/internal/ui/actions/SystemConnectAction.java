/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect()
 * Martin Oberhuber (Wind River) - [149285][ssh] multiple prompts and errors in case of incorrect username
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * This is the action for connecting to the remote subsystem
 */
public class SystemConnectAction extends SystemBaseAction
{
	public class ConnectJob extends Job
	{
		private ISubSystem _subsystem;
		public ConnectJob(ISubSystem subsystem)
		{
			super(SystemResources.ACTION_CONNECT_LABEL);
			_subsystem = subsystem;
		}


		public IStatus run(IProgressMonitor monitor)
		{
			try {
				if (_subsystem.getHost().getSystemType().isWindows())
					_subsystem.connect(monitor, false);
				else
				  	_subsystem.connect(monitor, true);
			}
			catch (OperationCanceledException e) {
				// user cancelled
				return Status.CANCEL_STATUS;
			}
			catch (SystemMessageException e) {
				SystemMessageDialog.displayMessage(e);
			}
			catch (Exception e) {
				SystemBasePlugin.logError(
						e.getLocalizedMessage()!=null ? e.getLocalizedMessage() : e.getClass().getName(),
						e);
			}
			if (monitor.isCanceled())
			{
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	}

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
    	setHelp(RSEUIPlugin.HELPPREFIX+"actn0047"); //$NON-NLS-1$
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
		ConnectJob job = new ConnectJob(ss);
		job.schedule();

	}
}