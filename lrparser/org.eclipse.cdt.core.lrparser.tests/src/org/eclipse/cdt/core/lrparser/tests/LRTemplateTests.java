package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TemplateTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRTemplateTests extends AST2TemplateTests {

	
	public static TestSuite suite() {
    	return suite(LRTemplateTests.class);
    }
	
	
	@Override
	@SuppressWarnings("unused")
	protected IASTTranslationUnit parse( String code, ParserLanguage lang,  boolean useGNUExtensions, boolean expectNoProblems, boolean parseComments) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
    	return ParseHelper.parse(code, language, expectNoProblems);
    }
    
    protected ILanguage getC99Language() {
    	return C99Language.getDefault();
    }
    
    protected ILanguage getCPPLanguage() {
    	return ISOCPPLanguage.getDefault();
    }
    
}
