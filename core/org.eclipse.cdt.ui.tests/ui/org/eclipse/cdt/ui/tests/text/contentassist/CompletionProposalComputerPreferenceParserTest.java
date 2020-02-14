/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.ui.tests.text.contentassist;

import static java.util.Arrays.asList;
import static org.eclipse.cdt.internal.ui.text.contentassist.CompletionProposalComputerPreferenceParser.parseCategoryOrder;
import static org.eclipse.cdt.internal.ui.text.contentassist.CompletionProposalComputerPreferenceParser.parseExcludedCategories;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class CompletionProposalComputerPreferenceParserTest {

	@Test
	public void testParseExcludedCategories() throws ParseException {
		assertEquals(asSet(), parseExcludedCategories("\0"));
		assertEquals(asSet(), parseExcludedCategories("\0\0"));
		assertEquals(asSet("cat1"), parseExcludedCategories("cat1\0"));
		assertEquals(asSet("cat1", "cat2"), parseExcludedCategories("cat1\0cat2"));
		assertEquals(asSet("cat1"), parseExcludedCategories("cat1${0x0}"));
		assertEquals(asSet("cat1", "cat2"), parseExcludedCategories("cat1${0x0}cat2"));
		assertEquals(asSet("cat1"), parseExcludedCategories("cat1$$$${0x0}"));
		assertEquals(asSet("cat1", "cat2"), parseExcludedCategories("cat1$$$${0x0}cat2"));
		assertEquals(asSet("cat1", "cat2", "cat3", "cat4"),
				parseExcludedCategories("cat1$$$${0x0}cat2${0x0}cat3\0cat4\0"));
	}

	@Test
	public void testParseCategoryOrder() throws ParseException {
		assertEquals(asMap("cat1", 1), parseCategoryOrder("cat1:1\0"));
		assertEquals(asMap("cat1", 1000), parseCategoryOrder("cat1:1000\0\0"));
		assertEquals(asMap("cat1", 1000, "cat2", 2000), parseCategoryOrder("cat1:1000\0cat2:2000"));
		assertEquals(asMap("cat1", 1), parseCategoryOrder("cat1:1${0x0}"));
		assertEquals(asMap("cat1", 1000, "cat2", 2000), parseCategoryOrder("cat1:1000${0x0}cat2:2000"));
		assertEquals(asMap("cat1", 1), parseCategoryOrder("cat1:1$$$$${0x0}"));
		assertEquals(asMap("cat1", 1000, "cat2", 2000), parseCategoryOrder("cat1:1000$$$$${0x0}cat2:2000"));
		assertEquals(asMap("cat1", 1000, "cat2", 2000, "cat3", 3000, "cat4", 4000),
				parseCategoryOrder("cat1:1000$$$$${0x0}cat2:2000${0x0}cat3:3000\0cat4:4000\0"));
	}

	@Test(expected = ParseException.class)
	public void testParseIntFailsGracefully1() throws ParseException {
		assertEquals(asMap(), parseCategoryOrder("cat1:this is not a number\0"));
	}

	@Test(expected = ParseException.class)
	public void testParseIntFailsGracefully2() throws ParseException {
		assertEquals(asMap(), parseCategoryOrder("cat1 missing number\0"));
	}

	@Test(expected = ParseException.class)
	public void testParseIntFailsGracefully3() throws ParseException {
		assertEquals(asMap(), parseCategoryOrder("cat1:0:extra field\0"));
	}

	private Set<String> asSet(String... elem) {
		return new HashSet<>(asList(elem));
	}

	private Map<String, Integer> asMap(Object... elem) {
		HashMap<String, Integer> map = new HashMap<>();
		for (int i = 0; i < elem.length; i += 2) {
			map.put((String) elem[i + 0], (Integer) elem[i + 1]);
		}
		return map;
	}
}
