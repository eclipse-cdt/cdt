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
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

/**
 *
 */
abstract public class StringAsserts {
	private StringAsserts() {
		super();
	}

	private static int getDiffPos(String str1, String str2) {
		int len1 = Math.min(str1.length(), str2.length());

		int diffPos = -1;
		for (int i = 0; i < len1; i++) {
			if (str1.charAt(i) != str2.charAt(i)) {
				diffPos = i;
				break;
			}
		}
		if (diffPos == -1 && str1.length() != str2.length()) {
			diffPos = len1;
		}
		return diffPos;
	}

	private static final int printRange = 6;

	public static void assertEqualString(String actual, String expected) {
		if (actual == null || expected == null) {
			if (actual == expected) {
				return;
			}
			if (actual == null) {
				Assert.assertTrue("Content not as expected: is 'null' expected: " + expected, false);
			} else {
				Assert.assertTrue("Content not as expected: expected 'null' is: " + actual, false);
			}
		}

		int diffPos = getDiffPos(actual, expected);
		if (diffPos != -1) {
			int diffAhead = Math.max(0, diffPos - printRange);
			int diffAfter = Math.min(actual.length(), diffPos + printRange);

			String diffStr = actual.substring(diffAhead, diffPos) + '^' + actual.substring(diffPos, diffAfter);

			// use detailed message
			String message = "Content not as expected: is\n" + actual + "\nDiffers at pos " + diffPos + ": " + diffStr //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					+ "\nexpected:\n" + expected; //$NON-NLS-1$

			Assert.assertEquals(message, expected, actual);
		}
	}

	public static void assertEqualStringIgnoreDelim(String actual, String expected) throws IOException {
		if (actual == null || expected == null) {
			if (actual == expected) {
				return;
			}
			if (actual == null) {
				Assert.assertTrue("Content not as expected: is 'null' expected: " + expected, false);
			} else {
				Assert.assertTrue("Content not as expected: expected 'null' is: " + actual, false);
			}
		}

		BufferedReader read1 = new BufferedReader(new StringReader(actual));
		BufferedReader read2 = new BufferedReader(new StringReader(expected));

		int line = 1;
		do {
			String s1 = read1.readLine();
			String s2 = read2.readLine();

			if (s1 == null || !s1.equals(s2)) {
				if (s1 == null && s2 == null) {
					return;
				}
				String diffStr = (s1 == null) ? s2 : s1;

				String message = "Content not as expected: Content is: \n" + actual + "\nDiffers at line " + line + ": "
						+ diffStr + "\nExpected contents: \n" + expected;
				Assert.assertEquals(message, expected, actual);
			}
			line++;
		} while (true);
	}

	public static void assertEqualStringsIgnoreOrder(String[] actuals, String[] expecteds) {
		ArrayList<String> list1 = new ArrayList<>(Arrays.asList(actuals));
		ArrayList<String> list2 = new ArrayList<>(Arrays.asList(expecteds));

		for (int i = list1.size() - 1; i >= 0; i--) {
			if (list2.remove(list1.get(i))) {
				list1.remove(i);
			}
		}

		int n1 = list1.size();
		int n2 = list2.size();

		if (n1 + n2 > 0) {
			if (n1 == 1 && n2 == 1) {
				assertEqualString(list1.get(0), list2.get(0));
			}

			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < n1; i++) {
				String s1 = list1.get(i);
				if (s1 != null) {
					buf.append(s1);
					buf.append("\n");
				}
			}
			String actual = buf.toString();

			buf = new StringBuilder();
			for (int i = 0; i < n2; i++) {
				String s2 = list2.get(i);
				if (s2 != null) {
					buf.append(s2);
					buf.append("\n");
				}
			}
			String expected = buf.toString();

			String message = "Content not as expected: Content is: \n" + actual + "\nExpected contents: \n" + expected;
			Assert.assertEquals(message, expected, actual);
		}
	}

	public static void assertExpectedExistInProposals(String[] actuals, String[] expecteds) {
		ArrayList<String> list1 = new ArrayList<>(Arrays.asList(actuals));
		ArrayList<String> list2 = new ArrayList<>(Arrays.asList(expecteds));

		for (int i = list1.size() - 1; i >= 0; i--) {
			if (list2.remove(list1.get(i))) {
				list1.remove(i);
			}
		}

		int n1 = list1.size();
		int n2 = list2.size();

		if (n2 > 0) {
			if (n1 == 1 && n2 == 1) {
				assertEqualString(list1.get(0), list2.get(0));
			}

			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < n1; i++) {
				String s1 = list1.get(i);
				if (s1 != null) {
					buf.append(s1);
					buf.append("\n");
				}
			}
			String actual = buf.toString();

			buf = new StringBuilder();
			for (int i = 0; i < n2; i++) {
				String s2 = list2.get(i);
				if (s2 != null) {
					buf.append(s2);
					buf.append("\n");
				}
			}
			String expected = buf.toString();

			String message = "Content not as expected: Content is: \n" + actual + "\nExpected contents: \n" + expected;
			Assert.assertEquals(message, expected, actual);
		}
	}

}
