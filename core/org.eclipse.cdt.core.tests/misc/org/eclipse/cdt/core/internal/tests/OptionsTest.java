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

import org.eclipse.cdt.internal.core.options.BaseOption;
import org.eclipse.cdt.internal.core.options.BooleanOption;
import org.eclipse.cdt.internal.core.options.PreferenceStorage;
import org.osgi.service.prefs.Preferences;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OptionsTest extends TestCase {

	private final BooleanOption negative = new BooleanOption("negative", false, "Negative");
	private final BooleanOption positive = new BooleanOption("positive", true, "Positive");
	private final BaseOption<Object> unknown = new BaseOption(Object.class, "unknown", true, "Unknown") {
	};

	public static Test suite() {
		return new TestSuite(OptionsTest.class);
	}

	public void testLoadBoolean() {
		Preferences preferences = anyPreferences();
		PreferenceStorage storage = new PreferenceStorage(preferences);
		assertEquals(Boolean.FALSE, storage.load(negative));
		assertEquals(Boolean.TRUE, storage.load(positive));
	}

	public void testLoadUnknown() {
		Preferences preferences = anyPreferences();
		PreferenceStorage storage = new PreferenceStorage(preferences);
		try {
			assertEquals(new Object(), storage.load(unknown));
			fail("UnsupportedOperationException expected for unknown value class");
		} catch (UnsupportedOperationException e) {
			//expected
		}
	}

	public void testSaveBoolean() {
		Preferences preferences = anyPreferences();
		PreferenceStorage storage = new PreferenceStorage(preferences);
		storage.save(true, negative);
		assertEquals(true, preferences.getBoolean(negative.identifer(), negative.defaultValue()));
		storage.save(false, positive);
		assertEquals(false, preferences.getBoolean(positive.identifer(), positive.defaultValue()));
	}

	public void testSaveUnknown() {
		Preferences preferences = anyPreferences();
		PreferenceStorage storage = new PreferenceStorage(preferences);
		try {
			storage.save(new Object(), unknown);
			fail("UnsupportedOperationException expected for unknown value class");
		} catch (UnsupportedOperationException e) {
			//expected
		}
	}

	private Preferences anyPreferences() {
		return new org.eclipse.core.internal.preferences.EclipsePreferences();
	}

}
