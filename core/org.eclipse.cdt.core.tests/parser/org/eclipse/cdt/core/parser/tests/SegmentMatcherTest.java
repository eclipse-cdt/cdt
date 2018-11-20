/*******************************************************************************
 * Copyright (c) 2011 Tomasz Wesolowski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
 *    Jens Elmenthaler - further tweaking
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.parser.util.SegmentMatcher;

import junit.framework.TestCase;

public class SegmentMatcherTest extends TestCase {

	public void testSimple() {

		assertTrue(matchSegments("", "fooBarBaz"));
		assertTrue(matchSegments("fBB", "fooBarBaz"));
		assertTrue(matchSegments("fooBB", "fooBarBaz"));
		assertTrue(matchSegments("foBaBaz", "fooBarBaz"));
		assertTrue(matchSegments("fBBaz", "fooBarBaz"));
		assertTrue(matchSegments("fooBarBaz", "fooBarBaz"));
		assertTrue(matchSegments("foo", "fooBarBaz"));
		assertTrue(matchSegments("fooB", "fooBarBaz"));
		assertTrue(matchSegments("fBBaz", "fooBarBaz"));
		assertTrue(matchSegments("fooB", "fooBarBaz"));
		assertTrue(matchSegments("fBBaz", "fooBarBaz"));
		// Improvement compared to JDT: an all upper case abbreviation should
		// also match as a single segment.
		assertTrue(matchSegments("fBBaz", "fooBARBaz"));
		// Improvement compared to JDT: you don't need to specify all segments
		// in between.
		assertTrue(matchSegments("fBaz", "fooBARBaz"));

		assertFalse(matchSegments("FooBarBaz", "fooBarBaz"));
		assertFalse(matchSegments("fooBARBaz", "fooBarBaz"));
		assertTrue(matchSegments("fooBarbaz", "fooBarBaz"));
		assertFalse(matchSegments("barBaz", "fooBarBaz"));
		assertFalse(matchSegments("BarBaz", "fooBarBaz"));
		assertTrue(matchSegments("fBaz", "fooBarBaz"));
		assertFalse(matchSegments("fBaBar", "fooBarBaz"));
		assertFalse(matchSegments("fBBB", "fooBarBaz"));
		assertFalse(matchSegments("fBBBarBaz", "fooBarBaz"));
		assertFalse(matchSegments("foooBarBaz", "fooBarBaz"));
		assertFalse(matchSegments("foBrBaz", "fooBarBaz"));

	}

	public void testSuffix() {

		assertTrue(matchSegments("fooBar", "fooBar123"));
		assertTrue(matchSegments("fooBar", "fooBarrr"));
		assertTrue(matchSegments("fooBar", "fooBarr__"));

	}

	public void testNumeric() {

		assertTrue(matchSegments("fBBaz", "foo29BarBaz"));
		assertTrue(matchSegments("fBBaz", "fooBar100Baz10"));
		assertTrue(matchSegments("fB100Baz1", "fooBar100Baz10"));
		assertTrue(matchSegments("fB100Baz10", "fooBar100Baz10"));
		assertTrue(matchSegments("fooBar100Baz10", "fooBar100Baz10"));

		assertFalse(matchSegments("fBar100Ba", "fooBarBaz"));
		assertTrue(matchSegments("f100Baz", "fooBar100Baz10"));
		assertFalse(matchSegments("fB1000Baz", "fooBar100Baz"));
		assertFalse(matchSegments("sV", "seed48"));

	}

	public void testSeparator() {

		assertTrue(matchSegments("fBB", "foo_Bar_Baz"));
		assertTrue(matchSegments("fBB", "foo_BarBaz"));
		assertTrue(matchSegments("fBB", "foo_bar_baz"));
		// Improvement compared to JDT:
		assertTrue(matchSegments("FBB", "FOO_BAR_BAZ"));

		assertTrue(matchSegments("fBB", "foo__barBaz"));
		assertTrue(matchSegments("fBB", "foo__bar__baz"));
		assertTrue(matchSegments("fB_B", "foo__bar__Baz"));
		assertTrue(matchSegments("f__b", "foo__bar"));

		assertFalse(matchSegments("fB_B", "foo__bar__baz"));
		assertFalse(matchSegments("f___b", "foo__bar"));
		assertFalse(matchSegments("f__bb", "foo__bar__baz"));

		assertFalse(matchSegments("f_B_B", "fooBarBaz"));
		assertFalse(matchSegments("f_B", "foo_bar"));
		assertFalse(matchSegments("foo_B", "foo_bar"));
		assertFalse(matchSegments("foo_Bar", "foo_bar"));
		assertFalse(matchSegments("fO_bar", "foo_bar"));
		assertFalse(matchSegments("f__b", "foo_bar"));

	}

	public void testPrefixChars() {

		assertFalse(matchSegments("$asd", "_asd"));
		assertFalse(matchSegments("_$$", "__"));
		assertFalse(matchSegments("__$", "__"));

		// require everything to be exactly the same from start up until the first section
		assertTrue(matchSegments("__f", "__fooBar"));
		assertTrue(matchSegments("__fooB", "__fooBar"));
		assertFalse(matchSegments("_fooB", "__fooBar"));
		assertFalse(matchSegments("_FooB", "__fooBar"));
		assertFalse(matchSegments("_$fooB", "__fooBar"));

		assertTrue(matchSegments("___", "___"));
		assertFalse(matchSegments("$__", "___"));
		assertFalse(matchSegments("__$", "___"));
		assertTrue(matchSegments("__", "___"));
		assertFalse(matchSegments("____", "___"));

	}

	public void testAbbreviations() {
		assertTrue(matchSegments("IFB", "IFooBar"));
		assertTrue(matchSegments("IFoB", "IFooBar"));
		assertTrue(matchSegments("XYZ", "XYZFooBar"));
	}

	public void testSingleSegment() {
		assertTrue(matchSegments("foo", "fooBar"));
		assertFalse(matchSegments("bar", "fooBar"));
	}

	public void testGetPrefixForBinarySearch() {
		// Segments can be skipped, because of that the first letter as well
		// as the leading separator must not be added to the binary search prefix.
		assertEquals("foo", getPrefixForBinarySearch("fooBar"));
		assertEquals("foo", getPrefixForBinarySearch("foo6"));
		assertEquals("foo", getPrefixForBinarySearch("foo_"));
		assertEquals("foo", getPrefixForBinarySearch("foo$"));
		assertEquals("___", getPrefixForBinarySearch("___"));
		assertEquals("___foo", getPrefixForBinarySearch("___fooBar"));
		assertEquals("___foo", getPrefixForBinarySearch("___foo3"));
		assertEquals("___foo", getPrefixForBinarySearch("___foo_"));
		assertEquals("___foo", getPrefixForBinarySearch("___foo$"));
		assertEquals("$__", getPrefixForBinarySearch("$__"));
		assertEquals("$__foo", getPrefixForBinarySearch("$__fooBar"));
		assertEquals("$__foo", getPrefixForBinarySearch("$__foo3"));
		assertEquals("$__foo", getPrefixForBinarySearch("$__foo_"));
	}

	/**
	 * Only checks segment matching (i.e. without case-insensitive prefix matching)
	 */
	private boolean matchSegments(String pattern, String name) {
		SegmentMatcher matcher = new SegmentMatcher(pattern.toCharArray());
		return matcher.matchSegments(name.toCharArray());
	}

	private String getPrefixForBinarySearch(String pattern) {
		SegmentMatcher matcher = new SegmentMatcher(pattern.toCharArray());
		return String.valueOf(matcher.getPrefixForBinarySearch());
	}
}
