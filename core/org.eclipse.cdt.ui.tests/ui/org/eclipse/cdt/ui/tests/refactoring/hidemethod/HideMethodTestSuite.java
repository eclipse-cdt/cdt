/*******************************************************************************
 * Copyright (c) 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Ball (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.hidemethod;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTester;

/**
 * Test suite to run just the Hide Method unit tests.
 *
 * @author Tom Ball
 */
public class HideMethodTestSuite extends TestSuite {

	@SuppressWarnings("nls")
	public static Test suite() throws Exception {
		TestSuite suite = new HideMethodTestSuite();
		suite.addTest(RefactoringTester.suite("HideMethodRefactoringTests",
				"resources/refactoring/HideMethod.rts"));
		suite.addTest(RefactoringTester.suite("HideMethodRefactoringHistoryTests",
				"resources/refactoring/HideMethodHistory.rts"));
		return suite;
	}
}
