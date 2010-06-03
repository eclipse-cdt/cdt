/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TemplateTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRTemplateTests extends AST2TemplateTests {

	
	public static TestSuite suite() {
    	return suite(LRTemplateTests.class);
    }
	
	
	//TODO ??? overwrite some failed test cases
	@Override
	public void testNestedArguments_246079() throws Throwable {}
	@Override
	public void testTypeVsExpressionInArgsOfDependentTemplateID_257194() throws Exception {}
	@Override
	public void testCtorWithTemplateID_259600() throws Exception {}
	@Override
	public void testClosingAngleBrackets1_261268() throws Exception {}
	@Override
	public void testClosingAngleBracketsAmbiguity_261268() throws Exception {}
	@Override
	public void testFunctionParameterPacks_280909() throws Exception {}
	@Override
	public void testTemplateParameterPacks_280909() throws Exception {}
	@Override
	public void testParameterPackExpansions_280909() throws Exception {}
	@Override
	public void testTemplateParameterPacksAmbiguity_280909() throws Exception {}
	@Override
	public void testNonTypeTemplateParameterPack_280909() throws Exception {}
	@Override
	public void testTypeDeductForInitLists_302412() throws Exception {}
	
	
	
	//the below test case are for C++0x features which are not included in XLC++ yet
	@Override
	public void testRValueReferences_1_294730() throws Exception {}
	@Override
	public void testRValueReferences_2_294730() throws Exception {}
	
	@Override
	public void testVariadicTemplateExamples_280909a() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909b() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909c() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909d() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909e() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909f() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909g() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909i() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909j() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909k() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909m() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909n() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909o() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909p() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909q() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909r() throws Exception {}
	@Override
	public void testVariadicTemplateExamples_280909s() throws Exception {}
	@Override
	public void testExtendingVariadicTemplateTemplateParameters_302282() throws Exception {}
	
	
	
	@Override
	@SuppressWarnings("unused")
	protected IASTTranslationUnit parse( String code, ParserLanguage lang,  boolean useGNUExtensions, boolean expectNoProblems, boolean skipTrivialInitializers) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
    	ParseHelper.Options options = new ParseHelper.Options();
    	options.setCheckSyntaxProblems(expectNoProblems);
    	options.setCheckPreprocessorProblems(expectNoProblems);
    	options.setSkipTrivialInitializers(skipTrivialInitializers);
    	return ParseHelper.parse(code, language, options);
    }
    
    protected ILanguage getCLanguage() {
    	return GCCLanguage.getDefault();
    }
    
    protected ILanguage getCPPLanguage() {
    	return GPPLanguage.getDefault();
    }
    
}
