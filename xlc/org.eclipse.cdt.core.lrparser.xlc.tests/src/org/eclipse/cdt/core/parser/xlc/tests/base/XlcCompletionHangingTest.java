/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
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
package org.eclipse.cdt.core.parser.xlc.tests.base;

import org.eclipse.cdt.core.lrparser.tests.LRCompletionHangingTest;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;

import junit.framework.TestSuite;

public class XlcCompletionHangingTest extends LRCompletionHangingTest {

	public static TestSuite suite() {
		return new TestSuite(XlcCompletionHangingTest.class);
	}

	//TODO ??? overwrite some failed test cases
	@Override
	public void testCompletionTemplateClassForCPP() throws Exception {
	}

	@Override
	public void testCompletionGnuCPP() throws Exception {
	}

	@Override
	protected XlcCLanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}

	@Override
	protected XlcCPPLanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}

	public void testCompletionXlc() throws Exception {

		String code = " __static_assert" + CONTENT_ASIST_CURSOR + "(a>" + CONTENT_ASIST_CURSOR
				+ "b, \"no 64-bit support\"); \n" + "   vector " + CONTENT_ASIST_CURSOR + "unsigned "
				+ CONTENT_ASIST_CURSOR + "int d = ++a;        \n";
		runTestCase(code, getCPPLanguage());
	}
}
