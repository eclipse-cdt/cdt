/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.Random;

import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

import junit.framework.TestCase;

/**
 * Tests for {@link CharArrayObjectMap}.
 */
public class CharArrayObjectMapTest extends TestCase {

	public void testMapAdd() {
		CharArrayObjectMap map = new CharArrayObjectMap(4);
		char[] key1 = "key1".toCharArray();
		Integer value1 = 43;
		map.put(key1, value1);

		char[] key2 = "key1".toCharArray();
		Object value2 = map.get(key2);
		assertEquals(value1, value2);

		for (int i = 0; i < 25; ++i) {
			map.put(("ikey" + i).toCharArray(), Integer.valueOf(i));
		}

		for (int i = 0; i < 25; ++i) {
			Object ivalue1 = map.get(("ikey" + i).toCharArray());
			assertEquals(i, ivalue1);
		}
	}

	public void testDuplicates() {
		CharArrayObjectMap map = new CharArrayObjectMap(4);
		String[] keys = new String[] { "a", "b", "c", "c", "value", "value", "context", "context", "result", "d", "e",
				"f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
				"z" };
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			map.put(key.toCharArray(), key + i);
		}
		assertEquals(29, map.size());
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (i != 2 && i != 4 && i != 6 && i != 31) {
				assertEquals(key + i, map.get(key.toCharArray()));
			}
		}
	}

	public void testCollisionRatio() {
		Random random = new Random(239);
		CharArrayObjectMap map = new CharArrayObjectMap(1);
		for (int i = 0; i < 20000; i++) {
			int r = random.nextInt();
			map.put(("key" + Integer.toUnsignedString(i)).toCharArray(), i);
			double collisionRatio = (double) map.countCollisions() / map.size();
			assertTrue(String.format("Collision ratio %.3f is unexpectedly high for map size of %d.", collisionRatio,
					map.size()), collisionRatio <= 0.4);
		}
	}
}
