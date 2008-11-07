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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Display;
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
			final Display display = Display.getCurrent();
			String jobName =  NLS.bind(CommonMessages.MSG_DISCONNECT_PROGRESS, conn.getName());
			Job disconnectJob = new Job(jobName) 
			{										
				public IStatus run(IProgressMonitor monitor){
					ISubSystem[] subsystems = sr.getSubSystems(conn);
					if (subsystems != null && subsystems.length > 0){
						boolean cancelled = false;
						// disconnect each connector service associated with the host
						for (int i = 0; i < subsystems.length && !cancelled; i++){
																					
							final ISubSystem subSystem = subsystems[i];
							
							if(subSystem.getSubSystemConfiguration().supportsSubSystemConnect()){
								if (subSystem.isConnected()){
									// should always have a connector service
									IConnectorService cs = subSystem.getConnectorService();
									if (cs.isConnected()){
										try {
											cs.disconnect(monitor);
										}
										catch (Exception e){					
											SystemBasePlugin.logError(e.getMessage());
										}
										
										// failed to disconnect?
										if (cs.isConnected()){
											cancelled = true;
										}								
										else {
											cs.reset();
											display.asyncExec(new Runnable(){
												public void run(){
													// this will take care of updating all subsystems
													sr.connectedStatusChange(subSystem, false, true, false);
												}
											});
										}
									}
								}
							}
							if (monitor.isCanceled()){
								cancelled = true;
							}
						}					
						
						if (cancelled){ // either monitor got cancelled or disconnect failed
							setChecked(false);
							sr.setHostOffline(conn, false);
							return Status.CANCEL_STATUS;
						}
					}
					return Status.OK_STATUS;
				}
				
				};
									
			disconnectJob.schedule();
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