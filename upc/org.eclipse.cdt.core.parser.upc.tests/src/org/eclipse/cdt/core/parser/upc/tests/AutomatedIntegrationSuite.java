/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.upc.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomatedIntegrationSuite extends TestSuite {
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTestSuite(UPCC99CommentTests.class);
		suite.addTestSuite(UPCC99CompletionBasicTest.class);
		suite.addTestSuite(UPCC99CompletionParseTest.class);
		suite.addTestSuite(UPCC99DOMLocationInclusionTests.class);
		suite.addTestSuite(UPCC99DOMLocationMacroTests.class);
		suite.addTestSuite(UPCC99DOMLocationTests.class);
		suite.addTestSuite(UPCC99DOMPreprocessorInformationTest.class);
		suite.addTestSuite(UPCC99KnRTests.class);
		suite.addTestSuite(UPCC99SelectionParseTest.class);
		suite.addTestSuite(UPCC99SpecTests.class);
		suite.addTestSuite(UPCC99Tests.class);
		suite.addTestSuite(UPCLanguageExtensionTests.class);
		suite.addTestSuite(UPCC99DigraphTrigraphTests.class);
		suite.addTestSuite(UPCC99GCCTests.class);
		suite.addTestSuite(UPCC99UtilOldTests.class);
		
		return suite;
	}
}
