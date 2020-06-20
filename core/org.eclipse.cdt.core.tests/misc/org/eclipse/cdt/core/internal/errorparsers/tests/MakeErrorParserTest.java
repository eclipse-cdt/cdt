/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev (Quoin Inc.) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation, based on GCCErrorParserTests
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.io.IOException;

import org.eclipse.cdt.core.ErrorParserManager;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This test is designed to exercise the error parser capabilities for GNU make.
 */
public class MakeErrorParserTests extends GenericErrorParserTests {

	private static final String[] GMAKE_ERROR_STREAM0 = {
			// Infos
			"make: [Hello.o] Error 1 (ignored)", "make[2]: [all] Error 2 (ignored)",
			// Warnings
			"make: [Hello.o] Error 1", "make: Circular .folder/file.h <- .folder/file2.h dependency dropped.",
			"make[1]: Circular folder/file.h <- Makefile dependency dropped.",
			// Errors
			"make: *** [Hello.o] Error 1", "make[3]: *** [Hello.o] Error 1",
			"make: *** No rule to make target `one', needed by `all'.  Stop.",
			"make: *** No rule to make target `all'.  Stop.", "make: *** missing.mk: No such file or directory.  Stop.",
			"make: Target `all' not remade because of errors.",
			// Ignored
			"make[3]: Nothing to be done for `all'.", "make[2]: `all' is up to date.", };
	private static final int GMAKE_ERROR_STREAM0_INFOS = 2;
	private static final int GMAKE_ERROR_STREAM0_WARNINGS = 3;
	private static final int GMAKE_ERROR_STREAM0_ERRORS = 6;

	private static final String[] GMAKE_ERROR_STREAM1 = {
			// Warning
			"GNUmakefile:12: warning: overriding commands for target `target'",
			"Makefile1:10: include.mk: No such file or directory",
			// Errors
			"Makefile2:10: *** missing separator.  Stop.",
			"Makefile3:10: *** missing separator (did you mean TAB instead of 8 spaces?).  Stop.",
			"Makefile4:10: *** commands commence before first target. Stop.",
			"Makefile5:10: *** Recursive variable 'VAR' references itself (eventually). Stop.",
			"Makefile6:10: *** target pattern contains no `%'.  Stop.",
			// Ignored. Do not intercept compiler warnings
			"mytest.cpp:19: warning: unused variable 'i'", "hello.c:14:17: error: foo.h: No such file or directory", };
	private static final int GMAKE_ERROR_STREAM1_WARNINGS = 2;
	private static final int GMAKE_ERROR_STREAM1_ERRORS = 5;
	private static final String[] GMAKE_ERROR_STREAM1_FILENAMES = { "GNUmakefile", "Makefile1", "Makefile2",
			"Makefile3", "Makefile4", "Makefile5", "Makefile6" };

	private static final String[] GMAKE_ERROR_STREAM2 = {
			// Errors
			"gmake[3]: *** [Hello.o] Error 1", "make-381.exe: *** [Hello.o] Error 1",
			"gmake381: Target `all' not remade because of errors.", };
	private static final int GMAKE_ERROR_STREAM2_WARNINGS = 0;
	private static final int GMAKE_ERROR_STREAM2_ERRORS = 3;

	public MakeErrorParserTests() {
		super();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(MakeErrorParserTests.class);
		return suite;
	}

	public void testGmakeSanity() throws Exception {
		assertNotNull(ErrorParserManager.getErrorParserCopy(GMAKE_ERROR_PARSER_ID));
	}

	public void testGmakeMessages0() throws IOException {
		runParserTest(GMAKE_ERROR_STREAM0, GMAKE_ERROR_STREAM0_ERRORS, GMAKE_ERROR_STREAM0_WARNINGS,
				GMAKE_ERROR_STREAM0_INFOS, null, null, new String[] { GMAKE_ERROR_PARSER_ID });
	}

	public void testGMakeMessages1() throws IOException {
		runParserTest(GMAKE_ERROR_STREAM1, GMAKE_ERROR_STREAM1_ERRORS, GMAKE_ERROR_STREAM1_WARNINGS,
				GMAKE_ERROR_STREAM1_FILENAMES, null, new String[] { GMAKE_ERROR_PARSER_ID });
	}

	public void testGmakeMessages2() throws IOException {
		runParserTest(GMAKE_ERROR_STREAM2, GMAKE_ERROR_STREAM2_ERRORS, GMAKE_ERROR_STREAM2_WARNINGS, null, null,
				new String[] { GMAKE_ERROR_PARSER_ID });
	}
}
