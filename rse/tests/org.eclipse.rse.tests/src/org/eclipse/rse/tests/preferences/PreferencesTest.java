/* ******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation.
 * David McKnight (IBM) - initial API and implementation.
 * Kushal Munir (IBM) - initial API and implementation.
 * David Dykstal (IBM) - moved SystemPreferencesManager to a new package
 *                     - created and used RSEPreferencesManager
 * ******************************************************************************/

package org.eclipse.rse.tests.preferences;

import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.ui.SystemPreferencesManager;

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
		RSEPreferencesManager.addActiveProfile("bogus01"); //$NON-NLS-1$
		RSEPreferencesManager.addActiveProfile("bogus02"); //$NON-NLS-1$
		String[] profiles = RSEPreferencesManager.getActiveProfiles();
		assertTrue(profiles.length >= 2);
		assertEquals("bogus02", profiles[RSEPreferencesManager.getActiveProfilePosition("bogus02")]); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("bogus01", profiles[RSEPreferencesManager.getActiveProfilePosition("bogus01")]); //$NON-NLS-1$ //$NON-NLS-2$
		RSEPreferencesManager.renameActiveProfile("bogus02", "bogus99"); //$NON-NLS-1$ //$NON-NLS-2$
		profiles = RSEPreferencesManager.getActiveProfiles();
		assertEquals("bogus99", profiles[RSEPreferencesManager.getActiveProfilePosition("bogus99")]); //$NON-NLS-1$ //$NON-NLS-2$
		RSEPreferencesManager.deleteActiveProfile("bogus01"); //$NON-NLS-1$
		RSEPreferencesManager.deleteActiveProfile("bogus99"); //$NON-NLS-1$
		assertEquals(-1, RSEPreferencesManager.getActiveProfilePosition("bogus02")); //$NON-NLS-1$
		assertEquals(-1, RSEPreferencesManager.getActiveProfilePosition("bogus01")); //$NON-NLS-1$
		assertEquals(-1, RSEPreferencesManager.getActiveProfilePosition("bogus99")); //$NON-NLS-1$
	}
	
	public void testUserIds() {
		RSEPreferencesManager.setUserId("a.b.c", "bogusUser"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("bogusUser", RSEPreferencesManager.getUserId("a.b.c")); //$NON-NLS-1$ //$NON-NLS-2$
		RSEPreferencesManager.clearUserId("a.b.c"); //$NON-NLS-1$
		assertNull(RSEPreferencesManager.getUserId("a.b.c")); //$NON-NLS-1$
	}
	
	public void testDefaultUserIds() {
		IRSECoreRegistry registry = RSECorePlugin.getDefault().getRegistry();
		IRSESystemType systemType = registry.getSystemType("Local"); //$NON-NLS-1$
		String oldValue = RSEPreferencesManager.getDefaultUserId(systemType);
		RSEPreferencesManager.setDefaultUserId(systemType, "bogus1"); //$NON-NLS-1$
		assertEquals("bogus1", RSEPreferencesManager.getDefaultUserId(systemType)); //$NON-NLS-1$
		RSEPreferencesManager.setDefaultUserId("Local", "bogus2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("bogus2", RSEPreferencesManager.getDefaultUserId(systemType)); //$NON-NLS-1$
		RSEPreferencesManager.setDefaultUserId(systemType, oldValue);
		assertEquals(oldValue, RSEPreferencesManager.getDefaultUserId(systemType));
	}
	
	public void testShowLocalConnection() {
		assertTrue(SystemPreferencesManager.getShowLocalConnection());
	}
	
}
