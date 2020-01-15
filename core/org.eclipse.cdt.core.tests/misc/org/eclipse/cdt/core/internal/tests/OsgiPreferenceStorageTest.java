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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		assertFalse(storage.consumable(Object.class));
		assertTrue(storage.consumable(String.class));
		assertTrue(storage.consumable(Boolean.class));
		assertTrue(storage.consumable(byte[].class));
		assertFalse(storage.consumable(Byte[].class));
		assertTrue(storage.consumable(Double.class));
		assertTrue(storage.consumable(Float.class));
		assertTrue(storage.consumable(Integer.class));
		assertTrue(storage.consumable(Long.class));
	}

	@Test
	public void testLoadString() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		preferences.put(string2020.identifer(), "2002");
		assertEquals("2002", storage.load(string2020));
	}

	@Test
	public void testLoadBoolean() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		preferences.putBoolean(negative.identifer(), true);
		assertEquals(true, storage.load(negative));
		preferences.putBoolean(positive.identifer(), false);
		assertEquals(false, storage.load(positive));
	}

	@Test
	public void testLoadByteArray() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		preferences.putByteArray(bytes2020.identifer(), new byte[] { 20, 02 });
		assertArrayEquals(new byte[] { 20, 02 }, storage.load(bytes2020));
	}

	@Test
	public void testLoadDouble() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		preferences.putDouble(double2020.identifer(), 2002d);
		assertEquals(2002d, storage.load(double2020), 0);
	}

	@Test
	public void testLoadFloat() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		preferences.putFloat(float2020.identifer(), 2002f);
		assertEquals(2002f, storage.load(float2020), 0);
	}

	@Test
	public void testLoadInt() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		preferences.putLong(int2020.identifer(), 2002l);
		assertEquals(2002l, (long) storage.load(int2020));
	}

	@Test
	public void testLoadLong() {
		Preferences preferences = anyPreferences();
		OsgiPreferenceStorage storage = new OsgiPreferenceStorage(preferences);
		preferences.putLong(long2020.identifer(), 2002l);
		assertEquals(2002l, (long) storage.load(long2020));
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
