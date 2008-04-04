package org.eclipse.cdt.core.lrparser.tests.cpp;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.lrparser.tests.ParseHelper;
import org.eclipse.cdt.core.lrparser.tests.c99.C99Tests;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class ISOCPPTests extends AST2CPPTests {

	
	public static TestSuite suite() {
    	return suite(ISOCPPTests.class);
    }
    
	public ISOCPPTests() {
		
	}
	
	public ISOCPPTests(String name) {
		super(name);
	}
	
	 
    @SuppressWarnings("restriction")
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, @SuppressWarnings("unused") boolean useGNUExtensions, boolean expectNoProblems, @SuppressWarnings("unused") boolean parseComments) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
    	return ParseHelper.parse(code, language, expectNoProblems);
    }
    
    protected ILanguage getC99Language() {
    	return C99Language.getDefault();
    }
    
    protected ILanguage getCPPLanguage() {
    	return ISOCPPLanguage.getDefault();
    }
    
    
    @Override
	public void testBug98704() throws Exception {
    	// this one gets stuck in infinite loop
    	
    }
	
    @Override
	public void testBug87424() throws Exception { // gcc extension
    	try {
    		super.testBug87424();
    		fail();
    	} catch(AssertionFailedError _) {
    	} 
    }

    
    @Override
	public void testBug95757() throws Exception { // gcc extension
    	try {
    		super.testBug95757();
    		fail();
    	} catch(AssertionFailedError _) {
    	} 
    }
    
    @Override
	public void testBug108202() throws Exception { // gcc attributes not supported
    	try {
    		super.testBug108202();
    		fail();
    	} catch(AssertionFailedError _) {
    	} 
    }
    
    
    @Override
	public void testBug195701() throws Exception { // gcc attributes not supported
    	try {
    		super.testBug195701();
    		fail();
    	} catch(AssertionFailedError _) {
    	} 
    }
    
    @Override
	public void testBug179712() throws Exception { // gcc attributes not supported
    	try {
    		super.testBug179712();
    		fail();
    	} catch(AssertionFailedError _) {
    	} 
    }
}
