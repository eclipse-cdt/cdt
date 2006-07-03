/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
/*
 * Created on May 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.core.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.cdescriptor.tests.CDescriptorTests;
import org.eclipse.cdt.core.internal.tests.PositionTrackerTests;
import org.eclipse.cdt.core.model.tests.AllCoreTests;
import org.eclipse.cdt.core.model.tests.BinaryTests;
import org.eclipse.cdt.core.model.tests.ElementDeltaTests;
import org.eclipse.cdt.core.model.tests.WorkingCopyTests;
import org.eclipse.cdt.core.parser.failedTests.ASTFailedTests;
import org.eclipse.cdt.core.parser.failedTests.FailedCompleteParseASTTest;
import org.eclipse.cdt.core.parser.failedTests.STLFailedTests;
import org.eclipse.cdt.core.parser.tests.ParserTestSuite;

/**
 * @author vhirsl
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AutomatedIntegrationSuite extends TestSuite {

	public AutomatedIntegrationSuite() {}
	
	public AutomatedIntegrationSuite(Class theClass, String name) {
		super(theClass, name);
	}
	
	public AutomatedIntegrationSuite(Class theClass) {
		super(theClass);
	}
	
	public AutomatedIntegrationSuite(String name) {
		super(name);
	}
	
	public static Test suite() {
		final AutomatedIntegrationSuite suite = new AutomatedIntegrationSuite();
		
		// Add all success tests
		suite.addTest(CDescriptorTests.suite());
		//suite.addTest(GCCErrorParserTests.suite());
		suite.addTest(ParserTestSuite.suite());
		suite.addTest(AllCoreTests.suite());
		suite.addTest(BinaryTests.suite());
		suite.addTest(ElementDeltaTests.suite());
		suite.addTest(WorkingCopyTests.suite());
        suite.addTest(PositionTrackerTests.suite());
		
		// TODO turning off indexer/search tests until the PDOM
		// settles. These'll probably have to be rewritten anyway.
//		suite.addTest(SearchTestSuite.suite());
//		suite.addTest(DependencyTests.suite());
//		suite.addTest(RegressionTestSuite.suite());
		//Indexer Tests need to be run after any indexer client tests
		//as the last test shuts down the indexing thread
//		suite.addTest(DOMSourceIndexerTests.suite());
		// Last test to trigger report generation
		
		// Add all failed tests
		suite.addTestSuite(ASTFailedTests.class);
		suite.addTestSuite(STLFailedTests.class);
		suite.addTestSuite(FailedCompleteParseASTTest.class);

		return suite;
	}
	
}
