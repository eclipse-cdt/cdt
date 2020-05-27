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

import org.eclipse.cdt.cmake.is.core.participant.Arglets.IncludePath_C_POSIX;
import org.eclipse.cdt.cmake.is.core.participant.Arglets.SystemIncludePath_C;
import org.eclipse.cdt.cmake.is.core.participant.IArglet.IArgumentCollector;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class SystemIncludePath_C_Test {

	private SystemIncludePath_C testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new SystemIncludePath_C();
	}

	@Test
	public final void testProcessArgument() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;
		final IPath cwd = new Path("");

		String name = "/an/Include/Path";

		// -isystem /an/Include/Path
		result = new ParseContext();
		assertEquals(8 + name.length() + 3, testee.processArgument(result, cwd, "-isystem   " + name + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
		// -isystem '/an/Include/Path'
		result = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-isystem   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
		// -isystem "/an/Include/Path"
		result = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-isystem   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		name = (new Path("A:an\\Include/Path")).toOSString();
		// -isystem A:an\Include/Path
		result = new ParseContext();
		assertEquals(8 + 1 + name.length(), testee.processArgument(result, cwd, "-isystem " + name + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		// -isystemA:an\Include/Path
		result = new ParseContext();
		assertEquals(8 + name.length(), testee.processArgument(result, cwd, "-isystem" + name + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
	}

	/**
	 * Test method for
	 * {@link IncludePath_C_POSIX#processArgument(IArgumentCollector, IPath, java.lang.String)}
	 */
	@Test
	public final void testProcessArgument_WS() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;
		final IPath cwd = new Path("");

		String name = "/ye olde/In clu de/Pa the";
		// -isystem '/ye olde/In clu de/Pa the'
		result = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-isystem   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
		// -isystem "/ye olde/In clu de/Pa the"
		result = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-isystem   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		name = (new Path("A:an\\In CLU  de/Pat h")).toOSString();
		// -isystem"A:an\In CLU de/Pat h"
		result = new ParseContext();
		assertEquals(8 + name.length() + 2,
				testee.processArgument(result, cwd, "-isystem" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		// -isystem'A:an\In CLU de/Pat h'
		result = new ParseContext();
		assertEquals(8 + name.length() + 2, testee.processArgument(result, cwd, "-isystem" + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		name = (new Path("/Inc/CLUde/Path")).toOSString();
		// -isystem/Inc/CLUde/Path
		result = new ParseContext();
		assertEquals(8 + name.length(), testee.processArgument(result, cwd, "-isystem" + name + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
	}

}
