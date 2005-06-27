/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.io.StringBufferInputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * This test is designed to exercise the error parser capabilities.
 */
public class GCCErrorParserTests extends GenericErrorParserTests {
	public static final String GCC_ERROR_PARSER_ID = "org.eclipse.cdt.core.GCCErrorParser";	
	public static final String [] GCC_ERROR_STREAM1 = {
"qcc -c -I/qnx630/target/qnx6/usr/include -I/qnx630/target/qnx6/usr/include/photon -V3.3.1,gcc_ntox86 -w5 -O2   -I. ../abmain.c abmain.o", 
"In file included from ../globals.h:9,",
"                 from ../abmain.c:36:",
"../_combolist.h:34:24: warning: no newline at end of file",
};
	public static final int GCC_ERROR_STREAM1_WARNINGS = 1;
	public static final int GCC_ERROR_STREAM1_ERRORS = 0;
	public static final String [] GCC_ERROR_STREAM1_FILENAMES = { "_combolist.h" };

	public static final String [] GCC_ERROR_STREAM2 = {
"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp:234: warning: `",
"	   RPNEvaluator<NumericType>::OperandConstant' is implicitly a typename",
"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp:234: warning: implicit typename", 
"	   is deprecated, please see the documentation for details"
};
	public static final int GCC_ERROR_STREAM2_WARNINGS = 2;
	public static final int GCC_ERROR_STREAM2_ERRORS = 0;
	public static final String [] GCC_ERROR_STREAM2_FILENAMES = { "RPNEvaluator.hpp" };
	public static final String [] GCC_ERROR_STREAM2_DESCRIPTIONS = { "please see the documentation" };
	
	public static final String [] GCC_ERROR_STREAM3 = {
"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp:370: error: ISO C++ says that `",
"   char& String::operator[](unsigned int)' and `operator[]' are ambiguous even ", 
"   though the worst conversion for the former is better than the worst ",
"   conversion for the latter"
};
	public static final int GCC_ERROR_STREAM3_WARNINGS = 0;
	public static final int GCC_ERROR_STREAM3_ERRORS = 1;
	public static final String [] GCC_ERROR_STREAM3_FILENAMES = { "RPNEvaluator.hpp" };
	public static final String [] GCC_ERROR_STREAM3_DESCRIPTIONS = { "ISO C++", "are ambiguous", "worst conversion", "conversion for the latter" };

	public static final String [] GCC_ERROR_STREAM4 = {
"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp: In member function `",
"   NumericType RPNEvaluator<NumericType>::evaluate(const char*) [with ",
"   NumericType = int8]':",
"C:/QNX630/workspace/System/src/CommonScriptClasses.cpp:609:   instantiated from here",
"C:/QNX630/workspace/System/inc/RPNEvaluator.hpp:370: error: ISO C++ says that `",
"   char& String::operator[](unsigned int)' and `operator[]' are ambiguous even ",
"   though the worst conversion for the former is better than the worst ",
"   conversion for the latter"
};
	public static final int GCC_ERROR_STREAM4_WARNINGS = 0;
	public static final int GCC_ERROR_STREAM4_ERRORS = 1;
	public static final String [] GCC_ERROR_STREAM4_FILENAMES = { "RPNEvaluator.hpp" };
	public static final String [] GCC_ERROR_STREAM4_DESCRIPTIONS = { "ISO C++", "are ambiguous", "worst conversion for", "conversion for the latter" };
		
	/**
	 * Constructor for IndexManagerTest.
	 * @param name
	 */
	public GCCErrorParserTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(GCCErrorParserTests.class);
		return suite;
	}
	
	public void testMultipleIncludesError() {
		runParserTest(GCC_ERROR_STREAM1,
		              GCC_ERROR_STREAM1_ERRORS,
		              GCC_ERROR_STREAM1_WARNINGS,
		              GCC_ERROR_STREAM1_FILENAMES,
		              null);
	}

	public void testMultiLineDescriptionError() {
		runParserTest(GCC_ERROR_STREAM2,
					  GCC_ERROR_STREAM2_ERRORS,
					  GCC_ERROR_STREAM2_WARNINGS,
					  GCC_ERROR_STREAM2_FILENAMES,
					  GCC_ERROR_STREAM2_DESCRIPTIONS);		
	}

	public void testLongMultiLineDescriptionError() {
		runParserTest(GCC_ERROR_STREAM3,
					  GCC_ERROR_STREAM3_ERRORS,
					  GCC_ERROR_STREAM3_WARNINGS,
					  GCC_ERROR_STREAM3_FILENAMES,
					  GCC_ERROR_STREAM3_DESCRIPTIONS);		
	}

	public void testMultiFileMultiLineSingleError() {
		runParserTest(GCC_ERROR_STREAM4,
					  GCC_ERROR_STREAM4_ERRORS,
					  GCC_ERROR_STREAM4_WARNINGS,
					  GCC_ERROR_STREAM4_FILENAMES,
					  GCC_ERROR_STREAM4_DESCRIPTIONS);		
	}

	private void runParserTest(String [] dataStream, 
	                           int expectedErrorCount, 
	                           int expectedWarningCount, 
	                           String [] expectedFileNames,
	                           String [] expectedDescriptions ) {
		String [] parserID = { GCC_ERROR_PARSER_ID	};
		CountingMarkerGenerator markerGenerator = new CountingMarkerGenerator();

		IProject project = getTempProject();
		assertNotNull(project);
		
		ErrorParserManager manager;
		manager = new ImaginaryFilesErrorParserManager(project, markerGenerator, parserID);
		
		String errorStream = makeStringFromArray(dataStream, "\n");
		StringBufferInputStream inputStream = new StringBufferInputStream(errorStream);
		assertNotNull(inputStream);
		
		try {
			transferInputStreamToOutputStream(inputStream, manager.getOutputStream(), 1024);
		} catch(Exception ex) {
			assertTrue(false);
		} finally {
			try {
				manager.close();
			} catch(Exception ex) {
				/* Ignore */
			}
		}
		manager.reportProblems();
		
		assertEquals(expectedErrorCount, markerGenerator.numErrors);
		assertEquals(expectedWarningCount, markerGenerator.numWarnings);
		assertEquals(expectedFileNames.length, markerGenerator.uniqFiles.size());
		for(int i= 0; i < expectedFileNames.length; i++) {
			IPath path = ((IFile)markerGenerator.uniqFiles.get(i)).getLocation();
			assertEquals(expectedFileNames[i], path.lastSegment());			
		}
		
		if(expectedDescriptions != null) {
			assertNotNull(markerGenerator.lastDescription);
			for(int i = 0; i < expectedDescriptions.length; i++) {
				assertTrue(markerGenerator.lastDescription.matches(expectedDescriptions[i]));
			}
		}
	}
}
