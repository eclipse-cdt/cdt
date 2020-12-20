/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14.constexpr;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @deprecated In preparation for moving to JUnit5 test suites are deprecated. See Bug 569839
 */
@Deprecated
public class AllConstexprEvalTestSuite {
	public static Test suite() throws Exception {
		final TestSuite suite = new TestSuite();
		suite.addTest(ConstructorTests.NonIndexingTests.suite());
		suite.addTest(ConstructorTests.SingleProjectTests.suite());
		suite.addTest(MemberFunctionTests.NonIndexingTests.suite());
		suite.addTest(MemberFunctionTests.SingleProjectTests.suite());
		suite.addTest(MemberVariableTests.NonIndexingTests.suite());
		suite.addTest(MemberVariableTests.SingleProjectTests.suite());
		suite.addTest(FunctionTests.NonIndexingTests.suite());
		suite.addTest(FunctionTests.SingleProjectTests.suite());
		suite.addTest(FunctionTemplateTests.NonIndexingTests.suite());
		suite.addTest(FunctionTemplateTests.SingleProjectTests.suite());
		suite.addTest(ClassTemplateTests.NonIndexingTests.suite());
		suite.addTest(ClassTemplateTests.SingleProjectTests.suite());
		suite.addTest(IfStatementTests.NonIndexingTests.suite());
		suite.addTest(IfStatementTests.SingleProjectTests.suite());
		suite.addTest(SwitchStatementTests.NonIndexingTests.suite());
		suite.addTest(SwitchStatementTests.SingleProjectTests.suite());
		suite.addTest(WhileStatementTests.NonIndexingTests.suite());
		suite.addTest(WhileStatementTests.SingleProjectTests.suite());
		suite.addTest(DoWhileStatementTests.NonIndexingTests.suite());
		suite.addTest(DoWhileStatementTests.SingleProjectTests.suite());
		suite.addTest(ForStatementTests.NonIndexingTests.suite());
		suite.addTest(ForStatementTests.SingleProjectTests.suite());
		suite.addTest(RangeBasedForStatementTests.NonIndexingTests.suite());
		suite.addTest(RangeBasedForStatementTests.SingleProjectTests.suite());
		suite.addTest(BinaryOperatorOverloadingTests.NonIndexingTests.suite());
		suite.addTest(BinaryOperatorOverloadingTests.SingleProjectTests.suite());
		suite.addTest(UnaryOperatorOverloadingTests.NonIndexingTests.suite());
		suite.addTest(UnaryOperatorOverloadingTests.SingleProjectTests.suite());
		suite.addTest(ArrayTests.NonIndexingTests.suite());
		suite.addTest(ArrayTests.SingleProjectTests.suite());
		suite.addTest(BinaryExpressionTests.NonIndexingTests.suite());
		suite.addTest(BinaryExpressionTests.SingleProjectTests.suite());
		suite.addTest(UnaryExpressionTests.NonIndexingTests.suite());
		suite.addTest(UnaryExpressionTests.SingleProjectTests.suite());
		suite.addTest(ReferenceTests.NonIndexingTests.suite());
		suite.addTest(ReferenceTests.SingleProjectTests.suite());
		suite.addTest(TypeAliasTests.NonIndexingTests.suite());
		suite.addTest(TypeAliasTests.SingleProjectTests.suite());
		suite.addTest(PointerTests.NonIndexingTests.suite());
		suite.addTest(PointerTests.SingleProjectTests.suite());
		suite.addTest(UserDefinedLiteralTests.NonIndexingTests.suite());
		suite.addTest(UserDefinedLiteralTests.SingleProjectTests.suite());
		suite.addTest(IntegralValueTests.NonIndexingTests.suite());
		suite.addTest(IntegralValueTests.SingleProjectTests.suite());
		suite.addTest(FloatingPointValueTests.NonIndexingTests.suite());
		suite.addTest(FloatingPointValueTests.SingleProjectTests.suite());
		suite.addTest(CStringValueTests.NonIndexingTests.suite());
		suite.addTest(CStringValueTests.SingleProjectTests.suite());
		suite.addTest(StructuredBindingTests.NonIndexingTests.suite());
		suite.addTest(StructuredBindingTests.SingleProjectTests.suite());
		return suite;
	}
}
