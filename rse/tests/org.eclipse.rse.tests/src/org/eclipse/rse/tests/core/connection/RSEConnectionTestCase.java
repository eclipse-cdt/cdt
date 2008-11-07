/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * David McKnight   (IBM)        - [186363] get rid of obsolete calls to SubSystem.connect()
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 ********************************************************************************/
package org.eclipse.rse.tests.core.connection;

import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * Basic connection tests.
 */
public class RSEConnectionTestCase extends RSEBaseConnectionTestCase {

	public RSEConnectionTestCase(String name) {
		super(name);
	}

	/**
	 * Check whether connections are case sensitive
	 */
	public void testConnectionCaseInSensitive() throws Exception {
		// -test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISystemProfile prof = RSECorePlugin.getTheSystemProfileManager().getDefaultPrivateSystemProfile();
		ISystemProfile testprof = RSECorePlugin.getTheSystemProfileManager().cloneSystemProfile(prof, "testConnectionCaseInSensitive");
		IHost h1 = sr.createLocalHost(testprof, "TestConn", "mober");
		assertNotNull(h1);
		assertEquals(h1.getAliasName(), "TestConn");

		// Case variant of connection is found in profile
		IHost h2 = sr.getHost(testprof, "testCONN");
		assertNotNull(h2);
		assertEquals(h1, h2);

		sr.deleteSystemProfile(testprof);
	}

