/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.cdt.core.options.BaseOption;
import org.eclipse.cdt.core.options.OsgiPreferenceStorage;
import org.junit.Test;
import org.osgi.service.prefs.Preferences;

public class OptionsTest {

	private final BaseOption<Boolean> negative = new BaseOption(Boolean.class, "negative", false, "Negative");
	private final BaseOption<Boolean> positive = new BaseOption(Boolean.class, "positive", true, "Positive");
	private final BaseOption<Object> unknown = new BaseOption(Object.class, "unknown", true, "Unknown");

	@Test
	public void testLoadBoolean() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		assertEquals(Boolean.FALSE, storage.load(negative));
		assertEquals(Boolean.TRUE, storage.load(positive));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testLoadUnknown() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		assertEquals(new Object(), storage.load(unknown));
		fail("UnsupportedOperationException expected for unknown value type");
	}

	@Test
	public void testSaveBoolean() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		storage.save(true, negative);
		assertEquals(true, preferences.getBoolean(negative.identifer(), negative.defaultValue()));
		storage.save(false, positive);
		assertEquals(false, preferences.getBoolean(positive.identifer(), positive.defaultValue()));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSaveUnknown() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		storage.save(new Object(), unknown);
		fail("UnsupportedOperationException expected for unknown value type");
	}

	private Preferences anyPreferences() {
		return new org.eclipse.core.internal.preferences.EclipsePreferences();
	}

}
