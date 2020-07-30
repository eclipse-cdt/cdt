/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.cmake.is.core.participant.Arglets.MacrosFile_GCC;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class MacrosFile_GCCTest {
	private MacrosFile_GCC testee;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testee = new MacrosFile_GCC();
	}

	/**
	 * Test method for {@link MacrosFile_GCC#processArgument(IParseContext, IPath, java.lang.String)} .
	 */
	@Test
	public final void testProcessArgument() {
		final String more = " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result;
		String parsed;

		String name = "/an/Macros/file.inc";
		IPath cwd = new Path("");
		// -imacros/an/Macros/file.inc
		result = new ParseContext();
		assertEquals(8 + name.length(), testee.processArgument(result, cwd, "-imacros" + name + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);
		// -imacros'/an/Macros/file.inc'
		result = new ParseContext();
		assertEquals(8 + name.length() + 2, testee.processArgument(result, cwd, "-imacros" + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);
		// -imacros"/an/Macros/file.inc"
		result = new ParseContext();
		assertEquals(8 + name.length() + 2,
				testee.processArgument(result, cwd, "-imacros" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);

		// -imacros /an/Macros/file.inc
		result = new ParseContext();
		assertEquals(8 + name.length() + 3, testee.processArgument(result, cwd, "-imacros   " + name + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);
		// -imacros '/an/Macros/file.inc'
		result = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-imacros   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);
		// -imacros "/an/Macros/file.inc"
		result = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-imacros   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);

		name = (new Path("A:an\\Macros/file.inc")).toOSString();
		// -imacrosA:an\Macros/file.inc
		result = new ParseContext();
		assertEquals(8 + name.length(), testee.processArgument(result, cwd, "-imacros" + name + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);
	}

	/**
	 * Test method for {@link MacrosFile_GCC#processArgument(IParseContext, IPath, java.lang.String)}
	 */
	@Test
	public final void testProcessArgument_WS() {
		final String more = " -o CMakeFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
		ParseContext result = new ParseContext();
		String parsed;

		String name = "/ye olde/Ma cr os/fi le.inc";
		IPath cwd = new Path("");
		// -imacros'/ye olde/Ma cr os/fi le.inc'
		result = new ParseContext();
		assertEquals(8 + name.length() + 2, testee.processArgument(result, cwd, "-imacros" + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);
		// -imacros"/ye olde/Ma cr os/fi le.inc"
		result = new ParseContext();
		assertEquals(8 + name.length() + 2,
				testee.processArgument(result, cwd, "-imacros" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);

		// -imacros '/ye olde/Ma cr os/fi le.inc'
		result = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-imacros   " + "'" + name + "'" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);
		// -imacros "/ye olde/Ma cr os/fi le.inc"
		result = new ParseContext();
		assertEquals(8 + name.length() + 3 + 2,
				testee.processArgument(result, cwd, "-imacros   " + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);

		name = (new Path("A:an\\Ma cr os/fi le.inc")).toOSString();
		// -imacros'A:an\Ma cr os/fi le.inc'
		result = new ParseContext();
		assertEquals(8 + name.length() + 2,
				testee.processArgument(result, cwd, "-imacros" + "\"" + name + "\"" + more));
		assertEquals("#entries", 1, result.getMacroFiles().size());
		parsed = result.getMacroFiles().get(0);
		assertEquals("name", name, parsed);
	}
}
