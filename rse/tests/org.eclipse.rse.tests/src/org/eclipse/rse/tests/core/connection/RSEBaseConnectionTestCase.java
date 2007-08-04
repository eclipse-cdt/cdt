/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * Uwe Stieber (Wind River) - refactoring and cleanup.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Xuan Chen (IBM)               - [198859] Update the API doc for getRemoteSystemConnection.
 ********************************************************************************/
package org.eclipse.rse.tests.core.connection;

import java.util.Properties;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.tests.internal.RSEConnectionManager;

/**
 * Abstract superclass for JUnit PDE test cases that require an IHost.
 * This superclass creates a single RSE IHost that can
 * be reused by multiple testcases run during the same PDE invocation.
 */
public class RSEBaseConnectionTestCase extends RSECoreTestCase {
	private final IRSEConnectionManager connectionManager = new RSEConnectionManager();
	private final IRSEConnectionProperties localSystemConnectionProperties;
	
	/**
	 * Constructor.
	 */
	public RSEBaseConnectionTestCase() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param name The test name.
	 */
	public RSEBaseConnectionTestCase(String name) {
		super(name);

		// Pre-create the local system connection properties
		Properties properties = new Properties();
		properties.setProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID, IRSESystemType.SYSTEMTYPE_LOCAL_ID);
		properties.setProperty(IRSEConnectionProperties.ATTR_ADDRESS, "localhost"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_NAME, "Local"); //$NON-NLS-1$
		localSystemConnectionProperties = getConnectionManager().loadConnectionProperties(properties, false);
	}

	/**
	 * Returns the associated RSE connection manager instance.
	 * 
	 * @return The connection manager instance. Should be never <code>null</code>.
	 */
	protected IRSEConnectionManager getConnectionManager() {
		return connectionManager;
	}
	
	/**
	 * Lookup and return the local system type connection. This connection
	 * should be usually available on all systems.
	 * 
	 * @return The local system type connection or <code>null</code> if the lookup fails.
	 */
	protected IHost getLocalSystemConnection() {
		assertNotNull("Local system connection properties are not available!", localSystemConnectionProperties); //$NON-NLS-1$
		
		Exception exception = null;
		String cause = null;
		
		IHost connection = null;
		try {
			connection = getConnectionManager().findOrCreateConnection(localSystemConnectionProperties);
		} catch (Exception e) {
			exception = e;
			cause = exception.getLocalizedMessage();
		}
		assertNull("Failed to find and create local system connection! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("Failed to find and create local system connection! Cause unknown!", connection); //$NON-NLS-1$
		
		return connection;
	}
	
	/**
	 * Lookup/create and return the remote system connection according to the list of system parameters.
	 * @param systemTypeID The type id string of the remote system.
	 * @param systemAddress The address of the remote system.
	 * @param systemName The connection name.
	 * @param userID The user id used to logon to the remote system.
	 * @param password The password of the user id to logon to the remote system.
	 * 
	 * @return The remote system connection or <code>null</code> if the lookup/creation fails.
	 */
	protected IHost getRemoteSystemConnection(String systemTypeID, String systemAddress, String systemName, String userID, String password) {
		
		Exception exception = null;
		String cause = null;
		// Pre-create the local system connection properties
		Properties properties = new Properties();
		properties.setProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID, systemTypeID);
		properties.setProperty(IRSEConnectionProperties.ATTR_ADDRESS, systemAddress); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_NAME, systemName); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_USERID, userID); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_PASSWORD, password); //$NON-NLS-1$
		
		IRSEConnectionProperties remoteSystemConnectionProperties;
		remoteSystemConnectionProperties = getConnectionManager().loadConnectionProperties(properties, false);
		
		IHost connection = null;
		try {
			connection = getConnectionManager().findOrCreateConnection(remoteSystemConnectionProperties);
		} catch (Exception e) {
			exception = e;
			cause = exception.getLocalizedMessage();
		}
		assertNull("Failed to find and create remote system connection! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("Failed to find and create remote system connection! Cause unknown!", connection); //$NON-NLS-1$
		
		return connection;
	}
}