	/**
	 * Creating/disposing elements in the systemView can lead
	 * to "Widget is disposed" exception when Refresh is called
	 * rarely so there is much to refresh. This might be due to
	 * the elementComparer only comparing by absolute name.
	 */
	public void testBug251625() throws Exception {
		// -test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		Job j = new Job("testBug251625") {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
					ISystemProfile prof = sr.createSystemProfile("Foo", true);
					IRSESystemType st = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID);
					IHost h1 = sr.createHost("Foo", st, "vxsim0", "localhost", "vxsim0");
					IHost h2 = sr.createHost("Foo", st, "vxsim1", "localhost", "vxsim1");
					IHost h3 = sr.createHost("Foo", st, "vxsim2", "localhost", "vxsim2");
					sr.fireEvent(new SystemResourceChangeEvent(sr, ISystemResourceChangeEvents.EVENT_REFRESH, null));
					// flushEventQueue();
					Thread.sleep(10000);
					sr.deleteHost(h1);
					sr.deleteHost(h2);
					sr.deleteHost(h3);
					IHost h4 = sr.createHost("Foo", st, "vxsim1", "localhost", "vxsim1");
					sr.fireEvent(new SystemResourceChangeEvent(sr, ISystemResourceChangeEvents.EVENT_REFRESH, null));
					// flushEventQueue(); // will throw exception in main Thread!
					Thread.sleep(10000);
					sr.deleteSystemProfile(prof);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();
		while (j.getState() != Job.NONE) {
			flushEventQueue();
		}
	}

	/**
	 * Test creation of connections.
	 */
	public void testConnectionCreation() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;

		Properties properties = new Properties();
		properties.setProperty(IRSEConnectionProperties.ATTR_PROFILE_NAME, "TestProfile"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_NAME, "TestHost1"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_ADDRESS, "localhost"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID, IRSESystemType.SYSTEMTYPE_UNIX_ID);
		properties.setProperty(IRSEConnectionProperties.ATTR_USERID, "userid"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_PASSWORD, "password"); //$NON-NLS-1$

		IRSEConnectionProperties props = getConnectionManager().loadConnectionProperties(properties, false);
		IHost	connection = getConnectionManager().findOrCreateConnection(props);
		assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), connection); //$NON-NLS-1$

		props.setProperty(IRSEConnectionProperties.ATTR_NAME, "TestHost2"); //$NON-NLS-1$
		connection = getConnectionManager().findOrCreateConnection(props);
		assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), connection); //$NON-NLS-1$

		props.setProperty(IRSEConnectionProperties.ATTR_NAME, "TestHost3"); //$NON-NLS-1$
		connection = getConnectionManager().findOrCreateConnection(props);
		assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), connection); //$NON-NLS-1$

		props.setProperty(IRSEConnectionProperties.ATTR_NAME, "TestHost4"); //$NON-NLS-1$
		connection = getConnectionManager().findOrCreateConnection(props);
		assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), connection); //$NON-NLS-1$

		props.setProperty(IRSEConnectionProperties.ATTR_NAME, "TestHost5"); //$NON-NLS-1$
		connection = getConnectionManager().findOrCreateConnection(props);
		assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), connection); //$NON-NLS-1$

		props.setProperty(IRSEConnectionProperties.ATTR_NAME, "TestHost6"); //$NON-NLS-1$
		connection = getConnectionManager().findOrCreateConnection(props);
		assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), connection); //$NON-NLS-1$

		props.setProperty(IRSEConnectionProperties.ATTR_NAME, "vxsim_128.11.75.12/4_Cores"); //$NON-NLS-1$
		connection = getConnectionManager().findOrCreateConnection(props);
		assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), connection); //$NON-NLS-1$

	}

	/**
	 * Test removal of connections
	 */
	public void testConnectionRemoval() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;

		String profileName = "TestProfile"; //$NON-NLS-1$

		getConnectionManager().removeConnection(profileName, "TestHost1"); //$NON-NLS-1$
		getConnectionManager().removeConnection(profileName, "TestHost2"); //$NON-NLS-1$
		getConnectionManager().removeConnection(profileName, "TestHost3"); //$NON-NLS-1$
		getConnectionManager().removeConnection(profileName, "TestHost4"); //$NON-NLS-1$
		getConnectionManager().removeConnection(profileName, "TestHost5"); //$NON-NLS-1$
		getConnectionManager().removeConnection(profileName, "TestHost6"); //$NON-NLS-1$
		getConnectionManager().removeConnection(profileName, "vxsim_128.11.75.12/4_Cores"); //$NON-NLS-1$
	}

	/**
	 * Test the connect and disconnect methods
	 */
	public void testConnect() {
		//-test-author-:DavidDykstal
		IHost connection = getLocalSystemConnection();
		if (isTestDisabled())
			return;

		Exception exception = null;
		String cause = null;

		ISubSystem subsystem = null;
		try {
			subsystem = getConnectionManager().getFileSubSystem(connection, "local.files"); //$NON-NLS-1$
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to get local.files subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("No local.files subystem", subsystem); //$NON-NLS-1$

		RSEUIPlugin.getDefault().getPreferenceStore().setValue(ISystemPreferencesConstants.ALERT_SSL, false);
		RSEUIPlugin.getDefault().getPreferenceStore().setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);

		exception = null;
		cause = null;

		try {
			subsystem.connect(false, null);
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to connect local.files subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertTrue("local.files subsystem is not connected!", subsystem.isConnected()); //$NON-NLS-1$

		exception = null;
		cause = null;

		try {
			subsystem.disconnect();
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to discconnect local.files subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		// The local.files subsystem should be not disconnectable!
		assertTrue("local.files subsystem is not connected but is expected to!", subsystem.isConnected()); //$NON-NLS-1$
	}

	/**
	 * Test resolving a filter string.
	 */
	public void testResolveFilterString() {
		//-test-author-:DavidDykstal
		IHost connection = getLocalSystemConnection();
		if (isTestDisabled())
			return;

		Exception exception = null;
		String cause = null;

		ISubSystem subsystem = null;
		try {
			subsystem = getConnectionManager().getFileSubSystem(connection, "local.files"); //$NON-NLS-1$
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to get local.files subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("No local.files subystem", subsystem); //$NON-NLS-1$

		exception = null;
		cause = null;

		try {
			subsystem.connect(false, null);
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to connect local.files subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertTrue("local.files subsystem is not connected!", subsystem.isConnected()); //$NON-NLS-1$

		exception = null;
		cause = null;

		Object[] objects = null;
		try {
			objects = subsystem.resolveFilterString("/bin/*", null); //$NON-NLS-1$
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		} finally {
			try { subsystem.disconnect(); } catch (Exception e) { /* ignored */ }
		}
		assertNull("Failed to resolve filter string for local.files subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("Unexpected return value null for resolveFilterString!", objects); //$NON-NLS-1$
	}
}
