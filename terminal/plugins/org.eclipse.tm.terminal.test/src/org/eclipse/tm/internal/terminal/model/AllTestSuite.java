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
package org.eclipse.tm.internal.terminal.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Internal Terminal Model test cases.
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
		suite.addTestSuite(SnapshotChangesTest.class);
		suite.addTestSuite(SynchronizedTerminalTextDataTest.class);
		suite.addTestSuite(TerminalTextDataFastScrollTest.class);
		suite.addTestSuite(TerminalTextDataFastScrollMaxHeightTest.class);
		suite.addTestSuite(TerminalTextDataPerformanceTest.class);
		suite.addTestSuite(TerminalTextDataSnapshotTest.class);
		suite.addTestSuite(TerminalTextDataSnapshotWindowTest.class);
		suite.addTestSuite(TerminalTextDataStoreTest.class);
		suite.addTestSuite(TerminalTextDataTest.class);
		suite.addTestSuite(TerminalTextDataWindowTest.class);
		return suite;
	}

}
