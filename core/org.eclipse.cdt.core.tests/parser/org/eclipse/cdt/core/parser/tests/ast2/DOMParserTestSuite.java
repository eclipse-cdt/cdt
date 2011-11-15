/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Emanuel Graf (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.prefix.CompletionTestSuite;

/**
 * @author jcamelon
 */
public class DOMParserTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite= new TestSuite(DOMParserTestSuite.class.getName());
		suite.addTest(AST2Tests.suite());
		suite.addTestSuite(GCCTests.class);
		suite.addTest(AST2CPPTests.suite());
		suite.addTest(ASTCPPSpecDefectTests.suite());
		suite.addTest(AST2CPPImplicitNameTests.suite());
		suite.addTest(AST2TemplateTests.suite());
		suite.addTest(ClassTypeHelperTests.suite());
		suite.addTestSuite(QuickParser2Tests.class);
		suite.addTest(CompleteParser2Tests.suite());
		suite.addTest(DOMLocationTests.suite());
		suite.addTestSuite(DOMLocationMacroTests.class);
		suite.addTest(ImageLocationTests.suite());
		suite.addTest(DOMLocationInclusionTests.suite());
		suite.addTestSuite(AST2KnRTests.class);
		suite.addTestSuite(AST2UtilTests.class);
		suite.addTestSuite(AST2UtilOldTests.class);
		suite.addTestSuite(AST2SelectionParseTest.class);
		suite.addTest(ASTNodeSelectorTest.suite());
		suite.addTestSuite(CodeReaderCacheTest.class);
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
		return suite;
	}
}
