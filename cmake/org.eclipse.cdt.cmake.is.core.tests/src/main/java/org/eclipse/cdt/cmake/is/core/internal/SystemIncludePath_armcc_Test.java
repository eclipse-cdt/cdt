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

import org.eclipse.cdt.cmake.is.core.Arglets.SystemIncludePath_armcc;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class SystemIncludePath_armcc_Test {

	private SystemIncludePath_armcc testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new SystemIncludePath_armcc();
	}

	@Test
	public final void testProcessArgument() {
		final String more = " -g "
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext entries;
		ICLanguageSettingEntry parsed;
		final IPath cwd = new Path("");

		String name = "/an/Include/Path";

		// -J/an/Include/Path
		entries = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(entries, cwd, "-J" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -J'/an/Include/Path'
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-J" + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -J"/an/Include/Path"
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-J" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		name = (new Path("A:an\\In CLU  de/Pat h")).toOSString();
		// -J"A:an\In CLU de/Pat h"
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-J" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		// -J'A:an\In CLU de/Pat h'
		entries = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(entries, cwd, "-J" + "'" + name + "'" + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
	}

}
