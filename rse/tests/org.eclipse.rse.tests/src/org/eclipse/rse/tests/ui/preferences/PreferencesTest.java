/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation.
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 ********************************************************************************/

package org.eclipse.rse.tests.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPreferencesManager;

/**
 * Tests for {@link SystemPreferencesManager}.
 * Test various aspects of mnemonic generation and assignment.
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

	public void testShowLists() {
		//-test-author-:DavidDykstal
		if (isTestDisabled())
			return;
		Preferences store = RSEUIPlugin.getDefault().getPluginPreferences();
		boolean showLists = store.getBoolean(ISystemPreferencesConstants.SHOW_EMPTY_LISTS);
		assertTrue(showLists);
	}

}
