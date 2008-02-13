/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests.c99;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ParserTestSuiteConvenient extends TestSuite {
	
	// this test suite is just for my convenience
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		
//		suite.addTestSuite(C99Tests.class); // has some tests that do fail
		
//		suite.addTestSuite(C99SpecTests.class); // a couple of failures
		
		suite.addTest(new C99SpecTests("test6_7_2_3s10"));
		
		
//		suite.addTestSuite(C99KnRTests.class); // mostly fail due to ambiguities
//		
//		// The majority of the content assist test are in the ui tests plugin
//		suite.addTestSuite(C99CompletionBasicTest.class);	
//		// this one still has a lot of failing tests though
//		suite.addTestSuite(C99SelectionParseTest.class);
//		
//		suite.addTestSuite(C99DOMLocationInclusionTests.class);
//		suite.addTestSuite(C99DOMLocationTests.class);
//		suite.addTestSuite(C99DOMLocationMacroTests.class);
//		suite.addTestSuite(C99DOMPreprocessorInformationTest.class);
//		suite.addTestSuite(C99CommentTests.class);
//		suite.addTestSuite(C99DigraphTrigraphTests.class);
//		suite.addTestSuite(C99GCCTests.class);
//		suite.addTestSuite(C99UtilOldTests.class);
//		suite.addTestSuite(C99UtilTests.class);
//		suite.addTestSuite(C99CompleteParser2Tests.class);
//		suite.addTestSuite(C99TaskParserTest.class);

		
		return suite;
	}	
}
