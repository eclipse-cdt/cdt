/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 *******************************************************************************/
package org.eclipse.rse.tests.core.registries;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.RSECoreTestCase;

/**
 * Tests the subsystem configuration proxy functionality.
 */
public class SubSystemConfigurationProxyTestCase extends RSECoreTestCase {

	private void assertProxyApplicable(ISubSystemConfigurationProxy proxy, IRSESystemType systemType, boolean isApplicable) {
		if (proxy.appliesToSystemType(systemType) != isApplicable) {
			StringBuffer buf = new StringBuffer(120);
			buf.append("Proxy \""); //$NON-NLS-1$
			buf.append(proxy.getId());
			buf.append("\" is expected "); //$NON-NLS-1$
			if (!isApplicable)
				buf.append("not "); //$NON-NLS-1$
			buf.append("to be applicable to systemType \""); //$NON-NLS-1$
			buf.append(systemType.getId());
			buf.append("\", but returned "); //$NON-NLS-1$
			if (isApplicable)
				buf.append("not "); //$NON-NLS-1$
			buf.append("to be!"); //$NON-NLS-1$
			assertTrue(buf.toString(), false);
		}
	}

	public void testSubSystemConfigurationProxy() {
		//-test-author-:UweStieber
		if (isTestDisabled())
			return;
		ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
		assertNotNull("Failed to fetch RSE system registry instance!", systemRegistry); //$NON-NLS-1$

		// get all subsystem configuration proxies and pick out the ones from our
		// tests plugin.
		ISubSystemConfigurationProxy[] proxies = systemRegistry.getSubSystemConfigurationProxies();
		for (int i = 0; i < proxies.length; i++) {
			ISubSystemConfigurationProxy proxy = proxies[i];
			if (proxy.getDeclaringBundle().equals(RSETestsPlugin.getDefault().getBundle())) {
				// Thats one of the subsystem configurations declared in our test subsystem
				assertNotNull("Unexpected retrun value null for proxy.toString()!", proxy.toString()); //$NON-NLS-1$
				assertEquals("Proxy object changed hash code between two calls!", proxy.hashCode(), proxy.hashCode()); //$NON-NLS-1$
				assertFalse("Unexpected return value true for proxy.equals(null)!", proxy.equals(null)); //$NON-NLS-1$
				assertTrue("Unexpected return value false for proxy.equals(proxy)!", proxy.equals(proxy)); //$NON-NLS-1$

				// a few specific value we test only for one well known test subsystem
				if ("org.eclipse.rse.tests.subsystems.TestSubSystem".equals(proxy.getId())) { //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getDescription()!", "Test Subsystem", proxy.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getVendor()!", "Eclipse.org", proxy.getVendor()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getName()!", "Tests", proxy.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getDeclaredSystemTypeIds()!", "org.eclipse.rse.systemtype.local;org.eclipse.rse.systemtype.windows", proxy.getDeclaredSystemTypeIds()); //$NON-NLS-1$ //$NON-NLS-2$
					assertFalse("Unexpected return value true for proxy.supportsAllSystemTypes()!", proxy.supportsAllSystemTypes()); //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getPriority()!", 50000, proxy.getPriority()); //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getCategory()!", "users", proxy.getCategory()); //$NON-NLS-1$ //$NON-NLS-2$
					assertNotNull("Unexpected return value null for proxy.getSubSystemConfiguration()!", proxy.getSubSystemConfiguration()); //$NON-NLS-1$

					// walk through all known system types. Only "Local" and "Windows" should match!
					IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
					assertNotNull("Failed to fetch list of registered system types!", systemTypes); //$NON-NLS-1$
					for (int j = 0; j < systemTypes.length; j++) {
						IRSESystemType systemType = systemTypes[j];
						assertNotNull("Invalid null value in list of registered system types!", systemType); //$NON-NLS-1$
						if (systemType.getId().equals(IRSESystemType.SYSTEMTYPE_LOCAL_ID) || systemType.getId().equals(IRSESystemType.SYSTEMTYPE_WINDOWS_ID)) {
							assertProxyApplicable(proxy, systemType, true);
						} else {
							assertProxyApplicable(proxy, systemType, false);
						}
					}
				}

				if ("org.eclipse.rse.tests.subsystems.TestSubSystem2".equals(proxy.getId())) { //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getDescription()!", "Test Subsystem 2", proxy.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getVendor()!", "Eclipse.org", proxy.getVendor()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getName()!", "Tests2", proxy.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getDeclaredSystemTypeIds()!", "org.eclipse.rse.tests.*", proxy.getDeclaredSystemTypeIds()); //$NON-NLS-1$ //$NON-NLS-2$
					assertFalse("Unexpected return value true for proxy.supportsAllSystemTypes()!", proxy.supportsAllSystemTypes()); //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getPriority()!", 100000, proxy.getPriority()); //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getCategory()!", "users", proxy.getCategory()); //$NON-NLS-1$ //$NON-NLS-2$
					assertNotNull("Unexpected return value null for proxy.getSubSystemConfiguration()!", proxy.getSubSystemConfiguration()); //$NON-NLS-1$

					// walk through all known system types. All system types declared by the tests plugin are expected to match
					IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
					assertNotNull("Failed to fetch list of registered system types!", systemTypes); //$NON-NLS-1$
					for (int j = 0; j < systemTypes.length; j++) {
						IRSESystemType systemType = systemTypes[j];
						assertNotNull("Invalid null value in list of registered system types!", systemType); //$NON-NLS-1$
						if (systemType.getId().startsWith("org.eclipse.rse.tests.")) { //$NON-NLS-1$
							assertProxyApplicable(proxy, systemType, true);
						} else {
							assertProxyApplicable(proxy, systemType, false);
						}
					}
				}

				if ("org.eclipse.rse.tests.subsystems.TestSubSystem3".equals(proxy.getId())) { //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getDescription()!", "Test Subsystem 3", proxy.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getVendor()!", "Eclipse TM Project", proxy.getVendor()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getName()!", "Tests3", proxy.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("Unexpected return value for proxy.getDeclaredSystemTypeIds()!", "org.eclipse.rse.systemtype.*n?x", proxy.getDeclaredSystemTypeIds()); //$NON-NLS-1$ //$NON-NLS-2$
					assertFalse("Unexpected return value true for proxy.supportsAllSystemTypes()!", proxy.supportsAllSystemTypes()); //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getPriority()!", 2000, proxy.getPriority()); //$NON-NLS-1$
					assertEquals("Unexpected return value for proxy.getCategory()!", "users", proxy.getCategory()); //$NON-NLS-1$ //$NON-NLS-2$
					assertNotNull("Unexpected return value null for proxy.getSubSystemConfiguration()!", proxy.getSubSystemConfiguration()); //$NON-NLS-1$

					// walk through all known system types. Only "Unix" and "Linux" should match!
					IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
					assertNotNull("Failed to fetch list of registered system types!", systemTypes); //$NON-NLS-1$
					for (int j = 0; j < systemTypes.length; j++) {
						IRSESystemType systemType = systemTypes[j];
						assertNotNull("Invalid null value in list of registered system types!", systemType); //$NON-NLS-1$
						if ("Unix".equalsIgnoreCase(systemType.getName()) || "Linux".equalsIgnoreCase(systemType.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
							assertProxyApplicable(proxy, systemType, true);
						} else {
							assertProxyApplicable(proxy, systemType, false);
						}
					}
				}
			}
		}
	}
}
