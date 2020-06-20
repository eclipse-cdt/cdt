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

public class AllConstexprEvalTest {
	public static Test suite() throws Exception {
		final TestSuite suite = new TestSuite();
		suite.addTest(ConstructorTest.NonIndexing.suite());
		suite.addTest(ConstructorTest.SingleProject.suite());
		suite.addTest(MemberFunctionTest.NonIndexing.suite());
		suite.addTest(MemberFunctionTest.SingleProject.suite());
		suite.addTest(MemberVariableTest.NonIndexing.suite());
		suite.addTest(MemberVariableTest.SingleProject.suite());
		suite.addTest(FunctionTest.NonIndexing.suite());
		suite.addTest(FunctionTest.SingleProject.suite());
		suite.addTest(FunctionTemplateTest.NonIndexing.suite());
		suite.addTest(FunctionTemplateTest.SingleProject.suite());
		suite.addTest(ClassTemplateTest.NonIndexing.suite());
		suite.addTest(ClassTemplateTest.SingleProject.suite());
		suite.addTest(IfStatementTest.NonIndexing.suite());
		suite.addTest(IfStatementTest.SingleProject.suite());
		suite.addTest(SwitchStatementTest.NonIndexing.suite());
		suite.addTest(SwitchStatementTest.SingleProject.suite());
		suite.addTest(WhileStatementTest.NonIndexing.suite());
		suite.addTest(WhileStatementTest.SingleProject.suite());
		suite.addTest(DoWhileStatementTest.NonIndexing.suite());
		suite.addTest(DoWhileStatementTest.SingleProject.suite());
		suite.addTest(ForStatementTest.NonIndexing.suite());
		suite.addTest(ForStatementTest.SingleProject.suite());
		suite.addTest(RangeBasedForStatementTest.NonIndexing.suite());
		suite.addTest(RangeBasedForStatementTest.SingleProject.suite());
		suite.addTest(BinaryOperatorOverloadingTest.NonIndexing.suite());
		suite.addTest(BinaryOperatorOverloadingTest.SingleProject.suite());
		suite.addTest(UnaryOperatorOverloadingTest.NonIndexing.suite());
		suite.addTest(UnaryOperatorOverloadingTest.SingleProject.suite());
		suite.addTest(ArrayTest.NonIndexing.suite());
		suite.addTest(ArrayTest.SingleProject.suite());
		suite.addTest(BinaryExpressionTest.NonIndexing.suite());
		suite.addTest(BinaryExpressionTest.SingleProject.suite());
		suite.addTest(UnaryExpressionTest.NonIndexing.suite());
		suite.addTest(UnaryExpressionTest.SingleProject.suite());
		suite.addTest(ReferenceTest.NonIndexing.suite());
		suite.addTest(ReferenceTest.SingleProject.suite());
		suite.addTest(TypeAliasTest.NonIndexing.suite());
		suite.addTest(TypeAliasTest.SingleProject.suite());
		suite.addTest(PointerTest.NonIndexing.suite());
		suite.addTest(PointerTest.SingleProject.suite());
		suite.addTest(UserDefinedLiteralTest.NonIndexing.suite());
		suite.addTest(UserDefinedLiteralTest.SingleProject.suite());
		suite.addTest(IntegralValueTest.NonIndexing.suite());
		suite.addTest(IntegralValueTest.SingleProject.suite());
		suite.addTest(FloatingPointValueTest.NonIndexing.suite());
		suite.addTest(FloatingPointValueTest.SingleProject.suite());
		suite.addTest(CStringValueTest.NonIndexing.suite());
		suite.addTest(CStringValueTest.SingleProject.suite());
		suite.addTest(StructuredBindingTest.NonIndexing.suite());
		suite.addTest(StructuredBindingTest.SingleProject.suite());
		return suite;
	}
}
