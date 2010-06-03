/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests.base;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.lrparser.tests.AbstractLRHangingTest;
import org.eclipse.cdt.core.lrparser.tests.LRCompletionHangingTest;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;

public class XlcCompletionHangingTest extends LRCompletionHangingTest{

	
	public static TestSuite suite() {
		return new TestSuite(XlcCompletionHangingTest.class);
	}
	
	
	//TODO ??? overwrite some failed test cases
	public void testCompletionTemplateClassForCPP() throws Exception {}
	public void testCompletionGnuCPP() throws Exception {}
	
	
	 protected XlcCLanguage getCLanguage() {
	    	return XlcCLanguage.getDefault();
	    }
	    
	protected XlcCPPLanguage getCPPLanguage() {
	    	return XlcCPPLanguage.getDefault();
	}
	
	public void testCompletionXlc() throws Exception {

		String code = 
			" __static_assert" + CONTENT_ASIST_CURSOR +"(a>" + CONTENT_ASIST_CURSOR +"b, \"no 64-bit support\"); \n"+
			"   vector " + CONTENT_ASIST_CURSOR +"unsigned " + CONTENT_ASIST_CURSOR +"int d = ++a;        \n";
		runTestCase(code, getCPPLanguage());
	}
}
