/*
 * Created on May 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.core.suite;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestListener;
import junit.framework.AssertionFailedError;

import java.text.DecimalFormat;

import org.eclipse.cdt.core.build.managed.tests.AllBuildTests;
import org.eclipse.cdt.core.model.tests.AllCoreTests;
import org.eclipse.cdt.core.model.tests.BinaryTests;
import org.eclipse.cdt.core.model.tests.ElementDeltaTests;
import org.eclipse.cdt.core.model.tests.WorkingCopyTests;
import org.eclipse.cdt.core.parser.failedTests.*;
import org.eclipse.cdt.core.parser.tests.ParserTestSuite;
import org.eclipse.cdt.core.model.failedTests.*;

/**
 * @author vhirsl
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AutomatedIntegrationSuite extends TestSuite implements TestListener {

	private String currentTestName;
	// Statistical information
	private int numberOfRuns = 0;
	private int numberOfFailures = 0;
	private int numberOfErrors = 0;
	// success tests
	private int numberOfSuccessTests = 0;
	private int numberOfFailedSuccessTests = 0;
	// failed tests for open bug reports
	private int numberOfFailedTests = 0;
	private int numberOfFailedFailedTests = 0;
	// switching to failed tests
	private boolean failedTests = false;
	private boolean skipTest = false;
	

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
		suite.addTest(AllBuildTests.suite());
		suite.addTest(ParserTestSuite.suite());
		suite.addTest(AllCoreTests.suite());
		suite.addTest(BinaryTests.suite());
		suite.addTest(ElementDeltaTests.suite());
		suite.addTest(WorkingCopyTests.suite());
		
		// Last test to trigger report generation
		suite.addTest(suite.new GenerateReport("testStartFailedTests"));
		
		// Add all failed tests
		suite.addTestSuite(ACEFailedTest.class);
		suite.addTestSuite(DOMFailedTest.class);
		suite.addTestSuite(LokiFailures.class);
		suite.addTestSuite(ScannerFailedTest.class);
		suite.addTestSuite(STLFailedTests.class);
		suite.addTestSuite(CModelElementsFailedTests.class);

		// Last test to trigger report generation
		suite.addTest(suite.new GenerateReport("testGenerateReport"));

		return suite;
	}
	
	/**
	 * Runs the tests and collects their result in a TestResult.
	 * Overloaded method
	 */
	public void run(TestResult result) {
		// Add oneself as a listener
		result.addListener(this);
		// Call a base class method
		super.run(result);
		// Remove a listener
		result.removeListener(this);
	}

	 
	/**
	 * An error occurred.
	 */
	public void addError(Test test, Throwable t) {
		++numberOfErrors;
		System.out.println("Error : " + test);
		System.out.println("\tReason : " + t);
		System.out.println("\tStack trace : ");
		t.printStackTrace(System.out);
	}
	/**
	 * A failure occurred.
	 */
	public void addFailure(Test test, AssertionFailedError t) {
		++numberOfFailures;
		if (failedTests) {
			++numberOfFailedFailedTests;  
		}
		else {
			++numberOfFailedSuccessTests;
		}
		System.out.println("Failure : " + test);
		System.out.println("\tReason : " + t);
		System.out.println("\tStackTrace : ");
		t.printStackTrace(System.out);
	}
	/**
	 * A test ended.
	 */
	public void endTest(Test test) {
		if (currentTestName == null) {
			System.out.println("Internal error - endTest: currentTestName == null");
		}
		else {
			if (skipTest) {
				skipTest = false;
			}
			else {
				++numberOfRuns;
				if (failedTests) {
					++numberOfFailedTests;
					System.out.println(test);
				}
				else {
					++numberOfSuccessTests;
				}
				// System.out.println(test);
			}
			currentTestName = null;
		}
	}
	/**
	 * A test started.
	 */
	public void startTest(Test test) {
		if (currentTestName != null) {
			System.out.println("Internal error - startTest: currentTestName != null");
		}
		else {
			currentTestName = test.toString();
		}
	}
	
	/*
	 * generateReport
	 * 
	 * @author vhirsl
	 *
	 * To change the template for this generated type comment go to
	 * Window>Preferences>Java>Code Generation>Code and Comments
	 */
	protected void generateReport() {
	 	System.out.println("\n*** Generating report: ***\n");
	 	System.out.println("\tNumber of runs: " + numberOfRuns);
		System.out.println("\tNumber of failures: " + numberOfFailures);
		System.out.println("\tNumber of errors: " + numberOfErrors);
		float successRate = (numberOfRuns-numberOfFailures)/(float)numberOfRuns;
		DecimalFormat df = new DecimalFormat("##.##%");
		System.out.println("Sanity success rate : " + df.format(successRate));
		System.out.println("\tNumber of success tests: " + numberOfSuccessTests);
		System.out.println("\tNumber of failed tests: " + numberOfFailedTests);
		successRate = numberOfSuccessTests/(float)(numberOfSuccessTests+numberOfFailedTests);
		System.out.println("Expected success test rate : " + df.format(successRate));
		successRate = (numberOfSuccessTests-numberOfFailedSuccessTests)/
					  (float)(numberOfSuccessTests+numberOfFailedTests-numberOfFailedFailedTests);
		System.out.print("Observed success test rate : " + df.format(successRate)); 
		System.out.println(" (failed success tests = " + numberOfFailedSuccessTests + ", failed failed tests = " + numberOfFailedFailedTests + ")");
	}
	
	private void startFailedTests() {
		failedTests = true;
		System.out.println("\n*** Starting failed tests ***\n");
	}
	/*
	 * Public inner class to invoke generateReport
	 * 
	 * @author vhirsl
	 *
	 * To change the template for this generated type comment go to
	 * Window>Preferences>Java>Code Generation>Code and Comments
	 */
	public class GenerateReport extends TestCase {
		public GenerateReport(String name) {
			super(name);
		}
		public GenerateReport()
		{
		}
		public void testGenerateReport() {
			// skip this one
			AutomatedIntegrationSuite.this.skipTest = true;

			// Calls a method of the outer class
			AutomatedIntegrationSuite.this.generateReport();
		}
		
		public void testStartFailedTests() {
			// skip this one
			AutomatedIntegrationSuite.this.skipTest = true;
			
			// Calls a method of the outer class
			AutomatedIntegrationSuite.this.startFailedTests();
		}
	}
}
