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
package org.eclipse.cdt.core.lrparser.tests.c99;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.lrparser.tests.ParseHelper;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMLocationTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99DOMLocationTests extends DOMLocationTests {

	public C99DOMLocationTests() { }
	public C99DOMLocationTests(String name) { super(name); }
	 
    @Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
    	return ParseHelper.parse(code, language, expectNoProblems);
    }
    
    protected ILanguage getC99Language() {
    	return C99Language.getDefault();
    }
	
	protected ILanguage getCPPLanguage() {
		return ISOCPPLanguage.getDefault();
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
    
    @Override
	public void test162180_3() throws Exception {
    	try {
    		super.test162180_3();
    		fail();
    	}
    	catch(AssertionFailedError e) {}
    }
    
    @Override
	public void testBug86698_2() throws Exception { // I don't think C++ supports nested functions
    	try {
    		super.testBug86698_2();
    		fail();
    	}
    	catch(AssertionFailedError e) {}
    }

}
