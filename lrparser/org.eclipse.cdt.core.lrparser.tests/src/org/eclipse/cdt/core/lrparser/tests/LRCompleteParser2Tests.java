/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.CompleteParser2Tests;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

public class LRCompleteParser2Tests extends CompleteParser2Tests {

	public static TestSuite suite() {
		return suite(LRCompleteParser2Tests.class);
	}

	public LRCompleteParser2Tests() {
	}

	public LRCompleteParser2Tests(String name) {
		super(name);
	}

	@Override
	@SuppressWarnings("unused")
	protected IASTTranslationUnit parse(String code, boolean expectedToPass, ParserLanguage lang, boolean gcc)
			throws Exception {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		return ParseHelper.parse(code, language, expectedToPass);
	}

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

	// Tests that are failing at this point

	//	@Override
	//	public void testBug39676_tough() { // is this C99?
	//		try {
	//			super.testBug39676_tough();
	//		} catch(AssertionFailedError expectedException) {
	//			return;
	//		} catch(Exception expectedException) {
	//			return;
	//		}
	//		fail();
	//	}

	//	public void testPredefinedSymbol_bug70928_infinite_loop_test1() throws Exception { // gcc extension
	//		try {
	//			super.testPredefinedSymbol_bug70928_infinite_loop_test1();
	//			fail();
	//		} catch(AssertionError expectedException) { }
	//	}
	//
	//	public void testPredefinedSymbol_bug70928_infinite_loop_test2() throws Exception { // gcc extension
	//		try {
	//			super.testPredefinedSymbol_bug70928_infinite_loop_test2();
	//			fail();
	//		} catch(AssertionError expectedException) { }
	//	}

	//	@Override
	//	public void testBug102376() throws Exception { // gcc extension
	//		try {
	//			super.testBug102376();
	//			fail();
	//		} catch(AssertionFailedError expectedException) { }
	//	}

	//	@Override
	//	public void test158192_declspec_in_declarator() throws Exception {
	//		try {
	//			super.test158192_declspec_in_declarator();
	//			fail();
	//		} catch(AssertionFailedError expectedException) { }
	//	}
	//
	//	@Override
	//	public void test158192_declspec_on_class() throws Exception {
	//		try {
	//			super.test158192_declspec_on_class();
	//			fail();
	//		} catch(AssertionFailedError expectedException) { }
	//	}
	//
	//	@Override
	//	public void test158192_declspec_on_variable() throws Exception {
	//		try {
	//			super.test158192_declspec_on_variable();
	//			fail();
	//		} catch(AssertionFailedError expectedException) { }
	//	}
	//
	//	@Override
	//	public void testPredefinedSymbol_bug70928() throws Exception {
	//		try {
	//			super.testPredefinedSymbol_bug70928();
	//			fail();
	//		} catch(AssertionFailedError expectedException) { }
	//	}

	@Override
	public void testBug64010() throws Exception { // 10000 else-ifs, busts LPG's stack
		try {
			//super.testBug64010();
			//fail();
		} catch (AssertionFailedError expectedException) {
		}
	}

	//
	//	@Override
	//	public void testGNUASMExtension() throws Exception {
	//		try {
	//			super.testGNUASMExtension();
	//			fail();
	//		} catch(AssertionFailedError expectedException) {
	//		} catch(AssertionError expectedException) {
	//		}
	//	}
	//
	//	@Override
	//	public void testBug39551B() throws Exception {
	//		try {
	//			super.testBug39551B();
	//			fail();
	//		} catch(AssertionFailedError expectedException) { }
	//	}
	//
	//
	//

}
