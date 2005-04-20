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
import org.eclipse.cdt.core.filetype.tests.ResolverTests;
import org.eclipse.cdt.core.indexer.tests.DependencyTests;
import org.eclipse.cdt.core.indexer.tests.SourceIndexerTests;
import org.eclipse.cdt.core.model.tests.AllCoreTests;
import org.eclipse.cdt.core.model.tests.BinaryTests;
import org.eclipse.cdt.core.model.tests.ElementDeltaTests;
import org.eclipse.cdt.core.model.tests.WorkingCopyTests;
import org.eclipse.cdt.core.parser.failedTests.ASTFailedTests;
import org.eclipse.cdt.core.parser.failedTests.FailedCompleteParseASTTest;
import org.eclipse.cdt.core.parser.failedTests.STLFailedTests;
import org.eclipse.cdt.core.parser.tests.ParserTestSuite;
import org.eclipse.cdt.core.search.tests.SearchTestSuite;
import org.eclipse.cdt.core.tests.RegressionTestSuite;

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
		suite.addTest(ResolverTests.suite());
		suite.addTest(ParserTestSuite.suite());
		suite.addTest(AllCoreTests.suite());
		suite.addTest(BinaryTests.suite());
		suite.addTest(ElementDeltaTests.suite());
		suite.addTest(WorkingCopyTests.suite());
		suite.addTest(SearchTestSuite.suite());
		suite.addTest(DependencyTests.suite());
		suite.addTest(RegressionTestSuite.suite());
		//Indexer Tests need to be run after any indexer client tests
		//as the last test shuts down the indexing thread
		suite.addTest(SourceIndexerTests.suite());
		// Last test to trigger report generation
		
		// Add all failed tests
		suite.addTestSuite(ASTFailedTests.class);
		suite.addTestSuite(STLFailedTests.class);
		suite.addTestSuite(FailedCompleteParseASTTest.class);

		return suite;
	}
	
}
