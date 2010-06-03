/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
	
	//the below test case are for C++0x features which are not included in XLC++ yet
	
	public void testRValueReference_294730() throws Exception {}
	public void testRValueReferenceTypedefs_294730() throws Exception {}
	public void testDirectBinding_294730() throws Exception {}
	public void testListInitialization_302412a() throws Exception {}
	public void testListInitialization_302412b() throws Exception {}
	public void testListInitialization_302412c() throws Exception {}
	public void testListInitialization_302412d() throws Exception {}
	public void testListInitialization_302412e() throws Exception {}
	public void testListInitialization_302412f() throws Exception {}
	public void testScopedEnums_305975a() throws Exception {}
	public void testScopedEnums_305975b() throws Exception {}
	public void testScopedEnums_305975c() throws Exception {}
	public void testScopedEnums_305975d() throws Exception {}
	public void testScopedEnums_305975e() throws Exception {}
	public void testScopedEnums_305975g() throws Exception {}
	
	//unicode character type
	public void testNewCharacterTypes_305976() throws Exception {}
	
	//auto type
	public void testAutoType_289542() throws Exception {}
	public void testAutoType_305970() throws Exception {}
	public void testAutoType_305987() throws Exception {}
	public void testNewFunctionDeclaratorSyntax_305972() throws Exception {}
	
	//DeclType
	public void testDeclType_294730() throws Exception {}
	
	
	
	//TODO ??? overwrite some failed test cases
	public void testOrderOfAmbiguityResolution_259373() throws Exception {}
	public void testPureVirtualVsInitDeclarator_267184() throws Exception {}
	public void testDeclarationAmbiguity_Bug269953() throws Exception {}
	public void testInitSyntax_302412() throws Exception {}
	
	protected ILanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}
	
	protected ILanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}
}
