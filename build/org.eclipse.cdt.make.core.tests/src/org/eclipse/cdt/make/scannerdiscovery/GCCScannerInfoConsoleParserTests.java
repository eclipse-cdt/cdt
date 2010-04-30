/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.scannerdiscovery;

import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerInfoConsoleParser;

/**
 * Scanner configuration console parser tests
 * 
 * @author vhirsl
 */
public class GCCScannerInfoConsoleParserTests extends BaseBOPConsoleParserTests {
	
	public static TestSuite suite() {
		return suite(GCCScannerInfoConsoleParserTests.class);
	}

	public GCCScannerInfoConsoleParserTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fOutputParser= new GCCScannerInfoConsoleParser();
		fOutputParser.startup(null, null, fCollector, null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		fOutputParser.shutdown();
	}
	
	/*
	 * Tests GCCScannerInfoConsoleParser. Utility object not provided.
	 * Only tests parsing of the input (make build output) 
	 */
	public void testParsingIncludePaths() {
		fOutputParser.processLine("gcc -I /dir/include -I c:\\dir\\include -ID:/dir/include -c test.c");	// absolute paths //$NON-NLS-1$
		fOutputParser.processLine("gcc -I -I /dir2/include -c test.c");	// empty -I //$NON-NLS-1$
		fOutputParser.processLine("gcc -I../back_dir/include -I./cur_dir/include -c test.c"); // relative paths //$NON-NLS-1$
		fOutputParser.processLine("gcc '-I /squoted/dir1' -I '/squoted/dir2' -I'/squoted/dir3' -c test.c"); // single quote dirs //$NON-NLS-1$
		fOutputParser.processLine("gcc \"-I /dquoted/dir1\" -I \"/dquoted/dir2\" -I\"/dquoted/dir3\" -c test.c"); // double quote dirs //$NON-NLS-1$
		fOutputParser.processLine("gcc '-I /with spaces 1' -I'/with spaces 2' -c test.c"); // dirs with spaces 1,2 //$NON-NLS-1$
		fOutputParser.processLine("gcc \"-I /with spaces 3\" -I \"/with spaces 4\" -c test.c"); // dirs with spaces 3,4 //$NON-NLS-1$
		fOutputParser.processLine("gcc -I /with\\ spaces\\ 5 -c test.c"); // dirs with spaces 5 //$NON-NLS-1$
		fOutputParser.processLine("gcc -I '\\\\server1\\include' '-I\\\\server2\\include' -I \"\\\\\\\\server3\\\\include\" -c test.c"); // UNC paths //$NON-NLS-1$
		fOutputParser.processLine("gcc -I //server4/include -I '//server5/include' '-I//server6/include' -c test.c"); // UNC paths //$NON-NLS-1$
		fOutputParser.processLine("gcc -I \\"); //$NON-NLS-1$
		fOutputParser.processLine("/multiline\\"); //$NON-NLS-1$
		fOutputParser.processLine("/dir -c test.c"); // multiline //$NON-NLS-1$
		fOutputParser.processLine("gcc -Imultiline2 \\"); //$NON-NLS-1$
		fOutputParser.processLine("-Imultiline3\\"); //$NON-NLS-1$
		fOutputParser.processLine(" -DAA=\"BB\" test.c"); //$NON-NLS-1$
		
		@SuppressWarnings("unchecked")
		List<String> sumIncludes = fCollector.getCollectedScannerInfo(null, ScannerInfoTypes.INCLUDE_PATHS);
		assertTrue(sumIncludes.contains("/dir/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("c:\\dir\\include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("D:/dir/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/dir2/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("../back_dir/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("./cur_dir/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/squoted/dir1")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/squoted/dir2")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/squoted/dir3")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/dquoted/dir1")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/dquoted/dir2")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/dquoted/dir3")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/with spaces 1")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/with spaces 2")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/with spaces 3")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/with spaces 4")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/with spaces 5")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("\\\\server1\\include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("\\\\server2\\include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("\\\\server3\\include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("//server4/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("//server5/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("//server6/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("/multiline/dir")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("multiline2")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("multiline3")); //$NON-NLS-1$
		assertTrue(sumIncludes.size() == 26);
	}
}
