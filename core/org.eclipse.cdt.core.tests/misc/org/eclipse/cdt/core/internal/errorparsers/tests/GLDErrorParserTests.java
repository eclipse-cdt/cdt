/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ed Swartz (Nokia) - initial API and implementation, based on GCCErrorParserTests
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * This test is designed to exercise the error parser capabilities for GNU ld.
 */
public class GLDErrorParserTests extends GenericErrorParserTests {

	@Test
	public void testLinkerMessages0() throws IOException {
		runParserTest(
				// old style: no colons before sections
				new String[] { "make -k all", "gcc -o hallo.o main.c libfoo.a",
						"main.c(.text+0x14): undefined reference to `foo()'",
						"main.o(.rodata+0x14): undefined reference to `something'",
						"(.text.myfunc+0x42): undefined reference to `bar'",
						"make: Target `all' not remade because of errors.", },
				3, // errors
				0, // warnings
				0, // Infos
				new String[] { "main.c", "main.o", "project" }, new String[] { "undefined reference to `foo()'",
						"undefined reference to `something'", "undefined reference to `bar'" },
				new String[] { GLD_ERROR_PARSER_ID });
	}

	@Test
	public void testLinkerMessages1() throws IOException {
		runParserTest(
				// new style: colons before sections
				new String[] { "make -k all", "gcc -o hallo.o main.c libfoo.a",
						"main.c:(.text+0x14): undefined reference to `foo()'",
						"main.o:(.rodata+0x14): undefined reference to `something'",
						"(.text.myfunc+0x42): undefined reference to `bar'",
						"make: Target `all' not remade because of errors.", },
				3, // errors
				0, // warnings
				0, // Infos
				new String[] { "main.c", "main.o", "project" }, new String[] { "undefined reference to `foo()'",
						"undefined reference to `something'", "undefined reference to `bar'" },
				new String[] { GLD_ERROR_PARSER_ID });
	}

	@Test
	public void testLinkerMessages2() throws IOException {
		runParserTest(
				new String[] { "make -k all", "gcc -o hallo.o main.c libfoo.a", "libfoo.a(foo.o): In function `foo':",
						"foo.c:(.text+0x7): undefined reference to `bar'",
						"make: Target `all' not remade because of errors.", },
				1, // errors
				0, // warnings
				0, // Infos
				new String[] { "foo.c" }, new String[] { "undefined reference to `bar'" },
				new String[] { GLD_ERROR_PARSER_ID });
	}

	@Test
	public void testLinkerMessages_DangerousFunction_bug248669() throws IOException {
		runParserTest(new String[] { "mktemp.o(.text+0x19): In function 'main':",
				"mktemp.c:15: the use of 'mktemp' is dangerous, better use 'mkstemp'", "1.o: In function `main':",
				"1.c:(.text+0x19): warning: the use of `mktemp' is dangerous, better use `mkstemp'", }, 0, // errors
				2, // warnings
				0, // Infos
				new String[] { "1.c", "mktemp.c" },
				new String[] { "the use of 'mktemp' is dangerous, better use 'mkstemp'",
						"the use of `mktemp' is dangerous, better use `mkstemp'", },
				new String[] { GLD_ERROR_PARSER_ID });
	}

	@Test
	public void testLinkerMessages_PrecedingPath_bug314253() throws IOException {
		runParserTest(new String[] {
				"ld: warning: libstdc++.so.5, needed by testlib_1.so, may conflict with libstdc++.so.6",
				"/usr/bin/ld: warning: libstdc++.so.5, needed by testlib_2.so, may conflict with libstdc++.so.6",
				"C:\\bin\\ld.exe: warning: libstdc++.so.5, needed by testlib_3.so, may conflict with libstdc++.so.6",
				"c:/bin/ld.exe: warning: libstdc++.so.5, needed by testlib_4.so, may conflict with libstdc++.so.6",
				"D:\\mingw\\bin\\..\\lib\\gcc-lib\\mingw32\\3.2.3\\..\\..\\..\\..\\mingw32\\bin\\ld.exe: cannot find -ljpeg",
				"notld: warning: ld error parser has no business parsing this message", }, 1, // errors
				4, // warnings
				0, // Infos
				null,
				new String[] { "libstdc++.so.5, needed by testlib_1.so, may conflict with libstdc++.so.6",
						"libstdc++.so.5, needed by testlib_2.so, may conflict with libstdc++.so.6",
						"libstdc++.so.5, needed by testlib_3.so, may conflict with libstdc++.so.6",
						"libstdc++.so.5, needed by testlib_4.so, may conflict with libstdc++.so.6",
						"cannot find -ljpeg", },
				new String[] { GLD_ERROR_PARSER_ID });
	}

