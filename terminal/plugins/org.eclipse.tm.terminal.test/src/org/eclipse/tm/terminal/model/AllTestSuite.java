/*******************************************************************************
 * Copyright (c) 2008, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Public Terminal Model test cases.
 * Runs in internal model package to allow access to default visible items.
 */
public class AllTestSuite extends TestCase {
	public AllTestSuite() {
		super(null);
	}

	public AllTestSuite(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestSuite.class.getName());
		suite.addTest(new JUnit4TestAdapter(TerminalColorUITest.class));
		suite.addTestSuite(StyleTest.class);
		return suite;
	}

}
