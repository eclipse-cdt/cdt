/*******************************************************************************
 * Copyright (c) 2018 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.nvidia;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.cmake.is.core.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.IToolCommandlineParser.IResult;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class NvccSystemIncludePathTest {

	private NvccSystemIncludePathArglet testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new NvccSystemIncludePathArglet();
	}

	@Test
	public final void testProcessArgument() {
		DefaultToolCommandlineParser tcp = new DefaultToolCommandlineParser("egal", null, null, testee);

		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.cu";

		final IPath cwd = new Path("");
		ICLanguageSettingEntry parsed;

		// -isystem=/an/Include/Path
		String name = "/an/Include/Path";
		IResult result = tcp.processArgs(cwd, "-isystem" + "=" + name + more);
		assertEquals("#entries", 1, result.getSettingEntries().size());
		parsed = result.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -isystem='/an/Include/Path'
		result = tcp.processArgs(cwd, "-isystem" + "=" + "'" + name + "'" + more);
		assertEquals("#entries", 1, result.getSettingEntries().size());
		parsed = result.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
		// -isystem="/an/Include/Path"
		result = tcp.processArgs(cwd, "-isystem" + "=" + "\"" + name + "\"" + more);
		assertEquals("#entries", 1, result.getSettingEntries().size());
		parsed = result.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		name = (new Path("A:an\\In CLU  de/Pat h")).toOSString();
		// -isystem="A:an\In CLU de/Pat h"
		result = tcp.processArgs(cwd, "-isystem" + "=" + "\"" + name + "\"" + more);
		assertEquals("#entries", 1, result.getSettingEntries().size());
		parsed = result.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());

		// -isystem='A:an\In CLU de/Pat h'
	}

}
