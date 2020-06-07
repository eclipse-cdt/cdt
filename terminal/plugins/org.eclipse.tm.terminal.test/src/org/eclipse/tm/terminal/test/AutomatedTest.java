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
package org.eclipse.tm.terminal.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Master test suite to run all terminal unit tests.
 */
public class AutomatedTest extends TestCase {

	public static final String PI_TERMINAL_TESTS = "org.eclipse.tm.terminal.test"; //$NON-NLS-1$

	public AutomatedTest() {
		super(null);
	}

	public AutomatedTest(String name) {
		super(name);
	}

	/**
	 * Call each AllTests class from each of the test packages.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(AutomatedTest.class.getName());
		suite.addTest(org.eclipse.tm.internal.terminal.emulator.AllTest.suite());
		suite.addTest(org.eclipse.tm.internal.terminal.model.AllTest.suite());
		suite.addTest(org.eclipse.tm.terminal.model.AllTest.suite());
		suite.addTestSuite(org.eclipse.tm.internal.terminal.connector.TerminalConnectorTest.class);
		suite.addTestSuite(org.eclipse.tm.internal.terminal.connector.TerminalToRemoteInjectionOutputStreamTest.class);
		return suite;
	}
}
