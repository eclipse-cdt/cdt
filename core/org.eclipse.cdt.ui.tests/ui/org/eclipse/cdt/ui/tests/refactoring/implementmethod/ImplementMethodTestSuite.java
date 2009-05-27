/*******************************************************************************
 * Copyright (c) 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Ball - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.implementmethod;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTester;

/**
 * Test suite to run just the Implement Method unit tests.
 *
 * @author Tom Ball
 */
public class ImplementMethodTestSuite extends TestSuite {

	@SuppressWarnings("nls")
	public static Test suite() throws Exception {
		TestSuite suite = new ImplementMethodTestSuite();
		suite.addTest(RefactoringTester.suite("ImplementMethodRefactoringTest",
				"resources/refactoring/ImplementMethod.rts"));
		return suite;
	}
}
