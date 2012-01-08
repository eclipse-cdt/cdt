/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 *
 * @author Mike Kucera
 */
public class CharArrayMapTest extends TestCase {

	private static class Slice { // convenience class
		final char[] chars;
		final int start;
		final int length;
		public Slice(char[] chars, int start, int length) {
			this.chars = chars;
			this.length = length;
			this.start = start;
		}
		@Override
		public String toString() {
			return new String(chars, start, length);
		}
	}

	public void disabled_testPerformance() {
		final int iterations = 10000;
		// insert tons of keys
		char[][] keys = new char[iterations][];
		for(int i = 0; i < keys.length; i++) {
			keys[i] = String.valueOf(i).toCharArray();
		}

		System.gc();
		long mapTime = timeMap(keys);

		System.gc();
		long oldMapTime = timeOldMap(keys);

		System.out.println("mapTime: " + mapTime);
		System.out.println("oldMapTime: " + oldMapTime);
		assertTrue(oldMapTime > mapTime);
	}

	private static long timeMap(char[][] keys) {
		long start = System.currentTimeMillis();
		CharArrayMap<Integer> map = new CharArrayMap<Integer>(keys.length);
		for(int i = 0; i < keys.length; i++) {
			map.put(keys[i], i);
		}
		assertEquals(keys.length, map.size());
		for(int i = 0; i < keys.length; i++) {
			assertEquals(new Integer(i), map.get(keys[i]));
		}
		return System.currentTimeMillis() - start;
	}

	private static long timeOldMap(char[][] keys) {
		long start = System.currentTimeMillis();
		CharArrayObjectMap oldMap = new CharArrayObjectMap(keys.length);
		for(int i = 0; i < keys.length; i++) {
			oldMap.put(keys[i], new Integer(i));
		}
		assertEquals(keys.length, oldMap.size());
		for(int i = 0; i < keys.length; i++) {
			assertEquals(new Integer(i), oldMap.get(keys[i]));
		}
		return System.currentTimeMillis() - start;
	}

	public void testBasicUsage1() {
		char[] key1 = "first key".toCharArray();
		char[] key2 = "second key".toCharArray();
		char[] key3 = "third key".toCharArray();
		char[] key4 = "forth key".toCharArray();

		CharArrayMap<Integer> map = new CharArrayMap<Integer>();
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());

		map.put(key1, 1);
		map.put(key2, 2);
		map.put(key3, 3);
		map.put(key4, 4);

		assertFalse(map.isEmpty());
		assertEquals(4, map.size());

		assertEquals(new Integer(1), map.get(key1));
		assertEquals(new Integer(2), map.get(key2));
		assertEquals(new Integer(3), map.get(key3));
		assertEquals(new Integer(4), map.get(key4));

		assertTrue(map.containsKey(key1));
		assertTrue(map.containsKey(key2));
		assertTrue(map.containsKey(key3));
		assertTrue(map.containsKey(key4));

		assertTrue(map.containsValue(1));
		assertTrue(map.containsValue(2));
		assertTrue(map.containsValue(3));
		assertTrue(map.containsValue(4));

		Set<Integer> values = new HashSet<Integer>();
		values.add(1);
		values.add(2);
		values.add(3);
		values.add(4);

		for(int i : map.values()) {
			assertTrue(values.remove(i));
		}

		// remove a mapping
		assertEquals(new Integer(1), map.remove(key1));
		assertEquals(3, map.size());
	    assertNull(map.get(key1));
	    assertFalse(map.containsKey(key1));
	    assertFalse(map.containsValue(1));
	    assertNull(map.remove(key1)); // its already removed

