/*******************************************************************************
 * Copyright (c) 2018-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.nvidia;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.cmake.is.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.IToolCommandlineParser.IResult;
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
		DefaultToolCommandlineParser tcp = new DefaultToolCommandlineParser(null, null, testee);

		final String more = " -g -MMD -MT CMakeFiles/execut1.dir/util1.c.o -MF \"CMakeFiles/execut1.dir/util1.c.o.d\""
				+ " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.cu";

		final IPath cwd = new Path("");

		// -isystem=/an/Include/Path
		String name = "/an/Include/Path";
		IResult result = tcp.processArgs(cwd, "-isystem" + "=" + name + more);
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
		// -isystem='/an/Include/Path'
		result = tcp.processArgs(cwd, "-isystem" + "=" + "'" + name + "'" + more);
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));
		// -isystem="/an/Include/Path"
		result = tcp.processArgs(cwd, "-isystem" + "=" + "\"" + name + "\"" + more);
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		name = (new Path("A:an\\In CLU  de/Pat h")).toOSString();
		// -isystem="A:an\In CLU de/Pat h"
		result = tcp.processArgs(cwd, "-isystem" + "=" + "\"" + name + "\"" + more);
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		// -isystem='A:an\In CLU de/Pat h'
	}

}
