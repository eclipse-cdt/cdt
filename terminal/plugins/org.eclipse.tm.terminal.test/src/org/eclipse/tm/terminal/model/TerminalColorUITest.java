/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

import static org.eclipse.tm.terminal.model.TerminalColor.BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.BLUE;
import static org.eclipse.tm.terminal.model.TerminalColor.CYAN;
import static org.eclipse.tm.terminal.model.TerminalColor.GREEN;
import static org.eclipse.tm.terminal.model.TerminalColor.MAGENTA;
import static org.eclipse.tm.terminal.model.TerminalColor.RED;
import static org.eclipse.tm.terminal.model.TerminalColor.WHITE;
import static org.eclipse.tm.terminal.model.TerminalColor.YELLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.swt.widgets.Display;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is a UI test because {@link TerminalColor#convertColor(boolean, boolean)
 * requires a Display to operate the ColorRegistry.
 */
public class TerminalColorUITest {

	private static Display display = null;

	@BeforeClass
	public static void createDisplay() {
		Display current = Display.getCurrent();
		if (current == null) {
			display = new Display();
		}
	}

	@AfterClass
	public static void disposeDisplay() {
		if (display != null) {
			display.dispose();
		}
	}

	@Test
	public void testInversionsStandard() {

		assertEquals(BLACK.convertColor(false, false), WHITE.convertColor(true, false));
		assertNotEquals(BLACK.convertColor(false, false), WHITE.convertColor(false, false));

		assertEquals(RED.convertColor(false, false), RED.convertColor(true, false));
		assertEquals(GREEN.convertColor(false, false), GREEN.convertColor(true, false));
		assertEquals(YELLOW.convertColor(false, false), YELLOW.convertColor(true, false));
		assertEquals(BLUE.convertColor(false, false), BLUE.convertColor(true, false));
		assertEquals(MAGENTA.convertColor(false, false), MAGENTA.convertColor(true, false));
		assertEquals(CYAN.convertColor(false, false), CYAN.convertColor(true, false));

		assertEquals(WHITE.convertColor(false, false), BLACK.convertColor(true, false));
		assertNotEquals(WHITE.convertColor(false, false), BLACK.convertColor(false, false));

	}

	@Test
	public void testInversionsBright() {
		assertEquals(BLACK.convertColor(false, true), WHITE.convertColor(true, true));
		assertNotEquals(BLACK.convertColor(false, true), WHITE.convertColor(false, true));

		assertEquals(RED.convertColor(false, true), RED.convertColor(true, true));
		assertEquals(GREEN.convertColor(false, true), GREEN.convertColor(true, true));
		assertEquals(YELLOW.convertColor(false, true), YELLOW.convertColor(true, true));
		assertEquals(BLUE.convertColor(false, true), BLUE.convertColor(true, true));
		assertEquals(MAGENTA.convertColor(false, true), MAGENTA.convertColor(true, true));
		assertEquals(CYAN.convertColor(false, true), CYAN.convertColor(true, true));

		assertEquals(WHITE.convertColor(false, true), BLACK.convertColor(true, true));
		assertNotEquals(WHITE.convertColor(false, true), BLACK.convertColor(false, true));
	}
}
