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

import org.eclipse.cdt.cmake.is.core.Arglets.MacroUndefine_C_POSIX;
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
	 * {@link MacroUndefine_C_POSIX#processArgument(IParseContext, IPath, java.lang.String)}
	 * .
	 */
	@Test
	public final void testProcessArgument() {
		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext entries;
		ICLanguageSettingEntry parsed;
		final IPath cwd = new Path("");

		String name = "FOO";
		// -UFOO
		entries = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(entries, cwd, "-U" + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
		assertEquals("kind", ICSettingEntry.UNDEFINED, (parsed.getFlags() & ICSettingEntry.UNDEFINED));
		assertEquals("name", name, parsed.getName());
		assertEquals("value", "", parsed.getValue());
		// -U FOO
		entries = new ParseContext();
		assertEquals(2 + 2 + name.length(), testee.processArgument(entries, cwd, "-U  " + name + more));
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
		assertEquals("kind", ICSettingEntry.UNDEFINED, (parsed.getFlags() & ICSettingEntry.UNDEFINED));
		assertEquals("name", name, parsed.getName());
		assertEquals("value", "", parsed.getValue());
	}
}
