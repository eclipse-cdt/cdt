/*******************************************************************************
 * Copyright (c) 2015 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.cmake.is.core.Arglets.IncludePath_C_POSIX;
import org.eclipse.cdt.cmake.is.core.IArglet.IParseContext;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
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
	 * {@link IncludePath_C_POSIX#processArgument(IParseContext, IPath, java.lang.String)}
	 * .
	 */
	@Test
	public final void testProcessArgument() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext entries;
		ICLanguageSettingEntry parsed;

		String name = "/an/Include/Path";
		IPath cwd = new Path("");
		// -I/an/Include/Path
		entries = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(entries, cwd, "-I" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -I'/an/Include/Path'
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-I" + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -I"/an/Include/Path"
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-I" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		// -I /an/Include/Path
		entries = new ParseContext();
		assertEquals(2 + name.length() + 3, testee.processArgument(entries, cwd, "-I   " + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -I '/an/Include/Path'
		entries = new ParseContext();
		assertEquals(2 + name.length() + 3 + 2,
				testee.processArgument(entries, cwd, "-I   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -I "/an/Include/Path"
		entries = new ParseContext();
		assertEquals(2 + name.length() + 3 + 2,
				testee.processArgument(entries, cwd, "-I   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		name = (new Path("A:an\\Include/Path")).toOSString();
		// -IA:an\Include/Path
		entries = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(entries, cwd, "-I" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
	}

	/**
	 * Test method for
	 * {@link IncludePath_C_POSIX#processArgument(IParseContext, IPath, java.lang.String)}
	 */
	@Test
	public final void testProcessArgument_WS() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext entries = new ParseContext();
		ICLanguageSettingEntry parsed;

		String name = "/ye olde/In clu de/Pa the";
		IPath cwd = new Path("");
		// -I'/ye olde/In clu de/Pa the'
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-I" + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -I"/ye olde/In clu de/Pa the"
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-I" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		// -I '/ye olde/In clu de/Pa the'
		entries = new ParseContext();
		assertEquals(2 + name.length() + 3 + 2,
				testee.processArgument(entries, cwd, "-I   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -I "/ye olde/In clu de/Pa the"
		entries = new ParseContext();
		assertEquals(2 + name.length() + 3 + 2,
				testee.processArgument(entries, cwd, "-I   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		name = (new Path("A:an\\In CLU  de/Pat h")).toOSString();
		// -I'A:an\In CLU de/Pat h'
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-I" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
	}

	/**
	 * Test method for
	 * {@link IncludePath_C_POSIX#processArgument(IParseContext, IPath, java.lang.String)}
	 * .
	 */
	@Test
	public final void testProcessArgument_RelativePath() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ICLanguageSettingEntry parsed;

		String name = (new Path("a/relative/Include/Path")).toOSString();
		IPath cwd = new Path("/compiler/working/dir");
		ParseContext entries = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(entries, cwd, "-I" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		String absPath = cwd.append(name).toString();
		assertEquals("name", absPath, parsed.getName());

		name = (new Path("a\\relative\\Include\\Path")).toOSString();
		cwd = new Path("\\compiler\\working\\dir");
		entries = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(entries, cwd, "-I" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		absPath = cwd.append(name).toString();
		assertEquals("name", absPath, parsed.getName());

		name = (new Path("../../src/Include/Path")).toOSString();
		cwd = new Path("/compiler/working/dir");
		entries = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(entries, cwd, "-I" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		absPath = cwd.append(name).toString();
		assertEquals("name", absPath, parsed.getName());

		name = (new Path("..\\..\\src\\Include\\Path")).toOSString();
		cwd = new Path("\\compiler\\working\\dir");
		entries = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(entries, cwd, "-I" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		absPath = cwd.append(name).toString();
		assertEquals("name", absPath, parsed.getName());
	}
}
