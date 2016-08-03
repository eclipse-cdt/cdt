/*******************************************************************************
 * Copyright (c) 2016 COSEDA Technologies GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dominic Scharfe (COSEDA Technologies GmbH) - Initial Implementation
 *******************************************************************************/ 
package org.eclipse.cdt.dsf.mi.service.command.commands;

import static org.junit.Assert.*;

import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSetArgs.MIArgumentAdjustable;
import org.junit.Test;

public class TestMIArgumentAdjustable {

	/**
	 * Convenience method for testing
	 * {@link MIArgumentAdjustable#getAdjustedValue()}.
	 */
	private void testAdjust(String expected, String value) {
		assertEquals(expected, new MIArgumentAdjustable(value).getAdjustedValue());
	}

	@Test
	public void keepArgument() {
		testAdjust("arg1", "arg1");
	}

	@Test
	public void replaceLineBreak() {
		testAdjust("arg1'$'\\n''", "arg1\n");
	}
	
	@Test
	public void replaceSemicolon() {
		testAdjust("arg1\";\"2", "arg1;2");
	}

	/**
	 * Testcase to reproduce
	 * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=474648">Bug
	 * 474648</a> - Wrong arguments are passed to a program in debug mode
	 */
	@Test
	public void bug474648() {
		testAdjust("--abc=\"x\";\"y\";\"z\"", "--abc=\"x;y;z\"");
	}
}
