/*******************************************************************************
 * Copyright (c) 2020 Ghaith Hachem.
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.jsoncdb.core.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.jsoncdb.core.participant.Arglets;
import org.eclipse.cdt.jsoncdb.core.participant.Arglets.Target_Clang;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

public class BuiltinsDetectTarget_Clang_Test {

	private Target_Clang testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new Target_Clang();
	}

	/**
	 * Test method for {@link Arglets.Sysroot_GCC#processArgument}.
	 */
	@Test
	public final void testProcessArgument_target() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext context;
		String parsed;

		final IPath cwd = new Path("");
		// --target=some-triplet-string
		String arg = "--target=test-triplet-entry";

		context = new ParseContext();
		testee.processArgument(context, cwd, arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
		// --sysroot="/a/Path"
		context = new ParseContext();
		arg = "--target=test-triplet-entry";
		testee.processArgument(context, cwd, arg + " " + arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
	}

}
