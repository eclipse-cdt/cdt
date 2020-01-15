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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.core.options.BaseOption;
import org.eclipse.cdt.core.options.OsgiPreferenceStorage;
import org.junit.Test;
import org.osgi.service.prefs.Preferences;

public class OsgiPreferenceStorageTest {

	private final BaseOption<Object> unknown = new BaseOption<>(Object.class, "unknown", "", "Unknown",
			"An option with unclear semantic, i.e. typical one");
	private final BaseOption<String> string2020 = new BaseOption<>(String.class, "string2020", "2020", "String 2020");
	private final BaseOption<Boolean> negative = new BaseOption<>(Boolean.class, "negative", false, "Negative");
	private final BaseOption<Boolean> positive = new BaseOption<>(Boolean.class, "positive", true, "Positive");
	private final BaseOption<byte[]> bytes2020 = new BaseOption<>(byte[].class, "bytes2020", new byte[] { 20, 20 },
			"Bytes 2020");
	private final BaseOption<Double> double2020 = new BaseOption<>(Double.class, "double2020", 2020d, "Double 2020");
	private final BaseOption<Float> float2020 = new BaseOption<>(Float.class, "float2020", 2020f, "Float 2020");
	private final BaseOption<Integer> int2020 = new BaseOption<>(Integer.class, "int2020", 2020, "Int 2020");
	private final BaseOption<Long> long2020 = new BaseOption<>(Long.class, "long2020", 2020l, "Long 2020");

	@Test(expected = NullPointerException.class)
	public void testNullPreferences() {
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(null);
	}

	@Test
	public void testConsumable() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		assertEquals(false, storage.consumable(Object.class));
		assertEquals(true, storage.consumable(String.class));
		assertEquals(true, storage.consumable(Boolean.class));
		assertEquals(true, storage.consumable(byte[].class));
		assertEquals(false, storage.consumable(Byte[].class));
		assertEquals(true, storage.consumable(Double.class));
		assertEquals(true, storage.consumable(Float.class));
		assertEquals(true, storage.consumable(Integer.class));
		assertEquals(true, storage.consumable(Long.class));
	}

	@Test
	public void testLoad() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		assertEquals("2020", storage.load(string2020));
		assertEquals(Boolean.FALSE, storage.load(negative));
		assertEquals(Boolean.TRUE, storage.load(positive));
		assertArrayEquals(new byte[] { 20, 20 }, storage.load(bytes2020));
		assertEquals(2020d, storage.load(double2020), 0);
		assertEquals(2020f, storage.load(float2020), 0);
		assertEquals(2020, (int) storage.load(int2020));
		assertEquals(2020l, (long) storage.load(long2020));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testLoadUnknown() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		assertEquals(new Object(), storage.load(unknown));
	}

	@Test
	public void testSaveString() {
		Preferences anyPreferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(anyPreferences);
		storage.save("2002", string2020);
		assertEquals("2002", anyPreferences.get(string2020.identifer(), string2020.defaultValue()));
	}

	@Test
	public void testSaveBoolean() {
		Preferences anyPreferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(anyPreferences);
		storage.save(true, negative);
		assertEquals(true, anyPreferences.getBoolean(negative.identifer(), negative.defaultValue()));
		storage.save(false, positive);
		assertEquals(false, anyPreferences.getBoolean(positive.identifer(), positive.defaultValue()));
	}

	@Test
	public void testSaveByteArray() {
		Preferences anyPreferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(anyPreferences);
		storage.save(new byte[] { 20, 02 }, bytes2020);
		assertArrayEquals(new byte[] { 20, 02 },
				anyPreferences.getByteArray(bytes2020.identifer(), bytes2020.defaultValue()));
	}

	@Test
	public void testSaveDouble() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		storage.save(2002d, double2020);
		assertEquals(2002d, preferences.getDouble(double2020.identifer(), double2020.defaultValue()), 0);
	}

	@Test
	public void testSaveFloat() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		storage.save(2002f, float2020);
		assertEquals(2002f, preferences.getDouble(float2020.identifer(), float2020.defaultValue()), 0);
	}

	@Test
	public void testSaveInt() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		storage.save(2002, int2020);
		assertEquals(2002, preferences.getInt(int2020.identifer(), int2020.defaultValue()));
	}

	@Test
	public void testSaveLong() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		storage.save(2002l, long2020);
		assertEquals(2002l, preferences.getLong(long2020.identifer(), long2020.defaultValue()));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSaveUnknown() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		storage.save(new Object(), unknown);
	}

	private Preferences anyPreferences() {
		return new org.eclipse.core.internal.preferences.EclipsePreferences();
	}

}
