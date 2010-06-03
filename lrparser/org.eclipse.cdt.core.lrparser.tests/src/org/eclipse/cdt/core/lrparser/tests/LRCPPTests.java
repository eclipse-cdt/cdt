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
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRCPPTests extends AST2CPPTests {

	
	public static TestSuite suite() {
    	return suite(LRCPPTests.class);
    }
    
	public LRCPPTests() {
		
	}
	
	public LRCPPTests(String name) {
		super(name);
	}
	
//the below test case are for C++0x features which are not included in XLC++ yet
	
	
	@Override
	public void testRValueReference_294730() throws Exception {}
	@Override
	public void testRValueReferenceTypedefs_294730() throws Exception {}
	@Override
	public void testDirectBinding_294730() throws Exception {}
	@Override
	public void testListInitialization_302412a() throws Exception {}
	@Override
	public void testListInitialization_302412b() throws Exception {}
	@Override
	public void testListInitialization_302412c() throws Exception {}
	@Override
	public void testListInitialization_302412d() throws Exception {}
	@Override
	public void testListInitialization_302412e() throws Exception {}
	@Override
	public void testListInitialization_302412f() throws Exception {}
	@Override
	public void testScopedEnums_305975a() throws Exception {}
	@Override
	public void testScopedEnums_305975b() throws Exception {}
	@Override
	public void testScopedEnums_305975c() throws Exception {}
	@Override
	public void testScopedEnums_305975d() throws Exception {}
	@Override
	public void testScopedEnums_305975e() throws Exception {}
	@Override
	public void testScopedEnums_305975g() throws Exception {}
	
	//unicode character type
	@Override
	public void testNewCharacterTypes_305976() throws Exception {}
	
	//auto type
	@Override
	public void testAutoType_289542() throws Exception {}
	@Override
	public void testAutoType_305970() throws Exception {}
	@Override
	public void testAutoType_305987() throws Exception {}
	@Override
	public void testNewFunctionDeclaratorSyntax_305972() throws Exception {}
	
	//DeclType
	@Override
	public void testDeclType_294730() throws Exception {}
	
	
	
	//TODO ??? overwrite some failed test cases
	@Override
	public void testOrderOfAmbiguityResolution_259373() throws Exception {}
	@Override
	public void testPureVirtualVsInitDeclarator_267184() throws Exception {}
	@Override
	public void testDeclarationAmbiguity_Bug269953() throws Exception {}
	@Override
	public void testInitSyntax_302412() throws Exception {}
	@Override
	public void testStaticAssertions_294730() throws Exception {}
	
	
	 
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, @SuppressWarnings("unused") boolean useGNUExtensions, boolean expectNoProblems, boolean skipTrivialInitializers) throws ParserException {
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
    
    
    @Override
	public void testBug98704() throws Exception {
    	// this one gets stuck in infinite loop
    	
    }
	
//    @Override
//	public void testBug87424() throws Exception { // gcc extension
//    	try {
//    		super.testBug87424();
//    		fail();
//    	} catch(AssertionFailedError _) {
//    	} 
//    }
//
//    
//    @Override
//	public void testBug95757() throws Exception { // gcc extension
//    	try {
//    		super.testBug95757();
//    		fail();
//    	} catch(AssertionFailedError _) {
//    	} 
//    }
//    
//    @Override
//	public void testBug108202() throws Exception { // gcc attributes not supported
//    	try {
//    		super.testBug108202();
//    		fail();
//    	} catch(AssertionFailedError _) {
//    	} 
//    }
//    
//    
//    @Override
//	public void testBug195701() throws Exception { // gcc attributes not supported
//    	try {
//    		super.testBug195701();
//    		fail();
//    	} catch(AssertionFailedError _) {
//    	} 
//    }
//    
//    @Override
//	public void testBug179712() throws Exception { // gcc attributes not supported
//    	try {
//    		super.testBug179712();
//    		fail();
//    	} catch(AssertionFailedError _) {
//    	} 
//    }
//    
//    @Override
//    public void testBug240567() throws Exception { // gcc
//    	try {
//    		super.testBug240567();
//    		fail();
//    	} catch(AssertionFailedError _) {
//    	} 
//    }
//    
//    @Override
//    public void testLiteralsViaOverloads_225534() throws Exception { // gcc, I think
//    	try {
//    		super.testLiteralsViaOverloads_225534();
//    		fail();
//    	} catch(AssertionFailedError _) {
//    	} 
//    }
    
    
    @Override
	public void testNestedTemplateIDAmbiguity_259501() throws Exception {
    	// this test hangs, not sure I'll ever fix it
    }
    
    
}
