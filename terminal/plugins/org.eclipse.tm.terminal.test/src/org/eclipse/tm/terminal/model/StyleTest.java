/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

import junit.framework.TestCase;

public class StyleTest extends TestCase {
	final TerminalColor c1 = TerminalColor.getIndexedTerminalColor(1);
	final TerminalColor c2 = TerminalColor.getIndexedTerminalColor(2);
	final TerminalColor c3 = TerminalColor.getIndexedTerminalColor(3);

	public void testGetStyle() {
		TerminalStyle s1 = TerminalStyle.getStyle(c1, c2, true, false, true, false);
		TerminalStyle s2 = TerminalStyle.getStyle(c1, c2, true, false, true, false);
		assertEquals(s1, s2);
		assertSame(s1, s2);
		s1 = s1.setBlink(!s1.isBlink());
		assertNotSame(s1, s2);
		assertFalse(s1.equals(s2));
		s1 = s1.setBlink(!s1.isBlink());
		assertSame(s1, s2);
	}

	public void testSetForeground() {
		TerminalStyle s1 = TerminalStyle.getStyle(c1, c2, true, false, true, false);
		TerminalStyle s2 = s1;
		s2 = s1.setForeground(c3);
		assertNotSame(s1, s2);
		assertFalse(s1.equals(s2));
		assertSame(s2.getForegroundTerminalColor(), c3);
		assertSame(s1.getForegroundTerminalColor(), c1);
		assertSame(s1.getBackgroundTerminalColor(), c2);
		assertSame(s2.getBackgroundTerminalColor(), c2);
		s2 = s2.setForeground(c1);
		assertSame(s1, s2);
	}

	public void testSetBackground() {
		TerminalStyle s1 = TerminalStyle.getStyle(c1, c2, true, false, true, false);
		TerminalStyle s2 = s1;
		s2 = s1.setBackground(c3);
		assertNotSame(s1, s2);
		assertFalse(s1.equals(s2));
		assertSame(s2.getForegroundTerminalColor(), c1);
		assertSame(s1.getForegroundTerminalColor(), c1);
		assertSame(s1.getBackgroundTerminalColor(), c2);
		assertSame(s2.getBackgroundTerminalColor(), c3);
		s2 = s2.setBackground(c2);
		assertSame(s1, s2);
	}

	public void testSetBold() {
		TerminalStyle s1 = getDefaultStyle();
		TerminalStyle s2 = s1;
		assertSame(s1, s2);
		assertFalse(s2.isBold());
		s2 = s2.setBold(true);
		assertNotSame(s1, s2);
		assertTrue(s2.isBold());
		s2 = s2.setBold(false);
		assertSame(s1, s2);
		assertFalse(s2.isBold());
	}

	public void testSetBlink() {
		TerminalStyle s1 = getDefaultStyle();
		TerminalStyle s2 = s1;
		assertSame(s1, s2);
		assertFalse(s2.isBlink());
		s2 = s2.setBlink(true);
		assertNotSame(s1, s2);
		assertTrue(s2.isBlink());
		s2 = s2.setBlink(false);
		assertSame(s1, s2);
		assertFalse(s2.isBlink());
	}

	public void testSetUnderline() {
		TerminalStyle s1 = getDefaultStyle();
		TerminalStyle s2 = s1;
		assertSame(s1, s2);
		assertFalse(s2.isUnderline());
		s2 = s2.setUnderline(true);
		assertNotSame(s1, s2);
		assertTrue(s2.isUnderline());
		s2 = s2.setUnderline(false);
		assertSame(s1, s2);
		assertFalse(s2.isUnderline());
	}

	public void testSetReverse() {
		TerminalStyle s1 = getDefaultStyle();
		TerminalStyle s2 = s1;
		assertSame(s1, s2);
		assertFalse(s2.isReverse());
		s2 = s2.setReverse(true);
		assertNotSame(s1, s2);
		assertTrue(s2.isReverse());
		s2 = s2.setReverse(false);
		assertSame(s1, s2);
		assertFalse(s2.isReverse());
	}

	private TerminalStyle getDefaultStyle() {
		return TerminalStyle.getStyle(c1, c2, false, false, false, false);
	}

}
