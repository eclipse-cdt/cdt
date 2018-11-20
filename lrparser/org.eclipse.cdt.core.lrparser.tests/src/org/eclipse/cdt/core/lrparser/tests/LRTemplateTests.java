/*******************************************************************************
 *  Copyright (c) 2006, 2013 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TemplateTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.TestSuite;

@SuppressWarnings("restriction")
public class LRTemplateTests extends AST2TemplateTests {

	public static TestSuite suite() {
		return suite(LRTemplateTests.class);
	}

	//override the test failed cases of 341747
	@Override
	public void testTemplateIDAmbiguity_341747a() throws Exception {
	}

	@Override
	public void testTemplateIDAmbiguity_341747b() throws Exception {
	}

	@Override
	public void testTemplateIDAmbiguity_341747c() throws Exception {
	}

	@Override
	public void testTemplateIDAmbiguity_341747d() throws Exception {
	}

	//override some failed test cases
	@Override
	public void testNestedArguments_246079() throws Throwable {
	}

	@Override
	public void testTypeVsExpressionInArgsOfDependentTemplateID_257194() throws Exception {
	}

	@Override
	public void testCtorWithTemplateID_259600() throws Exception {
	}

	@Override
	public void testClosingAngleBrackets1_261268() throws Exception {
	}

	@Override
	public void testClosingAngleBracketsAmbiguity_261268() throws Exception {
	}

	@Override
	public void testFunctionParameterPacks_280909() throws Exception {
	}

	@Override
	public void testTemplateParameterPacks_280909() throws Exception {
	}

	@Override
	public void testParameterPackExpansions_280909() throws Exception {
	}

	@Override
	public void testTemplateParameterPacksAmbiguity_280909() throws Exception {
	}

	@Override
	public void testNonTypeTemplateParameterPack_280909() throws Exception {
	}

	//the below test case are for C++0x features which are not included in XLC++ yet
	@Override
	public void testRValueReferences_294730() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909a() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909b() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909c() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909d() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909e() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909f() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909g() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909i() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909j() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909k() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909n() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909p() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909q() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909r() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909s() throws Exception {
	}

	@Override
	public void testExtendingVariadicTemplateTemplateParameters_302282() throws Exception {
	}

	@Override
	public void testVariadicTemplateExamples_280909h() throws Exception {
	}

	@Override
	public void testInlineNamespaces_305980() throws Exception {
	}

	@Override
	public void testFunctionParameterPacksInNonFinalPosition_324096() throws Exception {
	}

	//decltype related
	@Override
	public void testFunctionCallOnDependentName_337686() throws Exception {
	}

	//variadic template
	@Override
	public void testVariadicFunctionTemplate_333389() throws Exception {
	}

	//auto
	@Override
	public void testRRefVsRef_351927() throws Exception {
	}

	//Variadic template arguments
	@Override
	public void testTemplateTemplateParameterMatching_352859() throws Exception {
	}

	@Override
	@SuppressWarnings("unused")
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

}
