/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.model;

/**
 * The interface to easily build or update testing model.
 * It is intended to use from the Tests Runner provider plug-in.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestModelUpdater {

	/**
	 * Specifies that a new test suite has been started.
	 * 
	 * @param name the name of the started test suite
	 */
	public void enterTestSuite(String name);
	
	/**
	 * Specifies that the last test suite has been finished.
	 * Automatically exists from the currently running test case (if any).
	 */
	public void exitTestSuite();
	
	/**
	 * Specifies that a new test case has been started.
	 * 
	 * @param name the name of the started test case
	 */
	public void enterTestCase(String name);
	
	/**
	 * Sets the status of the currently running test case.
	 * The exception is thrown if no test case is running.
	 * 
	 * @param status new test status
	 */
	public void setTestStatus(ITestItem.Status status);
	
	/**
	 * Sets the execution time of the currently running test case.
	 * If the execution time has already been set, it will be overridden.
	 * The exception is thrown if no test case is running.
	 * 
	 * @param testingTime test execution time
	 */
	public void setTestingTime(int testingTime);
	
	/**
	 * Specifies that the currently running test case has been finished.
	 * The test execution time is set if Tests Runner provider plug-in 
	 * requires time measurement.
	 */
	public void exitTestCase();
	
	/**
	 * Add a new testing message connected to the currently running test case.
	 * 
	 * @param file message file name
	 * @param line message line number
	 * @param level message level
	 * @param text message text
	 * 
	 * @note If file name is <code>null</code> or empty or if line number is
	 * negative or 0 then message location will not be set.
	 */
	public void addTestMessage(String file, int line, ITestMessage.Level level, String text);
	
	
	/**
	 * Access the top most currently running test suite object.
	 * 
	 * @return top most test suite
	 */
	public ITestSuite currentTestSuite();
	
	/**
	 * Access the currently running test case object.
	 * 
	 * @return top most test case
	 */
	public ITestCase currentTestCase();
	
}
