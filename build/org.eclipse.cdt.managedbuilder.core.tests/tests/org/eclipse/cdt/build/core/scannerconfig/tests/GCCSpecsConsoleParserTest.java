/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
 package org.eclipse.cdt.build.core.scannerconfig.tests;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCSpecsConsoleParser;

public class GCCSpecsConsoleParserTest extends TestCase {
	GCCSpecsConsoleParser parser;
	private IScannerInfoCollector collector;
	List<String> includes;
	List<String> symbols;

	@Override
	protected void setUp() throws Exception {
		collector = new IScannerInfoCollector() {

			@Override
			public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void contributeToScannerConfig(Object resource, Map scannerInfo1) {
				Map<ScannerInfoTypes, List<String>> scannerInfo = scannerInfo1;
				includes = scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
				symbols = scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
			}
		};
		parser = new GCCSpecsConsoleParser();
		parser.startup(null, null, collector, null);
	}

	private void enterLine(String line) {
		parser.processLine(line);
		parser.shutdown();
	}

	private void checkMacro(String name, String value) {
		assertTrue("No symbols", symbols.size() > 0);
		String string = symbols.get(0);
		if (string.contains("=")) {
			String[] val = string.split("=", 2);
			assertEquals(name, val[0]);
			assertEquals(value, val[1]);
		} else {
			assertEquals(name, string);
			assertEquals(value, "");
		}

	}

	public void testProcessLine_NoArgs() {
		enterLine("#define __MY_MACRO__ __MY_VALUE__");
		checkMacro("__MY_MACRO__", "__MY_VALUE__");
	}
	public void testProcessLine_Const() {
		enterLine("#define A (3)");
		checkMacro("A", "(3)");
	}
	public void testProcessLine_EmptyArgList() {
		enterLine("#define A() B");
		checkMacro("A()", "B");
	}
	public void testProcessLine_ParamUnused() {
		enterLine("#define A(X) B");
		checkMacro("A(X)", "B");
	}
	public void testProcessLine_ParamSpace() {
		enterLine("#define __MY_MACRO__(P1, P2) __MY_VALUE__(P1, P2)");
		checkMacro("__MY_MACRO__(P1, P2)", "__MY_VALUE__(P1, P2)");
	}
	public void testProcessLine_EmptyBody() {
		enterLine("#define __MY_MACRO__(P1, P2) ");
		checkMacro("__MY_MACRO__(P1, P2)", "");
	}

}