		map.clear();
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());

		// test null values
		map.put(key1, null);
		assertEquals(1, map.size());
		assertNull(map.get(key1));
		assertTrue(map.containsKey(key1));
		assertTrue(map.containsValue(null));

		// overrideing values should
		map.put(key1, 100);
		assertEquals(1, map.size());
		assertEquals(new Integer(100), map.get(key1));
		assertTrue(map.containsValue(100));
		assertFalse(map.containsValue(null));
		// override the value
		map.put(key1, 200);
		assertEquals(1, map.size());
		assertEquals(new Integer(200), map.get(key1));
		assertTrue(map.containsValue(200));
		assertFalse(map.containsValue(100));
	}

	public void testBasicUsage2() {
		char[] chars = "pantera, megadeth, soulfly, metallica, in flames, lamb of god, carcass".toCharArray();

		Slice[] slices = {
			new Slice(chars, 0, 7),
			new Slice(chars, 9, 8),
			new Slice(chars, 19, 7),
			new Slice(chars, 28, 9),
			new Slice(chars, 39, 9),
			new Slice(chars, 50, 11),
			new Slice(chars, 63, 7)
		};

		char[][] keys = {
			"pantera".toCharArray(),
			"megadeth".toCharArray(),
			"soulfly".toCharArray(),
			"metallica".toCharArray(),
			"in flames".toCharArray(),
			"lamb of god".toCharArray(),
			"carcass".toCharArray()
		};

		CharArrayMap<Integer> map = new CharArrayMap<Integer>();
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());

		for(int i = 0; i < slices.length; i++) {
			Slice slice = slices[i];
			map.put(slice.chars, slice.start, slice.length, i);
		}

		assertFalse(map.isEmpty());
		assertEquals(7, map.size());

		// should still work with equivalent keys
		for(int i = 0; i < keys.length; i++) {
			Slice slice = slices[i];
			assertEquals(new Integer(i), map.get(slice.chars, slice.start, slice.length));
			assertEquals(new Integer(i), map.get(keys[i]));
			assertTrue(map.containsKey(slice.chars, slice.start, slice.length));
			assertTrue(map.containsKey(keys[i]));
			assertTrue(map.containsValue(i));
		}

		Set<Integer> values = new HashSet<Integer>();
		for(int i = 0; i < keys.length; i++) {
			values.add(i);
		}

		for(int i : map.values()) {
			assertTrue(values.remove(i));
		}

		// remove the last two keys
		map.remove(keys[5]);
		map.remove(slices[6].chars, slices[6].start, slices[6].length);

		assertEquals(5, map.size());

		// remaining keys should still be there
		for(int i = 0; i < 5; i++) {
			Slice slice = slices[i];
			assertEquals(new Integer(i), map.get(slice.chars, slice.start, slice.length));
			assertEquals(new Integer(i), map.get(keys[i]));
			assertTrue(map.containsKey(slice.chars, slice.start, slice.length));
			assertTrue(map.containsKey(keys[i]));
			assertTrue(map.containsValue(i));
		}

		map.clear();
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());
	}

	
	public void testOrderedMap() {
		char[] chars = "alpha beta aaa cappa almost".toCharArray();
		Slice[] slices = {
			new Slice(chars, 0, 5),
			new Slice(chars, 6, 4),
			new Slice(chars, 11, 3),
			new Slice(chars, 15, 5),
			new Slice(chars, 21, 6)
		};
		int[] order = {3, 4, 1, 5, 2};
		
		CharArrayMap<Integer> map = CharArrayMap.createOrderedMap();
		
		for(int i = 0; i < slices.length; i++) {
			Slice slice = slices[i];
			map.put(slice.chars, slice.start, slice.length, order[i]);
		}
		
		List<String> properOrder = Arrays.asList("aaa", "almost", "alpha", "beta", "cappa");
		
		Collection<char[]> keys = map.keys();
		assertEquals(5, keys.size());
		{
			int i = 0;
			for(char[] key : keys) {
				assertEquals(properOrder.get(i), String.valueOf(key));
				i++;
			}
		}
		
		Collection<Integer> values = map.values();
		assertEquals(5, values.size());
		{
			int i = 1;
			for(int value : values) {
				assertEquals(i++, value);
			}
		}
	}
	
	
	public void testProperFail() {
		char[] hello = "hello".toCharArray();
		CharArrayMap<Integer> map = new CharArrayMap<Integer>();
		Integer value = new Integer(9);

		try {
			map.put(null, value);
			fail();
		} catch(NullPointerException _) {}

		try {
			map.put(hello, -1, 5, value);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.put(hello, 0, -1, value);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.put(hello, 0, 100, value);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.get(null);
			fail();
		} catch(NullPointerException _) {}

		try {
			map.get(hello, -1, 5);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.get(hello, 0, -1);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.get(hello, 0, 100);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.remove(null);
			fail();
		} catch(NullPointerException _) {}

		try {
			map.remove(hello, -1, 5);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.remove(hello, 0, -1);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.remove(hello, 0, 100);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.containsKey(null);
			fail();
		} catch(NullPointerException _) {}

		try {
			map.containsKey(hello, -1, 5);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.containsKey(hello, 0, -1);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			map.containsKey(hello, 0, 100);
			fail();
		} catch(IndexOutOfBoundsException _) {}

		try {
			new CharArrayMap<Integer>(-1);
		} catch(IllegalArgumentException _) {}
	}
}
