package org.eclipse.cdt.core.suite;

import java.io.FileOutputStream;
import java.io.PrintStream;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.testing.ITestHarness;
import org.eclipse.ui.testing.TestableObject;

/**
 * @see IPlatformRunnable
 */
public class RunTests implements IPlatformRunnable, ITestHarness, TestListener {

	private TestableObject testableObject;
	private PrintStream stream;
	private String testReport;
	private String pluginName = "org.eclipse.cdt.core.tests";
	
	/**
	 *
	 */
	public RunTests() {
	}

	/**
	 * @see IPlatformRunnable#run
	 */
	public Object run(Object args) throws Exception {

		stream = System.out;
		
		String [] sargs = (String[])args;
		
		for (int i = 0; i <sargs.length; ++i) {
			if (sargs[i].equals("-testout") && ++i < sargs.length) {
				stream = new PrintStream(new FileOutputStream(sargs[i]));
			} else if (sargs[i].equals("-testreport") && ++i < sargs.length) {
				testReport = sargs[i];
			}
		}
		
		IPlatformRunnable application = getApplication();
		testableObject = PlatformUI.getTestableObject();
		testableObject.setTestHarness(this);
		return application.run(args);
	}
	
	/*
	 * return the application to run, or null if not even the default application
	 * is found.
	 */
	private IPlatformRunnable getApplication() throws CoreException {
		// Assume we are in 3.0 mode.
		// Find the name of the application as specified by the PDE JUnit launcher.
		// If no application is specified, the 3.0 default workbench application
		// is returned.
		IExtension extension =
			Platform.getPluginRegistry().getExtension(
					Platform.PI_RUNTIME,
					Platform.PT_APPLICATIONS,
					"org.eclipse.ui.ide.workbench");
		
		// If the extension does not have the correct grammar, return null.
		// Otherwise, return the application object.
		IConfigurationElement[] elements = extension.getConfigurationElements();
		if (elements.length > 0) {
			IConfigurationElement[] runs = elements[0].getChildren("run");
			if (runs.length > 0) {
				Object runnable = runs[0].createExecutableExtension("class");
				if (runnable instanceof IPlatformRunnable)
					return (IPlatformRunnable) runnable;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.testing.ITestHarness#runTests()
	 */
	public void runTests() {
		testableObject.testingStarting();
		testableObject.runTest(new Runnable() {
			public void run() {
				TestResult results = new TestResult();
				results.addListener(RunTests.this);
				
				stream.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
				stream.print("<?xml-stylesheet type=\"text/xsl\" href=\"");
				stream.print(testReport);
				stream.println("\"?>");
				
				stream.print("<testRun name=\"");
				stream.print(pluginName);
				stream.println("\">");
				
				startSuite(AutomatedIntegrationSuite.class.getName());
				AutomatedIntegrationSuite.suite().run(results);
				endSuite();
				
				stream.print("</testRun>");
				results.removeListener(RunTests.this);
				results.stop();
			}
		});
		testableObject.testingFinished();
	}

	// Test Listener methods
	
	Throwable failure;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestListener#addError(junit.framework.Test, java.lang.Throwable)
	 */
	public void addError(Test test, Throwable t) {
		failure = t;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#addFailure(junit.framework.Test, junit.framework.AssertionFailedError)
	 */
	public void addFailure(Test test, AssertionFailedError t) {
		failure = t;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#endTest(junit.framework.Test)
	 */
	public void endTest(Test test) {
		double time = (System.currentTimeMillis() - startTime) / 1000.0;
		
		stream.print(" time=\"");
		stream.print(time);
		stream.print("\" result=\"");
		
		boolean printerror = false;
		if (failure == null)
			stream.print("pass");
		else if (failure instanceof AssertionFailedError)
			stream.print("failed");
		else {
			stream.print("error");
			printerror = true;
		}

		stream.print("\"");
		
		if (printerror) {
			stream.println(">");
			failure.printStackTrace(stream);
			stream.println("\t\t\t</test>");
		} else
			stream.println("/>");
		
		failure = null;
	}

	private Class currentTest;
	
	private long startTime;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestListener#startTest(junit.framework.Test)
	 */
	public void startTest(Test test) {
		if (test.getClass() != currentTest) {
			if (currentTest != null)
				endClass();
			stream.print("\t\t<testClass name=\"");
			stream.print(test.getClass().getName());
			stream.println("\">");
			currentTest = test.getClass();
		}

		String name = test.toString();
		name = name.substring(0, name.indexOf('('));
		stream.print("\t\t\t<test name=\"");
		stream.print(name);
		stream.print("\"");
		
		startTime = System.currentTimeMillis();
	}
	
	// Report Generator
	
	private void startSuite(String name) {
		stream.print("\t<testSuite name=\"");
		stream.print(name);
		stream.println("\">");
	}
	
	private void endSuite() {
		if (currentTest != null) {
			endClass();
			currentTest = null;
		}
		stream.println("\t</testSuite>");
	}
	
	private void endClass() {
		stream.println("\t\t</testClass>");
	}
}
