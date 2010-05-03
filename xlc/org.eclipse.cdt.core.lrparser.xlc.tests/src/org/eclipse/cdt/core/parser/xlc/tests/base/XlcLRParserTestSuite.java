/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests.base;





import junit.framework.Test;
import junit.framework.TestSuite;

public class XlcLRParserTestSuite extends TestSuite {
	
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
		
			addTest(XlcLRCommentTests.suite());
			addTest(XlcLRCompleteParser2Tests.suite());
			addTest(XlcLRCompletionBasicTest.suite());	
			addTest(XlcLRCompletionParseTest.suite());
			addTest(XlcLRCPPSpecTest.suite());
			addTest(XlcLRCPPTests.suite()); 
			addTest(XlcLRCSpecTests.suite()); // a couple of failures
			addTest(XlcLRDigraphTrigraphTests.suite());
			addTest(XlcLRDOMLocationInclusionTests.suite());
			addTest(XlcLRDOMLocationMacroTests.suite());
			addTest(XlcLRDOMLocationTests.suite());
			addTest(XlcLRDOMPreprocessorInformationTest.suite());
			addTest(XlcLRGCCTests.suite());
			addTest(XlcLRGCCCompleteParseExtensionsTest.suite());
			addTest(XlcLRImageLocationTests.suite());
			addTest(XlcLRKnRTests.suite()); // mostly fail due to ambiguities
			addTest(XlcLRNodeSelectorTest.suite());
			addTest(XlcLRQuickParser2Tests.suite());
			addTest(XlcLRSelectionParseTest.suite()); // this one still has a lot of failing tests though
			addTest(XlcLRSemanticsTests.suite());
			addTest(XlcLRTaskParserTest.suite());
			addTest(XlcLRTemplateTests.suite());
			addTest(XlcLRTests.suite()); // has some tests that do fail
			addTest(XlcLRUtilOldTests.suite());
			addTest(XlcLRUtilTests.suite());
			addTest(XlcCompletionHangingTest.suite());
			//addTest(XlcLRCPPImplicitNameTests.suite());
			//addTest(LRInactiveCodeTests.suite());

		}};
	}	
}

