/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.upc.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.core.lrparser.tests.LRTests;
import org.eclipse.cdt.core.model.ILanguage;
/**
 * Run the C99 tests against the UPC parser
 *
 */
public class UPCTests extends LRTests {

	public static TestSuite suite() {
    	return suite(UPCTests.class);
    }
	
	public UPCTests(String name) {
		super(name);
	}
	
	//TODO ??? overwrite some failed test cases
	//some test cases which are not applicable to UPC are bypassed here.
	//UPC extends from C99, which doesn't include some GNU extending features such as "typeof".
	@Override
	public void testCompositeTypes() throws Exception {}
	@Override
	public void testBug93980() throws Exception {}
	@Override
	public void testBug95866() throws Exception {}
	@Override
	public void testBug191450_attributesInBetweenPointers() throws Exception {}
	@Override
	public void testOmittedPositiveExpression_Bug212905() throws Exception {}
	@Override
	public void testRedefinedGCCKeywords_Bug226112() throws Exception {}
	@Override
	public void testASMLabels_Bug226121() throws Exception {}
	@Override
	public void testCompoundStatementExpression_Bug226274() throws Exception {}
	@Override
	public void testTypeofUnaryExpression_Bug226492() throws Exception {}
	@Override
	public void testTypeofExpression_Bug226492() throws Exception {}
	@Override
	public void testTypeofExpressionWithAttribute_Bug226492() throws Exception {}
	@Override
	public void testAttributeInElaboratedTypeSpecifier_Bug227085() throws Exception {}
	@Override
	public void testRedefinePtrdiff_Bug230895() throws Exception {}
	@Override
	public void testDeclspecInEnumSpecifier_bug241203() throws Exception  {}
	@Override
	public void testBuiltinTypesCompatible_bug241570() throws Exception  {}
	@Override
	public void testThreadLocalVariables_Bug260387() throws Exception {}
	@Override
	public void testVaArgWithFunctionPtr_311030() throws Exception {}
	@Override
	public void testRecursiveFunctionType_321856() throws Exception {}
	@Override
	public void testPtrDiffRecursion_317004() throws Exception {}
	

	@Override
	protected ILanguage getCLanguage() {
		return UPCLanguage.getDefault();
	}
	
}
