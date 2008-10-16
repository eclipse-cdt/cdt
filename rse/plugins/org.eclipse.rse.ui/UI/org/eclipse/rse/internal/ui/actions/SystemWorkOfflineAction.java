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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * David McKnight (IBM)          - [251026] Work Offline requires being selected twice to turn on Offline Mode
 *******************************************************************************/

package org.eclipse.rse.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * Action for switching RSE Connections offline
 * 
 * @author yantzi
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
		setHelp(RSEUIPlugin.HELPPREFIX+"wofa0000"); //$NON-NLS-1$
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
		final IHost conn = (IHost)getFirstSelection();
		final ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry(); 
	
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
			final ISubSystem[] subsystems = sr.getSubSystems(conn);
			final List subsystemsDisconnected = new ArrayList();		
			
			if (subsystems != null)
			{
				ISystemResourceChangeListener listener = new ISystemResourceChangeListener()
				{			
					public void systemResourceChanged(
							ISystemResourceChangeEvent event) {
						Object src = event.getSource();
						if (src instanceof SubSystem){
							if (!((SubSystem)src).isConnected()){
								if (!subsystemsDisconnected.contains(src))
									subsystemsDisconnected.add(src);
								if (subsystemsDisconnected.size() == subsystems.length){
									sr.removeSystemResourceChangeListener(this);
								}
							}
						}
					}												
				};

				sr.addSystemResourceChangeListener(listener);
				
				boolean cancelled = false;				
				for (int i = 0; i < subsystems.length && !cancelled; i++)
				{
					try 
					{
						// disconnect launches a job but doesn't wait for completion
						subsystems[i].disconnect(false);
						
					} catch (InterruptedException e) {
						// user cancelled disconnect
						cancelled = true;
					} catch (Exception e) {
						SystemBasePlugin.logError("SystemWorkOfflineAction.run", e); //$NON-NLS-1$
					}
				}
			}
			
			Job job = new Job("Ensure Disconnected") //$NON-NLS-1$
			{
				public IStatus run(IProgressMonitor monitor){
						// while 
						while (subsystemsDisconnected.size() < subsystems.length){
							try {
								Thread.sleep(1000);
							}
							catch (InterruptedException e){								
							}
						}
					
						// check that everything was disconnedted okay and this is not the local connection
						if(sr.isAnySubSystemConnected(conn) && !conn.getSystemType().isLocal())
						{
							// backout changes, likely because user cancelled the disconnect
							setChecked(false);
							sr.setHostOffline(conn, false);
						}
						return Status.OK_STATUS;
					}
			};
			job.setSystem(true);
			job.schedule();
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