package org.eclipse.cdt.core.suite;

import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.testing.ITestHarness;
import org.eclipse.ui.testing.TestableObject;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * @see IPlatformRunnable
 */
public class RunTests implements ITestHarness, TestListener {

	private TestableObject testableObject;
	private PrintStream stream;
	private String testReport;
	private String pluginName = "org.eclipse.cdt.core.tests";
	Document doc;
	Element testRun;
	Element testSuite;
	Element testClass;
	Element test;
	
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
				
				try {
					doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

					ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"" + testReport +"\"");
					doc.appendChild(pi);
					
					testRun = doc.createElement("testRun");
					doc.appendChild(testRun);
					testRun.setAttribute("name", pluginName);
					
					startSuite(AutomatedIntegrationSuite.class.getName());
					AutomatedIntegrationSuite.suite().run(results);
					currentTest = null;
					
					results.removeListener(RunTests.this);
					results.stop();
					
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.transform(new DOMSource(doc), new StreamResult(stream));
				} catch (Throwable t) {
					System.out.println("runTests failed");
					t.printStackTrace();
				}
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
	public void endTest(Test t) {
		double time = (System.currentTimeMillis() - startTime) / 1000.0;
		
		test.setAttribute("time", String.valueOf(time));
		
		String result;
		if (failure == null)
			result = "pass";
		else {
			CDATASection data = doc.createCDATASection(failure.toString());
			test.appendChild(data);
			
			if (failure instanceof AssertionFailedError)
				result = "failed";
			else
				result = "error";
		}

		test.setAttribute("result", result);
		
		failure = null;
	}

	private Class currentTest;
	
	private long startTime;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestListener#startTest(junit.framework.Test)
	 */
	public void startTest(Test t) {
		if (t.getClass() != currentTest) {
			currentTest = t.getClass();
			testClass = doc.createElement("testClass");
			testSuite.appendChild(testClass);
			testClass.setAttribute("name", currentTest.getName());
		}
		
		test = doc.createElement("test");
		testClass.appendChild(test);
		String name = t.toString();
		name = name.substring(0, name.indexOf('('));
		test.setAttribute("name", name);
		
		startTime = System.currentTimeMillis();
	}
	
	// Report Generator
	
	private void startSuite(String name) {
		testSuite = doc.createElement("testSuite");
		testRun.appendChild(testSuite);
		testSuite.setAttribute("name", name);
	}

}
