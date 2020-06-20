/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Emanuel Graf (IFS)
 *    Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.parser.tests.ast2.cxx14.GenericLambdaTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.InitCaptureTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.ReturnTypeDeductionTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.VariableTemplateTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.DeductionGuideTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.LambdaExpressionTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.StructuredBindingTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.TemplateAutoTest;
import org.eclipse.cdt.core.parser.tests.prefix.CompletionTestSuite;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author jcamelon
 */
public class DOMParserTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(DOMParserTestSuite.class.getName());
		suite.addTest(AST2Test.suite());
		suite.addTestSuite(GCCTest.class);
		suite.addTest(AST2CPPTest.suite());
		suite.addTest(ASTCPPSpecDefectTest.suite());
		suite.addTest(AST2CPPImplicitNameTest.suite());
		suite.addTest(AST2TemplateTest.suite());
		suite.addTest(TypeTraitsTest.suite());
		suite.addTestSuite(QuickParser2Test.class);
		suite.addTest(CompleteParser2Test.suite());
		suite.addTest(DOMLocationTest.suite());
		suite.addTestSuite(DOMLocationMacroTest.class);
		suite.addTest(ImageLocationTest.suite());
		suite.addTestSuite(AST2KnRTest.class);
		suite.addTestSuite(AST2UtilTest.class);
		suite.addTestSuite(AST2UtilOldTest.class);
		suite.addTestSuite(AST2SelectionParseTest.class);
		suite.addTest(ASTNodeSelectorTest.suite());
		suite.addTest(AST2CPPSpecTest.suite());
		suite.addTestSuite(AST2CSpecTest.class);
		suite.addTestSuite(DOMSelectionParseTest.class);
		suite.addTestSuite(GCCCompleteParseExtensionsTest.class);
		suite.addTestSuite(DOMPreprocessorInformationTest.class);
		suite.addTest(CommentTest.suite());
		suite.addTest(TaskParserTest.suite());
		suite.addTest(CompletionTestSuite.suite());
		suite.addTestSuite(CharArrayMapTest.class);
		suite.addTest(FaultToleranceTest.suite());
		suite.addTest(LanguageExtensionsTest.suite());
		suite.addTest(ASTInactiveCodeTest.suite());
		suite.addTest(AccessControlTest.suite());
		suite.addTest(VariableReadWriteFlagsTest.suite());
		suite.addTest(AST2CPPAttributeTest.suite());
		// C++14 tests
		suite.addTest(VariableTemplateTest.suite());
		suite.addTestSuite(ReturnTypeDeductionTest.class);
		suite.addTestSuite(GenericLambdaTest.class);
		suite.addTestSuite(InitCaptureTest.class);
		// C++17 tests
		suite.addTest(TemplateAutoTest.suite());
		suite.addTestSuite(LambdaExpressionTest.class);
		suite.addTestSuite(StructuredBindingTest.class);
		suite.addTestSuite(DeductionGuideTest.class);
		return suite;
	}
}
