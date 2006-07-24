/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.errorparsers.xlc.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllXlcErrorParserTests {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllXlcErrorParserTests.suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Testsuite for xlc compiler error parser");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestUndeclIdent.class);
		suite.addTestSuite(TestMissingArg.class);
		suite.addTestSuite(TestFloatingPoint.class);
		suite.addTestSuite(TestFuncArg.class);
		suite.addTestSuite(TestOperModi.class);
		suite.addTestSuite(TestConditional.class);
		suite.addTestSuite(TestSyntaxError.class);
		suite.addTestSuite(TestNoFuncProto.class);
		//$JUnit-END$
		return suite;
	}
}