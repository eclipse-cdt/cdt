/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.astwriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.rewrite.RewriteBaseTest;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;
import org.osgi.framework.Bundle;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SourceRewriteTest extends TestSuite {
	private static final String testRegexp = "//!(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String codeTypeRegexp = "//%(C|CPP)( GNU)?$"; //$NON-NLS-1$
	private static final String resultRegexp = "//=.*$"; //$NON-NLS-1$

	enum MatcherState {
		skip, inTest, inSource, inExpectedResult
	}

	protected static BufferedReader createReader(String file) throws IOException {
		Bundle bundle = CTestPlugin.getDefault().getBundle();
		Path path = new Path(file);
		file = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
		return new BufferedReader(new FileReader(file));
	}

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("AstWriterTests");
		suite.addTest(
				SourceRewriteTest.suite("ExpressionTests", "resources/rewrite/ASTWriterExpressionTestSource.awts"));

		suite.addTest(
				SourceRewriteTest.suite("DelcSpecifierTests", "resources/rewrite/ASTWriterDeclSpecTestSource.awts"));
		suite.addTest(SourceRewriteTest.suite("Commented DelcSpecifierTests",
				"resources/rewrite/ASTWriterCommentedDeclSpecTestSource.awts"));

		suite.addTest(
				SourceRewriteTest.suite("DeclaratorTests", "resources/rewrite/ASTWriterDeclaratorTestSource.awts"));
		suite.addTest(SourceRewriteTest.suite("Commented DeclaratorTests",
				"resources/rewrite/ASTWriterCommentedDeclaratorTestSource.awts"));

		suite.addTest(
				SourceRewriteTest.suite("StatementsTests", "resources/rewrite/ASTWriterStatementTestSource.awts"));
		suite.addTest(SourceRewriteTest.suite("Commented StatementsTests",
				"resources/rewrite/ASTWriterCommentedStatementTestSource.awts"));

		suite.addTest(SourceRewriteTest.suite("NameTests", "resources/rewrite/ASTWriterNameTestSource.awts"));
		suite.addTest(SourceRewriteTest.suite("Commented NameTests",
				"resources/rewrite/ASTWriterCommentedNameTestSource.awts"));

		suite.addTest(
				SourceRewriteTest.suite("InitializerTests", "resources/rewrite/ASTWriterInitializerTestSource.awts"));

		suite.addTest(
				SourceRewriteTest.suite("DeclarationTests", "resources/rewrite/ASTWriterDeclarationTestSource.awts"));
		suite.addTest(SourceRewriteTest.suite("Commented DeclarationTests",
				"resources/rewrite/ASTWriterCommentedDeclarationTestSource.awts"));

		suite.addTest(SourceRewriteTest.suite("TemplatesTests", "resources/rewrite/ASTWriterTemplateTestSource.awts"));

		suite.addTest(SourceRewriteTest.suite("CommentTests", "resources/rewrite/ASTWriterCommentedTestSource.awts"));
		suite.addTest(
				SourceRewriteTest.suite("NewCommentTests", "resources/rewrite/ASTWriterCommentedTestSource2.awts"));
		suite.addTest(SourceRewriteTest.suite("AttributeTests", "resources/rewrite/ASTWriterAttributeTestSource.awts"));
		suite.addTestSuite(ExpressionWriterTest.class);
		return suite;
	}

	public static Test suite(String name, String file) throws Exception {
		BufferedReader in = createReader(file);
		ArrayList<RewriteBaseTest> testCases = createTests(in);
		in.close();
		return createSuite(testCases, name);
	}

	private static TestSuite createSuite(ArrayList<RewriteBaseTest> testCases, String name) {
		TestSuite suite = new TestSuite(name);
		for (RewriteBaseTest subject : testCases) {
			suite.addTest(subject);
		}
		return suite;
	}

	protected static boolean lineMatchesBeginOfTest(String line) {
		return createMatcherFromString(testRegexp, line).find();
	}

	protected static boolean lineMatchesCodeType(String line) {
		return createMatcherFromString(codeTypeRegexp, line).find();
	}

	protected static Matcher createMatcherFromString(String pattern, String line) {
		return Pattern.compile(pattern).matcher(line);
	}

	protected static String getNameOfTest(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(testRegexp, line);
		if (matcherBeginOfTest.find()) {
			return matcherBeginOfTest.group(1);
		} else {
			return "Not Named";
		}
	}

	protected static boolean lineMatchesBeginOfResult(String line) {
		return createMatcherFromString(resultRegexp, line).find();
	}

	private static ArrayList<RewriteBaseTest> createTests(BufferedReader inputReader) throws Exception {
		ASTWriterTestSourceFile file = null;
		MatcherState matcherState = MatcherState.skip;
		ArrayList<RewriteBaseTest> testCases = new ArrayList<>();

		String line;
		while ((line = inputReader.readLine()) != null) {
			if (lineMatchesBeginOfTest(line)) {
				matcherState = MatcherState.inTest;
				file = new ASTWriterTestSourceFile("ASTWritterTest.h"); //$NON-NLS-1$
				testCases.add(createTestClass(getNameOfTest(line), file));
				continue;
			} else if (lineMatchesBeginOfResult(line)) {
				matcherState = MatcherState.inExpectedResult;
				continue;
			} else if (lineMatchesCodeType(line)) {
				matcherState = MatcherState.inSource;
				if (file != null) {
					file.setParserLanguage(getParserLanguage(line));
					file.setUseGNUExtensions(useGNUExtensions(line));
				}
				continue;
			}

			switch (matcherState) {
			case inSource:
				if (file != null) {
					file.addLineToSource(line);
				}
				break;
			case inExpectedResult:
				if (file != null) {
					file.addLineToExpectedSource(line);
				}
				break;
			default:
				break;
			}
		}
		return testCases;
	}

	protected static boolean useGNUExtensions(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(codeTypeRegexp, line);
		if (matcherBeginOfTest.find()) {
			String codeType = matcherBeginOfTest.group(2);
			if (codeType == null) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	protected static ParserLanguage getParserLanguage(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(codeTypeRegexp, line);
		if (matcherBeginOfTest.find()) {
			String codeType = matcherBeginOfTest.group(1);
			if (codeType.equalsIgnoreCase("CPP")) { //$NON-NLS-1$
				return ParserLanguage.CPP;
			} else {
				return ParserLanguage.C;
			}
		}
		return ParserLanguage.C;
	}

	private static RewriteBaseTest createTestClass(String testName, ASTWriterTestSourceFile file) throws Exception {
		ASTWriterTester test = new ASTWriterTester(testName, file);
		TextSelection sel = file.getSelection();
		if (sel != null) {
			test.setFileWithSelection(file.getName());
			test.setSelection(sel);
		}
		return test;
	}
}
