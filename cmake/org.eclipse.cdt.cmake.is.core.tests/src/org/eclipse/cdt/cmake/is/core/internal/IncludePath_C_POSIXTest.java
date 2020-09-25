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
import org.eclipse.cdt.cmake.is.core.participant.IArglet.IArgumentCollector;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class IncludePath_C_POSIXTest {

	private IncludePath_C_POSIX testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new IncludePath_C_POSIX();
	}

	/**
	 * Test method for
	 * {@link IncludePath_C_POSIX#processArgument(IArgumentCollector, IPath, java.lang.String)}
	 * .
	 */
	@Test
	public final void testProcessArgument() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;

		String name = "/an/Include/Path";
		IPath cwd = new Path("");
		// -I/an/Include/Path
		result = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(result, cwd, "-I" + name + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
		// -I'/an/Include/Path'
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-I" + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
		// -I"/an/Include/Path"
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-I" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));

		// -I /an/Include/Path
		result = new ParseContext();
		assertEquals(2 + name.length() + 3, testee.processArgument(result, cwd, "-I   " + name + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
		// -I '/an/Include/Path'
		result = new ParseContext();
		assertEquals(2 + name.length() + 3 + 2, testee.processArgument(result, cwd, "-I   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
		// -I "/an/Include/Path"
		result = new ParseContext();
		assertEquals(2 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-I   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));

		name = (new Path("A:an\\Include/Path")).toOSString();
		// -IA:an\Include/Path
		result = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(result, cwd, "-I" + name + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
	}

	/**
	 * Test method for
	 * {@link IncludePath_C_POSIX#processArgument(IArgumentCollector, IPath, java.lang.String)}
	 */
	@Test
	public final void testProcessArgument_WS() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result = new ParseContext();

		String name = "/ye olde/In clu de/Pa the";
		IPath cwd = new Path("");
		// -I'/ye olde/In clu de/Pa the'
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-I" + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
		// -I"/ye olde/In clu de/Pa the"
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-I" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));

		// -I '/ye olde/In clu de/Pa the'
		result = new ParseContext();
		assertEquals(2 + name.length() + 3 + 2, testee.processArgument(result, cwd, "-I   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
		// -I "/ye olde/In clu de/Pa the"
		result = new ParseContext();
		assertEquals(2 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-I   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));

		name = (new Path("A:an\\In CLU  de/Pat h")).toOSString();
		// -I'A:an\In CLU de/Pat h'
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-I" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
	}

	/**
	 * Test method for
	 * {@link IncludePath_C_POSIX#processArgument(IArgumentCollector, IPath, java.lang.String)}
	 * .
	 */
	@Test
	public final void testProcessArgument_RelativePath() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";

		String name = (new Path("a/relative/Include/Path")).toOSString();
		IPath cwd = new Path("/compiler/working/dir");
		ParseContext result = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(result, cwd, "-I" + name + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		String absPath = cwd.append(name).toString();
		assertEquals("name", absPath, result.getIncludePaths().get(0));

		name = (new Path("a\\relative\\Include\\Path")).toOSString();
		cwd = new Path("\\compiler\\working\\dir");
		result = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(result, cwd, "-I" + name + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		absPath = cwd.append(name).toString();
		assertEquals("name", absPath, result.getIncludePaths().get(0));

		name = (new Path("../../src/Include/Path")).toOSString();
		cwd = new Path("/compiler/working/dir");
		result = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(result, cwd, "-I" + name + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		absPath = cwd.append(name).toString();
		assertEquals("name", absPath, result.getIncludePaths().get(0));

		name = (new Path("..\\..\\src\\Include\\Path")).toOSString();
		cwd = new Path("\\compiler\\working\\dir");
		result = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(result, cwd, "-I" + name + more));
		assertEquals("#entries", 1, result.getIncludePaths().size());
		absPath = cwd.append(name).toString();
		assertEquals("name", absPath, result.getIncludePaths().get(0));
	}
}
