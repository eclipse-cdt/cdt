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

	// old style: no colons before sections
	public static final String[] GLD_ERROR_STREAM0 = {
		"make -k all",
		"gcc -o hallo.o main.c libfoo.a",
		"main.c(.text+0x14): undefined reference to `foo()'",
		"main.o(.rodata+0x14): undefined reference to `something'",
		"make: Target `all' not remade because of errors." };
	public static final int GLD_ERROR_STREAM0_WARNINGS = 0;
	public static final int GLD_ERROR_STREAM0_ERRORS = 2;
	public static final String[] GLD_ERROR_STREAM0_FILENAMES = {"main.c","main.o"};
	
	// new style: colons before sections
	public static final String[] GLD_ERROR_STREAM1 = {
		"make -k all",
		"gcc -o hallo.o main.c libfoo.a",
		"main.c:(.text+0x14): undefined reference to `foo()'",
		"main.o:(.rodata+0x14): undefined reference to `something'",
		"make: Target `all' not remade because of errors." };
	public static final int GLD_ERROR_STREAM1_WARNINGS = 0;
	public static final int GLD_ERROR_STREAM1_ERRORS = 2;
	public static final String[] GLD_ERROR_STREAM1_FILENAMES = {"main.c","main.o"};

	public static final String[] GLD_ERROR_STREAM2 = {
		"make -k all",
		"gcc -o hallo.o main.c libfoo.a",
		"libfoo.a(foo.o): In function `foo':",
		"foo.c:(.text+0x7): undefined reference to `bar'",
		"make: Target `all' not remade because of errors." };
	public static final int GLD_ERROR_STREAM2_WARNINGS = 0;
	public static final int GLD_ERROR_STREAM2_ERRORS = 1;
	public static final String[] GLD_ERROR_STREAM2_FILENAMES = {"foo.c"};


	public GLDErrorParserTests() {
		super();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(GLDErrorParserTests.class);
		return suite;
	}

	public void testLinkerMessages0() throws IOException {
		runParserTest(GLD_ERROR_STREAM0, GLD_ERROR_STREAM0_ERRORS, GLD_ERROR_STREAM0_WARNINGS, GLD_ERROR_STREAM0_FILENAMES,
				null, new String[]{GLD_ERROR_PARSER_ID});
	}
	public void testLinkerMessages1() throws IOException {
		runParserTest(GLD_ERROR_STREAM1, GLD_ERROR_STREAM1_ERRORS, GLD_ERROR_STREAM1_WARNINGS, GLD_ERROR_STREAM1_FILENAMES,
				null, new String[]{GLD_ERROR_PARSER_ID});
	}
	public void testLinkerMessages2() throws IOException {
		runParserTest(GLD_ERROR_STREAM2, GLD_ERROR_STREAM2_ERRORS, GLD_ERROR_STREAM2_WARNINGS, GLD_ERROR_STREAM2_FILENAMES,
				null, new String[]{GLD_ERROR_PARSER_ID});
	}
}
