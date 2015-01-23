/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.parser.tests.scanner;

import java.io.IOException;

import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
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

import junit.framework.ComparisonFailure;

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
		initializeScanner(getContent(input), ParserLanguage.CPP, mode, new ScannerInfo());
	}

	protected void initializeScanner(String input, ParserLanguage lang) throws IOException {
		initializeScanner(getContent(input), lang, ParserMode.COMPLETE_PARSE, new ScannerInfo());
	}
	
	protected void initializeScanner(String input, ParserLanguage lang, IScannerExtensionConfiguration scannerConfig) throws IOException {
		initializeScanner(getContent(input), lang, ParserMode.COMPLETE_PARSE, new ScannerInfo(), scannerConfig);
	}

	private FileContent getContent(String input) {
		return FileContent.create("<test-code>", input.toCharArray());
	}

	protected void initializeScanner(FileContent input, ParserLanguage lang, ParserMode mode, IScannerInfo scannerInfo) throws IOException {
		initializeScanner(input, lang, mode, scannerInfo, null);
	}
	
	protected void initializeScanner(FileContent input, ParserLanguage lang, ParserMode mode, IScannerInfo scannerInfo, IScannerExtensionConfiguration scannerConfig) throws IOException {
		IncludeFileContentProvider readerFactory= FileCodeReaderFactory.getInstance();
		//IScannerExtensionConfiguration scannerConfig;
	
		if (scannerConfig == null) {
		    if (lang == ParserLanguage.C) {
		    	scannerConfig= GCCScannerExtensionConfiguration.getInstance();
		    } else {
		    	scannerConfig= GPPScannerExtensionConfiguration.getInstance(scannerInfo);
		    }
		}
	    
		fScanner= new CPreprocessor(input, scannerInfo, lang, NULL_LOG, scannerConfig, readerFactory);
		fLocationResolver= fScanner.getLocationMap();
	}

	protected void initializeScanner() throws Exception {
		initializeScanner(getAboveComment());
	}

	protected StringBuilder[] getTestContent(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), sections);
	}

	protected String getAboveComment() throws IOException {
		return getTestContent(1)[0].toString();
	}

	protected void fullyTokenize() throws Exception {
		try	{
			for (;;) {
				IToken t= fScanner.nextToken();
			}
		} catch (EndOfFileException e) {
		}
	}

	protected void validateToken(int tokenType) throws Exception {
		IToken t= fScanner.nextToken();
		assertEquals(tokenType, t.getType());
	}

	protected void validateToken(int tokenType, String image) throws Exception {
		try {
			IToken t= fScanner.nextToken();
			assertEquals(tokenType, t.getType());
			assertEquals(image, t.getImage());
		} catch (EndOfFileException e) {
			fail("Missing token " + image);
		}
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

	protected void validateLString(String expectedImage) throws Exception {
		validateToken(IToken.tLSTRING, "L\"" + expectedImage + "\"");
	}
	
	protected void validateUTF16String(String expectedImage) throws Exception {
		validateToken(IToken.tUTF16STRING, "u\"" + expectedImage + "\"");
	}
	
	protected void validateUTF32String(String expectedImage) throws Exception {
		validateToken(IToken.tUTF32STRING, "U\"" + expectedImage + "\"");
	}
	
	protected void validateUserDefinedLiteralString(String expectedImage, String expectedSuffix) throws Exception {
		validateToken(IToken.tUSER_DEFINED_STRING_LITERAL, "\"" + expectedImage + "\"" + expectedSuffix);
	}
	
	protected void validateChar(String expectedImage) throws Exception {
		validateToken(IToken.tCHAR, "'" + expectedImage + "'");
	}

	protected void validateWideChar(String expectedImage) throws Exception {
		validateToken(IToken.tLCHAR, "L'" + expectedImage + "'");
	}
	
	protected void validateUTF16Char(String expectedImage) throws Exception {
		validateToken(IToken.tUTF16CHAR, "u'" + expectedImage + "'");
	}
	
	protected void validateUTF32Char(String expectedImage) throws Exception {
		validateToken(IToken.tUTF32CHAR, "U'" + expectedImage + "'");
	}
	
	protected void validateFloatingPointLiteral(String expectedImage) throws Exception {
		validateToken(IToken.tFLOATINGPT, expectedImage);
	}

	protected void validateEOF() throws Exception {
		try {
			IToken t= fScanner.nextToken();
			fail("superfluous token " + t);
		} catch(EndOfFileException e) {
		}
	}

	private void assertCharArrayEquals(char[] expected, char[] actual) {
		if (!CharArrayUtils.equals(expected, actual))
			throw new ComparisonFailure(null, new String(expected), new String(actual));
	}

	protected void validateDefinition(String name, String value) {
		IMacroBinding expObject = fScanner.getMacroDefinitions().get(name);
		assertNotNull(expObject);
		assertCharArrayEquals(value.toCharArray(), expObject.getExpansion());
	}

	protected void validateDefinition(String name, int value) {
		validateDefinition(name, String.valueOf(value));
	}

	protected void validateAsUndefined(String name) {
		assertNull(fScanner.getMacroDefinitions().get(name.toCharArray()));
	}

	protected void validateProblemCount(int count) throws Exception {
		assertEquals(count, fLocationResolver.getScannerProblems().length);
	}
	
	protected void validateProblem(int idx, int problemID, String detail) throws Exception {
		IASTProblem problem= fLocationResolver.getScannerProblems()[idx];
		assertEquals(problemID, problem.getID());
		if (detail != null) {
			assertEquals(detail, problem.getArguments()[0]);
		}
	}
}
