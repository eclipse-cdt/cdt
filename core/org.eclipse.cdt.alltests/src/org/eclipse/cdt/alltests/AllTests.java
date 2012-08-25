package org.eclipse.cdt.alltests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Container for all the junit3 tests in CDT
 * 
 * Keep in mind that some of these tests require that a GNU toolchain be in
 * PATH. (I.e., on Windows, make sure you have MinGW or Cygwin)
 */
public class AllTests extends TestSuite {
	public static Test suite() throws Exception {
		final AllTests suite = new AllTests();
		suite.addTest(org.eclipse.cdt.autotools.tests.AllAutotoolsTests.suite());
//	// There are intermittent failures in these tests. No pattern to failures. Seems like indexer is interrupted  
	if (System.getProperty("cdt.skip.known.test.failures") == null) { //$NON-NLS-1$
		suite.addTest(org.eclipse.cdt.codan.core.test.AutomatedIntegrationSuite.suite());
		suite.addTest(org.eclipse.cdt.core.lrparser.tests.LRParserTestSuite.suite());
		suite.addTest(org.eclipse.cdt.core.parser.xlc.tests.suite.XlcTestSuite.suite());
		suite.addTest(org.eclipse.cdt.core.parser.upc.tests.UPCParserTestSuite.suite());
	}
		suite.addTest(org.eclipse.cdt.core.suite.AutomatedIntegrationSuite.suite());
		
	// These tests fail intermittently due to gdb not shutting down and thus
	// not being able to delete the project (exe locked)
	if (System.getProperty("cdt.skip.known.test.failures") == null) {		 //$NON-NLS-1$
		suite.addTest(org.eclipse.cdt.debug.core.tests.AllDebugTests.suite());
	}
		suite.addTest(org.eclipse.cdt.errorparsers.xlc.tests.AllXlcErrorParserTests.suite());
		suite.addTest(org.eclipse.cdt.make.core.tests.AutomatedIntegrationSuite.suite());
		suite.addTest(org.eclipse.cdt.managedbuilder.tests.suite.AllManagedBuildTests.suite());
		suite.addTest(org.eclipse.cdt.managedbuilder.ui.tests.suite.AllManagedBuildUITests.suite());
		suite.addTest(org.eclipse.cdt.testsrunner.test.TestsRunnerSuite.suite());
		suite.addTest(org.eclipse.cdt.ui.tests.AutomatedSuite.suite());
		return suite;
	}
}
