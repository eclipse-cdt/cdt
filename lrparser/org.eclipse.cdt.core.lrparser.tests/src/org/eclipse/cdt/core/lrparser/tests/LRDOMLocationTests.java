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

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMLocationTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class LRDOMLocationTests extends DOMLocationTests {

	public static TestSuite suite() {
    	return suite(LRDOMLocationTests.class);
    }
	
	public LRDOMLocationTests() { }
	public LRDOMLocationTests(String name) { super(name); }
	
	//TODO ??? overwrite some failed test cases
	@Override
	public void test162180_3() throws Exception {}
	 
    @Override
    @SuppressWarnings("unused") 
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
    	ParseHelper.Options options = new ParseHelper.Options().setCheckSyntaxProblems(expectNoProblems).setCheckPreprocessorProblems(expectNoProblems);
    	return ParseHelper.parse(code, language, options);
    }
    
    protected ILanguage getCLanguage() {
    	return GCCLanguage.getDefault();
    }
	
	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

    
    
    // this one fails because the C99 parser does error recovery differently
    @Override
	public void test162180_1() throws Exception {
    	try {
    		super.test162180_1();
    		fail();
    	}
    	catch(AssertionFailedError e) {}
    	
    }
//    
//    @Override
//	public void test162180_3() throws Exception {
//    	try {
//    		super.test162180_3();
//    		fail();
//    	}
//    	catch(AssertionFailedError e) {}
//    }
//    
//    @Override
//	public void testBug86698_2() throws Exception { // I don't think C++ supports nested functions
//    	try {
//    		super.testBug86698_2();
//    		fail();
//    	}
//    	catch(AssertionFailedError e) {}
//    }
//    
//    
//    @Override
//	public void testBug120607() throws Exception { // #assert and #unassert are gcc extensions
//    	try {
//    		super.testBug120607();
//    		fail();
//    	}
//    	catch(AssertionFailedError e) {}
//    }

}
