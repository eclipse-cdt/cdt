/*
 * Created on May 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.core.suite;

import java.text.DecimalFormat;
import java.util.ArrayList;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.eclipse.cdt.core.build.managed.tests.ManagedBuildTests;
import org.eclipse.cdt.core.build.managed.tests.StandardBuildTests;
import org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest;
import org.eclipse.cdt.core.indexer.tests.IndexManagerTests;
import org.eclipse.cdt.core.model.failedTests.CModelElementsFailedTests;
import org.eclipse.cdt.core.model.tests.AllCoreTests;
import org.eclipse.cdt.core.model.tests.BinaryTests;
import org.eclipse.cdt.core.model.tests.ElementDeltaTests;
import org.eclipse.cdt.core.model.tests.WorkingCopyTests;
import org.eclipse.cdt.core.parser.failedTests.ASTFailedTests;
import org.eclipse.cdt.core.parser.failedTests.FullParseFailedTests;
import org.eclipse.cdt.core.parser.failedTests.LokiFailures;
import org.eclipse.cdt.core.parser.failedTests.STLFailedTests;
import org.eclipse.cdt.core.parser.tests.ParserTestSuite;
import org.eclipse.cdt.core.search.tests.ClassDeclarationPatternTests;
import org.eclipse.cdt.core.search.tests.FunctionMethodPatternTests;
import org.eclipse.cdt.core.search.tests.OtherPatternTests;
import org.eclipse.cdt.core.search.tests.ParseTestOnSearchFiles;
import org.eclipse.core.boot.IPlatformRunnable;

/**
 * @author vhirsl
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AutomatedIntegrationSuite extends TestSuite 
									   implements TestListener, IPlatformRunnable {

	private TestResult testResult = null;
	private String currentTestName;
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
		
		// First test to trigger report generation
		suite.addTest(suite.new GenerateReport("startSuccessTests"));

		// Add all success tests
		suite.addTest(ManagedBuildTests.suite());
		suite.addTest(StandardBuildTests.suite());
		suite.addTest(ParserTestSuite.suite());
		suite.addTest(AllCoreTests.suite());
		suite.addTest(BinaryTests.suite());
		suite.addTest(ElementDeltaTests.suite());
		suite.addTest(WorkingCopyTests.suite());
		suite.addTestSuite(ClassDeclarationPatternTests.class );
		suite.addTestSuite(FunctionMethodPatternTests.class );
		suite.addTestSuite(OtherPatternTests.class );		
		suite.addTestSuite( ParseTestOnSearchFiles.class);
		suite.addTestSuite( CompletionProposalsTest.class);
		//Indexer Tests need to be run after any indexer client tests
		//as the last test shuts down the indexing thread
		suite.addTest(IndexManagerTests.suite());
		// Last test to trigger report generation
		suite.addTest(suite.new GenerateReport("startFailedTests"));
		
		// Add all failed tests
		suite.addTestSuite(ASTFailedTests.class);
		suite.addTestSuite(LokiFailures.class);
		suite.addTestSuite(STLFailedTests.class);
		suite.addTestSuite(CModelElementsFailedTests.class);
		suite.addTest(FullParseFailedTests.suite());

		// Last test to trigger report generation
		suite.addTest(suite.new GenerateReport("generateReport"));

		return suite;
	}
	
	/**
	 * Runs the tests and collects their result in a TestResult.
	 * Overloaded method
	 */
	public void run(TestResult result) {
		// To get counts from the result
		testResult = result;
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
//		System.out.println("Error : " + test);
//		System.out.println("\tReason : " + t);
//		System.out.println("\tStack trace : ");
//		t.printStackTrace(System.out);
	}
	/**
	 * A failure occurred.
	 */
	public void addFailure(Test test, AssertionFailedError t) {
		if (failedTests) {
			++numberOfFailedFailedTests;  
		}
		else {
			++numberOfFailedSuccessTests;
		}
//		System.out.println("Failure : " + test);
//		System.out.println("\tReason : " + t);
//		System.out.println("\tStackTrace : ");
//		t.printStackTrace(System.out);
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
				if (failedTests) {
					++numberOfFailedTests;
					// System.out.println(test);
				}
				else {
					++numberOfSuccessTests;
				}
				System.out.println(test);
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
		int numberOfRuns = testResult.runCount();
		int numberOfFailures = testResult.failureCount();
		int numberOfErrors = testResult.errorCount();

		System.out.println();
	 	System.out.println("*** Generating report: ***");
		System.out.println();
	 	System.out.println("\tNumber of runs: " + numberOfRuns);
		System.out.println("\tNumber of failures: " + numberOfFailures);
		System.out.println("\tNumber of errors: " + numberOfErrors);
		float successRate = (numberOfRuns-numberOfFailures-numberOfErrors)/(float)numberOfRuns;
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
		System.out.println();
	}
	
		private void startSuccessTests() {
			failedTests = false;
			System.out.println();
			System.out.println("*** Starting success tests ***");
			System.out.println();
		}
	
		private void startFailedTests() {
			failedTests = true;
			System.out.println();
			System.out.println("*** Starting failed tests ***");
			System.out.println();
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
		public GenerateReport(){}
		
		public void generateReport() {
			// skip this one
			AutomatedIntegrationSuite.this.skipTest = true;

			// Calls a method of the outer class
			AutomatedIntegrationSuite.this.generateReport();
		}
		
		public void startSuccessTests() {
			// skip this one
			AutomatedIntegrationSuite.this.skipTest = true;
			
			// Calls a method of the outer class
			AutomatedIntegrationSuite.this.startSuccessTests();
		}

		public void startFailedTests() {
			// skip this one
			AutomatedIntegrationSuite.this.skipTest = true;
			
			// Calls a method of the outer class
			AutomatedIntegrationSuite.this.startFailedTests();
		}

		/* (non-Javadoc)
		 * @see junit.framework.Test#countTestCases()
		 * We don't want these test cases to be counted
		 */
		public int countTestCases() {
			return 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		// Used when started from as a regression test suite after the build
		TestRunner testRunner = new TestRunner(new AISResultPrinter(System.out));
		TestResult testResult = testRunner.doRun(suite());
		
		return prepareReport(testResult);
	}

	protected ArrayList prepareReport(TestResult testResult) {	
		// TestRunner.run(suite());
		ArrayList efMessages = new ArrayList();
		int errorCount = testResult.errorCount();
		int failureCount = testResult.failureCount();
		if (errorCount > 0) {
			String em = new String("There ");
			em += (errorCount == 1)?"is ":"are ";
			em += Integer.toString(errorCount);
			em += " error";
			em += (errorCount == 1)?"!":"s!";
			efMessages.add(em);
		}
		if (failureCount > 0) {
			String fm = new String("There ");
			fm += (failureCount == 1)?"is ":"are ";
			fm += Integer.toString(failureCount);
			fm += " failure";
			fm += (failureCount == 1)?"!":"s!";
			efMessages.add(fm);
		}
		if (efMessages.isEmpty()) {
			efMessages.add(new String("Regression test run SUCCESSFUL!"));
		}
		else {
			efMessages.add(new String("Please see raw test suite output for details."));
		}
		return efMessages;
	}
}
