/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

import junit.framework.TestCase;

/**
 * @author Doug Schaefer
 */
public class CharArrayUtilsTest extends TestCase {

	public void testMapAdd() {
		CharArrayObjectMap map = new CharArrayObjectMap(4);
		char[] key1 = "key1".toCharArray();
		Object value1 = new Integer(43);
		map.put(key1, value1);
		
		char[] key2 = "key1".toCharArray();
		Object value2 = map.get(key2);
		assertEquals(value1, value2);
		
		for (int i = 0; i < 5; ++i) {
			map.put(("ikey" + i).toCharArray(), new Integer(i));
		}
		
		Object ivalue1 = map.get("ikey1".toCharArray());
		assertEquals(ivalue1, new Integer(1));
		
		Object ivalue4 = map.get("ikey4".toCharArray());
		assertEquals(ivalue4, new Integer(4));
	}
	
	public void testEquals_Bug289852() {
		assertTrue(CharArrayUtils.equals("pre_abc".toCharArray(), 4, 3, "abc".toCharArray(), false));
		assertFalse(CharArrayUtils.equals("pre_abc".toCharArray(), 4, 4, "abcd".toCharArray(), false));
		assertTrue(CharArrayUtils.equals("pre_abc".toCharArray(), 4, 2, "ab".toCharArray(), false));
		
		assertTrue(CharArrayUtils.equals("pre_abc".toCharArray(), 4, 3, "ABC".toCharArray(), true));
		assertFalse(CharArrayUtils.equals("pre_abc".toCharArray(), 4, 4, "abcd".toCharArray(), true));
		assertTrue(CharArrayUtils.equals("pre_abc".toCharArray(), 4, 2, "AB".toCharArray(), true));
	}

	public void testTrim() {
		assertEquals("", new String(CharArrayUtils.trim("".toCharArray())));
		assertEquals("", new String(CharArrayUtils.trim("   ".toCharArray())));
		assertEquals("a", new String(CharArrayUtils.trim("   a".toCharArray())));
		assertEquals("a", new String(CharArrayUtils.trim("   a  ".toCharArray())));
		assertEquals("a  b", new String(CharArrayUtils.trim("   a  b  ".toCharArray())));
		assertEquals("a  b", new String(CharArrayUtils.trim("a  b ".toCharArray())));
	}

	public void testLastIndexOf() {
		assertEquals(-1, CharArrayUtils.lastIndexOf('a', "".toCharArray()));
		assertEquals(3, CharArrayUtils.lastIndexOf('a', "array".toCharArray()));
		assertEquals(-1, CharArrayUtils.lastIndexOf('a', "array".toCharArray(), 4));
		assertEquals(3, CharArrayUtils.lastIndexOf('a', "array".toCharArray(), 3));

		assertEquals(8, CharArrayUtils.lastIndexOf("aaabbbaa".toCharArray(),
				"aaabbbaaaaabbbaabbbaa".toCharArray()));
		assertEquals(-1, CharArrayUtils.lastIndexOf("aaabbbaa".toCharArray(),
				"aabbbaabbbaa".toCharArray()));
		assertEquals(6, CharArrayUtils.lastIndexOf("".toCharArray(), "123456".toCharArray()));
		assertEquals(4, CharArrayUtils.lastIndexOf("56".toCharArray(), "123456".toCharArray()));
		assertEquals(-1, CharArrayUtils.lastIndexOf("123".toCharArray(), "".toCharArray()));
	}
}
