/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.core.runtime.CoreException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * CModelExceptionTest
 *
 * @author Judy N. Green
 * @since Jul 19, 2002
 */
public class CModelExceptionTest extends TestCase {
	// Shared values setup and torn down
	private CModelStatus cModelStatus;
	private CoreException coreException;

	/**
	 * Constructor for TestCModelException.
	 * @param name
	 */
	public CModelExceptionTest(String name) {
		super(name);
	}

	/**
	 * Sets up the test fixture.
	 *
	 * Called before every test case method.
	 *
	 * Example code test the packages in the project
	 *  "com.qnx.tools.ide.cdt.core"
	 */
	@Override
	protected void setUp() {
		// create shared resources and setup the test fixture
		cModelStatus = new CModelStatus();
		coreException = new CoreException(cModelStatus);
	}

	/**
	* Tears down the test fixture.
	*
	* Called after every test case method.
	*/
	@Override
	protected void tearDown() {
		// release resources here and clean-up
	}

	public static TestSuite suite() {
		return new TestSuite(CModelExceptionTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public void testCreationNoStatus() {
		CModelException testException = new CModelException(coreException);
		// should be the same object inside
		assertTrue("Object compare failed", testException.getException() == coreException);
	}

	public void testCreationWithStatus() {
		CModelException testException = new CModelException(coreException, ICModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		// should not be null
		assertTrue("TestException.getStatus() is null", (testException.getStatus() != null));

		// should have the same status as was set on creation
		assertTrue("Object compare failed",
				testException.getStatus().getCode() == ICModelStatusConstants.INDEX_OUT_OF_BOUNDS);
	}

	public void testElementDoesNotExist() {
		CModelException testException = new CModelException(coreException,
				ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST);
		// should not exist since this is the value we set on creation
		assertTrue("Object unexpectedly exists", testException.doesNotExist());
	}

	public void testElementExists() {
		CModelException testException = new CModelException(coreException, ICModelStatusConstants.INVALID_CONTENTS);
		// should not exist since this is the value we set on creation
		assertTrue("Object unexpectedly does not exist", !testException.doesNotExist());
	}
}
