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
		
			addTestSuite(LRTests.class); // has some tests that do fail
			
			addTestSuite(LRCSpecTests.class); // a couple of failures
			addTestSuite(LRKnRTests.class); // mostly fail due to ambiguities
			
			// The majority of the content assist test are in the ui tests plugin
			addTestSuite(LRCompletionBasicTest.class);	
			addTestSuite(LRCompletionParseTest.class);
			// this one still has a lot of failing tests though
			addTestSuite(LRSelectionParseTest.class);
			
			addTestSuite(LRDOMLocationInclusionTests.class);
			addTestSuite(LRDOMLocationTests.class);
			addTestSuite(LRDOMLocationMacroTests.class);
			addTestSuite(LRDOMPreprocessorInformationTest.class);
			addTestSuite(LRCommentTests.class);
			addTestSuite(LRDigraphTrigraphTests.class);
			addTestSuite(LRGCCTests.class);
			addTestSuite(LRUtilOldTests.class);
			addTestSuite(LRUtilTests.class);
			addTestSuite(LRCompleteParser2Tests.class);
			addTestSuite(LRTaskParserTest.class);
			
			addTestSuite(LRCPPSpecTest.class);
			addTestSuite(LRCPPTests.class); 
			addTestSuite(LRTemplateTests.class);
		
		}};
	}	
}

