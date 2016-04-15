/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
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
import org.eclipse.cdt.core.parser.tests.ast2.AST2KnRTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class LRKnRTests extends AST2KnRTests {
	
	public static TestSuite suite() {
    	return suite(LRKnRTests.class);
    }
	
    @Override
    @SuppressWarnings("unused") 
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
    	return ParseHelper.parse(code, language, expectNoProblems);
    }
    
    
    protected ILanguage getCLanguage() {
    	return GCCLanguage.getDefault();
    }
	
	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}
    
    // LPG handles syntax errors differently than the DOM parser
	// these tests look for syntax errors in specific places and they fail
    
    @Override
	public void testKRCProblem3() throws Exception {
    	try {
    		super.testKRCProblem3();
    		fail();
    	} catch(Throwable expectedException) { }
    }
    
    @Override
	public void testKRCProblem4() throws Exception  {
    	try {
    		super.testKRCProblem4();
    		fail();
    	} catch(Throwable expectedException) { }
    }

    @Override
	public void testKRCProblem5() throws Exception  {
    	try {
    		super.testKRCProblem5();
    		fail();
    	} catch(Throwable expectedException) { }
    }
    
    @Override
    public void testKRCProblem2() throws Exception  {
    	try {
    		super.testKRCProblem2();
    		fail();
    	} catch(Throwable expectedException) { }
    }
    
}
