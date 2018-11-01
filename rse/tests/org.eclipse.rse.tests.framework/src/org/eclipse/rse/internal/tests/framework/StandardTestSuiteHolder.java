/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework;

import junit.framework.TestSuite;

import org.eclipse.rse.tests.framework.DelegatingTestSuiteHolder;

/**
 * A SuiteHolder can deliver a test suite when asked.  It references
 * a JUnit TestSuite named by a "suite" tag to do this.
 */
public class StandardTestSuiteHolder extends DelegatingTestSuiteHolder {
	
	private TestSuite suite;
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getTestSuite()
	 */
	public TestSuite getTestSuite() {
		if (suite == null) {
			suite = (TestSuite) getObjectValue("class"); //$NON-NLS-1$
		}
		return suite;
	}

}


