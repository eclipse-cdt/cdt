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

import org.eclipse.cdt.core.parser.tests.ast2.cxx14.GenericLambdaTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.InitCaptureTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.ReturnTypeDeductionTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.VariableTemplateTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.LambdaExpressionTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.StructuredBindingTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.TemplateAutoTests;
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
		suite.addTest(AST2Tests.suite());
		suite.addTestSuite(GCCTests.class);
		suite.addTest(AST2CPPTests.suite());
		suite.addTest(ASTCPPSpecDefectTests.suite());
		suite.addTest(AST2CPPImplicitNameTests.suite());
		suite.addTest(AST2TemplateTests.suite());
		suite.addTest(TypeTraitsTests.suite());
		suite.addTestSuite(QuickParser2Tests.class);
		suite.addTest(CompleteParser2Tests.suite());
		suite.addTest(DOMLocationTests.suite());
		suite.addTestSuite(DOMLocationMacroTests.class);
		suite.addTest(ImageLocationTests.suite());
		suite.addTestSuite(AST2KnRTests.class);
		suite.addTestSuite(AST2UtilTests.class);
		suite.addTestSuite(AST2UtilOldTests.class);
		suite.addTestSuite(AST2SelectionParseTest.class);
		suite.addTest(ASTNodeSelectorTest.suite());
		suite.addTest(AST2CPPSpecTest.suite());
		suite.addTestSuite(AST2CSpecTest.class);
		suite.addTestSuite(DOMSelectionParseTest.class);
		suite.addTestSuite(GCCCompleteParseExtensionsTest.class);
		suite.addTestSuite(DOMPreprocessorInformationTest.class);
		suite.addTest(CommentTests.suite());
		suite.addTest(TaskParserTest.suite());
		suite.addTest(CompletionTestSuite.suite());
		suite.addTestSuite(CharArrayMapTest.class);
		suite.addTest(FaultToleranceTests.suite());
		suite.addTest(LanguageExtensionsTest.suite());
		suite.addTest(ASTInactiveCodeTests.suite());
		suite.addTest(AccessControlTests.suite());
		suite.addTest(VariableReadWriteFlagsTest.suite());
		suite.addTest(AST2CPPAttributeTests.suite());
		// C++14 tests
		suite.addTest(VariableTemplateTests.suite());
		suite.addTestSuite(ReturnTypeDeductionTests.class);
		suite.addTestSuite(GenericLambdaTests.class);
		suite.addTestSuite(InitCaptureTests.class);
		// C++17 tests
		suite.addTest(TemplateAutoTests.suite());
		suite.addTestSuite(LambdaExpressionTests.class);
		suite.addTestSuite(StructuredBindingTests.class);
		return suite;
	}
}
