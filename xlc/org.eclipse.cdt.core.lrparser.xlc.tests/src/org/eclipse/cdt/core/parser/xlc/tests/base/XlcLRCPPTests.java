/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests.base;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.lrparser.tests.LRCPPTests;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;

public class XlcLRCPPTests extends LRCPPTests {
	public static TestSuite suite() {
		return suite(XlcLRCPPTests.class);
	}
	//CDT_70_FIX_FROM_50-#9
	public void testStaticAssertions_294730() throws Exception {
		String code= getAboveComment();
		code = code.replaceAll("static_assert", "__static_assert");
		parseAndCheckBindings(code, ParserLanguage.CPP);
	}
	@Override  //invalid case for xlc parser
	public void testRestrictIsNoCPPKeyword_Bug228826() throws Exception {}
	
	protected ILanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}
	
	protected ILanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}
}
