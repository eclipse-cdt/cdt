/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 ********************************************************************************/

package samples.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.model.IHost;

import samples.RSESamplesPlugin;

/**
 * Our system class that manages connecting to, and disconnecting from,
 * our remote server-side code.
 */
public class DeveloperConnectorService extends AbstractConnectorService {

	private boolean connected = false;

	/**
	 * Constructor for DeveloperConnectorService.
	 * @param host
	 */
	public DeveloperConnectorService(IHost host)
	{
		super(
			RSESamplesPlugin.getResourceString("connectorservice.devr.name"), //$NON-NLS-1$
			RSESamplesPlugin.getResourceString("connectorservice.devr.desc"), //$NON-NLS-1$
			host,
			0
		);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isConnected()
	 */
	public boolean isConnected()
	{
		return connected;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#internalConnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void internalConnect(IProgressMonitor monitor) throws Exception
	{
		super.internalConnect(monitor);
		// pretend. Normally, we'd connect to our remote server-side code here
		connected=true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#internalDisconnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void internalDisconnect(IProgressMonitor monitor) throws Exception
	{
		super.internalDisconnect(monitor);
		// pretend. Normally, we'd disconnect from our remote server-side code here
		connected=false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsRemoteServerLaunching()
	 */
	public boolean supportsRemoteServerLaunching()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#hasRemoteServerLauncherProperties()
	 */
	public boolean hasRemoteServerLauncherProperties()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsServerLaunchProperties()
	 */
	public boolean supportsServerLaunchProperties()
	{
		return false;
	}

	/**
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsPassword()
	 */
	public boolean supportsPassword() {
		return false;
	}

	/**
	 * @return false
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#supportsUserId()
	 */
	public boolean supportsUserId() {
		return false;
	}

}
