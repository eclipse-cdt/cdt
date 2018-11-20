/*******************************************************************************
 *  Copyright (c) 2006, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2Tests;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.TestSuite;

/**
 *
 * @author Mike Kucera
 *
 */
@SuppressWarnings({ "nls", "restriction" })
public class LRTests extends AST2Tests {

	public static TestSuite suite() {
		return suite(LRTests.class);
	}

	public LRTests(String name) {
		super(name);
	}

	//TODO ??? overwrite some failed test cases
	@Override
	public void testFnReturningPtrToFn() throws Exception {
	}

	@Override
	public void testBug270275_int_is_equivalent_to_signed_int() throws Exception {
	}

	@Override
	public void testFunctionDefTypes() throws Exception {
	}

	@Override
	public void testBug80171() throws Exception {
	}

	@Override
	public void testBug192165() throws Exception {
	}

	@Override
	public void testTypenameInExpression() throws Exception {
	}

	@Override
	public void testParamWithFunctionType_84242() throws Exception {
	}

	@Override
	public void testParamWithFunctionTypeCpp_84242() throws Exception {
	}

	@Override
	public void testFunctionReturningPtrToArray_216609() throws Exception {
	}

	@Override
	public void testNestedFunctionDeclarators() throws Exception {
	}

	@Override
	public void testConstantExpressionBinding() throws Exception {
	}

	@Override
	public void testAmbiguousDeclaration_259373() throws Exception {
	}

	@Override
	public void testSizeofFunctionType_252243() throws Exception {
	}

	@Override
	public void testSkipAggregateInitializer_297550() throws Exception {
	}

	@Override
	public void testDeepElseif_298455() throws Exception {
	}

	@Override
	public void testAttributeSyntax_298841() throws Exception {
	}

	@Override
	public void testEmptyTrailingMacro_303152() throws Exception {
	}

	@Override // not applicable to lr parser memory usage.
	public void testLargeTrivialAggregateInitializer_253690() throws Exception {
	}

	@Override
	public void testMaximumTrivialExpressionsInInitializers_412380() throws Exception {
	}

	@Override
	public void testNoSkipTrivialAggregateInitializer_412380() throws Exception {
	}

	@Override
	public void testNoSkipNonTrivialAggregateInitializer_412380() throws Exception {
	}

	@Override
	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems) throws ParserException {
		return parse(code, lang, useGNUExtensions, expectNoProblems, -1);
	}

	@Override
	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
			boolean expectNoProblems, int limitTrivialInitializers) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckSyntaxProblems(expectNoProblems);
		options.setCheckPreprocessorProblems(expectNoProblems);
		options.setLimitTrivialInitializers(limitTrivialInitializers);
		return ParseHelper.parse(code, language, options);
	}

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

	public void testMultipleHashHash() throws Exception {
		String code = "#define TWICE(a)  int a##tera; int a##ther; \n TWICE(pan)";
		parseAndCheckBindings(code, ParserLanguage.C);
	}

	public void testBug191279() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" /**/ \n");
		sb.append("# define YO 99 /**/ \n");
		sb.append("# undef YO /**/ ");
		sb.append(" /* $ */ ");
		String code = sb.toString();
		parseAndCheckBindings(code, ParserLanguage.C);
	}

	public void testBug191324() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("int x$y = 99; \n");
		sb.append("int $q = 100; \n"); // can use $ as first character in identifier
		sb.append("#ifndef SS$_INVFILFOROP \n");
		sb.append("int z; \n");
		sb.append("#endif \n");
		String code = sb.toString();
		parseAndCheckBindings(code, ParserLanguage.C);
	}

	public void testBug192009_implicitInt() throws Exception {
		String code = "main() { int x; }";
		IASTTranslationUnit tu = parse(code, ParserLanguage.C, false, true);

		IASTDeclaration[] declarations = tu.getDeclarations();
		assertEquals(1, declarations.length);

		IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[0];
		ICASTSimpleDeclSpecifier declSpec = (ICASTSimpleDeclSpecifier) main.getDeclSpecifier();
		assertEquals(0, declSpec.getType());

		assertEquals("main", main.getDeclarator().getName().toString());
	}

	/* I don't care about C98
	 */
	@Override
	public void testBug196468_emptyArrayInitializer() {
	}

	public void _testBug196468_emptyArrayInitializer() throws Exception {
		super.testBug196468_emptyArrayInitializer();
	}

	/* LPG holds on to all the tokens as you parse, so I don't think
	 * it would be easy to fix this bug.
	 */
	@Override
	public void testScalabilityOfLargeTrivialInitializer_253690() {
	}

	public void _testScalabilityOfLargeTrivialInitializer_253690() throws Exception {

		super.testScalabilityOfLargeTrivialInitializer_253690();
	}

	/* All of the identifiers in the code resolve correctly.
	 * The problem is that some of the expressions parse wrong but
	 * thats not actually a big deal. Fixing this bug will be
	 * difficult so defer it to the future.
	 */
	@Override
	public void testBinaryVsCastAmbiguities_237057() {
	}

	public void _testBinaryVsCastAmbiguities_237057() throws Exception {
		super.testBinaryVsCastAmbiguities_237057();
	}

	/* All of the identifiers in the code resolve correctly.
	 * The problem is that some of the expressions parse wrong but
	 * thats not actually a big deal. Fixing this bug will be
	 * difficult so defer it to the future.
	 */
	@Override
	public void testCastVsFunctionCallAmbiguities_237057() {
	}

	public void _testCastVsFunctionCallAmbiguities_237057() throws Exception {
		super.testCastVsFunctionCallAmbiguities_237057();
	}

	/* The LR parser generates the AST for switch statements
	 * differently than the DOM parser.
	 */
	@Override
	public void testCaseRange_211882() {
	}

	public void _testCaseRange_211882() throws Exception {
		super.testCaseRange_211882();
	}
}
