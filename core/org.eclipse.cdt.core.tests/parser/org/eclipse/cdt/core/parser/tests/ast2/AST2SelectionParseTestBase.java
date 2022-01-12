/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.FileBasePluginTestCase;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.resources.IFile;

/**
 * @author dsteffle
 */
public class AST2SelectionParseTestBase extends FileBasePluginTestCase {

	public AST2SelectionParseTestBase() {
	}

	public AST2SelectionParseTestBase(String name) {
		super(name);
	}

	private static final IParserLogService NULL_LOG = new NullLogService();

	public AST2SelectionParseTestBase(String name, Class className) {
		super(name, className);
	}

	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length) throws ParserException {
		return parse(code, lang, false, false, offset, length);
	}

	protected IASTNode parse(IFile file, ParserLanguage lang, int offset, int length) throws ParserException {
		IASTTranslationUnit tu = parse(file, lang, false, false);
		return tu.getNodeSelector(null).findNode(offset, length);
	}

	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length, boolean expectedToPass)
			throws ParserException {
		return parse(code, lang, false, expectedToPass, offset, length);
	}

	protected IASTNode parse(String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems,
			int offset, int length) throws ParserException {
		IASTTranslationUnit tu = parse(code, lang, useGNUExtensions, expectNoProblems);
		return tu.getNodeSelector(null).findNode(offset, length);
	}

	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems) throws ParserException {
		FileContent codeReader = FileContent.create("<test-code>", code.toCharArray());
		ScannerInfo scannerInfo = new ScannerInfo();
		IScanner scanner = AST2TestBase.createScanner(codeReader, lang, ParserMode.COMPLETE_PARSE, scannerInfo);

		ISourceCodeParser parser2 = null;
		if (lang == ParserLanguage.CPP) {
			ICPPParserExtensionConfiguration config = null;
			if (useGNUExtensions)
				config = new GPPParserExtensionConfiguration();
			else
				config = new ANSICPPParserExtensionConfiguration();
			parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
		} else {
			ICParserExtensionConfiguration config = null;

			if (useGNUExtensions)
				config = new GCCParserExtensionConfiguration();
			else
				config = new ANSICParserExtensionConfiguration();

			parser2 = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
		}

		IASTTranslationUnit tu = parser2.parse();

		if (parser2.encounteredError() && expectNoProblems)
			throw new ParserException("FAILURE"); //$NON-NLS-1$

		if (lang == ParserLanguage.C && expectNoProblems) {
			assertEquals(CVisitor.getProblems(tu).length, 0);
			assertEquals(tu.getPreprocessorProblems().length, 0);
		} else if (lang == ParserLanguage.CPP && expectNoProblems) {
			assertEquals(CPPVisitor.getProblems(tu).length, 0);
			assertEquals(tu.getPreprocessorProblems().length, 0);
		}
		if (expectNoProblems)
			assertEquals(0, tu.getPreprocessorProblems().length);

		return tu;
	}

	protected IASTTranslationUnit parse(IFile file, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems) throws ParserException {
		IASTTranslationUnit tu = null;
		try {
			tu = CDOM.getInstance().getASTService().getTranslationUnit(file);
		} catch (UnsupportedDialectException e) {
			assertFalse(true); // shouldn't happen
		}

		if (lang == ParserLanguage.C && expectNoProblems) {
			assertEquals(CVisitor.getProblems(tu).length, 0);
			assertEquals(tu.getPreprocessorProblems().length, 0);
		} else if (lang == ParserLanguage.CPP && expectNoProblems) {
			assertEquals(CPPVisitor.getProblems(tu).length, 0);
			assertEquals(tu.getPreprocessorProblems().length, 0);
		}
		if (expectNoProblems)
			assertEquals(0, tu.getPreprocessorProblems().length);

		return tu;
	}
}
