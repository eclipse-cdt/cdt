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

import org.eclipse.cdt.core.lrparser.tests.LRTests;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;

public class XlcLRTests extends LRTests {

	public XlcLRTests(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	//TODO ??? overwrite some failed test cases
	public void testFnReturningPtrToFn() throws Exception {}
	public void testBug270275_int_is_equivalent_to_signed_int() throws Exception {}
	public void testFunctionDefTypes() throws Exception {}
	public void testBug80171() throws Exception {}
	public void testBug192165() throws Exception {}
	public void testTypenameInExpression() throws Exception {}
	public void testParamWithFunctionType_Bug84242() throws Exception {}
	public void testParamWithFunctionTypeCpp_Bug84242() throws Exception {}
	public void testFunctionReturningPtrToArray_Bug216609() throws Exception {}
	public void testNestedFunctionDeclarators() throws Exception {}
	public void testConstantExpressionBinding() throws Exception {}
	public void testAmbiguousDeclaration_Bug259373() throws Exception {}
	public void testSizeofFunctionType_252243() throws Exception {}
	public void testSkipAggregateInitializer_297550() throws Exception {}
	public void testDeepElseif_298455() throws Exception {}
	public void testAttributeSyntax_298841() throws Exception {}
	public void testEmptyTrailingMacro_303152() throws Exception {}
	
	
	public static TestSuite suite() {
		return suite(XlcLRTests.class);
	}
	
	protected ILanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}
	
	protected ILanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}

}
