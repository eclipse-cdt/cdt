/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.parser.tests.scanner;

import java.io.IOException;

import junit.framework.ComparisonFailure;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;

public abstract class PreprocessorTestsBase extends BaseTestCase {

	private static final IParserLogService NULL_LOG = new NullLogService();
	protected CPreprocessor fScanner;
	protected ILocationResolver fLocationResolver;

	public PreprocessorTestsBase(String name) {
		super(name);
	}

	public PreprocessorTestsBase() {
		super();
	}

	protected void initializeScanner(String input) throws IOException {
		initializeScanner(input, ParserMode.COMPLETE_PARSE);
	}

	protected void initializeScanner(String input, ParserMode mode) throws IOException {
		initializeScanner(input, ParserLanguage.CPP, mode);
	}

	protected void initializeScanner(String input, ParserLanguage lang) throws IOException {
		initializeScanner(input, lang, ParserMode.COMPLETE_PARSE);
	}

	protected void initializeScanner(String input, ParserLanguage lang, ParserMode mode) throws IOException {
		ICodeReaderFactory readerFactory= FileCodeReaderFactory.getInstance();
		CodeReader reader= new CodeReader(input.toCharArray());
		IScannerExtensionConfiguration scannerConfig;
	    IScannerInfo scannerInfo= new ScannerInfo();
	
	    if (lang == ParserLanguage.C) {
	    	scannerConfig= new GCCScannerExtensionConfiguration();
	    }
	    else {
	    	scannerConfig= new GPPScannerExtensionConfiguration();
	    }
	    
		fScanner= new CPreprocessor(reader, scannerInfo, lang, NULL_LOG, scannerConfig, readerFactory);
		fLocationResolver= (ILocationResolver) fScanner.getAdapter(ILocationResolver.class);
	}

	protected void initializeScanner() throws Exception {
		StringBuffer[] input= TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), 1);
		initializeScanner(input[0].toString());
	}

	protected int fullyTokenize() throws Exception {
		try	{
			for(;;) {
				IToken t= fScanner.nextToken();
				assertTrue(t.getType() <= IToken.tLAST);
			}
		}
		catch ( EndOfFileException e){
		}
		return fScanner.getCount();
	}

	protected void validateToken(int tokenType) throws Exception {
		IToken t= fScanner.nextToken();
		assertEquals(tokenType, t.getType());
	}

	protected void validateToken(int tokenType, String image) throws Exception {
		IToken t= fScanner.nextToken();
		assertEquals(tokenType, t.getType());
		assertEquals(image, t.getImage());
	}

	protected void validateInteger(String expectedImage) throws Exception {
		validateToken(IToken.tINTEGER, expectedImage);
	}

	protected void validateIdentifier(String expectedImage) throws Exception {
		validateToken(IToken.tIDENTIFIER, expectedImage);
	}

	protected void validateString(String expectedImage) throws Exception {
		validateToken(IToken.tSTRING, "\"" + expectedImage + "\"");
	}

	protected void validateChar(String expectedImage) throws Exception {
		validateToken(IToken.tCHAR, "'" + expectedImage + "'");
	}

	protected void validateWideChar(String expectedImage) throws Exception {
		validateToken(IToken.tLCHAR, "L'" + expectedImage + "'");
	}

	protected void validateLString(String expectedImage) throws Exception {
		validateToken(IToken.tLSTRING, "L\"" + expectedImage + "\"");
	}

	protected void validateFloatingPointLiteral(String expectedImage) throws Exception {
		validateToken(IToken.tFLOATINGPT, expectedImage);
	}

	protected void validateEOF() throws Exception {
		try {
			IToken t= fScanner.nextToken();
			fail("superfluous token " + t);
		}
		catch(EndOfFileException e) {
		}
	}

	private void assertCharArrayEquals(char[] expected, char[] actual) {
		if (!CharArrayUtils.equals(expected, actual))
			throw new ComparisonFailure(null, new String(expected), new String(actual));
	}

	protected void validateDefinition(String name, String value) {
		Object expObject = fScanner.getDefinitions().get(name);
		assertNotNull(expObject);
		assertTrue(expObject instanceof IMacroBinding);
		assertCharArrayEquals(value.toCharArray(), ((IMacroBinding)expObject).getExpansion());
	}

	protected void validateDefinition(String name, int value) {
		validateDefinition(name, String.valueOf(value));
	}

	protected void validateAsUndefined(String name) {
		assertNull(fScanner.getDefinitions().get(name.toCharArray()));
	}

	protected void validateProblemCount(int count) throws Exception {
		assertEquals(count, fLocationResolver.getScannerProblems().length);
	}
	
	protected void validateProblem(int idx, int problemID, String detail) throws Exception {
		IASTProblem problem= fLocationResolver.getScannerProblems()[idx];
		assertEquals(problemID, problem.getID());
		if (detail != null) {
			assertEquals(detail, problem.getArguments());
		}
	}

}
