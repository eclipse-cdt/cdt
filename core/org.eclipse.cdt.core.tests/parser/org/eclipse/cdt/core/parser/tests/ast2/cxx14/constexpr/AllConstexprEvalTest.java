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

public class AllConstexprEvalTests {
	public static Test suite() throws Exception {
		final TestSuite suite = new TestSuite();
		suite.addTest(ConstructorTests.NonIndexing.suite());
		suite.addTest(ConstructorTests.SingleProject.suite());
		suite.addTest(MemberFunctionTests.NonIndexing.suite());
		suite.addTest(MemberFunctionTests.SingleProject.suite());
		suite.addTest(MemberVariableTests.NonIndexing.suite());
		suite.addTest(MemberVariableTests.SingleProject.suite());
		suite.addTest(FunctionTests.NonIndexing.suite());
		suite.addTest(FunctionTests.SingleProject.suite());
		suite.addTest(FunctionTemplateTests.NonIndexing.suite());
		suite.addTest(FunctionTemplateTests.SingleProject.suite());
		suite.addTest(ClassTemplateTests.NonIndexing.suite());
		suite.addTest(ClassTemplateTests.SingleProject.suite());
		suite.addTest(IfStatementTests.NonIndexing.suite());
		suite.addTest(IfStatementTests.SingleProject.suite());
		suite.addTest(SwitchStatementTests.NonIndexing.suite());
		suite.addTest(SwitchStatementTests.SingleProject.suite());
		suite.addTest(WhileStatementTests.NonIndexing.suite());
		suite.addTest(WhileStatementTests.SingleProject.suite());
		suite.addTest(DoWhileStatementTests.NonIndexing.suite());
		suite.addTest(DoWhileStatementTests.SingleProject.suite());
		suite.addTest(ForStatementTests.NonIndexing.suite());
		suite.addTest(ForStatementTests.SingleProject.suite());
		suite.addTest(RangeBasedForStatementTests.NonIndexing.suite());
		suite.addTest(RangeBasedForStatementTests.SingleProject.suite());
		suite.addTest(BinaryOperatorOverloadingTests.NonIndexing.suite());
		suite.addTest(BinaryOperatorOverloadingTests.SingleProject.suite());
		suite.addTest(UnaryOperatorOverloadingTests.NonIndexing.suite());
		suite.addTest(UnaryOperatorOverloadingTests.SingleProject.suite());
		suite.addTest(ArrayTests.NonIndexing.suite());
		suite.addTest(ArrayTests.SingleProject.suite());
		suite.addTest(BinaryExpressionTests.NonIndexing.suite());
		suite.addTest(BinaryExpressionTests.SingleProject.suite());
		suite.addTest(UnaryExpressionTests.NonIndexing.suite());
		suite.addTest(UnaryExpressionTests.SingleProject.suite());
		suite.addTest(ReferenceTests.NonIndexing.suite());
		suite.addTest(ReferenceTests.SingleProject.suite());
		suite.addTest(TypeAliasTests.NonIndexing.suite());
		suite.addTest(TypeAliasTests.SingleProject.suite());
		suite.addTest(PointerTests.NonIndexing.suite());
		suite.addTest(PointerTests.SingleProject.suite());
		suite.addTest(UserDefinedLiteralTests.NonIndexing.suite());
		suite.addTest(UserDefinedLiteralTests.SingleProject.suite());
		suite.addTest(IntegralValueTests.NonIndexing.suite());
		suite.addTest(IntegralValueTests.SingleProject.suite());
		suite.addTest(FloatingPointValueTests.NonIndexing.suite());
		suite.addTest(FloatingPointValueTests.SingleProject.suite());
		suite.addTest(CStringValueTests.NonIndexing.suite());
		suite.addTest(CStringValueTests.SingleProject.suite());
		suite.addTest(StructuredBindingTests.NonIndexing.suite());
		suite.addTest(StructuredBindingTests.SingleProject.suite());
		return suite;
	}
}
