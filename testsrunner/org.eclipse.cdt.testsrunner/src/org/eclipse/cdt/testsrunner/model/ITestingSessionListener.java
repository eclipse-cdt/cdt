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
 * Testing session listener is notified of testing process going.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestingSessionListener {

	/**
	 * Notifies the listener that the new test suite running is started.
	 *
	 * @param testSuite the started test suite
	 */
	public void enterTestSuite(ITestSuite testSuite);
	
	/**
	 * Notifies the listener that the test suite running is finished.
	 *
	 * @param testSuite the finished test suite
	 */
	public void exitTestSuite(ITestSuite testSuite);
	
	/**
	 * Notifies the listener that the new test case running is started.
	 *
	 * @param testCase the started test case
	 */
	public void enterTestCase(ITestCase testCase);
	
	/**
	 * Notifies the listener that the test case running is finished.
	 *
	 * @param testCase the finished test case
	 */
	public void exitTestCase(ITestCase testCase);

	/**
	 * Notifies the listener that the children of the test suite were updated.
	 * 
	 * @param testSuite the test suite which require children update
	 */
	public void childrenUpdate(ITestSuite testSuite);
	
	/**
	 * Notifies the listener that the testing process is started.
	 */
	public void testingStarted();

	/**
	 * Notifies the listener that the testing process is finished.
	 */
	public void testingFinished();
	
}
