/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ed Swartz (Nokia) - initial API and implementation, based on GCCErrorParserTests
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;


import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * This test is designed to exercise the error parser capabilities for GNU ld.
 */
public class GLDErrorParserTests extends GenericErrorParserTests {

	public GLDErrorParserTests() {
		super();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(GLDErrorParserTests.class);
		return suite;
	}

	public void testLinkerMessages0() throws IOException {
		runParserTest(
				// old style: no colons before sections
				new String[] {
						"make -k all",
						"gcc -o hallo.o main.c libfoo.a",
						"main.c(.text+0x14): undefined reference to `foo()'",
						"main.o(.rodata+0x14): undefined reference to `something'",
						"make: Target `all' not remade because of errors.",
				},
				2, // errors
				0, // warnings
				0, // Infos
				new String[] {"main.c","main.o"},
				new String[] {
						"undefined reference to `foo()'",
						"undefined reference to `something'"
					},
				new String[]{GLD_ERROR_PARSER_ID}
			);
	}
	
	public void testLinkerMessages1() throws IOException {
		runParserTest(
				// new style: colons before sections
				new String[] {
						"make -k all",
						"gcc -o hallo.o main.c libfoo.a",
						"main.c:(.text+0x14): undefined reference to `foo()'",
						"main.o:(.rodata+0x14): undefined reference to `something'",
						"make: Target `all' not remade because of errors.",
				},
				2, // errors
				0, // warnings
				0, // Infos
				new String[] {"main.c","main.o"},
				new String[] {
						"undefined reference to `foo()'",
						"undefined reference to `something'"
					},
				new String[]{GLD_ERROR_PARSER_ID}
			);
	}

	public void testLinkerMessages2() throws IOException {
		runParserTest(
				new String[] {
						"make -k all",
						"gcc -o hallo.o main.c libfoo.a",
						"libfoo.a(foo.o): In function `foo':",
						"foo.c:(.text+0x7): undefined reference to `bar'",
						"make: Target `all' not remade because of errors.",
				},
				1, // errors
				0, // warnings
				0, // Infos
				new String[] {"foo.c"},
				new String[] {"undefined reference to `bar'"},
				new String[] {GLD_ERROR_PARSER_ID}
		);
	}

	public void testLinkerMessages_DangerousFunction_bug248669() throws IOException {
		runParserTest(
				new String[] {
						"mktemp.o(.text+0x19): In function 'main':",
						"mktemp.c:15: the use of 'mktemp' is dangerous, better use 'mkstemp'",
						"1.o: In function `main':",
						"1.c:(.text+0x19): warning: the use of `mktemp' is dangerous, better use `mkstemp'",
				},
				0, // errors
				2, // warnings
				0, // Infos
				new String[] {"1.c", "mktemp.c"},
				new String[] {
						"the use of 'mktemp' is dangerous, better use 'mkstemp'",
						"the use of `mktemp' is dangerous, better use `mkstemp'",
					},
				new String[] {GLD_ERROR_PARSER_ID}
		);
	}
	
	public void testLinkerMessages_PrecedingPath_bug314253() throws IOException {
		runParserTest(
				new String[] {
						"ld: warning: libstdc++.so.5, needed by testlib_1.so, may conflict with libstdc++.so.6",
						"/usr/bin/ld: warning: libstdc++.so.5, needed by testlib_2.so, may conflict with libstdc++.so.6",
						"C:\\bin\\ld.exe: warning: libstdc++.so.5, needed by testlib_3.so, may conflict with libstdc++.so.6",
						"c:/bin/ld.exe: warning: libstdc++.so.5, needed by testlib_4.so, may conflict with libstdc++.so.6",
						"D:\\mingw\\bin\\..\\lib\\gcc-lib\\mingw32\\3.2.3\\..\\..\\..\\..\\mingw32\\bin\\ld.exe: cannot find -ljpeg",
						"notld: warning: ld error parser has no business parsing this message",
				},
				1, // errors
				4, // warnings
				0, // Infos
				null,
				new String[] {
						"libstdc++.so.5, needed by testlib_1.so, may conflict with libstdc++.so.6",
						"libstdc++.so.5, needed by testlib_2.so, may conflict with libstdc++.so.6",
						"libstdc++.so.5, needed by testlib_3.so, may conflict with libstdc++.so.6",
						"libstdc++.so.5, needed by testlib_4.so, may conflict with libstdc++.so.6",
						"cannot find -ljpeg",
					},
				new String[] {GLD_ERROR_PARSER_ID}
		);
	}
}
