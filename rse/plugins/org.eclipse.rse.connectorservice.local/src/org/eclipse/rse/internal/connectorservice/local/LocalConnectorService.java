/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 ********************************************************************************/

package org.eclipse.rse.internal.connectorservice.local;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.BasicConnectorService;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
 
/**
 * System class required by the remote systems framework.
 * This represents the live connection at tool runtime.
 * Since we don't really have such a thing for local files, this
 * is pretty well empty.
 */
public class LocalConnectorService extends BasicConnectorService
{
			
	/**
	 * Constructor when we don't have a subsystem yet.
	 * Call setSubSystem after. 
	 */
	public LocalConnectorService(IHost host)
	{
		super(ConnectorServiceResources.Local_ConnectorService_Label, ConnectorServiceResources.Local_ConnectorService_Description, host, 0);
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SuperAbstractConnectorService#internalDisconnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void internalDisconnect(IProgressMonitor monitor) throws Exception
	{
		fireCommunicationsEvent(CommunicationsEvent.BEFORE_DISCONNECT);

		 // Fire comm event to signal state changed
 		notifyDisconnection();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SuperAbstractConnectorService#internalConnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void internalConnect(IProgressMonitor monitor) throws Exception
	{
	}
	
	/**
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isConnected()
	 */
	public boolean isConnected()
	{
		return true;
	}
	
    /**
     * Return the version, release, modification of the operating system.
     * <p>
     * Returns System.getProperty("os.version")
     */
    public String getVersionReleaseModification()
    {
    	return System.getProperty("os.version"); //$NON-NLS-1$
    }	
    /**
     * Return the home directory of the operating system for the current user, if available.
     * <p>
     * Returns System.getProperty("user.home")
     */
    public String getHomeDirectory()
    {
    	return System.getProperty("user.home"); //$NON-NLS-1$
    }        
    /**
     * Return the temp directory of the operating system for the current user, if available.
     * <p>
     * Returns System.getProperty("java.io.tmpdir")
     */
    public String getTempDirectory()
    {
    	return System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
    }
    
}