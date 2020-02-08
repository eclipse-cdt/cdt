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

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.cdt.cmake.is.core.Arglets;
import org.eclipse.cdt.cmake.is.core.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.IToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.ResponseFileArglets;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class ToolCommandlineParserTest {

	@Test
	public final void testResponseFileArgumentParser_At() throws Exception {

		DefaultToolCommandlineParser testee = new DefaultToolCommandlineParser("egal", new ResponseFileArglets.At(),
				null, new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX());

		IToolCommandlineParser.IResult entries;

		final String more = " -g -MMD  -o CMakeFiles/execut1.dir/util1.c.o"
				+ " -c /testprojects/C-subsrc/src/src-sub/main.c";
		ICLanguageSettingEntry parsed;

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
		entries = testee.processArgs(new Path(cwdP.toString()), "@" + relRspP.toString() + " -D" + defName + more);
		assertEquals("#entries", 4, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
		assertEquals("name", def1Name, parsed.getName());
		parsed = entries.getSettingEntries().get(1);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", cwdP.resolve(incDirName).toString(), parsed.getName());
		parsed = entries.getSettingEntries().get(2);
		assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
		assertEquals("name", def2Name, parsed.getName());
		parsed = entries.getSettingEntries().get(3);
		assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
		assertEquals("name", defName, parsed.getName());

		// @ a/response.file.txt
		entries = testee.processArgs(new Path(cwdP.toString()), "@ " + relRspP.toString() + " -D" + defName + more);
		assertEquals("#entries", 4, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
		assertEquals("name", def1Name, parsed.getName());
		parsed = entries.getSettingEntries().get(1);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", cwdP.resolve(incDirName).toString(), parsed.getName());
		parsed = entries.getSettingEntries().get(2);
		assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
		assertEquals("name", def2Name, parsed.getName());
		parsed = entries.getSettingEntries().get(3);
		assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
		assertEquals("name", defName, parsed.getName());

		Files.delete(absRspP);
	}

	/**
	 * Test for HERE documents on cmdline:@<< ... <<
	 */
	@Test
	public final void testResponseFileArgumentParser_At_heredoc() throws Exception {
		DefaultToolCommandlineParser testee = new DefaultToolCommandlineParser("egal", new ResponseFileArglets.At(),
				null, new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX());

		final String more = " -g -MMD  -o CMakeFiles/execut1.dir/util1.c.o"
				+ " -c /testprojects/C-subsrc/src/src-sub/main.c";
		IToolCommandlineParser.IResult entries;
		ICLanguageSettingEntry parsed;

		String name = "/ye/olde/Include/Pathe";
		IPath cwd = new Path("");
		entries = new ParseContext();
		// @<< ... <<
		entries = testee.processArgs(cwd, "@<<" + " -I" + name + " <<" + more);
		assertEquals("#entries", 1, entries.getSettingEntries().size());
		parsed = entries.getSettingEntries().get(0);
		assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
		assertEquals("name", name, parsed.getName());
	}
}
