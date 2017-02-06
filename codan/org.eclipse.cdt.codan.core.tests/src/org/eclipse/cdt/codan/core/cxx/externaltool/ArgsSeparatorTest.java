/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.externaltool;

import static org.junit.Assert.assertArrayEquals;
import junit.framework.TestCase;

/**
 * Tests for <code>{@link ArgsSeparator}</code>.
 */
@SuppressWarnings("nls")
public class ArgsSeparatorTest extends TestCase {
	private ArgsSeparator separator;

	@Override
	protected void setUp() {
		separator = new ArgsSeparator();
	}

	public void testWithSpaceAsDelimiter() {
		String[] args = separator.splitArguments("abc def ghi");
		assertArrayEquals(new String[] { "abc", "def", "ghi" }, args);
	}

	public void testWithSingleQuote() {
		String[] args = separator.splitArguments("abc 'def ghi' jkl");
		assertArrayEquals(new String[] { "abc", "def ghi", "jkl" }, args);
	}

	public void testWithDoubleQuote() {
		String[] args = separator.splitArguments("abc \"def ghi\" jkl");
		assertArrayEquals(new String[] { "abc", "def ghi", "jkl" }, args);
	}

	public void testWithEscapedSingleQuote() {
		String[] args = separator.splitArguments("abc 'def \\' ghi' jkl");
		assertArrayEquals(new String[] { "abc", "def \\' ghi", "jkl" }, args);
	}

	public void testWithEscapedDoubleQuote() {
		String[] args = separator.splitArguments("abc 'def \\\" ghi' jkl");
		assertArrayEquals(new String[] { "abc", "def \\\" ghi", "jkl" }, args);
	}
}
