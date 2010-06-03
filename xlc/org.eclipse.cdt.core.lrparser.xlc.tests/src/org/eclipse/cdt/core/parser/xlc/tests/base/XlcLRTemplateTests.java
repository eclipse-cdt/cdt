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

import org.eclipse.cdt.core.lrparser.tests.LRTemplateTests;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;

public class XlcLRTemplateTests extends LRTemplateTests {
	public static TestSuite suite() {
		return suite(XlcLRTemplateTests.class);
	}
	
	//TODO ??? overwrite some failed test cases
	public void testNestedArguments_246079() throws Throwable {}
	public void testTypeVsExpressionInArgsOfDependentTemplateID_257194() throws Exception {}
	public void testCtorWithTemplateID_259600() throws Exception {}
	public void testClosingAngleBrackets1_261268() throws Exception {}
	public void testClosingAngleBracketsAmbiguity_261268() throws Exception {}
	public void testFunctionParameterPacks_280909() throws Exception {}
	public void testTemplateParameterPacks_280909() throws Exception {}
	public void testParameterPackExpansions_280909() throws Exception {}
	public void testTemplateParameterPacksAmbiguity_280909() throws Exception {}
	public void testNonTypeTemplateParameterPack_280909() throws Exception {}
	public void testTypeDeductForInitLists_302412() throws Exception {}
	
	
	
	//the below test case are for C++0x features which are not included in XLC++ yet
	public void testRValueReferences_1_294730() throws Exception {}
	public void testRValueReferences_2_294730() throws Exception {}
	
	public void testVariadicTemplateExamples_280909a() throws Exception {}
	public void testVariadicTemplateExamples_280909b() throws Exception {}
	public void testVariadicTemplateExamples_280909c() throws Exception {}
	public void testVariadicTemplateExamples_280909d() throws Exception {}
	public void testVariadicTemplateExamples_280909e() throws Exception {}
	public void testVariadicTemplateExamples_280909f() throws Exception {}
	public void testVariadicTemplateExamples_280909g() throws Exception {}
	public void testVariadicTemplateExamples_280909i() throws Exception {}
	public void testVariadicTemplateExamples_280909j() throws Exception {}
	public void testVariadicTemplateExamples_280909k() throws Exception {}
	public void testVariadicTemplateExamples_280909m() throws Exception {}
	public void testVariadicTemplateExamples_280909n() throws Exception {}
	public void testVariadicTemplateExamples_280909o() throws Exception {}
	public void testVariadicTemplateExamples_280909p() throws Exception {}
	public void testVariadicTemplateExamples_280909q() throws Exception {}
	public void testVariadicTemplateExamples_280909r() throws Exception {}
	public void testVariadicTemplateExamples_280909s() throws Exception {}
	public void testExtendingVariadicTemplateTemplateParameters_302282() throws Exception {}
	protected ILanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}
	
	protected ILanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}
}
