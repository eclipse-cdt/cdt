/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.standardbuilder.core.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerInfoConsoleParser;

/**
 * Scanner configuration console parser tests
 * 
 * @author vhirsl
 */
public class ScannerConfigConsoleParserTests extends TestCase {
	
	private IScannerInfoConsoleParser clParser;

	public ScannerConfigConsoleParserTests(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		clParser = new GCCScannerInfoConsoleParser();			
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */

	protected void tearDown() throws Exception {
		super.tearDown();
		
		clParser.shutdown();
		clParser = null;
	}

	/*
	 * Tests GCCScannerInfoConsoleParser. Utility object not provided.
	 * Only tests parsing of the imput (make build output) 
	 */
	public void testParsingIncludePaths() {
		IScannerInfoCollector collector = new IScannerInfoCollector() {
            private List sumIncludes = new ArrayList();
            public void contributeToScannerConfig(Object resource, Map scannerInfo) {
                sumIncludes.addAll((List) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS));
            }
            public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
                if (type.equals(ScannerInfoTypes.INCLUDE_PATHS)) {
                    return sumIncludes;
                }
                return new ArrayList();
            }
        }; 
        // initialize it with the utility
		clParser.startup(null, null, collector, null);
		
		clParser.processLine("gcc -I /dir/include -I C:\\dir\\include -ID:/dir/include -c test.c");	// absolute paths
		clParser.processLine("gcc -I -I /dir2/include -c test.c");	// empty -I
		clParser.processLine("gcc -I../back_dir/include -I./cur_dir/include -c test.c"); // relative paths
		clParser.processLine("gcc '-I /squoted/dir1' -I '/squoted/dir2' -I'/squoted/dir3' -c test.c"); // single quote dirs
		clParser.processLine("gcc \"-I /dquoted/dir1\" -I \"/dquoted/dir2\" -I\"/dquoted/dir3\" -c test.c"); // double quote dirs
		clParser.processLine("gcc '-I /with spaces 1' -I'/with spaces 2' -c test.c"); // dirs with spaces 1,2
		clParser.processLine("gcc \"-I /with spaces 3\" -I \"/with spaces 4\" -c test.c"); // dirs with spaces 3,4
		clParser.processLine("gcc -I /with\\ spaces\\ 5 -c test.c"); // dirs with spaces 5
		clParser.processLine("gcc -I '\\\\server1\\include' '-I\\\\server2\\include' -I \"\\\\server3\\include\" -c test.c"); // UNC paths
		clParser.processLine("gcc -I //server4/include -I '//server5/include' '-I//server6/include' -c test.c"); // UNC paths
		clParser.processLine("gcc -I \\");
		clParser.processLine("/multiline\\");
		clParser.processLine("/dir -c test.c"); // multiline
		
