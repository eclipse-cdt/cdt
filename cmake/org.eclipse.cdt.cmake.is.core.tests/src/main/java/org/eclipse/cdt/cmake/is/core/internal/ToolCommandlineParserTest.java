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
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.cdt.cmake.is.core.participant.Arglets;
import org.eclipse.cdt.cmake.is.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.IToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.ResponseFileArglets;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class ToolCommandlineParserTest {

	@Test
	public final void testResponseFileArgumentParser_At() throws Exception {

		DefaultToolCommandlineParser testee = new DefaultToolCommandlineParser(new ResponseFileArglets.At(), null,
				new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX());

		IToolCommandlineParser.IResult result;

		final String more = " -g -MMD  -o CMakeFiles/execut1.dir/util1.c.o"
				+ " -c /testprojects/C-subsrc/src/src-sub/main.c";

		// generate response file
		final java.nio.file.Path dirP = Files.createTempDirectory("rfpt");
		final java.nio.file.Path cwdP = dirP.getParent();
		final java.nio.file.Path relRspP = dirP.getFileName().resolve(Paths.get("response.file.txt"));
		final java.nio.file.Path absRspP = cwdP.resolve(relRspP);

		final String incDirName = "an/include/dir";
		final String def1Name = "def1";
		final String def2Name = "def2";
		final String defName = "DEF_ON_COMMANDLINE";

		try (PrintWriter rspFilePw = new PrintWriter(
				Files.newOutputStream(absRspP, StandardOpenOption.WRITE, StandardOpenOption.CREATE));) {
			rspFilePw.printf(" -D%s=234 -I%s -D%s=987", def1Name, incDirName, def2Name);
			rspFilePw.close();
		}
		// @a/response/file.txt
		result = testee.processArgs(new Path(cwdP.toString()), "@" + relRspP.toString() + " -D" + defName + more);
		assertEquals("#defines", 3, result.getDefines().size());
		assertTrue("found", result.getDefines().containsKey(def1Name));
		assertEquals("value", "234", result.getDefines().get(def1Name));
		assertTrue("found", result.getDefines().containsKey(def2Name));
		assertEquals("value", "987", result.getDefines().get(def2Name));
		assertTrue("found", result.getDefines().containsKey(defName));
		assertEquals("value", "", result.getDefines().get(defName));

		assertEquals("#paths", 1, result.getIncludePaths().size());
		assertEquals("value", cwdP.resolve(incDirName).toString(), result.getIncludePaths().get(0));

		// @ a/response.file.txt
		result = testee.processArgs(new Path(cwdP.toString()), "@ " + relRspP.toString() + " -D" + defName + more);
		assertEquals("#defines", 3, result.getDefines().size());
		assertTrue("found", result.getDefines().containsKey(def1Name));
		assertEquals("value", "234", result.getDefines().get(def1Name));
		assertTrue("found", result.getDefines().containsKey(def2Name));
		assertEquals("value", "987", result.getDefines().get(def2Name));
		assertTrue("found", result.getDefines().containsKey(defName));
		assertEquals("value", "", result.getDefines().get(defName));

		assertEquals("#paths", 1, result.getIncludePaths().size());
		assertEquals("value", cwdP.resolve(incDirName).toString(), result.getIncludePaths().get(0));

		Files.delete(absRspP);
	}

	/**
	 * Test for HERE documents on cmdline:@<< ... <<
	 */
	@Test
	public final void testResponseFileArgumentParser_At_heredoc() throws Exception {
		DefaultToolCommandlineParser testee = new DefaultToolCommandlineParser(new ResponseFileArglets.At(), null,
				new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX());

		final String more = " -g -MMD  -o CMakeFiles/execut1.dir/util1.c.o"
				+ " -c /testprojects/C-subsrc/src/src-sub/main.c";
		IToolCommandlineParser.IResult result;

		String name = "/ye/olde/Include/Pathe";
		IPath cwd = new Path("");
		result = new ParseContext();
		// @<< ... <<
		result = testee.processArgs(cwd, "@<<" + " -I" + name + " <<" + more);
		assertEquals("#paths", 1, result.getIncludePaths().size());
		assertEquals("name", name, result.getIncludePaths().get(0));
	}
}
