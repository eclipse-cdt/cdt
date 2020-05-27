/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.cmake.is.core.participant.Arglets;
import org.eclipse.cdt.cmake.is.core.participant.Arglets.LangStd_GCC;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class BuiltinsDetectLangStd_GCC_Test {

	private LangStd_GCC testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new LangStd_GCC();
	}

	/**
	 * Test method for {@link Arglets.LangStd_GCC#processArgument}.
	 */
	@Test
	public final void testProcessArgument_std() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext context;
		String parsed;

		final IPath cwd = new Path("");
		// -std=
		String arg = "-std=c++14";

		context = new ParseContext();
		testee.processArgument(context, cwd, arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
		// -std=c11
		context = new ParseContext();
		arg = "-std=c11";
		testee.processArgument(context, cwd, arg + " " + arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
		// -std=c1x
		context = new ParseContext();
		arg = "-std=c1x";
		testee.processArgument(context, cwd, arg + " " + arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
		// -std=iso9899:1999
		context = new ParseContext();
		arg = "-std=iso9899:1999";
		testee.processArgument(context, cwd, arg + " " + arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
	}

	/**
	 * Test method for {@link Arglets.LangStd_GCC#processArgument}.
	 */
	@Test
	public final void testProcessArgument_ansi() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext context;
		String parsed;

		final IPath cwd = new Path("");
		// -ansi
		String arg = "-ansi";

		context = new ParseContext();
		testee.processArgument(context, cwd, arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
	}
}