	@Test
	public void testLinkerMessages_bug495661() throws IOException {
		runParserTest(
				// new style: colons before sections
				new String[] { "make all ",
				// @formatter:off
					"Building file: ../src/a.cpp",
					"Invoking: GCC C++ Compiler",
					"g++ -std=c++0x -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF\"src/a.d\" -MT\"src/a.o\" -o \"src/a.o\" \"../src/a.cpp\"",
					"Finished building: ../src/a.cpp",
					" ",
					"Building target: parser",
					"Invoking: GCC C++ Linker",
					"g++ -o \"a\"  ./src/a.o   ",
					"/usr/lib/gcc/x86_64-pc-linux-gnu/9.2.0/../../../../x86_64-pc-linux-gnu/bin/ld: ./src/a.o: in function `TLS wrapper function for A::max_compatdb_time':",
					"a.cpp:(.text._ZTWN1A17max_compatdb_timeE[_ZTWN1A17max_compatdb_timeE]+0x21): undefined reference to `A::max_compatdb_time'",
					"collect2: error: ld returned 1 exit status",
					"make: *** [makefile:47: parser] Error 1",
					"\"make all\" terminated with exit code 2. Build might be incomplete.", },
				// @formatter:on
				1, // errors
				0, // warnings
				0, // Infos
				new String[] { "a.cpp" }, new String[] { "undefined reference to `A::max_compatdb_time'" },
				new String[] { GLD_ERROR_PARSER_ID });
	}

	@Test
	public void testLinkerMessages_stubbed_function() throws IOException {
		runParserTest(new String[] { "make all ",
				// @formatter:off
				"Building target: f4.elf",
				"Invoking: Cross GCC Linker",
				"arm-none-eabi-gcc -mthumb -mcpu=cortex-m4 --specs=nosys.specs -o \"f4.elf\"  ./Src/main.o",
				"c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/../../../../arm-none-eabi/bin/ld.exe: c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/thumb/v7e-m/nofp/libc.a(libc_a-closer.o): in function `_close_r':",
				"/data/jenkins/workspace/GNU-toolchain/arm-11/src/newlib-cygwin/newlib/libc/reent/closer.c:47: warning: _close is not implemented and will always fail",
				"c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/../../../../arm-none-eabi/bin/ld.exe: c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/thumb/v7e-m/nofp/libc.a(libc_a-fstatr.o): in function `_fstat_r':",
				"/data/jenkins/workspace/GNU-toolchain/arm-11/src/newlib-cygwin/newlib/libc/reent/fstatr.c:55: warning: _fstat is not implemented and will always fail",
				"c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/../../../../arm-none-eabi/bin/ld.exe: c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/thumb/v7e-m/nofp/libc.a(libc_a-signalr.o): in function `_getpid_r':",
				"/data/jenkins/workspace/GNU-toolchain/arm-11/src/newlib-cygwin/newlib/libc/reent/signalr.c:83: warning: _getpid is not implemented and will always fail",
				"c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/../../../../arm-none-eabi/bin/ld.exe: c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/thumb/v7e-m/nofp/libc.a(libc_a-isattyr.o): in function `_isatty_r':",
				"/data/jenkins/workspace/GNU-toolchain/arm-11/src/newlib-cygwin/newlib/libc/reent/isattyr.c:52: warning: _isatty is not implemented and will always fail",
				"c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/../../../../arm-none-eabi/bin/ld.exe: c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/thumb/v7e-m/nofp/libc.a(libc_a-signalr.o): in function `_kill_r':",
				"/data/jenkins/workspace/GNU-toolchain/arm-11/src/newlib-cygwin/newlib/libc/reent/signalr.c:53: warning: _kill is not implemented and will always fail",
				"c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/../../../../arm-none-eabi/bin/ld.exe: c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/thumb/v7e-m/nofp/libc.a(libc_a-lseekr.o): in function `_lseek_r':",
				"/data/jenkins/workspace/GNU-toolchain/arm-11/src/newlib-cygwin/newlib/libc/reent/lseekr.c:49: warning: _lseek is not implemented and will always fail",
				"c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/../../../../arm-none-eabi/bin/ld.exe: c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/thumb/v7e-m/nofp/libc.a(libc_a-readr.o): in function `_read_r':",
				"/data/jenkins/workspace/GNU-toolchain/arm-11/src/newlib-cygwin/newlib/libc/reent/readr.c:49: warning: _read is not implemented and will always fail",
				"c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/../../../../arm-none-eabi/bin/ld.exe: c:/arm-11.3.rel1/bin/../lib/gcc/arm-none-eabi/11.3.1/thumb/v7e-m/nofp/libc.a(libc_a-writer.o): in function `_write_r':",
				"/data/jenkins/workspace/GNU-toolchain/arm-11/src/newlib-cygwin/newlib/libc/reent/writer.c:49: warning: _write is not implemented and will always fail",
				"Finished building target: f4.elf",
				" ",
				"",
				"14:37:57 Build Failed. 8 errors, 8 warnings. (took 1s.244ms)"},
				// @formatter:on
				0, // errors
				8, // warnings
				0, // Infos
				null, new String[] { /* Don't really know what to add here */ },
				new String[] { GLD_ERROR_PARSER_ID, GCC_ERROR_PARSER_ID });

	}
}
