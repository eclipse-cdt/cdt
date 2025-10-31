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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * CModelExceptionTest
 *
 * @author Judy N. Green
 * @since Jul 19, 2002
 */
public class CModelExceptionTest {
	// Shared values setup and torn down
	private CModelStatus cModelStatus;
	private CoreException coreException;

	@BeforeEach
	protected void setUp() {
		// create shared resources and setup the test fixture
		cModelStatus = new CModelStatus();
		coreException = new CoreException(cModelStatus);
	}

	@Test
	public void testCreationNoStatus() {
		CModelException testException = new CModelException(coreException);
		// should be the same object inside
		assertTrue(testException.getException() == coreException, "Object compare failed");
	}

	@Test
	public void testCreationWithStatus() {
		CModelException testException = new CModelException(coreException, ICModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		// should not be null
		assertTrue((testException.getStatus() != null), "TestException.getStatus() is null");

		// should have the same status as was set on creation
		assertTrue(testException.getStatus().getCode() == ICModelStatusConstants.INDEX_OUT_OF_BOUNDS,
				"Object compare failed");
	}

	@Test
	public void testElementDoesNotExist() {
		CModelException testException = new CModelException(coreException,
				ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST);
		// should not exist since this is the value we set on creation
		assertTrue(testException.doesNotExist(), "Object unexpectedly exists");
	}

	@Test
	public void testElementExists() {
		CModelException testException = new CModelException(coreException, ICModelStatusConstants.INVALID_CONTENTS);
		// should not exist since this is the value we set on creation
		assertTrue(!testException.doesNotExist(), "Object unexpectedly does not exist");
	}
}
