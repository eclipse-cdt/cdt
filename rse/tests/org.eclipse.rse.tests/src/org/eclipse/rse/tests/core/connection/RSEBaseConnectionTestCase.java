/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * Uwe Stieber (Wind River) - refactoring and cleanup.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Xuan Chen (IBM)               - [198859] Update the API doc for getRemoteSystemConnection.
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 *******************************************************************************/
package org.eclipse.rse.tests.core.connection;

import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.tests.internal.RSEConnectionManager;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

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
		setTargetName("local");
		if (isTestDisabled())
			return null;
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

	protected IHost getSSHHost()
	{
		setTargetName("ssh");
		if (isTestDisabled())
			return null;
		return getHost("sshConnection.properties");
	}

	protected IHost getFTPHost()
	{
		setTargetName("ftp");
		if (isTestDisabled())
			return null;
		return getHost("ftpConnection.properties");
	}

	protected IHost getLinuxHost()
	{
		setTargetName("linux");
		if (isTestDisabled())
			return null;
		return getHost("linuxConnection.properties");
	}

	protected IHost getWindowsHost()
	{
		setTargetName("windows");
		if (isTestDisabled())
			return null;
		return getHost("windowsConnection.properties");
	}

	protected IHost getHost(String propertiesFileName) {
		IHost host;

		// Calculate the location of the test connection properties
		IPath location = getTestDataLocation("", false); //$NON-NLS-1$
		assertNotNull("Cannot locate test data! Missing test data location?", location); //$NON-NLS-1$
		location = location.append(propertiesFileName);
		assertNotNull("Failed to construct location to 'connection.properties' test data file!", location); //$NON-NLS-1$
		assertTrue("Required test data file seems to be not a file!", location.toFile().isFile()); //$NON-NLS-1$
		assertTrue("Required test data file is not readable!", location.toFile().canRead()); //$NON-NLS-1$

		// Load the properties from the calculated location without backing up defaults
		IRSEConnectionProperties properties = getConnectionManager().loadConnectionProperties(location, false);
		assertNotNull("Failed to load test connection properties from location " + location.toOSString(), properties); //$NON-NLS-1$

		// Lookup and create the connection now if necessary
		host = getConnectionManager().findOrCreateConnection(properties);
		assertNotNull("Failed to create connection " + properties.getProperty(IRSEConnectionProperties.ATTR_NAME), host); //$NON-NLS-1$

		// For connections with dstore, need to disable SSL alerts
		// since the UI messagebox on connect would make the test hang
		ISubSystem[] ss = host.getSubSystems();
		for (int i = 0; i < ss.length; i++) {
			String id = ss[i].getSubSystemConfiguration().getId();
			if (id.indexOf("dstore.") >= 0) {
				IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
				////Ensure that the SSL acknowledge dialog does not show up.
				////We no longer need to setDefault first in order to set the value of a preference.
				////Since this is now in connectorservice.dstore/Activator, and it's sure to be active here
				// store.setDefault(ISystemPreferencesConstants.ALERT_SSL,ISystemPreferencesConstants.DEFAULT_ALERT_SSL);
				// store.setDefault(ISystemPreferencesConstants.ALERT_NONSSL, ISystemPreferencesConstants.DEFAULT_ALERT_NON_SSL);
				store.setValue(ISystemPreferencesConstants.ALERT_SSL, false);
				store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);
				break;
			}
		}

		return host;
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
		properties.setProperty(IRSEConnectionProperties.ATTR_ADDRESS, systemAddress);
		properties.setProperty(IRSEConnectionProperties.ATTR_NAME, systemName);
		properties.setProperty(IRSEConnectionProperties.ATTR_USERID, userID);
		if (password != null)
			properties.setProperty(IRSEConnectionProperties.ATTR_PASSWORD, password);

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

	protected IProgressMonitor getDefaultProgressMonitor() {
		return new NullProgressMonitor();
	}
}
