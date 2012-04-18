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
 * Interface to the test case (test) of the test hierarchy.
 * 
 * Test cases stores the name, status, testing time and messages.
 * Also has the reference to the parent test suite (if any).
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestCase extends ITestItem {

	/**
	 * Returns test messages that were produced during the test case running.
	 * 
	 * @return array of test messages
	 */
	public ITestMessage[] getTestMessages();

}
