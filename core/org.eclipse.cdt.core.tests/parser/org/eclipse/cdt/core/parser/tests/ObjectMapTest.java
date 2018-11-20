/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jul 19, 2004
 */
package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.parser.util.ObjectMap;

import junit.framework.TestCase;

/**
 * Tests for {@link ObjectMap}.
 */
public class ObjectMapTest extends TestCase {

	private static class HashObject {
		final public int hash;

		HashObject(int h) {
			hash = h;
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}

	public void insertContents(ObjectMap map, Object[][] contents) throws Exception {
		for (int i = 0; i < contents.length; i++) {
			map.put(contents[i][0], contents[i][1]);
		}
	}

	public void assertContents(ObjectMap map, Object[][] contents) throws Exception {
		for (int i = 0; i < contents.length; i++) {
			assertEquals(map.keyAt(i), contents[i][0]);
			assertEquals(map.getAt(i), contents[i][1]);
			assertEquals(map.get(contents[i][0]), contents[i][1]);
		}
		assertEquals(map.size(), contents.length);
	}

	public void testSimpleAdd() throws Exception {
		ObjectMap map = new ObjectMap(2);

		Object[][] contents = new Object[][] { { "1", "ob" } };

		insertContents(map, contents);
		assertContents(map, contents);

		assertEquals(map.size(), 1);
		assertEquals(map.capacity(), 8);
	}

	public void testSimpleCollision() throws Exception {
		ObjectMap map = new ObjectMap(2);

		HashObject key1 = new HashObject(1);
		HashObject key2 = new HashObject(1);

		Object[][] contents = new Object[][] { { key1, "1" }, { key2, "2" } };

		insertContents(map, contents);

		assertEquals(map.size(), 2);
		assertEquals(map.capacity(), 8);

		assertContents(map, contents);
	}

	public void testResize() throws Exception {
		ObjectMap map = new ObjectMap(1);

		assertEquals(map.size(), 0);
		assertEquals(map.capacity(), 8);

		Object[][] res = new Object[][] { { "0", "o0" }, { "1", "o1" }, { "2", "o2" }, { "3", "o3" }, { "4", "o4" } };

		insertContents(map, res);
		assertEquals(map.capacity(), 8);
		assertContents(map, res);
	}

	public void testCollisionResize() throws Exception {
		ObjectMap map = new ObjectMap(1);

		assertEquals(map.size(), 0);
		assertEquals(map.capacity(), 8);

		Object[][] res = new Object[][] { { new HashObject(0), "o0" }, { new HashObject(1), "o1" },
				{ new HashObject(0), "o2" }, { new HashObject(1), "o3" }, { new HashObject(0), "o4" } };

		insertContents(map, res);
		assertEquals(map.capacity(), 8);
		assertContents(map, res);
	}

	public void testReAdd() throws Exception {
		ObjectMap map = new ObjectMap(1);

		assertEquals(map.size(), 0);
		assertEquals(map.capacity(), 8);

		Object[][] res = new Object[][] { { "0", "o0" }, { "1", "o1" } };

		insertContents(map, res);
		assertEquals(map.capacity(), 8);
		assertContents(map, res);

		res = new Object[][] { { "0", "o00" }, { "1", "o01" }, { "10", "o10" }, { "11", "o11" } };

		insertContents(map, res);
		assertContents(map, res);
	}
}
