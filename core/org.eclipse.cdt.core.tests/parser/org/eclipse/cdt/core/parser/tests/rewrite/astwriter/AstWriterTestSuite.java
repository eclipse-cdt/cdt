/*******************************************************************************
 * Copyright (c) 2006, 2014 Institute for Software, HSR Hochschule fuer Technik
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
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.astwriter;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Emanuel Graf
 */
public class AstWriterTestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("AstWriterTests");
		suite.addTest(
				SourceRewriteTester.suite("ExpressionTests", "resources/rewrite/ASTWriterExpressionTestSource.awts"));

		suite.addTest(
				SourceRewriteTester.suite("DelcSpecifierTests", "resources/rewrite/ASTWriterDeclSpecTestSource.awts"));
		suite.addTest(SourceRewriteTester.suite("Commented DelcSpecifierTests",
				"resources/rewrite/ASTWriterCommentedDeclSpecTestSource.awts"));

		suite.addTest(
				SourceRewriteTester.suite("DeclaratorTests", "resources/rewrite/ASTWriterDeclaratorTestSource.awts"));
		suite.addTest(SourceRewriteTester.suite("Commented DeclaratorTests",
				"resources/rewrite/ASTWriterCommentedDeclaratorTestSource.awts"));

		suite.addTest(
				SourceRewriteTester.suite("StatementsTests", "resources/rewrite/ASTWriterStatementTestSource.awts"));
		suite.addTest(SourceRewriteTester.suite("Commented StatementsTests",
				"resources/rewrite/ASTWriterCommentedStatementTestSource.awts"));

		suite.addTest(SourceRewriteTester.suite("NameTests", "resources/rewrite/ASTWriterNameTestSource.awts"));
		suite.addTest(SourceRewriteTester.suite("Commented NameTests",
				"resources/rewrite/ASTWriterCommentedNameTestSource.awts"));

		suite.addTest(
				SourceRewriteTester.suite("InitializerTests", "resources/rewrite/ASTWriterInitializerTestSource.awts"));

		suite.addTest(
				SourceRewriteTester.suite("DeclarationTests", "resources/rewrite/ASTWriterDeclarationTestSource.awts"));
		suite.addTest(SourceRewriteTester.suite("Commented DeclarationTests",
				"resources/rewrite/ASTWriterCommentedDeclarationTestSource.awts"));

		suite.addTest(
				SourceRewriteTester.suite("TemplatesTests", "resources/rewrite/ASTWriterTemplateTestSource.awts"));

		suite.addTest(SourceRewriteTester.suite("CommentTests", "resources/rewrite/ASTWriterCommentedTestSource.awts"));
		suite.addTest(
				SourceRewriteTester.suite("NewCommentTests", "resources/rewrite/ASTWriterCommentedTestSource2.awts"));
		suite.addTest(
				SourceRewriteTester.suite("AttributeTests", "resources/rewrite/ASTWriterAttributeTestSource.awts"));
		suite.addTestSuite(ExpressionWriterTest.class);
		return suite;
	}
}
