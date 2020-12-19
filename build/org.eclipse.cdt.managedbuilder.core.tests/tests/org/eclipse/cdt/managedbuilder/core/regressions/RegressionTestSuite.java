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

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Regression tests for builder bugs
 */
public class RegressionTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(RegressionTestSuite.class.getName());

		// Test that common builder does the correct amount of work.
		suite.addTest(new JUnit4TestAdapter(Bug_303953Test.class));

		return suite;
	}

	public RegressionTestSuite() {
		super(null);
	}

	public RegressionTestSuite(String name) {
		super(name);
	}
}
