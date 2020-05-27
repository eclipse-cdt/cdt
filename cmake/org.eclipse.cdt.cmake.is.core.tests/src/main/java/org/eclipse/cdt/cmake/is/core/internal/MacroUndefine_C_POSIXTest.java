/*******************************************************************************
 * Copyright (c) 2015-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.cmake.is.core.participant.Arglets.MacroUndefine_C_POSIX;
import org.eclipse.cdt.cmake.is.core.participant.IArglet.IArgumentCollector;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class MacroUndefine_C_POSIXTest {

	private MacroUndefine_C_POSIX testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new MacroUndefine_C_POSIX();
	}

	/**
	 * Test method for
	 * {@link MacroUndefine_C_POSIX#processArgument(IArgumentCollector, IPath, java.lang.String)}
	 * .
	 */
	@Test
	public final void testProcessArgument() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;
		final IPath cwd = new Path("");

		String name = "FOO";
		// -UFOO
		result = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(result, cwd, "-U" + name + more));
		assertEquals("#entries", 1, result.getUndefines().size());
		assertEquals("name", name, result.getUndefines().get(0));
		// -U FOO
		result = new ParseContext();
		assertEquals(2 + 2 + name.length(), testee.processArgument(result, cwd, "-U  " + name + more));
		assertEquals("#entries", 1, result.getUndefines().size());
		assertEquals("name", name, result.getUndefines().get(0));
	}
}
