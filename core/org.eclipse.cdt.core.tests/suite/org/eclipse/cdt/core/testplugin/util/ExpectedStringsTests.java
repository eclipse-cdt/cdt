/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
package org.eclipse.cdt.core.testplugin.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author Peter Graves
 *
 *Some simple tests to make sure our ExtraStrings class seems to work.
 */
public class ExpectedStringsTests {
	@Test
	public void testGotAll() {
		ExpectedStrings myExp;
		String[] strings = { "stringOne", "stringTwo", "stringThree" };

		myExp = new ExpectedStrings(strings);
		assertTrue(!myExp.gotAll(), "No found strings");
		myExp.foundString("stringOne");
		assertTrue(!myExp.gotAll(), "1 found strings");
		myExp.foundString("stringTwo");
		assertTrue(!myExp.gotAll(), "2 found strings");
		myExp.foundString("stringThree");
		assertTrue(myExp.gotAll(), "All found strings");

	}

	@Test
	public void testGotExtra() {
		ExpectedStrings myExp;
		String[] strings = { "stringOne", "stringTwo", "stringThree" };

		myExp = new ExpectedStrings(strings);
		assertTrue(!myExp.gotExtra(), "No found strings");
		myExp.foundString("stringOne");
		assertTrue(!myExp.gotExtra(), "1 found strings");
		myExp.foundString("stringTwo");
		assertTrue(!myExp.gotExtra(), "2 found strings");
		myExp.foundString("stringThree");
		assertTrue(!myExp.gotExtra(), "All found strings");
		myExp.foundString("Somerandomestring");
		assertTrue(myExp.gotExtra(), "Extra String");

	}

	@Test
	public void testGetMissingString() {
		ExpectedStrings myExp;
		String[] strings = { "stringOne", "stringTwo", "stringThree" };

		myExp = new ExpectedStrings(strings);
		assertNotNull(myExp.getMissingString());
		myExp.foundString("stringOne");
		assertNotNull(myExp.getMissingString());
		myExp.foundString("stringTwo");
		assertNotNull(myExp.getMissingString());
		myExp.foundString("stringThree");
		assertNotNull(myExp.getMissingString());

	}

	@Test
	public void testGetExtraString() {
		ExpectedStrings myExp;
		String[] strings = { "stringOne", "stringTwo", "stringThree" };

		myExp = new ExpectedStrings(strings);
		assertNotNull(myExp.getExtraString());
		myExp.foundString("stringOnenot");
		assertNotNull(myExp.getMissingString());
		myExp.foundString("stringTwonot");
		assertNotNull(myExp.getMissingString());

	}

}
