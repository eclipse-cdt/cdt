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
 * Base interface to the structural item of test hierarchy (test suite or test
 * case).
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestItem {

	/**
	 * Represents status of the test item.
	 * 
	 * @note Order of values is significant (cause enumeration values comparison
	 * is necessary)
	 */
	public enum Status {
		NotRun,
		Skipped,
		Passed,
		Failed,
		Aborted;

		public boolean isError() {
			return (this == Failed) || (this == Aborted);
		}
	}
	
	/**
	 * @return name of the test item.
	 */
	public String getName();
	
	/**
	 * Returns status of the test item.
	 * For test case it is its own status.
	 * For test suite it is the greatest status of all its children.
	 * 
	 * @return test item status
	 */
	public Status getStatus();

	/**
	 * Returns execution time (in milliseconds) of the test item.
	 * For test case it is its own execution time.
	 * For test suite it is the sum of execution time of all its children.
	 * 
	 * @return item execution time (in milliseconds)
	 */
	public int getTestingTime();

	/**
	 * Returns parent of the current test item or null if not available 
	 * (e.g. it is a root test suite).
	 * 
	 * @return parent or null
	 */
	public ITestSuite getParent();
	
	/**
	 * Returns <code>true</code> if test item has children.
	 * Always returns <code>false</code> for test cases.
	 * 
	 * @return true if has children
	 */
	public boolean hasChildren();

	/**
	 * Returns all the children of the test item.
	 * For test case always returns empty array.
	 * For test suite returns all child test suites and test cases.
	 * 
	 * @return array of test item children
	 */
	public ITestItem[] getChildren();
	
	/**
	 * Visitor pattern support for the tests hierarchy.
	 * 
	 * @param visitor - any object that supports visitor interface
	 */
	public void visit(IModelVisitor visitor);

}
