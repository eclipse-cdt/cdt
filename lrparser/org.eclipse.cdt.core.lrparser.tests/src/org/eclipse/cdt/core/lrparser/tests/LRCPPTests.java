/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
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
	
	 
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, @SuppressWarnings("unused") boolean useGNUExtensions, boolean expectNoProblems, boolean skipTrivialInitializers) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
    	ParseHelper.Options options = new ParseHelper.Options();
    	options.setCheckSyntaxProblems(expectNoProblems);
    	options.setCheckPreprocessorProblems(expectNoProblems);
    	options.setSkipTrivialInitializers(skipTrivialInitializers);
    	return ParseHelper.parse(code, language, options);
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
    
    @Override
    public void testBug240567() throws Exception { // gcc
    	try {
    		super.testBug240567();
    		fail();
    	} catch(AssertionFailedError _) {
    	} 
    }
    
    @Override
    public void testLiteralsViaOverloads_225534() throws Exception { // gcc, I think
    	try {
    		super.testLiteralsViaOverloads_225534();
    		fail();
    	} catch(AssertionFailedError _) {
    	} 
    }
    
    
}
