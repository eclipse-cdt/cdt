/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * Uwe Stieber (Wind River) - refactoring and cleanup.
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * Martin Oberhuber (Wind River) - [219086] flush event queue to shield tests from each other
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * Martin Oberhuber (Wind River) - Support REXEC launch type for dstore
 * Tom Hochstein (Freescale)     - [301075] Host copy doesn't copy contained property sets
 *******************************************************************************/
package org.eclipse.rse.tests.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.Assert;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSEUserIdConstants;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.IRemoteServerLauncher;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ServerLaunchType;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.connection.IRSEConnectionManager;
import org.eclipse.rse.tests.core.connection.IRSEConnectionProperties;
import org.eclipse.rse.tests.testsubsystem.interfaces.ITestSubSystem;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

/**
 * RSE connection factory implementation.
 */
public class RSEConnectionManager implements IRSEConnectionManager {
	private IPath connectionDefaultsLocation = null;

	/**
	 * Constructor.
	 */
	public RSEConnectionManager() {
		// locate the connectionDefault.properties file.
		Bundle bundle = RSETestsPlugin.getDefault().getBundle();
		if (bundle != null) {
			IPath relative = new Path ("test.data/connectionDefault.properties"); //$NON-NLS-1$
			URL url = FileLocator.find(bundle, relative, null);
			if (url != null) {
				try {
					// Resolve the URL to an absolute path
					connectionDefaultsLocation = new Path(FileLocator.resolve(url).getFile());
				} catch (IOException e) { /* ignored on purpose */ }
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionManager#loadConnectionProperties(org.eclipse.core.runtime.IPath, boolean)
	 */
	public IRSEConnectionProperties loadConnectionProperties(IPath path, boolean allowDefaults) {
		assert path != null;

		Properties defaults = null;
		if (allowDefaults && connectionDefaultsLocation != null
				&& connectionDefaultsLocation.toFile().isFile()
				&& connectionDefaultsLocation.toFile().canRead()) {
			InputStream stream = null;
			try {
				defaults = new Properties();
				stream = new FileInputStream(connectionDefaultsLocation.toFile());
				defaults.load(stream);
			} catch (IOException e) {
				// There are no defaults if anything goes wrong reading them
				defaults = null;
			} finally {
				try { if (stream != null) stream.close(); } catch (IOException e) { /* ignored */ }
			}
		}


		Properties properties = null;
		if (path.toFile().isFile() && path.toFile().canRead()) {
			InputStream stream = null;
			try {
				stream = new FileInputStream(path.toFile());
				properties = defaults != null ? new Properties(defaults) : new Properties();
				properties.load(stream);
			} catch (IOException e) {
				// if anything goes wrong reading the properties
				// we do not return any.
				properties = null;
			} finally {
				try { if (stream != null) stream.close(); } catch (IOException e) { /* ignored */ }
			}
		}

		return properties != null ? new RSEConnectionProperties(properties) : (IRSEConnectionProperties)null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionManager#loadConnectionProperties(java.util.Properties, boolean)
	 */
	public IRSEConnectionProperties loadConnectionProperties(Properties properties, boolean allowDefaults) {
		assert properties != null;

		Properties defaults = null;
		if (allowDefaults && connectionDefaultsLocation != null
				&& connectionDefaultsLocation.toFile().isFile()
				&& connectionDefaultsLocation.toFile().canRead()) {
			InputStream stream = null;
			try {
				defaults = new Properties();
				stream = new FileInputStream(connectionDefaultsLocation.toFile());
				defaults.load(stream);
			} catch (IOException e) {
				// There are no defaults if anything goes wrong reading them
				defaults = null;
			} finally {
				try { if (stream != null) stream.close(); } catch (IOException e) { /* ignored */ }
			}
		}

		// Unfortunately, we cannot use the given properties directly (as
		// we cannot associate the defaults). We must copy everything from
		// the given properties object.
		Properties resultProperties = null;
		if (defaults != null) {
			resultProperties = new Properties(defaults);
			Enumeration names = properties.propertyNames();
			while (names.hasMoreElements()) {
				String name = (String)names.nextElement();
				if (name != null && properties.getProperty(name) != null) {
					resultProperties.setProperty(name, properties.getProperty(name));
				}
			}
		} else {
			resultProperties = properties;
		}

		return resultProperties != null ? new RSEConnectionProperties(resultProperties) : (IRSEConnectionProperties)null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionManager#copyConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	public IHost findConnection(String profileName, String name) {
		assert profileName != null && name != null;

		ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
		Assert.assertNotNull("FAILED(findConnection): RSE system registry unavailable!", systemRegistry); //$NON-NLS-1$

		ISystemProfile profile = systemRegistry.getSystemProfile(profileName);
		if (profile != null) {
			return systemRegistry.getHost(profile, name);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionManager#copyConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	public IHost copyConnection(IHost connection, String copyName) {
		assert connection != null;

		ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
		Assert.assertNotNull("FAILED(copyConnection): RSE system registry unavailable!", systemRegistry); //$NON-NLS-1$

		try {
			return systemRegistry.copyHost(connection, connection.getSystemProfile(), copyName, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Delete a host given its name and the name of its profile. If the host is not found then
	 * do nothing.
	 * @param profileName the name of the profile containing the host
	 * @param name the name of the host to delete
	 */
	public void removeConnection(String profileName, String name) {
		assert profileName != null && name != null;

		ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
		Assert.assertNotNull("FAILED(findOrCreateConnection): RSE system registry unavailable!", systemRegistry); //$NON-NLS-1$

		ISystemProfile profile = systemRegistry.getSystemProfile(profileName);
		if (profile != null) {
			IHost connection = systemRegistry.getHost(profile, name);
			if (connection != null) {
				systemRegistry.deleteHost(connection);
				systemRegistry.fireEvent(new SystemResourceChangeEvent(connection, ISystemResourceChangeEvents.EVENT_DELETE, systemRegistry));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionManager#findOrCreateConnection(org.eclipse.rse.tests.core.connection.IRSEConnectionProperties)
	 */
	public IHost findOrCreateConnection(IRSEConnectionProperties properties) {
		assert properties != null;

		IHost connection = null;

		ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
		Assert.assertNotNull("FAILED(findOrCreateConnection): RSE system registry unavailable!", systemRegistry); //$NON-NLS-1$

		Exception exception = null;
		String cause = null;

		// First lookup and create the profile
		String profileName = properties.getProperty(IRSEConnectionProperties.ATTR_PROFILE_NAME);
		//Assert.assertNotSame("FAILED(findOrCreateConnection): Invalid system profile name!", "unknown", profileName); //$NON-NLS-1$ //$NON-NLS-2$
		ISystemProfile profile = profileName == null ? systemRegistry.getSystemProfileManager().getDefaultPrivateSystemProfile() : systemRegistry.getSystemProfile(profileName);
		if (profile == null) {
			try {
				profile = systemRegistry.createSystemProfile(profileName, true);
			} catch(Exception e) {
				exception = e;
				cause = e.getLocalizedMessage();
			}
			Assert.assertNull("FAILED(findOrCreateConnection): Failed to create system profile '" + profileName + "'! Possible cause: " + cause, exception); //$NON-NLS-1$ //$NON-NLS-2$
		}
		profileName = profile.getName();
		Assert.assertNotNull("FAILED(findOrCreateConnection): Failed to find and/or create system profile '" + profileName + "'!", profile); //$NON-NLS-1$ //$NON-NLS-2$

		String name = properties.getProperty(IRSEConnectionProperties.ATTR_NAME);
		Assert.assertFalse("FAILED(findOrCreateConnection): Invalid host name!", "unknown".equals(name)); //$NON-NLS-1$ //$NON-NLS-2$
		connection = systemRegistry.getHost(profile, name);
		if (connection == null) {
			String userId = properties.getProperty(IRSEConnectionProperties.ATTR_USERID);
			Assert.assertFalse("FAILED(findOrCreateConnection): Invalid user id name!", "unknown".equals(userId)); //$NON-NLS-1$ //$NON-NLS-2$
			String password = properties.getProperty(IRSEConnectionProperties.ATTR_PASSWORD);
			Assert.assertFalse("FAILED(findOrCreateConnection): Invalid user password name!", "unknown".equals(password)); //$NON-NLS-1$ //$NON-NLS-2$
			String address = properties.getProperty(IRSEConnectionProperties.ATTR_ADDRESS);
			Assert.assertFalse("FAILED(findOrCreateConnection): Invalid remote system ip address or dns name!", "unknown".equals(address)); //$NON-NLS-1$ //$NON-NLS-2$
			String systemTypeId = properties.getProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID);
			Assert.assertFalse("FAILED(findOrCreateConnection): Invalid system type!", "unknown".equals(systemTypeId)); //$NON-NLS-1$ //$NON-NLS-2$
			IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(systemTypeId);
			String daemonPort = properties.getProperty(IRSEConnectionProperties.ATTR_DAEMON_PORT);
			Assert.assertFalse("FAILED(findOrCreateConnection): Invalid port!", "unknown".equals(daemonPort)); //$NON-NLS-1$ //$NON-NLS-2$
			String rexecPort = properties.getProperty(IRSEConnectionProperties.ATTR_REXEC_PORT);
			String serverLaunchType = properties.getProperty(IRSEConnectionProperties.ATTR_SERVER_LAUNCH_TYPE);
			String serverPath = properties.getProperty(IRSEConnectionProperties.ATTR_SERVER_PATH);
			String serverScript = properties.getProperty(IRSEConnectionProperties.ATTR_SERVER_SCRIPT);

			exception = null;
			cause = null;

			try {
				connection = systemRegistry.createHost(profileName, systemType, name, address, null, userId, IRSEUserIdConstants.USERID_LOCATION_HOST, null);
			} catch (Exception e) {
				exception = e;
				cause = e.getLocalizedMessage();
			}
			Assert.assertNull("FAILED(findOrCreateConnection): Failed to create connection IHost object! Possible cause: " + cause, exception); //$NON-NLS-1$

			if (userId != null && password != null) {
				SystemSignonInformation info = new SystemSignonInformation(address, userId, password, systemType);
				PasswordPersistenceManager.getInstance().add(info, true, false);
			}

			IServerLauncherProperties connProperties = connection.getConnectorServices()[0].getRemoteServerLauncherProperties();
			if (connProperties instanceof IRemoteServerLauncher) {
				IRemoteServerLauncher launcher = (IRemoteServerLauncher) connProperties;
				if (daemonPort != null) {
					int daemonPortNum = Integer.parseInt(daemonPort);
					launcher.setDaemonPort(daemonPortNum);
				}
				if (serverLaunchType != null) {
					launcher.setServerLaunchType(ServerLaunchType.get(serverLaunchType));
				}
				if (rexecPort != null) {
					int rexecPortNum = Integer.parseInt(rexecPort);
					launcher.setRexecPort(rexecPortNum);
				}
				if (serverPath != null) {
					launcher.setServerPath(serverPath);
				}
				if (serverScript != null) {
					launcher.setServerScript(serverScript);
				}
			}
		}
		Assert.assertNotNull("FAILED(findOrCreateConnection): Failed to find and/or create connection IHost object!", connection); //$NON-NLS-1$
		final Display display = Display.getCurrent();
		if (display!=null) {
			while(!display.isDisposed() && display.readAndDispatch()) {
				//running on main thread: wait until all async events are fired
			}
		}

		return connection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionManager#getFileSubSystem(org.eclipse.rse.core.model.IHost, java.lang.String)
	 */
	public FileServiceSubSystem getFileSubSystem(IHost connection, String desiredConfigurationId) throws Exception {
		assert connection != null && desiredConfigurationId != null;
		FileServiceSubSystem subsystem = (FileServiceSubSystem)RemoteFileUtility.getFileSubSystem(connection);
		ISubSystemConfiguration config = subsystem.getSubSystemConfiguration();
		String activeId = config.getId();
		if (!activeId.equals(desiredConfigurationId)) {
			if (subsystem.isConnected()) {
				throw new RuntimeException(MessageFormat.format("The subsystem is connected as {0}. Disconnect before changing.", new Object[] { activeId })); //$NON-NLS-1$
			}

			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			ISubSystemConfiguration desiredConfiguration = registry.getSubSystemConfiguration(desiredConfigurationId);
			if (subsystem.canSwitchTo(desiredConfiguration)) {
				subsystem.switchServiceFactory(desiredConfiguration);
			}
		}
		return subsystem;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionManager#getShellSubSystem(org.eclipse.rse.core.model.IHost)
	 */
	public IShellServiceSubSystem getShellSubSystem(IHost connection) throws Exception {
		assert connection != null;
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		ISubSystem[] subSystems = registry.getSubSystems(connection);
		for (int i = 0; i < subSystems.length; i++) {
			ISubSystem subSystem = subSystems[i];
			if (subSystem instanceof IShellServiceSubSystem) {
				return (IShellServiceSubSystem)subSystem;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.connection.IRSEConnectionManager#getTestSubSystem(org.eclipse.rse.core.model.IHost)
	 */
	public ITestSubSystem getTestSubSystem(IHost connection) throws Exception {
		assert connection != null;
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		ISubSystem[] subSystems = registry.getSubSystems(connection);
		for (int i = 0; i < subSystems.length; i++) {
			ISubSystem subSystem = subSystems[i];
			if (subSystem instanceof ITestSubSystem) {
				return (ITestSubSystem)subSystem;
			}
		}
		return null;
	}
}
