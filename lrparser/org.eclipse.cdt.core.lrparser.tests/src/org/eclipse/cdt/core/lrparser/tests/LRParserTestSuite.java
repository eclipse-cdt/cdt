/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;



import junit.framework.Test;
import junit.framework.TestSuite;

public class LRParserTestSuite extends TestSuite {
	
	// TODO: the following test are not being reused
	//
	// DOMGCCSelectionParseExtensionsTest
	// DOMSelectionParseTest
	// GCCCompleteParseExtensionsTest
	// QuickParser2Tests
	//
	// and perhaps others
	
	public static Test suite() {
		return new TestSuite() {{
		
			addTest(LRCommentTests.suite());
			addTest(LRCompleteParser2Tests.suite());
			addTest(LRCompletionBasicTest.suite());	
			addTest(LRCompletionParseTest.suite());
			addTest(LRCPPSpecTest.suite());
			addTest(LRCPPTests.suite()); 
			addTest(LRCSpecTests.suite()); // a couple of failures
			addTest(LRDigraphTrigraphTests.suite());
			addTest(LRDOMLocationInclusionTests.suite());
			addTest(LRDOMLocationMacroTests.suite());
			addTest(LRDOMLocationTests.suite());
			addTest(LRDOMPreprocessorInformationTest.suite());
			addTest(LRGCCTests.suite());
			addTest(LRGCCCompleteParseExtensionsTest.suite());
			addTest(LRImageLocationTests.suite());
			addTest(LRKnRTests.suite()); // mostly fail due to ambiguities
			addTest(LRNodeSelectorTest.suite());
			addTestSuite(LRQuickParser2Tests.class);
			addTest(LRSelectionParseTest.suite()); // this one still has a lot of failing tests though
			addTest(LRSemanticsTests.suite());
			addTest(LRTaskParserTest.suite());
			addTest(LRTemplateTests.suite());
			addTest(LRTests.suite()); // has some tests that do fail
			addTest(LRUtilOldTests.suite());
			addTest(LRUtilTests.suite());
			addTest(LRCompletionHangingTest.suite());
			addTest(LRCPPImplicitNameTests.suite());
			//addTest(LRInactiveCodeTests.suite());

		}};
	}	
}

