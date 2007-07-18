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
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.scannerdiscovery;

import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;

/**
 * Scanner configuration console parser tests
 * 
 * @author vhirsl
 */
public abstract class BaseBOPConsoleParserTests extends BaseTestCase {
	
	public static TestSuite suite() {
		return suite(BaseBOPConsoleParserTests.class);
	}

	protected IScannerInfoConsoleParser fOutputParser;
	protected IScannerInfoCollector fCollector;

	public BaseBOPConsoleParserTests(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fCollector= new TestScannerInfoCollector();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	public void testParsingSymbolDefinitions() {
		fOutputParser.processLine("gcc -DMACRO1 -D MACRO2=value2 -c test.c");	// simple definitions //$NON-NLS-1$
		fOutputParser.processLine("gcc -D -DMACRO3 -c test.c");	// empty -D //$NON-NLS-1$
		fOutputParser.processLine("gcc -D MACRO4='value4' -D 'MACRO5=value5' '-D MACRO6 = value6' -c test.c");	// single quotes //$NON-NLS-1$
		fOutputParser.processLine("gcc -D'MACRO7=\"value 7\"' -D MACRO8='\"value 8\"' -c test.c");	// single quotes //$NON-NLS-1$
		fOutputParser.processLine("gcc -DMACRO9=\"value9\" -D \"MACRO10=value10\" \"-D MACRO11 = value11\" -c test.c");	// double quotes //$NON-NLS-1$
		fOutputParser.processLine("gcc -D\"MACRO12=\\\"value 12\\\"\" -D MACRO13=\"\\\"value 13\\\"\" -c test.c");	// single quotes //$NON-NLS-1$
		fOutputParser.processLine("gcc -D \\"); //$NON-NLS-1$
		fOutputParser.processLine("MULTILINE=TRUE	\\"); //$NON-NLS-1$
		fOutputParser.processLine("-c test.c"); // multiline //$NON-NLS-1$
		fOutputParser.processLine("gcc -D 'SUM(x, y) = (x) + (y)' -c test.c"); // more complex macro definition //$NON-NLS-1$
		
		List sumSymbols = fCollector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
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
		fOutputParser.processLine("gcc -DMACRO1 -I ..\\inc -c ..\\source\\source.c");	// PR 80271 //$NON-NLS-1$

        List sumSymbols = fCollector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
		assertTrue(sumSymbols.contains("MACRO1")); //$NON-NLS-1$
		assertTrue(sumSymbols.size() == 1);
	}
	
	public void testParsingUnbalancedDoubleQuote_Bug186065() throws Exception {
		fOutputParser.processLine("../src/bug186065.cc:8: error: missing terminating \" character");	// PR 80271 //$NON-NLS-1$
		fOutputParser.processLine("gcc -DBUG186065_IS_FIXED"); //$NON-NLS-1$

        List sumSymbols = fCollector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
		assertTrue(sumSymbols.contains("BUG186065_IS_FIXED")); //$NON-NLS-1$
		assertTrue(sumSymbols.size() == 1);
	}
	
	public void _testCompilerCommand_bug194394() throws Exception {
		fOutputParser.processLine("/usr/bin/gcc -DA test1.c"); //$NON-NLS-1$
		fOutputParser.processLine("/usr/gcc-installs/gcc -DB test2.c"); //$NON-NLS-1$
		fOutputParser.processLine("/usr/gcc/gcc -DC test3.c"); //$NON-NLS-1$
		fOutputParser.processLine("/usr/gcc.exe -DD test4.c"); //$NON-NLS-1$
		fOutputParser.processLine("/usr/gcc-tool-x -DE test5.c"); //$NON-NLS-1$
		fOutputParser.processLine("/usr/gcc/something_else -DF test6.c"); //$NON-NLS-1$

        List sumSymbols = fCollector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
		assertTrue(sumSymbols.contains("A")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("B")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("C")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("D")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("E")); //$NON-NLS-1$
		assertFalse(sumSymbols.contains("F")); //$NON-NLS-1$
		assertEquals(5, sumSymbols.size());
	}
	
	public void testCommandsWithSemicolon_bug194394() throws Exception {
		fOutputParser.processLine("gcc -DA test1.c; gcc -DB test2.c"); //$NON-NLS-1$
		fOutputParser.processLine("nix -DC; gcc -DD test2.c"); //$NON-NLS-1$

        List sumSymbols = fCollector.getCollectedScannerInfo(null, ScannerInfoTypes.SYMBOL_DEFINITIONS); 
		assertTrue(sumSymbols.contains("A")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("B")); //$NON-NLS-1$
		assertFalse(sumSymbols.contains("C")); //$NON-NLS-1$
		assertTrue(sumSymbols.contains("D")); //$NON-NLS-1$
		assertEquals(3, sumSymbols.size());
	}

}
