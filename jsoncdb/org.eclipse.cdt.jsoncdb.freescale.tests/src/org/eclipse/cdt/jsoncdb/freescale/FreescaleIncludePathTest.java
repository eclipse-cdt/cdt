/*******************************************************************************
 * Copyright (c) 2023 Thomas Kucharczyk
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.jsoncdb.freescale;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.jsoncdb.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.jsoncdb.core.participant.IToolCommandlineParser.IResult;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Kucharcyzk
 */
public class FreescaleIncludePathTest {

	private FreescaleToolDetectionParticipant.SystemIncludePath_chc testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new FreescaleToolDetectionParticipant.SystemIncludePath_chc();
	}

	@Test
	public final void testProcessArgument() {
		DefaultToolCommandlineParser tcp = new DefaultToolCommandlineParser(null, null, testee);

		final String more = " -ViewHidden -NoEnv -NoBeep -WStdoutOn -WmsgNu=abcde -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS"
				+ "- -Mb -CPUHCS12XE           -I. -Cf -CPUHCS12XE "
				+ "-D__FAR_DATA -Mb -TD4LD4LLD4  -WmsgSe1801 -WmsgSd4444  -DDEBUG=1     -Onf -Onu -OnP -Ont -ObjN=CMakeFiles/AttachmentTest.dir/null.c.obj"
				+ "-Lasm=CMakeFiles/AttachmentTest.dir/null.c.obj.lst -IC:/HCS12_CW/lib/hc12c/include C:/_build/Debug/null.c";

		final IPath cwd = new Path("");

		String name = (new Path("A:\\an\\In CLU  de/Pat h")).toOSString();

		// -EnvLIBPATH="A:\an\In CLU de/Pat h"
		IResult result = tcp.processArgs(cwd, "-EnvLIBPATH=" + "\"" + name + "\"" + more);
		System.out.println("-EnvLIBPATH=" + "\"" + name + "\"" + more);
		System.out.println(cwd);

		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		// -EnvLIBPATH='A:an\In CLU de/Pat h'
		result = tcp.processArgs(cwd, "-EnvLIBPATH=" + "\"" + name + "\"" + more);
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		// -EnvLIBPATH=/an/Include/Path
		name = "/C:/code_warrior/HCS12_CW/lib/hc12c/include";
		result = tcp.processArgs(cwd, "-EnvLIBPATH" + "=" + name + more);
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		// -EnvLIBPATH='/an/Include/Path'
		result = tcp.processArgs(cwd, "-EnvLIBPATH" + "=" + "'" + name + "'" + more);
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

		// -EnvLIBPATH="/an/Include/Path"
		result = tcp.processArgs(cwd, "-EnvLIBPATH" + "=" + "\"" + name + "\"" + more);
		assertEquals("#entries", 1, result.getSystemIncludePaths().size());
		assertEquals("name", name, result.getSystemIncludePaths().get(0));

	}

}
