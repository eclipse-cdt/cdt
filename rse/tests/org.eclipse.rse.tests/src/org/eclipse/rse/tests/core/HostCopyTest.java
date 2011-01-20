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
 * Tom Hochstein (Freescale)     - [301075] Host copy doesn't copy contained property sets
 ********************************************************************************/

package org.eclipse.rse.tests.core;

import java.util.Properties;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.tests.core.connection.IRSEConnectionProperties;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

/**
 * Tests host copy.
 */
public class HostCopyTest extends RSEBaseConnectionTestCase {

	private IHost host = null;

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		createHosts();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		deleteHosts();
		super.tearDown();
	}

	/**
	 * Test copy of connections
	 */
	public void testCopy() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;

		String setName = "Test Property Set Level 1"; //$NON-NLS-1$
		String propertyName = "Test Property Level 1"; //$NON-NLS-1$
		String propertyValue = "Level 1"; //$NON-NLS-1$
		IPropertySet ps = host.createPropertySet(setName);
		assertNotNull("Failed to create property set " + setName, ps); //$NON-NLS-1$
		IProperty p = ps.addProperty(propertyName, propertyValue);
		assertNotNull("Failed to create property " + propertyName, p); //$NON-NLS-1$
		assertEquals("Failed to set value for property " + propertyName, propertyValue, p.getValue()); //$NON-NLS-1$
		
		String setName2 = "Test Property Set Level 2"; //$NON-NLS-1$
		String propertyName2 = "Test Property Level 2"; //$NON-NLS-1$
		String propertyValue2 = "Level 2"; //$NON-NLS-1$
		ps = ps.createPropertySet(setName2);
		assertNotNull("Failed to create property set " + setName2, ps); //$NON-NLS-1$
		p = ps.addProperty(propertyName2, propertyValue2);
		assertNotNull("Failed to create property " + propertyName2, p); //$NON-NLS-1$
		assertEquals("Failed to set value for property " + propertyName2, propertyValue2, p.getValue()); //$NON-NLS-1$

		String name = host.getAliasName();
		String copyName = name + "Copy"; //$NON-NLS-1$
		IHost copy = getConnectionManager().copyConnection(host, copyName);
		assertNotNull("Failed to copy connection " + name, copy); //$NON-NLS-1$
		
		ps = copy.getPropertySet(setName);
		assertNotNull("Failed to copy property set " + setName, ps); //$NON-NLS-1$
		p = ps.getProperty(propertyName);
		assertNotNull("Failed to copy property " + propertyName, p); //$NON-NLS-1$
		assertEquals("Failed to copy value for property " + propertyName, propertyValue, p.getValue()); //$NON-NLS-1$

		ps = ps.getPropertySet(setName2);
		assertNotNull("Failed to copy property set " + setName2, ps); //$NON-NLS-1$
		p = ps.getProperty(propertyName2);
		assertNotNull("Failed to copy property " + propertyName2, p); //$NON-NLS-1$
		assertEquals("Failed to copy value for property " + propertyName2, propertyValue2, p.getValue()); //$NON-NLS-1$

		getConnectionManager().removeConnection("TestProfile", copyName);
		host.removePropertySet(setName);
	}

	/**
	 * Create the test hosts.
	 */
	private void createHosts() throws Exception {

		/* Common host properties */
		Properties properties = new Properties();
		properties.setProperty(IRSEConnectionProperties.ATTR_PROFILE_NAME, "TestProfile"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_ADDRESS, "localhost"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID, IRSESystemType.SYSTEMTYPE_UNIX_ID);
		properties.setProperty(IRSEConnectionProperties.ATTR_USERID, "userid"); //$NON-NLS-1$
		properties.setProperty(IRSEConnectionProperties.ATTR_PASSWORD, "password"); //$NON-NLS-1$
		IRSEConnectionProperties props = getConnectionManager().loadConnectionProperties(properties, false);

		String hostName = "TestHost";
		properties.setProperty(IRSEConnectionProperties.ATTR_NAME, hostName);
		host = getConnectionManager().findOrCreateConnection(props);
		assertNotNull("Failed to create connection " + props.getProperty(IRSEConnectionProperties.ATTR_NAME), host); //$NON-NLS-1$
	}

	private void deleteHosts() {
		RSECorePlugin.getTheSystemRegistry().deleteHost(host);
	}

}
