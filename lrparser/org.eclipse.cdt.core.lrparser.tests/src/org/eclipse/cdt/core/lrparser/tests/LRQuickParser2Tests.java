/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ast2.QuickParser2Tests;
import org.eclipse.cdt.core.parser.tests.scanner.FileCodeReaderFactory;

public class LRQuickParser2Tests extends QuickParser2Tests {

	public LRQuickParser2Tests() {}
	public LRQuickParser2Tests(String name) { super(name); }
	
	
	@Override
	protected void parse(String code, boolean expectedToPass,
			ParserLanguage lang, @SuppressWarnings("unused") boolean gcc) throws Exception {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getC99Language();
		CodeReader reader = new CodeReader(code.toCharArray());
		// don't check preprocessor problems for this test suite (causes tons of failures)
    	ParseHelper.parse(reader, language, new ScannerInfo(), FileCodeReaderFactory.getInstance(), expectedToPass, false, 0, null, false);
	}
	
	
	 protected ILanguage getC99Language() {
	    return C99Language.getDefault();
	 }
	    
	 protected ILanguage getCPPLanguage() {
	  	return ISOCPPLanguage.getDefault();
	 }

	 
	 @Override
	 public void testBug36532() { 
		 // ParseHelper does not throw ParserException
		 // just ignore this test
	 }
	 
	 @Override
	 public void testBug39695() throws Exception {  // no support for __alignof__
		 try {
			super.testBug39695();
			fail();
		} catch(AssertionFailedError _) { }
	 }
	 
	 @Override
	 public void testBug39684() throws Exception {  // typeof is gcc extension
		 try {
			super.testBug39684();
			fail();
		} catch(AssertionFailedError _) { }
	 }
	 
	 @Override
	 public void testBug39698A() throws Exception { // gcc extension
		 try {
			super.testBug39698A();
			fail();
		} catch(AssertionFailedError _) { }
    }

	@Override
	public void testBug39698B() throws Exception { // gcc extension
		 try {
			super.testBug39698B();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	@Override
	public void testBug39704B() throws Exception { // gcc extension
		 try {
			super.testBug39704B();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	@Override
	public void testBug39704C() throws Exception { // gcc extension
		 try {
			super.testBug39704C();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	@Override
	public void testBug39677() throws Exception { // gcc extension
		 try {
			super.testBug39677();
			fail();
		} catch(AssertionFailedError _) { }
	}
	 
	
	@Override
	public void testBug57652() throws Exception { // gcc extension
		 try {
			super.testBug57652();
			fail();
		} catch(AssertionFailedError _) { }
	}

	
	@Override
	public void testBug39701A() throws Exception { // gcc extension
		 try {
			super.testBug39701A();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	@Override
	public void testBug39701B() throws Exception { // gcc extension
		 try {
			super.testBug39701B();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	@Override
	public void testBug39701C() throws Exception { // gcc extension
		 try {
			super.testBug39701C();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	

	
}
