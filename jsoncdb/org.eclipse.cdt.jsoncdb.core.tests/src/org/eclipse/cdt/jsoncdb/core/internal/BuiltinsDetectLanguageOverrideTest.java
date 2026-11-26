/********************************************************************************
 * Copyright (c) 2026 Igor V. Kovalenko and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.jsoncdb.core.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.jsoncdb.core.participant.Arglets;
import org.eclipse.cdt.jsoncdb.core.participant.Arglets.LangStd_GCC;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class BuiltinsDetectLanguageOverrideTest {

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
		String arg;

		final IPath cwd = new Path("");

		// -x identifier
		context = new ParseContext();
		arg = "-x c++";
		testee.processArgument(context, cwd, arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);

		// --language=identifier
		context = new ParseContext();
		arg = "--language=c++";
		testee.processArgument(context, cwd, arg + " " + arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);

		// --language identifier
		context = new ParseContext();
		arg = "--language c++";
		testee.processArgument(context, cwd, arg + " " + arg + more);
		assertEquals("#entries", 1, context.getBuiltinDetectionArgs().size());
		parsed = context.getBuiltinDetectionArgs().get(0);
		assertEquals("name", arg, parsed);
	}
}
