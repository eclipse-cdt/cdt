/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 ********************************************************************************/

package org.eclipse.rse.tests.core;

import java.util.Properties;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.tests.core.connection.IRSEConnectionProperties;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

/**
 * Tests for host move in the host pool.
 * Each testcase method should leave the host pool as it was prior to running the method.
 */
public class HostMoveTest extends RSEBaseConnectionTestCase {

	static final int NUMBER_OF_HOSTS = 6; // number of hosts
	private IHost hostArray[] = null;
	private ISystemRegistry registry = null;

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		registry = RSECorePlugin.getTheSystemRegistry();
		createHosts();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		deleteHosts();
		super.tearDown();
	}

	public void testMoveOneUp() throws Exception {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		checkPrecondition();
		IHost host = hostArray[NUMBER_OF_HOSTS - 1];
		IHost[] hosts = new IHost[] {host};
		registry.moveHosts("TestProfile", hosts, -1);
		assertEquals(NUMBER_OF_HOSTS - 2, registry.getHostPosition(host));
		flushEventQueue();
		assertEquals(NUMBER_OF_HOSTS - 2, registry.getHostPosition(host));
		registry.moveHosts("TestProfile", hosts, 1);
		assertEquals(NUMBER_OF_HOSTS - 1, registry.getHostPosition(host));
	}

	public void testMoveManyUp() throws Exception {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		checkPrecondition();
		IHost[] hosts = new IHost[] {hostArray[NUMBER_OF_HOSTS - 1], hostArray[NUMBER_OF_HOSTS - 2]};
		registry.moveHosts("TestProfile", hosts, -2);
		assertEquals(NUMBER_OF_HOSTS - 3, registry.getHostPosition(hostArray[NUMBER_OF_HOSTS - 1]));
		assertEquals(NUMBER_OF_HOSTS - 4, registry.getHostPosition(hostArray[NUMBER_OF_HOSTS - 2]));
		flushEventQueue();
		assertEquals(NUMBER_OF_HOSTS - 3, registry.getHostPosition(hostArray[NUMBER_OF_HOSTS - 1]));
		assertEquals(NUMBER_OF_HOSTS - 4, registry.getHostPosition(hostArray[NUMBER_OF_HOSTS - 2]));
		registry.moveHosts("TestProfile", hosts, 2);
		assertEquals(NUMBER_OF_HOSTS - 1, registry.getHostPosition(hostArray[NUMBER_OF_HOSTS - 1]));
		assertEquals(NUMBER_OF_HOSTS - 2, registry.getHostPosition(hostArray[NUMBER_OF_HOSTS - 2]));
	}

	public void testMoveFirstUp() throws Exception {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		checkPrecondition();
		IHost host = hostArray[0];
		assertEquals(0, registry.getHostPosition(host));
		IHost[] hosts = new IHost[] {host};
		registry.moveHosts("TestProfile", hosts, -1); // should not actually move
		assertEquals(0, registry.getHostPosition(host));
	}

	public void testMoveOneDown() throws Exception {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		checkPrecondition();
		IHost host = hostArray[1]; // second in the list
		assertEquals(1, registry.getHostPosition(host));
		IHost[] hosts = new IHost[] {host};
		registry.moveHosts("TestProfile", hosts, 1);
		assertEquals(2, registry.getHostPosition(host));
		registry.moveHosts("TestProfile", hosts, -1);
		assertEquals(1, registry.getHostPosition(host));
	}

	public void testMoveManyDown() throws Exception {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		checkPrecondition();
		IHost[] hosts = new IHost[] {hostArray[0], hostArray[2], hostArray[4]};
		assertEquals(0, registry.getHostPosition(hostArray[0]));
		assertEquals(2, registry.getHostPosition(hostArray[2]));
		assertEquals(4, registry.getHostPosition(hostArray[4]));
		registry.moveHosts("TestProfile", hosts, 1);
		assertEquals(1, registry.getHostPosition(hostArray[0]));
		assertEquals(3, registry.getHostPosition(hostArray[2]));
		assertEquals(5, registry.getHostPosition(hostArray[4]));
		registry.moveHosts("TestProfile", hosts, -1);
		assertEquals(0, registry.getHostPosition(hostArray[0]));
		assertEquals(2, registry.getHostPosition(hostArray[2]));
		assertEquals(4, registry.getHostPosition(hostArray[4]));
	}

	public void testMoveLastDown() throws Exception {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		checkPrecondition();
		IHost host = hostArray[NUMBER_OF_HOSTS - 1];
		assertEquals(NUMBER_OF_HOSTS - 1, registry.getHostPosition(host));
		IHost[] hosts = new IHost[] {host};
		registry.moveHosts("TestProfile", hosts, 1); // should not actually move
		assertEquals(NUMBER_OF_HOSTS - 1, registry.getHostPosition(host));
	}

	public void testNoHost() throws Exception {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		checkPrecondition();
		IHost[] hosts = new IHost[] {};
		registry.moveHosts("TestProfile", hosts, -1); // should not fail
	}

	/**
	 * Create the test hosts.
	 */
	private void createHosts() throws Exception {

		hostArray = new IHost[NUMBER_OF_HOSTS];

		/* Common host properties */
		Properties properties = new Properties();
		properties.setProperty(IRSEConnectionProperties.ATTR_PROFILE_NAME, "TestProfile"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_ADDRESS, "localhost"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID, IRSESystemType.SYSTEMTYPE_UNIX_ID);
		properties.setProperty(IRSEConnectionProperties.ATTR_USERID, "userid"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_PASSWORD, "password"); //$NON-NLS-1$
		IRSEConnectionProperties props = getConnectionManager().loadConnectionProperties(properties, false);

		for (int i = 0; i < hostArray.length; i++) {
			String hostName = getHostName(i);
			properties.setProperty(IRSEConnectionProperties.ATTR_NAME, hostName);
			hostArray[i] = getConnectionManager().findOrCreateConnection(props);
			assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), hostArray[i]); //$NON-NLS-1$
		}

	}

	private void deleteHosts() {
		for (int i = 1; i < hostArray.length; i++) {
			registry.deleteHost(hostArray[i]);
		}
	}

	private void checkPrecondition() {
		for (int i = 0; i < hostArray.length; i++) {
			assertEquals("Precondition check failed", i, registry.getHostPosition(hostArray[i]));
		}
	}

	private String getHostName(int i) {
		String hostName = "TestHost" + Integer.toString(i);
		return hostName;
	}

}
