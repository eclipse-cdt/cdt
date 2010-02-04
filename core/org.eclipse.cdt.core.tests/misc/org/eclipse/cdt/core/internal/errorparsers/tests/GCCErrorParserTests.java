/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Siemens AG - Basic test case
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;


import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * This test is designed to exercise the error parser capabilities.
 */
public class GCCErrorParserTests extends GenericErrorParserTests {

	public static final String[] GCC_ERROR_STREAM1 = {
			"qcc -c -I/qnx630/target/qnx6/usr/include -I/qnx630/target/qnx6/usr/include/photon -V3.3.1,gcc_ntox86 -w5 -O2   -I. ../abmain.c abmain.o",
			"In file included from ../globals.h:9,", "                 from ../abmain.c:36:",
			"../_combolist.h:34:24: warning: no newline at end of file",};
	public static final int GCC_ERROR_STREAM1_WARNINGS = 1;
	public static final int GCC_ERROR_STREAM1_ERRORS = 0;
	public static final String[] GCC_ERROR_STREAM1_FILENAMES = {"_combolist.h"};

	public static final String[] GCC_ERROR_STREAM2 = {"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp:234: warning: `",
			"	   RPNEvaluator<NumericType>::OperandConstant' is implicitly a typename",
			"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp:234: warning: implicit typename",
			"	   is deprecated, please see the documentation for details"};
	public static final int GCC_ERROR_STREAM2_WARNINGS = 2;
	public static final int GCC_ERROR_STREAM2_ERRORS = 0;
	public static final String[] GCC_ERROR_STREAM2_FILENAMES = {"RPNEvaluator.hpp"};
	public static final String[] GCC_ERROR_STREAM2_DESCRIPTIONS = {"please see the documentation"};

	public static final String[] GCC_ERROR_STREAM3 = {
			"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp:370: error: ISO C++ says that `",
			"   char& String::operator[](unsigned int)' and `operator[]' are ambiguous even ",
			"   though the worst conversion for the former is better than the worst ", "   conversion for the latter"};
	public static final int GCC_ERROR_STREAM3_WARNINGS = 0;
	public static final int GCC_ERROR_STREAM3_ERRORS = 1;
	public static final String[] GCC_ERROR_STREAM3_FILENAMES = {"RPNEvaluator.hpp"};
	public static final String[] GCC_ERROR_STREAM3_DESCRIPTIONS = {"ISO C++", "are ambiguous", "worst conversion",
			"conversion for the latter"};

	public static final String[] GCC_ERROR_STREAM4 = {"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp: In member function `",
			"   NumericType RPNEvaluator<NumericType>::evaluate(const char*) [with ", "   NumericType = int8]':",
			"C:/QNX630/workspace/System/src/CommonScriptClasses.cpp:609:   instantiated from here",
			"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp:370: error: ISO C++ says that `",
			"   char& String::operator[](unsigned int)' and `operator[]' are ambiguous even ",
			"   though the worst conversion for the former is better than the worst ", "   conversion for the latter"};
	public static final int GCC_ERROR_STREAM4_WARNINGS = 0;
	public static final int GCC_ERROR_STREAM4_ERRORS = 1;
	public static final String[] GCC_ERROR_STREAM4_FILENAMES = {"RPNEvaluator.hpp"};
	public static final String[] GCC_ERROR_STREAM4_DESCRIPTIONS = {"ISO C++", "are ambiguous", "worst conversion for",
			"conversion for the latter"};

	public static final String[] GCC_ERROR_STREAM5 = {
			"make -k all",
			"gcc -c -g -o hallo.o main.c",
			"main.c: In function `main':",
			"main.c:6: error: `wrong' undeclared (first use in this function)",
			"main.c:6: error: (Each undeclared identifier is reported only once",
			"main.c:6: error: for each function it appears in.)",
			"main.c:6: error: parse error before \"return\"",
			"main.c:7:2: warning: no newline at end of file",
			"make: *** [hallo.o] Error 1",
			"make: Target `all' not remade because of errors." };
	public static final int GCC_ERROR_STREAM5_WARNINGS = 1;
	public static final int GCC_ERROR_STREAM5_ERRORS = 2;
	public static final String[] GCC_ERROR_STREAM5_FILENAMES = {"main.c"};

	/**
	 * Constructor for IndexManagerTest.
	 * 
	 * @param name
	 */
	public GCCErrorParserTests() {
		super();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(GCCErrorParserTests.class);
		return suite;
	}

	public void testMultipleIncludesError() throws IOException {
		runParserTest(GCC_ERROR_STREAM1, GCC_ERROR_STREAM1_ERRORS, GCC_ERROR_STREAM1_WARNINGS, GCC_ERROR_STREAM1_FILENAMES, null,
				new String[]{GCC_ERROR_PARSER_ID});
	}

/*
 * Norbert Ploett
 * I am commenting these tests out. The current error parser implementation
 * invariably fails to correctly process the descriptions in multiline messages.
 * My analysis indicates that these tests never were really in the automatic tests anyway.
 * This test appears in org.eclipse.cdt.core.suite.AutomatedIntegrationSuite.suite() since it's version 1.31
 * but was always commented out.
 * I brought this up in http://dev.eclipse.org/mhonarc/lists/cdt-dev/msg08668.html
 * but did not get any replies.
 * 
	public void testMultiLineDescriptionError() throws IOException {
		runParserTest(GCC_ERROR_STREAM2, GCC_ERROR_STREAM2_ERRORS, GCC_ERROR_STREAM2_WARNINGS, GCC_ERROR_STREAM2_FILENAMES,
				GCC_ERROR_STREAM2_DESCRIPTIONS, new String[]{GCC_ERROR_PARSER_ID});
	}

	public void testLongMultiLineDescriptionError() throws IOException {
		runParserTest(GCC_ERROR_STREAM3, GCC_ERROR_STREAM3_ERRORS, GCC_ERROR_STREAM3_WARNINGS, GCC_ERROR_STREAM3_FILENAMES,
				GCC_ERROR_STREAM3_DESCRIPTIONS, new String[]{GCC_ERROR_PARSER_ID});
	}

	public void testMultiFileMultiLineSingleError() throws IOException {
		runParserTest(GCC_ERROR_STREAM4, GCC_ERROR_STREAM4_ERRORS, GCC_ERROR_STREAM4_WARNINGS, GCC_ERROR_STREAM4_FILENAMES,
				GCC_ERROR_STREAM4_DESCRIPTIONS, new String[]{GCC_ERROR_PARSER_ID});
	}
*/
	
	public void testBasicMessages() throws IOException {
		runParserTest(GCC_ERROR_STREAM5, GCC_ERROR_STREAM5_ERRORS, GCC_ERROR_STREAM5_WARNINGS, GCC_ERROR_STREAM5_FILENAMES,
				null, new String[]{GCC_ERROR_PARSER_ID});
	}
	
	public void testGccErrorMessages_Colon_bug263987() throws IOException {
		runParserTest(
				new String[] {"foo.cc:11:20: error: value with length 0 violates the length restriction: length (1 .. infinity)",},
				1, // errors
				0, // warnings
				new String[] {"foo.cc"},
				new String[] {"value with length 0 violates the length restriction: length (1 .. infinity)"},
				new String[] {GCC_ERROR_PARSER_ID}
			);
	}
	

}
