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
		
			addTestSuite(LRCommentTests.class);
			addTestSuite(LRCompleteParser2Tests.class);
			addTestSuite(LRCompletionBasicTest.class);	
			addTestSuite(LRCompletionParseTest.class);
			addTestSuite(LRCPPSpecFailingTest.class);
			addTestSuite(LRCPPSpecTest.class);
			addTestSuite(LRCPPTests.class); 
			addTestSuite(LRCSpecFailingTest.class);
			addTestSuite(LRCSpecTests.class); // a couple of failures
			addTestSuite(LRDigraphTrigraphTests.class);
			addTestSuite(LRDOMLocationInclusionTests.class);
			addTestSuite(LRDOMLocationMacroTests.class);
			addTestSuite(LRDOMLocationTests.class);
			addTestSuite(LRDOMPreprocessorInformationTest.class);
			addTestSuite(LRGCCTests.class);
			addTestSuite(LRImageLocationTests.class);
			addTestSuite(LRKnRTests.class); // mostly fail due to ambiguities
			addTestSuite(LRNodeSelectorTest.class);
			addTestSuite(LRQuickParser2Tests.class);
			addTestSuite(LRSelectionParseTest.class); // this one still has a lot of failing tests though
			addTestSuite(LRSemanticsTests.class);
			addTestSuite(LRTaskParserTest.class);
			addTestSuite(LRTemplateTests.class);
			addTestSuite(LRTests.class); // has some tests that do fail
			addTestSuite(LRUtilOldTests.class);
			addTestSuite(LRUtilTests.class);

		}};
	}	
}

