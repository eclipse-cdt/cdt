/********************************************************************************
 * Copyright (c) 2006 IBM Corporation and Wind River Systems, Inc.
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
 * Martin Oberhuber (Wind River) - adapted template for daytime example.
 ********************************************************************************/

package org.eclipse.rse.examples.daytime.connectorservice;

import java.net.ConnectException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.examples.daytime.DaytimeResources;
import org.eclipse.rse.examples.daytime.service.DaytimeService;
import org.eclipse.rse.examples.daytime.service.IDaytimeService;
import org.eclipse.rse.model.IHost;

/**
 * The DaytimeConnectorService takes care of keeping a "session" for accessing
 * the remote host to retrieve the time of day.
 * 
 * Since the daytime service is really connectionless, there is not much to do
 * here. We basically keep a local "connected" flag only, so to make sure that
 * the remote host is only accessed when the user explicitly requested so. 
 */
public class DaytimeConnectorService extends AbstractConnectorService {
	
	private boolean fIsConnected = false;
	private DaytimeService fDaytimeService;

	public DaytimeConnectorService(IHost host) {
		super(DaytimeResources.Daytime_Connector_Name, DaytimeResources.Daytime_Connector_Description, host, 13);
		fDaytimeService = new DaytimeService();
	}

	protected void internalConnect(IProgressMonitor monitor) throws Exception {
		internalConnect();
	}

	private void internalConnect() throws Exception
	{
		fDaytimeService.setHostName(getHostName());
		try {
			fDaytimeService.getTimeOfDay();
		} catch (ConnectException e) {
			String template = "Daytime service is not available on {0}.";
			String message = MessageFormat.format(template, new Object[] {getHostName()});
			throw new Exception(message);
		}
		//if no exception is thrown, we consider ourselves connected!
		fIsConnected = true;
		//TODO force a refresh of the Viewer in order to show the resource
	}

	public IDaytimeService getDaytimeService() {
		return fDaytimeService;
	}
	
	public boolean isConnected() {
		return fIsConnected;
	}

	public void disconnect() {
		fIsConnected = false;
		//TODO force a refresh of the Viewer in order to hide the resource
	}
	
	public boolean hasRemoteServerLauncherProperties() {
		return false;
	}

	public boolean supportsRemoteServerLaunching() {
		return false;
	}

	public boolean supportsServerLaunchProperties() {
		return false;
	}

}
