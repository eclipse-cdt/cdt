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
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMLocationTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99DOMLocationTests extends DOMLocationTests {

	public C99DOMLocationTests() { }
	public C99DOMLocationTests(String name) { super(name); }
	 
    @Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) 
    throws ParserException {
    	if(lang != ParserLanguage.C)
    		return super.parse(code, lang, useGNUExtensions, expectNoProblems);
		
    	return ParseHelper.parse(code, getLanguage(), expectNoProblems);
    }
    
    
    protected BaseExtensibleLanguage getLanguage() {
    	return C99Language.getDefault();
    }
    
    
    // this one fails because the C99 parser does error recovery differently
    public void test162180_1() throws Exception {
    	try {
    		super.test162180_1();
    		fail();
    	}
    	catch(AssertionFailedError e) {}
    	
    }
    
    public void test162180_3() throws Exception {
    	try {
    		super.test162180_3();
    		fail();
    	}
    	catch(AssertionFailedError e) {}
    }
}
