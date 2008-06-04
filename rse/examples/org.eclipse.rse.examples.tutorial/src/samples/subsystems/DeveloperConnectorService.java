/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [235626] Convert examples to MessageBundle format
 ********************************************************************************/

package samples.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.BasicConnectorService;

import samples.RSESamplesResources;

/**
 * Our system class that manages connecting to, and disconnecting from,
 * our remote server-side code.
 */
public class DeveloperConnectorService extends BasicConnectorService {

	private boolean connected = false;

	/**
	 * Constructor for DeveloperConnectorService.
	 * @param host
	 */
	public DeveloperConnectorService(IHost host)
	{
		super(
			RSESamplesResources.connectorservice_devr_name, RSESamplesResources.connectorservice_devr_desc,
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
		// pretend. Normally, we'd connect to our remote server-side code here
		connected=true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#internalDisconnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void internalDisconnect(IProgressMonitor monitor) throws Exception
	{
		// pretend. Normally, we'd disconnect from our remote server-side code here
		connected=false;
	}
}
