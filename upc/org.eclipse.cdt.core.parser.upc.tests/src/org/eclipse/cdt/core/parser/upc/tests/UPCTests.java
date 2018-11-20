/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
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
package org.eclipse.cdt.core.parser.upc.tests;

import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.core.lrparser.tests.LRTests;
import org.eclipse.cdt.core.model.ILanguage;

import junit.framework.TestSuite;

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
	public void testCompositeTypes() throws Exception {
	}

	@Override
	public void testBug93980() throws Exception {
	}

	@Override
	public void testBug95866() throws Exception {
	}

	@Override
	public void testBug191450_attributesInBetweenPointers() throws Exception {
	}

	@Override
	public void testOmittedPositiveExpression_212905() throws Exception {
	}

	@Override
	public void testRedefinedGCCKeywords_226112() throws Exception {
	}

	@Override
	public void testASMLabels_226121() throws Exception {
	}

	@Override
	public void testCompoundStatementExpression_226274() throws Exception {
	}

	@Override
	public void testTypeofUnaryExpression_226492() throws Exception {
	}

	@Override
	public void testTypeofExpression_226492() throws Exception {
	}

	@Override
	public void testTypeofExpressionWithAttribute_226492() throws Exception {
	}

	@Override
	public void testAttributeInElaboratedTypeSpecifier_227085() throws Exception {
	}

	@Override
	public void testRedefinePtrdiff_230895() throws Exception {
	}

	@Override
	public void testDeclspecInEnumSpecifier_241203() throws Exception {
	}

	@Override
	public void testBuiltinTypesCompatible_241570() throws Exception {
	}

	@Override
	public void testThreadLocalVariables_260387() throws Exception {
	}

	@Override
	public void testVaArgWithFunctionPtr_311030() throws Exception {
	}

	@Override
	public void testRecursiveFunctionType_321856() throws Exception {
	}

	@Override
	public void testPtrDiffRecursion_317004() throws Exception {
	}

	@Override
	protected ILanguage getCLanguage() {
		return UPCLanguage.getDefault();
	}
}
