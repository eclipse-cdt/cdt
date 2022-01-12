/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.SuggestedParenthesisChecker;

/**
 * Test for {@see SuggestedParenthesisChecker} class
 */
public class SuggestedParenthesisCheckerTest extends CheckerTestCase {
	//	 main() {
	//	   int a=1,b=3;
	//	   if (!a<10) b=4; // error here on line 3
	//	 }
	public void test1() throws Exception {
		IProblemPreference macro = getPreference(SuggestedParenthesisChecker.ER_ID,
				SuggestedParenthesisChecker.PARAM_NOT);
		macro.setValue(Boolean.TRUE);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//	   int a=1,b=3;
	//
	//	   if (b+a && a>b || b-a) b--; // error here on line 4
	//	 }
	public void test2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	//	 main() {
	//     int a=1,b=3;
	//	   if (!(a<10)) b=4; // no error here on line 3
	//	 }
	public void test3() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// main() {
	//   int a=1,b=3;
	//   if (a && !b) b=4; // no error here on line 3
	// }
	public void test_lastnot() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	 main() {
	//      int a=1,b=3;
	//	    if ((!a) && 10) b=4; // no error here on line 3
	//	 }
	public void test_fixed() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	 main() {
	//      int a=1,b=3;
	//	    if (a && b & a) b=4; //  error here on line 3
	//	 }
	public void test_mixedbin() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}
}
