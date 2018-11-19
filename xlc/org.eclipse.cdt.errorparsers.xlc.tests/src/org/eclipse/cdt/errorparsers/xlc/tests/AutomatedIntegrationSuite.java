/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.errorparsers.xlc.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AutomatedIntegrationSuite {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(AutomatedIntegrationSuite.suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Testsuite for xlc compiler error parser");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestInformationalMessage_1.class);
		suite.addTestSuite(TestInformationalMessage_2.class);
		suite.addTestSuite(TestInformationalMessage_3.class);
		suite.addTestSuite(TestWarning_1.class);
		suite.addTestSuite(TestError_1.class);
		suite.addTestSuite(TestSevereError_1.class);
		suite.addTestSuite(TestSevereError_2.class);
		suite.addTestSuite(TestSevereError_3.class);
		suite.addTestSuite(TestSevereError_4.class);
		suite.addTestSuite(TestSevereError_5.class);
		suite.addTestSuite(TestUnrecoverableError_1.class);
		suite.addTestSuite(TestUnrecoverableError_2.class);
		suite.addTestSuite(TestUnrecoverableError_3.class);

		suite.addTestSuite(TestCompatibility.class);
		suite.addTestSuite(TestRedefinition.class);
		suite.addTestSuite(TestRedeclaration.class);
		suite.addTestSuite(TestCommandOptionNotRecognized.class);

		suite.addTestSuite(TestLinkerCommandOptionNotRecognized.class);
		suite.addTestSuite(TestLinkerUndefinedSymbol.class);
		suite.addTestSuite(TestLinkerDuplicateSymbol.class);
		suite.addTestSuite(TestLinkerSevereError.class);
		suite.addTestSuite(TestLinkerErrorWhileReading.class);
		suite.addTestSuite(TestLinkerInfo.class);

		//$JUnit-END$
		return suite;
	}
}