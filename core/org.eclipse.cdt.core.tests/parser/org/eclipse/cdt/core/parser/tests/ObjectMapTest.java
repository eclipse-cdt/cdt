/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jul 19, 2004
 */
package org.eclipse.cdt.core.parser.tests;

import java.util.Random;

import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;

import junit.framework.TestCase;

/**
 * @author aniefer
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

    public void testSimpleAdd() throws Exception{
        ObjectMap map = new ObjectMap(2);

        Object[][] contents = new Object[][] { {"1", "ob" } };

        insertContents(map, contents);
        assertContents(map, contents);

        assertEquals(map.size(), 1);
        assertEquals(map.capacity(), 8);
    }

    public void testSimpleCollision() throws Exception{
        ObjectMap map = new ObjectMap(2);

        HashObject key1 = new HashObject(1);
        HashObject key2 = new HashObject(1);

        Object[][] contents = new Object[][] { {key1, "1" },
                							   {key2, "2" } };

        insertContents(map, contents);

        assertEquals(map.size(), 2);
        assertEquals(map.capacity(), 8);

        assertContents(map, contents);
    }

    public void testResize() throws Exception{
        ObjectMap map = new ObjectMap(1);

        assertEquals(map.size(), 0);
        assertEquals(map.capacity(), 8);

        Object[][] res = new Object[][] { { "0", "o0" },
							              { "1", "o1" },
							              { "2", "o2" },
							              { "3", "o3" },
							              { "4", "o4" } };

        insertContents(map, res);
        assertEquals(map.capacity(), 8);
        assertContents(map, res);
    }

    public void testCollisionResize() throws Exception{
        ObjectMap map = new ObjectMap(1);

        assertEquals(map.size(), 0);
        assertEquals(map.capacity(), 8);

        Object[][] res = new Object[][] { { new HashObject(0), "o0" },
							              { new HashObject(1), "o1" },
							              { new HashObject(0), "o2" },
							              { new HashObject(1), "o3" },
							              { new HashObject(0), "o4" } };

        insertContents(map, res);
        assertEquals(map.capacity(), 8);
        assertContents(map, res);
    }

    public void testReAdd() throws Exception{
        ObjectMap map = new ObjectMap(1);

        assertEquals(map.size(), 0);
        assertEquals(map.capacity(), 8);

        Object[][] res = new Object[][] { { "0", "o0" },
							              { "1", "o1" } };

        insertContents(map, res);
        assertEquals(map.capacity(), 8);
        assertContents(map, res);

        res = new Object[][]{ { "0",  "o00" },
                			  { "1",  "o01" },
                			  { "10", "o10" },
        					  { "11", "o11" } };

        insertContents(map, res);
        assertContents(map, res);
    }

	public void testMapAdd() {
		CharArrayObjectMap map = new CharArrayObjectMap(4);
		char[] key1 = "key1".toCharArray();
		Object value1 = new Integer(43);
		map.put(key1, value1);

		char[] key2 = "key1".toCharArray();
		Object value2 = map.get(key2);
		assertEquals(value1, value2);

		for (int i = 0; i < 25; ++i) {
			map.put(("ikey" + i).toCharArray(), new Integer(i));
		}

		for (int i = 0; i < 25; ++i) {
			Object ivalue1 = map.get(("ikey" + i).toCharArray());
			assertEquals(i, ivalue1);
		}
	}

	public void testCollisionRatio() {
		Random random = new Random(239);
		CharArrayObjectMap map = new CharArrayObjectMap(1);
		for (int i = 0; i < 20000; i++) {
			int r = random.nextInt();
			map.put(("key" + Integer.toUnsignedString(i)).toCharArray(), i);
			double collisionRatio = (double) map.countCollisions() / map.size();
			assertTrue(String.format("Collision ratio %.3f is unexpectedly high for map size of %d.", collisionRatio, map.size()),
					collisionRatio <= 0.4);
		}
	}
}
