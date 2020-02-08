/*******************************************************************************
 * Copyright (c) 2015-2018 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.cmake.is.core.Arglets.IncludePath_C_POSIX;
import org.eclipse.cdt.cmake.is.core.Arglets.SystemIncludePath_C;
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
		ParseContext entries;
		ICLanguageSettingEntry parsed;
		final IPath cwd = new Path("");

		String name = "/an/Include/Path";

		// -isystem /an/Include/Path
		entries = new ParseContext();
		assertEquals(8 + name.length() + 3, testee.processArgument(entries, cwd, "-isystem   " + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -isystem '/an/Include/Path'
		entries = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(entries, cwd, "-isystem   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -isystem "/an/Include/Path"
		entries = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(entries, cwd, "-isystem   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		name = (new Path("A:an\\Include/Path")).toOSString();
		// -isystem A:an\Include/Path
		entries = new ParseContext();
		assertEquals(8 + 1 + name.length(), testee.processArgument(entries, cwd, "-isystem " + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		// -isystemA:an\Include/Path
		entries = new ParseContext();
		assertEquals(8 + name.length(), testee.processArgument(entries, cwd, "-isystem" + name + more));
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
		ParseContext entries;
		ICLanguageSettingEntry parsed;
		final IPath cwd = new Path("");

		String name = "/ye olde/In clu de/Pa the";
		// -isystem '/ye olde/In clu de/Pa the'
		entries = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(entries, cwd, "-isystem   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -isystem "/ye olde/In clu de/Pa the"
		entries = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(entries, cwd, "-isystem   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		name = (new Path("A:an\\In CLU  de/Pat h")).toOSString();
		// -isystem"A:an\In CLU de/Pat h"
		entries = new ParseContext();
		assertEquals(8 + name.length() + 2,
				testee.processArgument(entries, cwd, "-isystem" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		// -isystem'A:an\In CLU de/Pat h'
		entries = new ParseContext();
		assertEquals(8 + name.length() + 2, testee.processArgument(entries, cwd, "-isystem" + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		name = (new Path("/Inc/CLUde/Path")).toOSString();
		// -isystem/Inc/CLUde/Path
		entries = new ParseContext();
		assertEquals(8 + name.length(), testee.processArgument(entries, cwd, "-isystem" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
	}

}
