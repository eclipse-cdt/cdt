/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import org.eclipse.cdt.internal.ui.text.CBreakIterator;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CBreakIteratorTest extends BreakIteratorTest {

	public static Test suite() {
		return new TestSuite(CBreakIteratorTest.class);
	}

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		fBreakIterator = new CBreakIterator();
	}

	public void testNext1() {
		assertNextPositions("word word", new int[] { 4, 5, 9 });
	}

	public void testNext2() {
		assertNextPositions("wordWord word", new int[] { 4, 8, 9, 13 });
	}

	public void testNextSpace() {
		assertNextPositions(" word ", new int[] { 1, 5, 6 });
	}

	public void testNextParen() {
		assertNextPositions("word(params)", new int[] { 4, 5, 11, 12 });
	}

	public void testNextLn() {
		String s = "word \n" + "  word2";
		assertNextPositions(s, new int[] { 4, 5, 6, 8, 13 });
	}

	public void testMultiNextLn() {
		String s = "word \n" + "\n" + "\n" + "  word2";
		assertNextPositions(s, new int[] { 4, 5, 6, 7, 8, 10, 15 });
	}

	public void testMultiNextLn2() {
		String s = "word \r\n" + "\r\n" + "\r\n" + "  word2";
		assertNextPositions(s, new int[] { 4, 5, 7, 9, 11, 13, 18 });
	}

	public void testNextCamelCaseWord() {
		String s = "   _isURLConnection_pool   ";
		assertNextPositions(s, new int[] { 3, 4, 6, 9, 20, 24, 27 });
	}

	public void testPrevious1() {
		String s = "word word";
		assertPreviousPositions(s, new int[] { 0, 4, 5 });
	}

	public void testPrevious2() {
		String s = "wordWord word";
		assertPreviousPositions(s, new int[] { 0, 4, 8, 9 });
	}

	public void testPreviousSpace() {
		String s = " word ";
		assertPreviousPositions(s, new int[] { 1, 5 });
	}

	public void testPreviousParen() {
		String s = "word(params)";
		assertPreviousPositions(s, new int[] { 0, 4, 5, 11 });
	}

	public void testPreviousLn() {
		String s = "word \n" + "  word2";
		assertPreviousPositions(s, new int[] { 0, 4, 5, 6, 8 });
	}

	public void testMultiPreviousLn() {
		String s = "word \n" + "\n" + "\n" + "  word2";
		assertPreviousPositions(s, new int[] { 0, 4, 5, 6, 7, 8, 10 });
	}

	public void testMultiPreviousLn2() {
		String s = "word \r\n" + "\r\n" + "\r\n" + "  word2";
		assertPreviousPositions(s, new int[] { 0, 4, 5, 7, 9, 11, 13 });
	}

	public void testPreviousCamelCaseWord() {
		String s = "   _isURLConnection_pool   ";
		assertPreviousPositions(s, new int[] { 0, 3, 4, 6, 9, 20, 24 });
	}
}
