/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayPool;
import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayUtils;

/**
 * @author Doug Schaefer
 */
public class CharArrayUtilsTest extends TestCase {

	public void testPoolAdd() {
		CharArrayPool dict = new CharArrayPool(1);
		
		char[] str1 = new char[] {'h', 'e', 'l', 'l', 'o'};
		char[] str2 = dict.add(str1);
		assertTrue(CharArrayUtils.equals(str1, str2));
		assertNotSame(str1, str2);
		char[] str3 = dict.add(str1);
		assertSame(str2, str3);
		
		char[] str4 = new char[] {'w', 'o', 'r', 'l', 'd'};
		char[] str5 = dict.add(str4, 0, str4.length);
		assertTrue(CharArrayUtils.equals(str4, str5));
		assertNotSame(str4, str5);
		char[] str6 = dict.add(str4);
		assertSame(str5, str6);
		
		char[] str7 = dict.add(str1, 0, str1.length);
		assertTrue(CharArrayUtils.equals(str1, str7));
		assertNotSame(str1, str7);
		char[] str8 = dict.add(str1);
		assertSame(str7, str8);
	}
	
	public void testPoolConflict() {
		CharArrayPool dict = new CharArrayPool(2);
		
		char[] str1 = new char[] {'h', 'e', 'l', 'l', 'o'};
		char[] str2 = dict.add(str1);
		
		// The hash algorithm should give this the same hash code
		char[] str3 = new char[] {'h', 'o', 'l', 'l', 'e'};
		char[] str4 = dict.add(str3);
		assertNotSame(str2, str4);

		char[] str5 = dict.add(str1);
		assertTrue(CharArrayUtils.equals(str1, str5));
		
		char[] str6 = new char[] {'w', 'o', 'r', 'l', 'd'};
		char[] str7 = dict.add(str6);
		assertTrue(CharArrayUtils.equals(str6, str7));
		
		char[] str8 = dict.add(str3);
		assertSame(str4, str8);
		
		char[] str9 = dict.add(str1);
		assertNotSame(str2, str9);

		// This should be the same since the removals are done by addition time,
		// not access time
		char[] str10 = dict.add(str6);
		assertSame(str7, str10);
	}
	
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
}
