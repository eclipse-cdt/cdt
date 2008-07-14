/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 *******************************************************************************/

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
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
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
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		RSEPreferencesManager.setUserId("a.b.c", "bogusUser"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("bogusUser", RSEPreferencesManager.getUserId("a.b.c")); //$NON-NLS-1$ //$NON-NLS-2$
		RSEPreferencesManager.clearUserId("a.b.c"); //$NON-NLS-1$
		assertNull(RSEPreferencesManager.getUserId("a.b.c")); //$NON-NLS-1$
	}

	public void testDefaultUserIds() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		IRSECoreRegistry registry = RSECorePlugin.getTheCoreRegistry();
		//TODO should we test deprecated methods as well? Probably yes...
		IRSESystemType systemTypeDeprecated = registry.getSystemType("Local"); //$NON-NLS-1$
		IRSESystemType systemType = registry.getSystemTypeById("org.eclipse.rse.systemtype.local"); //$NON-NLS-1$
		assertEquals(systemType, systemTypeDeprecated);
		String oldValue = RSEPreferencesManager.getDefaultUserId(systemType);
		RSEPreferencesManager.setDefaultUserId(systemType, "bogus1"); //$NON-NLS-1$
		assertEquals("bogus1", RSEPreferencesManager.getDefaultUserId(systemType)); //$NON-NLS-1$
		IRSESystemType localType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
		RSEPreferencesManager.setDefaultUserId(localType, "bogus2"); //$NON-NLS-1$
		assertEquals("bogus2", RSEPreferencesManager.getDefaultUserId(systemType)); //$NON-NLS-1$
		RSEPreferencesManager.setDefaultUserId(systemType, oldValue);
		assertEquals(oldValue, RSEPreferencesManager.getDefaultUserId(systemType));
	}

	public void testShowLocalConnection() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		assertTrue(SystemPreferencesManager.getShowLocalConnection());
	}

}
