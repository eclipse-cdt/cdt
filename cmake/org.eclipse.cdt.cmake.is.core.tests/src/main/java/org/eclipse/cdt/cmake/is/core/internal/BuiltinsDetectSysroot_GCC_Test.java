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
import org.eclipse.cdt.cmake.is.core.participant.Arglets.Sysroot_GCC;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class BuiltinsDetectSysroot_GCC_Test {

	private Sysroot_GCC testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new Sysroot_GCC();
	}

	/**
	 * Test method for {@link Arglets.Sysroot_GCC#processArgument}.
	 */
	@Test
	public final void testProcessArgument_sysroot() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext context;
		String parsed;

		final IPath cwd = new Path("");
		// --sysroot=/a/Path
		String arg = "--sysroot=/XAX/YYY";

		context = new ParseContext();
		testee.processArgument(context, cwd, arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
		// --sysroot="/a/Path"
		context = new ParseContext();
		arg = "--sysroot=\"/XXX/YYY\"";
		testee.processArgument(context, cwd, arg + " " + arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
	}

	/**
	 * Test method for {@link Arglets.Sysroot_GCC#processArgument}.
	 */
	@Test
	public final void testProcessArgument_isysroot() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext context;
		String parsed;

		final IPath cwd = new Path("");
		// -isysroot=/a/Path
		String arg = "-isysroot=/XAX/YYY";

		context = new ParseContext();
		testee.processArgument(context, cwd, arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
		// -isysroot="/a/Path"
		context = new ParseContext();
		arg = "-isysroot=\"/XXX/YYY\"";
		testee.processArgument(context, cwd, arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
	}

	/**
	 * Test method for {@link Arglets.Sysroot_GCC#processArgument}.
	 */
	@Test
	public final void testProcessArgument_no_sysroot_prefix() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext context;
		String parsed;

		final IPath cwd = new Path("");
		// --no-sysroot-prefix
		String arg = "--no-sysroot-prefix";

		context = new ParseContext();
		testee.processArgument(context, cwd, arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
	}
}
