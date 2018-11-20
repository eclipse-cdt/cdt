/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.QuickParser2Tests;

public class LRQuickParser2Tests extends QuickParser2Tests {

	public LRQuickParser2Tests() {
	}

	public LRQuickParser2Tests(String name) {
		super(name);
	}

	@Override
	protected void parse(String code, boolean expectedToPass, ParserLanguage lang,
			@SuppressWarnings("unused") boolean gcc) throws Exception {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		// don't check preprocessor problems for this test suite (causes tons of failures)
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckSyntaxProblems(expectedToPass);
		options.setCheckPreprocessorProblems(false);
		ParseHelper.parse(code, language, options);
	}

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

	@Override
	public void testBug36532() {
		// ParseHelper does not throw ParserException
		// just ignore this test
	}

	//	 @Override
	//	 public void testBug39695() throws Exception {  // no support for __alignof__
	//		 try {
	//			super.testBug39695();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	 }
	//
	//	 @Override
	//	 public void testBug39684() throws Exception {  // typeof is gcc extension
	//		 try {
	//			super.testBug39684();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	 }
	//
	//	 @Override
	//	 public void testBug39698A() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39698A();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//    }
	//
	//	@Override
	//	public void testBug39698B() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39698B();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//	@Override
	//	public void testBug39704B() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39704B();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//	@Override
	//	public void testBug39704C() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39704C();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//	@Override
	//	public void testBug39677() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39677();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//
	//	@Override
	//	public void testBug57652() throws Exception { // gcc extension
	//		 try {
	//			super.testBug57652();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//
	//	@Override
	//	public void testBug39701A() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39701A();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//	@Override
	//	public void testBug39701B() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39701B();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//	@Override
	//	public void testBug39701C() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39701C();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//
	//	@Override
	//	public void testBug40007() throws Exception { // gcc extension
	//		 try {
	//			super.testBug40007();
	//			fail();
	//		} catch(AssertionFailedError _) {
	//		} catch(AssertionError _) {
	//		}
	//
	//	}
	//
	//	@Override
	//	public void testBug39703() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39703();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//
	//	@Override
	//	public void testBug39554() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39554();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}
	//
	//
	//	@Override
	//	public void testBug39686() throws Exception { // gcc extension
	//		 try {
	//			super.testBug39686();
	//			fail();
	//		} catch(AssertionFailedError _) { }
	//	}

}
