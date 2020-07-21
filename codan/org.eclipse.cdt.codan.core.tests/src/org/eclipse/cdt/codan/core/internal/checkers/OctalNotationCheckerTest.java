/*******************************************************************************
 * Copyright (c) 2020 Sergey Vladimirov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tomasz Wesolowski [bug 354556]
 *     Sergey Vladimirov - implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.OctalNotationChecker;

public class OctalNotationCheckerTest extends CheckerTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(OctalNotationChecker.PROBLEM_ID);
	}

	//	int a = 10;
	public void testNotReportFalsePositiveForDecimalInt() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	int a = 070;
	public void testReportTruePositiveForOctalInteger() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	//	unsigned a = 015u;
	public void testReportTruePositiveForOctalUnsigned() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	//	float a = 0.042f;
	public void testNotReportFalsePositiveForFloat() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

}