		List sumIncludes = collector.getCollectedScannerInfo(null, ScannerInfoTypes.INCLUDE_PATHS);
        assertTrue(sumIncludes.contains("/dir/include"));
		assertTrue(sumIncludes.contains("C:\\dir\\include"));
		assertTrue(sumIncludes.contains("D:/dir/include"));
		assertTrue(sumIncludes.contains("/dir2/include"));
		assertTrue(sumIncludes.contains("../back_dir/include"));
		assertTrue(sumIncludes.contains("./cur_dir/include"));
		assertTrue(sumIncludes.contains("/squoted/dir1"));
		assertTrue(sumIncludes.contains("/squoted/dir2"));
		assertTrue(sumIncludes.contains("/squoted/dir3"));
		assertTrue(sumIncludes.contains("/dquoted/dir1"));
		assertTrue(sumIncludes.contains("/dquoted/dir2"));
		assertTrue(sumIncludes.contains("/dquoted/dir3"));
		assertTrue(sumIncludes.contains("/with spaces 1"));
		assertTrue(sumIncludes.contains("/with spaces 2"));
		assertTrue(sumIncludes.contains("/with spaces 3"));
		assertTrue(sumIncludes.contains("/with spaces 4"));
		assertTrue(sumIncludes.contains("/with spaces 5"));
		assertTrue(sumIncludes.contains("\\\\server1\\include"));
		assertTrue(sumIncludes.contains("\\\\server2\\include"));
		assertTrue(sumIncludes.contains("\\\\server3\\include"));
		assertTrue(sumIncludes.contains("//server4/include"));
		assertTrue(sumIncludes.contains("//server5/include"));
		assertTrue(sumIncludes.contains("//server6/include"));
		assertTrue(sumIncludes.contains("/multiline/dir"));
		assertTrue(sumIncludes.size() == 24);
	}
	
	public void testParsingSymbolDefinitions() {
        IScannerInfoCollector collector = new IScannerInfoCollector() {
            private List sumSymbols = new ArrayList();
            public void contributeToScannerConfig(Object resource, Map scannerInfo) {
                sumSymbols.addAll((List) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS));
            }
            public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
                if (type.equals(ScannerInfoTypes.SYMBOL_DEFINITIONS)) {
                    return sumSymbols;
                }
                return new ArrayList();
            }
        };
		// initialize it with the utility
        clParser.startup(null, null, collector, null);
		
		clParser.processLine("gcc -DMACRO1 -D MACRO2=value2 -c test.c");	// simple definitions
		clParser.processLine("gcc -D -DMACRO3 -c test.c");	// empty -D
		clParser.processLine("gcc -D MACRO4='value4' -D 'MACRO5=value5' '-D MACRO6 = value6' -c test.c");	// single quotes
		clParser.processLine("gcc -D'MACRO7=\"value 7\"' -D MACRO8='\"value 8\"' -c test.c");	// single quotes
		clParser.processLine("gcc -DMACRO9=\"value9\" -D \"MACRO10=value10\" \"-D MACRO11 = value11\" -c test.c");	// double quotes
		clParser.processLine("gcc -D\"MACRO12=\\\"value 12\\\"\" -D MACRO13=\"\\\"value 13\\\"\" -c test.c");	// single quotes
		clParser.processLine("gcc -D \\");
		clParser.processLine("MULTILINE=TRUE	\\");
		clParser.processLine("-c test.c"); // multiline
		clParser.processLine("gcc -D 'SUM(x, y) = (x) + (y)' -c test.c"); // more complex macro definition
		
		List sumSymbols = collector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
        assertTrue(sumSymbols.contains("MACRO1"));
		assertTrue(sumSymbols.contains("MACRO2=value2"));
		assertTrue(sumSymbols.contains("MACRO3"));
		assertTrue(sumSymbols.contains("MACRO4=value4"));
		assertTrue(sumSymbols.contains("MACRO5=value5"));
		assertTrue(sumSymbols.contains("MACRO6 = value6"));
		assertTrue(sumSymbols.contains("MACRO7=\"value 7\""));
		assertTrue(sumSymbols.contains("MACRO8=\"value 8\""));
		assertTrue(sumSymbols.contains("MACRO9=value9"));
		assertTrue(sumSymbols.contains("MACRO10=value10"));
		assertTrue(sumSymbols.contains("MACRO11 = value11"));
		assertTrue(sumSymbols.contains("MACRO12=\"value 12\""));
		assertTrue(sumSymbols.contains("MACRO13=\"value 13\""));
		assertTrue(sumSymbols.contains("MULTILINE=TRUE"));
		assertTrue(sumSymbols.contains("SUM(x, y) = (x) + (y)"));
		assertTrue(sumSymbols.size() == 15);
	}
	
	public void testParsingSymbolDefinitions_bug80271() {
        IScannerInfoCollector collector = new IScannerInfoCollector() {
            private List sumSymbols = new ArrayList();
            public void contributeToScannerConfig(Object resource, Map scannerInfo) {
                sumSymbols.addAll((List) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS));
            }
            public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
                if (type.equals(ScannerInfoTypes.SYMBOL_DEFINITIONS)) {
                    return sumSymbols;
                }
                return new ArrayList();
            }
        };
        // initialize it with the utility
        clParser.startup(null, null, collector, null);
		
		clParser.processLine("gcc -DMACRO1 -I ..\\inc -c ..\\source\\source.c");	// PR 80271

        List sumSymbols = collector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
		assertTrue(sumSymbols.contains("MACRO1"));
		assertTrue(sumSymbols.size() == 1);
	}
}
