/*******************************************************************************
 * Copyright (c) 2011 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Broadcom Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.regressions;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Regression tests for builder bugs
 */
public class RegressionTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(RegressionTests.class.getName());

		// Test that common builder does the correct amount of work.
		suite.addTestSuite(Bug_303953.class);

		return suite;
	}

	public RegressionTests() {
		super(null);
	}

	public RegressionTests(String name) {
		super(name);
	}
}
