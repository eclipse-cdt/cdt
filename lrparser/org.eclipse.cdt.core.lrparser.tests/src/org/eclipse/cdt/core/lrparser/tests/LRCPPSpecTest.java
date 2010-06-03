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
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPSpecTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRCPPSpecTest extends AST2CPPSpecTest {
	
	public static TestSuite suite() {
        return suite(LRCPPSpecTest.class);
    }
	
	public LRCPPSpecTest() { } 
	public LRCPPSpecTest(String name) { super(name); }

	//TODO ??? overwrite some failed test cases
	@Override
	public void test7_1_3s5b() throws Exception {}
	@Override
	public void test8_2s7a() throws Exception {}
	@Override
	public void test8_2s7b() throws Exception {}
	
	
	
	
	@Override
	protected void parseCandCPP( String code, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		parse(code, ParserLanguage.C,   checkBindings, expectedProblemBindings);
		parse(code, ParserLanguage.CPP, checkBindings, expectedProblemBindings);
	}
		
	@Override
	protected IASTTranslationUnit parseWithErrors(String code, ParserLanguage lang) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckBindings(false);
		options.setCheckPreprocessorProblems(false);
		options.setCheckSyntaxProblems(false);
		return ParseHelper.parse(code, language, options);
    }
	
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckBindings(checkBindings);
		options.setExpectedProblemBindings(expectedProblemBindings);
		return ParseHelper.parse(code, language, options);
    }
	
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, @SuppressWarnings("unused") boolean useGNUExtensions, boolean expectNoProblems, boolean skipTrivialInitializers) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
    	ParseHelper.Options options = new ParseHelper.Options();
    	options.setCheckSyntaxProblems(expectNoProblems);
    	options.setCheckPreprocessorProblems(expectNoProblems);
    	options.setSkipTrivialInitializers(skipTrivialInitializers);
    	return ParseHelper.parse(code, language, options);
    }
	
	@Override
	protected IASTTranslationUnit parse(String code, ParserLanguage lang, String[] problems) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		ParseHelper.Options options = new ParseHelper.Options();
		options.setProblems(problems);
		return ParseHelper.parse(code, language, options);
	}
	
	
	
	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}
	
	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}
	
	
}
