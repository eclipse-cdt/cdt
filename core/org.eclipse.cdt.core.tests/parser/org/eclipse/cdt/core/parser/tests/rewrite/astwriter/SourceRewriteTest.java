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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase.ScannerKind;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.params.provider.Arguments;
import org.osgi.framework.Bundle;

/**
 * This is not actually a test, but a test provider. See loadTests and its uses
 *
 * The possibly unusual structure here is a result of migrating this JUnit 3 test
 * suite creator to JUnit 5
 */
public class SourceRewriteTest {
	private static final String testRegexp = "//!(.*)\\s*(\\w*)*$"; //$NON-NLS-1$
	private static final String codeTypeRegexp = "//%(C|CPP|CPP20)( GNU)?$"; //$NON-NLS-1$
	private static final String resultRegexp = "//=.*$"; //$NON-NLS-1$

	enum MatcherState {
		skip, inTest, inSource, inExpectedResult
	}

	private static BufferedReader createReader(String file) throws IOException {
		Bundle bundle = CTestPlugin.getDefault().getBundle();
		Path path = new Path(file);
		file = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
		return new BufferedReader(new FileReader(file));
	}

	public static List<Arguments> loadTests() throws Exception {
		List<Arguments> suite = new ArrayList<>();
		suite.addAll(
				SourceRewriteTest.suite("ExpressionTests", "resources/rewrite/ASTWriterExpressionTestSource.awts"));

		suite.addAll(
				SourceRewriteTest.suite("DelcSpecifierTests", "resources/rewrite/ASTWriterDeclSpecTestSource.awts"));
		suite.addAll(SourceRewriteTest.suite("Commented DelcSpecifierTests",
				"resources/rewrite/ASTWriterCommentedDeclSpecTestSource.awts"));

		suite.addAll(
				SourceRewriteTest.suite("DeclaratorTests", "resources/rewrite/ASTWriterDeclaratorTestSource.awts"));
		suite.addAll(SourceRewriteTest.suite("Commented DeclaratorTests",
				"resources/rewrite/ASTWriterCommentedDeclaratorTestSource.awts"));

		suite.addAll(SourceRewriteTest.suite("StatementsTests", "resources/rewrite/ASTWriterStatementTestSource.awts"));
		suite.addAll(SourceRewriteTest.suite("Commented StatementsTests",
				"resources/rewrite/ASTWriterCommentedStatementTestSource.awts"));

		suite.addAll(SourceRewriteTest.suite("NameTests", "resources/rewrite/ASTWriterNameTestSource.awts"));
		suite.addAll(SourceRewriteTest.suite("Commented NameTests",
				"resources/rewrite/ASTWriterCommentedNameTestSource.awts"));

		suite.addAll(
				SourceRewriteTest.suite("InitializerTests", "resources/rewrite/ASTWriterInitializerTestSource.awts"));

		suite.addAll(
				SourceRewriteTest.suite("DeclarationTests", "resources/rewrite/ASTWriterDeclarationTestSource.awts"));
		suite.addAll(SourceRewriteTest.suite("Commented DeclarationTests",
				"resources/rewrite/ASTWriterCommentedDeclarationTestSource.awts"));

		suite.addAll(SourceRewriteTest.suite("TemplatesTests", "resources/rewrite/ASTWriterTemplateTestSource.awts"));

		suite.addAll(SourceRewriteTest.suite("CommentTests", "resources/rewrite/ASTWriterCommentedTestSource.awts"));
		suite.addAll(
				SourceRewriteTest.suite("NewCommentTests", "resources/rewrite/ASTWriterCommentedTestSource2.awts"));
		suite.addAll(SourceRewriteTest.suite("AttributeTests", "resources/rewrite/ASTWriterAttributeTestSource.awts"));
		return suite;
	}

	private static List<Arguments> suite(String name, String file) throws Exception {
		BufferedReader in = createReader(file);
		List<Arguments> testCases = createTests(name, in);
		in.close();
		return testCases;
	}

	private static boolean lineMatchesBeginOfTest(String line) {
		return createMatcherFromString(testRegexp, line).find();
	}

	private static boolean lineMatchesCodeType(String line) {
		return createMatcherFromString(codeTypeRegexp, line).find();
	}

	private static Matcher createMatcherFromString(String pattern, String line) {
		return Pattern.compile(pattern).matcher(line);
	}

	private static String getNameOfTest(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(testRegexp, line);
		if (matcherBeginOfTest.find()) {
			return matcherBeginOfTest.group(1);
		} else {
			return "Not Named";
		}
	}

	private static boolean lineMatchesBeginOfResult(String line) {
		return createMatcherFromString(resultRegexp, line).find();
	}

	private static List<Arguments> createTests(String name, BufferedReader inputReader) throws Exception {
		ASTWriterTestSourceFile file = null;
		MatcherState matcherState = MatcherState.skip;
		ArrayList<Arguments> testCases = new ArrayList<>();

		String line;
		while ((line = inputReader.readLine()) != null) {
			if (lineMatchesBeginOfTest(line)) {
				matcherState = MatcherState.inTest;
				file = new ASTWriterTestSourceFile("ASTWritterTest.h"); //$NON-NLS-1$
				testCases.add(Arguments.argumentSet(name + "." + getNameOfTest(line), file));
				continue;
			} else if (lineMatchesBeginOfResult(line)) {
				matcherState = MatcherState.inExpectedResult;
				continue;
			} else if (lineMatchesCodeType(line)) {
				matcherState = MatcherState.inSource;
				if (file != null) {
					file.setParserLanguage(getParserLanguage(line));
					file.setScannerKind(getScannerKind(line));
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

	private static ScannerKind getScannerKind(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(codeTypeRegexp, line);
		if (matcherBeginOfTest.find()) {
			String codeType = matcherBeginOfTest.group(1);
			String gnuExtensionsType = matcherBeginOfTest.group(2);
			if (gnuExtensionsType == null) {
				if (codeType.equalsIgnoreCase("CPP20")) { //$NON-NLS-1$
					return ScannerKind.STDCPP20;
				}
			} else {
				return ScannerKind.GNU;
			}
		}
		return ScannerKind.STD;
	}

	private static ParserLanguage getParserLanguage(String line) {
		Matcher matcherBeginOfTest = createMatcherFromString(codeTypeRegexp, line);
		if (matcherBeginOfTest.find()) {
			String codeType = matcherBeginOfTest.group(1);
			if (codeType.equalsIgnoreCase("CPP")) { //$NON-NLS-1$
				return ParserLanguage.CPP;
			} else if (codeType.equalsIgnoreCase("CPP20")) { //$NON-NLS-1$
				return ParserLanguage.CPP;
			} else {
				return ParserLanguage.C;
			}
		}
		return ParserLanguage.C;
	}
}
