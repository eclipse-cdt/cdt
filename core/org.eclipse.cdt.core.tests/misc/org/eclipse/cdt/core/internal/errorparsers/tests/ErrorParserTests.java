/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ErrorParserTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(ErrorParserTests.class.getName());

		// Just add more test cases here as you create them for
		// each class being tested
		suite.addTest(GCCErrorParserTests.suite());
		suite.addTest(GLDErrorParserTests.suite());
		suite.addTest(MakeErrorParserTests.suite());
		suite.addTest(FileBasedErrorParserTests.suite());
		suite.addTest(ErrorParserManagerTest.suite());
		suite.addTest(ErrorParserFileMatchingTest.suite());
		suite.addTest(ErrorParserEfsFileMatchingTest.suite());
		suite.addTest(RegexErrorParserTests.suite());
		return suite;
	}

}
