/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.cmake.is.core.participant.Arglets.SystemIncludePath_armcc;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
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
		ParseContext result;
		ICLanguageSettingEntry parsed;
		final IPath cwd = new Path("");

		String name = "/an/Include/Path";

		// -J/an/Include/Path
		result = new ParseContext();
		assertEquals(2 + name.length(), testee.processArgument(result, cwd, "-J" + name + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
		// -J'/an/Include/Path'
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-J" + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
		// -J"/an/Include/Path"
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-J" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		name = (new Path("A:an\\In CLU  de/Pat h")).toOSString();
		// -J"A:an\In CLU de/Pat h"
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-J" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		// -J'A:an\In CLU de/Pat h'
		result = new ParseContext();
		assertEquals(2 + name.length() + 2, testee.processArgument(result, cwd, "-J" + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
	}

}
