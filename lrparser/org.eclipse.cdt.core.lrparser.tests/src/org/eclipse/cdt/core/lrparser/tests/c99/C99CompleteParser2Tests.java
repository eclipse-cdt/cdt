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
import org.eclipse.cdt.core.parser.tests.ast2.CompleteParser2Tests;

public class C99CompleteParser2Tests extends CompleteParser2Tests {

	protected IASTTranslationUnit parse(String code, boolean expectedToPass,
			ParserLanguage lang, boolean gcc) throws Exception {
		
		if(lang != ParserLanguage.C)
			return super.parse(code, expectedToPass, lang, gcc);
		
		return ParseHelper.parse(code, getLanguage(), expectedToPass);
	}

	protected BaseExtensibleLanguage getLanguage() {
    	return C99Language.getDefault();
    }
	
	
	// Tests that are failing at this point
    
	public void testBug39676_tough() { // is this C99?
		try {
			super.testBug39676_tough();
		} catch(AssertionFailedError _) {
			return;
		} catch(Exception _) {
			return;
		}
		
		fail();
	} 
	
//	public void testPredefinedSymbol_bug70928_infinite_loop_test1() throws Exception { // gcc extension
//		try {
//			super.testPredefinedSymbol_bug70928_infinite_loop_test1();
//			fail();
//		} catch(AssertionError _) { }
//	}
//	
//	public void testPredefinedSymbol_bug70928_infinite_loop_test2() throws Exception { // gcc extension
//		try {
//			super.testPredefinedSymbol_bug70928_infinite_loop_test2();
//			fail();
//		} catch(AssertionError _) { }
//	}
//	
//	
//	public void testBug102376() throws Exception { // gcc extension
//		try {
//			super.testBug102376();
//			fail();
//		} catch(AssertionError _) { }
//	}
	
}
