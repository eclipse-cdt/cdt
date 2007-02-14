/* ******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation.
 * David McKnight (IBM) - initial API and implementation.
 * Kushal Munir (IBM) - initial API and implementation.
 * ******************************************************************************/

package org.eclipse.rse.tests.preferences;

import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.tests.core.RSECoreTestCase;

/**
 * Tests for {@link SystemPreferencesManager}.
 */
public class PreferencesTest extends RSECoreTestCase {
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testActiveProfiles() {
		SystemPreferencesManager.addActiveProfile("bogus01"); //$NON-NLS-1$
		SystemPreferencesManager.addActiveProfile("bogus02"); //$NON-NLS-1$
		String[] profiles = SystemPreferencesManager.getActiveProfiles();
		assertTrue(profiles.length >= 2);
		assertEquals("bogus02", profiles[SystemPreferencesManager.getActiveProfilePosition("bogus02")]); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("bogus01", profiles[SystemPreferencesManager.getActiveProfilePosition("bogus01")]); //$NON-NLS-1$ //$NON-NLS-2$
		SystemPreferencesManager.renameActiveProfile("bogus02", "bogus99"); //$NON-NLS-1$ //$NON-NLS-2$
		profiles = SystemPreferencesManager.getActiveProfiles();
		assertEquals("bogus99", profiles[SystemPreferencesManager.getActiveProfilePosition("bogus99")]); //$NON-NLS-1$ //$NON-NLS-2$
		SystemPreferencesManager.deleteActiveProfile("bogus01"); //$NON-NLS-1$
		SystemPreferencesManager.deleteActiveProfile("bogus99"); //$NON-NLS-1$
		assertEquals(-1, SystemPreferencesManager.getActiveProfilePosition("bogus02")); //$NON-NLS-1$
		assertEquals(-1, SystemPreferencesManager.getActiveProfilePosition("bogus01")); //$NON-NLS-1$
		assertEquals(-1, SystemPreferencesManager.getActiveProfilePosition("bogus99")); //$NON-NLS-1$
	}
	
	public void testUserIds() {
		SystemPreferencesManager.setUserId("a.b.c", "bogusUser"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("bogusUser", SystemPreferencesManager.getUserId("a.b.c")); //$NON-NLS-1$ //$NON-NLS-2$
		SystemPreferencesManager.clearUserId("a.b.c"); //$NON-NLS-1$
		assertNull(SystemPreferencesManager.getUserId("a.b.c")); //$NON-NLS-1$
	}
	
	public void testDefaultUserIds() {
		IRSECoreRegistry registry = RSECorePlugin.getDefault().getRegistry();
		IRSESystemType systemType = registry.getSystemType("Local"); //$NON-NLS-1$
		String oldValue = SystemPreferencesManager.getDefaultUserId(systemType);
		SystemPreferencesManager.setDefaultUserId(systemType, "bogus1"); //$NON-NLS-1$
		assertEquals("bogus1", SystemPreferencesManager.getDefaultUserId(systemType)); //$NON-NLS-1$
		SystemPreferencesManager.setDefaultUserId("Local", "bogus2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("bogus2", SystemPreferencesManager.getDefaultUserId(systemType)); //$NON-NLS-1$
		SystemPreferencesManager.setDefaultUserId(systemType, oldValue);
		assertEquals(oldValue, SystemPreferencesManager.getDefaultUserId(systemType));
	}
	
	public void testShowLocalConnection() {
		assertTrue(SystemPreferencesManager.getShowLocalConnection());
	}
	
}
