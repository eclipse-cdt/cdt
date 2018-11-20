/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ScannerTestSuite extends TestSuite {

	public static Test suite() {
		TestSuite suite = new ScannerTestSuite();
		suite.addTest(LexerTests.suite());
		suite.addTest(LocationMapTests.suite());
		suite.addTest(PortedScannerTests.suite());
		suite.addTest(PreprocessorTests.suite());
		suite.addTest(InclusionTests.suite());
		suite.addTest(PreprocessorBugsTests.suite());
		suite.addTest(ExpansionExplorerTests.suite());
		suite.addTest(InactiveCodeTests.suite());
		suite.addTest(StreamHasherTests.suite());
		suite.addTest(FileCharArrayTests.suite());
		return suite;
	}
}
