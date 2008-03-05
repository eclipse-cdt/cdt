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
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;

import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.CompleteParser2Tests;

public class C99CompleteParser2Tests extends CompleteParser2Tests {

	@Override
	protected IASTTranslationUnit parse(String code, boolean expectedToPass,
			ParserLanguage lang, boolean gcc) throws Exception {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
		return ParseHelper.parse(code, language, expectedToPass);
	}

	protected ILanguage getC99Language() {
    	return C99Language.getDefault();
    }
	
	protected ILanguage getCPPLanguage() {
		return ISOCPPLanguage.getDefault();
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

	
	@Override
	public void testBug102376() throws Exception { // gcc extension
		try {
			super.testBug102376();
			fail();
		} catch(AssertionFailedError _) { }
	}

	@Override
	public void test158192_declspec_in_declarator() throws Exception {
		try {
			super.test158192_declspec_in_declarator();
			fail();
		} catch(AssertionFailedError _) { }
	}

	@Override
	public void test158192_declspec_on_class() throws Exception {
		try {
			super.test158192_declspec_on_class();
			fail();
		} catch(AssertionFailedError _) { }
	}

	@Override
	public void test158192_declspec_on_variable() throws Exception {
		try {
			super.test158192_declspec_on_variable();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	@Override
	public void testPredefinedSymbol_bug70928() throws Exception {
		try {
			super.testPredefinedSymbol_bug70928();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	@Override
	public void testBug64010() throws Exception { // 10000 else-ifs, busts LPG's stack
		try {
			super.testBug64010();
			fail();
		} catch(AssertionFailedError _) { }
	}

}
