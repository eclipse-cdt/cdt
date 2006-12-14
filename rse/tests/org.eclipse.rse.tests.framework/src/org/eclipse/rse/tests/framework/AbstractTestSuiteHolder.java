/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.framework;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IActionFilter;

/**
 * Instances of AbstractHolder hold either predefined or generated test suites. Subclass this only if you are providing
 * another means of delivering a test suite to the framework that is not already defined in the framework.
 */
public abstract class AbstractTestSuiteHolder implements ITestSuiteHolder, TestListener, IActionFilter {
	private Calendar myCalendar = null;
	private TestResult testResult = null;
	private StringWriter stringWriter = null;
	private IProgressMonitor monitor = null;
	private List listeners = new Vector();
	private TestFailure failure;

	/**
	 * Construct a new AbstractTestSuiteHolder. Subclasses should invoke super() (usually implicit)
	 * and implement their own constructors.
	 */
	protected AbstractTestSuiteHolder() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.IHolder#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void run(IProgressMonitor monitor) {
		stringWriter = new StringWriter(1000);
		myCalendar = Calendar.getInstance();
		TestSuite suite = getTestSuite();
		monitor.beginTask("Running " + suite.getName(), suite.countTestCases()); //$NON-NLS-1$
		setTestResult(new TestResult());
		this.monitor = monitor;
		testResult.addListener(this);
		suite.run(testResult);
		testResult.removeListener(this);
		this.monitor = null;
		monitor.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolder#reset()
	 */
	public final void reset() {
		stringWriter = null;
		monitor = null;
		testResult = null;
		myCalendar = null;
		for (Iterator z = listeners.iterator(); z.hasNext();) {
			ITestSuiteHolderListener listener = (ITestSuiteHolderListener) z.next();
			listener.testHolderReset(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolder#getResultString()
	 */
	public final String getResultString() {
		if (stringWriter == null) {
			return "Results not available.\n"; //$NON-NLS-1$
		}
		return stringWriter.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolder#addListener(org.eclipse.rse.tests.framework.ITestSuiteHolderListener)
	 */
	public final void addListener(ITestSuiteHolderListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolder#removeListener(org.eclipse.rse.tests.framework.ITestSuiteHolderListener)
	 */
	public final void removeListener(ITestSuiteHolderListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolder#getTestResult()
	 */
	public synchronized final TestResult getTestResult() {
		return testResult;
	}

	/**
	 * Used to predefine a test result for this holder.
	 * @param testResult
	 */
	private synchronized final void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolder#getLastRunTime()
	 */
	public final Calendar getLastRunTime() {
		return myCalendar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestListener#addError(junit.framework.Test,
	 *      java.lang.Throwable)
	 */
	public final void addError(Test test, Throwable t) {
		failure = new TestFailure(test, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestListener#addFailure(junit.framework.Test,
	 *      junit.framework.AssertionFailedError)
	 */
	public final void addFailure(Test test, AssertionFailedError t) {
		failure = new TestFailure(test, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestListener#startTest(junit.framework.Test)
	 */
	public final void startTest(Test test) {
		failure = null;
		if (test instanceof AnnotatingTestCase) {
			AnnotatingTestCase a = (AnnotatingTestCase) test;
			a.reset();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestListener#endTest(junit.framework.Test)
	 */
	public final void endTest(Test test) {
		monitor.worked(1);
		writeRemarks(test);
		if (failure != null) {
			stringWriter.write(failure.trace());
			stringWriter.write("*** " + failure.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			stringWriter.write("*** " + test.toString() + " passed\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		for (Iterator z = listeners.iterator(); z.hasNext();) {
			ITestSuiteHolderListener listener = (ITestSuiteHolderListener) z.next();
			listener.testEnded(this);
		}
		if (monitor.isCanceled()) {
			testResult.stop();
		}
	}
	
	/**
	 * Writes the remarks for a test onto the log maintained by this holder.
	 * @param test The test whose remarks are being obtained. This will be an instance of Test, but
	 * it will do nothing unless the test is an AnnotatingTestCase.
	 * @see AnnotatingTestCase
	 */
	private void writeRemarks(Test test) {
		if (test instanceof AnnotatingTestCase) {
			stringWriter.write("\n"); //$NON-NLS-1$
			AnnotatingTestCase a = (AnnotatingTestCase) test;
			stringWriter.write(a.getAnnotations());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		return (target == this && name.equals("hasResult") && value.equals("true") && testResult != null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolder#getTestSuite()
	 */
	public abstract TestSuite getTestSuite();

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolder#getName()
	 */
	public abstract String getName();
	
}