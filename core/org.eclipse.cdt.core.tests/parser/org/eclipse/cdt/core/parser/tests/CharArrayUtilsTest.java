/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.parser.util.CharArrayUtils;

import junit.framework.TestCase;

/**
 * @author Doug Schaefer
 */
public class CharArrayUtilsTest extends TestCase {

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

		assertEquals(8, CharArrayUtils.lastIndexOf("aaabbbaa".toCharArray(), "aaabbbaaaaabbbaabbbaa".toCharArray()));
		assertEquals(-1, CharArrayUtils.lastIndexOf("aaabbbaa".toCharArray(), "aabbbaabbbaa".toCharArray()));
		assertEquals(6, CharArrayUtils.lastIndexOf("".toCharArray(), "123456".toCharArray()));
		assertEquals(4, CharArrayUtils.lastIndexOf("56".toCharArray(), "123456".toCharArray()));
		assertEquals(-1, CharArrayUtils.lastIndexOf("123".toCharArray(), "".toCharArray()));

		char[] buffer = "A::B::C".toCharArray();

		assertEquals(CharArrayUtils.lastIndexOf("::".toCharArray(), buffer), 4);
		assertTrue(CharArrayUtils.equals(CharArrayUtils.lastSegment(buffer, "::".toCharArray()), "C".toCharArray()));

		buffer = "A::B::C:foo".toCharArray();
		assertEquals(CharArrayUtils.lastIndexOf("::".toCharArray(), buffer), 4);
		assertTrue(
				CharArrayUtils.equals(CharArrayUtils.lastSegment(buffer, "::".toCharArray()), "C:foo".toCharArray()));
	}
}
