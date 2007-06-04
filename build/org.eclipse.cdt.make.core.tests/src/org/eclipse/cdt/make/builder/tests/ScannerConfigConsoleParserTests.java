/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.builder.tests;

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
		
		clParser.processLine("gcc -I /dir/include -I C:\\dir\\include -ID:/dir/include -c test.c");	// absolute paths //$NON-NLS-1$
		clParser.processLine("gcc -I -I /dir2/include -c test.c");	// empty -I //$NON-NLS-1$
		clParser.processLine("gcc -I../back_dir/include -I./cur_dir/include -c test.c"); // relative paths //$NON-NLS-1$
		clParser.processLine("gcc '-I /squoted/dir1' -I '/squoted/dir2' -I'/squoted/dir3' -c test.c"); // single quote dirs //$NON-NLS-1$
		clParser.processLine("gcc \"-I /dquoted/dir1\" -I \"/dquoted/dir2\" -I\"/dquoted/dir3\" -c test.c"); // double quote dirs //$NON-NLS-1$
		clParser.processLine("gcc '-I /with spaces 1' -I'/with spaces 2' -c test.c"); // dirs with spaces 1,2 //$NON-NLS-1$
		clParser.processLine("gcc \"-I /with spaces 3\" -I \"/with spaces 4\" -c test.c"); // dirs with spaces 3,4 //$NON-NLS-1$
		clParser.processLine("gcc -I /with\\ spaces\\ 5 -c test.c"); // dirs with spaces 5 //$NON-NLS-1$
		clParser.processLine("gcc -I '\\\\server1\\include' '-I\\\\server2\\include' -I \"\\\\server3\\include\" -c test.c"); // UNC paths //$NON-NLS-1$
		clParser.processLine("gcc -I //server4/include -I '//server5/include' '-I//server6/include' -c test.c"); // UNC paths //$NON-NLS-1$
		clParser.processLine("gcc -I \\"); //$NON-NLS-1$
		clParser.processLine("/multiline\\"); //$NON-NLS-1$
		clParser.processLine("/dir -c test.c"); // multiline //$NON-NLS-1$
		
		List sumIncludes = collector.getCollectedScannerInfo(null, ScannerInfoTypes.INCLUDE_PATHS);
        assertTrue(sumIncludes.contains("/dir/include")); //$NON-NLS-1$
		assertTrue(sumIncludes.contains("C:\\dir\\include")); //$NON-NLS-1$
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
		
		clParser.processLine("gcc -DMACRO1 -D MACRO2=value2 -c test.c");	// simple definitions //$NON-NLS-1$
		clParser.processLine("gcc -D -DMACRO3 -c test.c");	// empty -D //$NON-NLS-1$
		clParser.processLine("gcc -D MACRO4='value4' -D 'MACRO5=value5' '-D MACRO6 = value6' -c test.c");	// single quotes //$NON-NLS-1$
		clParser.processLine("gcc -D'MACRO7=\"value 7\"' -D MACRO8='\"value 8\"' -c test.c");	// single quotes //$NON-NLS-1$
		clParser.processLine("gcc -DMACRO9=\"value9\" -D \"MACRO10=value10\" \"-D MACRO11 = value11\" -c test.c");	// double quotes //$NON-NLS-1$
		clParser.processLine("gcc -D\"MACRO12=\\\"value 12\\\"\" -D MACRO13=\"\\\"value 13\\\"\" -c test.c");	// single quotes //$NON-NLS-1$
		clParser.processLine("gcc -D \\"); //$NON-NLS-1$
		clParser.processLine("MULTILINE=TRUE	\\"); //$NON-NLS-1$
		clParser.processLine("-c test.c"); // multiline //$NON-NLS-1$
		clParser.processLine("gcc -D 'SUM(x, y) = (x) + (y)' -c test.c"); // more complex macro definition //$NON-NLS-1$
		
		List sumSymbols = collector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
        assertTrue(sumSymbols.contains("MACRO1")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO2=value2")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO3")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO4=value4")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO5=value5")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO6 = value6")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO7=\"value 7\"")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO8=\"value 8\"")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO9=value9")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO10=value10")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO11 = value11")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO12=\"value 12\"")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MACRO13=\"value 13\"")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("MULTILINE=TRUE")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("SUM(x, y) = (x) + (y)")); //$NON-NLS-1$
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
		
		clParser.processLine("gcc -DMACRO1 -I ..\\inc -c ..\\source\\source.c");	// PR 80271 //$NON-NLS-1$

        List sumSymbols = collector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
		assertTrue(sumSymbols.contains("MACRO1")); //$NON-NLS-1$
		assertTrue(sumSymbols.size() == 1);
	}
	
	public void testParsingUnbalancedDoubleQuote_Bug186065() throws Exception {
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
		
		clParser.processLine("../src/bug186065.cc:8: error: missing terminating \" character");	// PR 80271 //$NON-NLS-1$
		clParser.processLine("gcc -DBUG186065_IS_FIXED"); //$NON-NLS-1$

        List sumSymbols = collector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
		assertTrue(sumSymbols.contains("BUG186065_IS_FIXED")); //$NON-NLS-1$
		assertTrue(sumSymbols.size() == 1);
	}
}
