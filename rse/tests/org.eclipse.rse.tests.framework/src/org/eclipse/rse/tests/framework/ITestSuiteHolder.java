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

import java.util.Calendar;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Instances of this type deliver JUnit test suites on demand.
 */
public interface ITestSuiteHolder {

	/**
	 * Runs the test suite held by this holder. Saves the result which is
	 * available through getResult().
	 * 
	 * @param monitor a fresh progress monitor for this run. It is advanced by one
	 * for each testcase executed in this suite.
	 */
	public void run(IProgressMonitor monitor);

	/**
	 * Resets this result used by this job monitor and notifies any listeners.
	 */
	public void reset();

	/**
	 * @return the string that is the result of the last time this was run.
	 */
	public String getResultString();

	/**
	 * @return the test result from the last time this was run.  
	 * This will be null if the test has not been run since it was created
	 * or reset.
	 */
	public TestResult getTestResult();

	/**
	 * @return the Calendar representing the time this test was last run.
	 * This will be null if the test has not been run since it was created or
	 * reset.
	 */
	public Calendar getLastRunTime();

	/**
	 * @return the test suite held by this holder.
	 */
	public abstract TestSuite getTestSuite();

	/**
	 * @return the name of the test suite held by this holder.
	 */
	public abstract String getName();
	
	/**
	 * Adds a new listener to this holder. This listener can process events that happen in the 
	 * lifecycle of the test suite held by this holder.
	 * @param listener the listener to add
	 */
	public void addListener(ITestSuiteHolderListener listener);

	/**
	 * Removes a listener from this holder. Does nothing if the holder has no knowledge of
	 * this listener
	 * @param listener the listener to remove.
	 */
	public void removeListener(ITestSuiteHolderListener listener);

}