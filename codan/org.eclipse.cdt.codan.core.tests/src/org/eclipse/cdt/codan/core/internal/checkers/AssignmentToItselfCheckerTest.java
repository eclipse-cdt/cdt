/*******************************************************************************
 * Copyright (c) 2010 Severin Gehwolf
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Severin Gehwolf  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.core.resources.IMarker;

/**
 * Test for {@see AssignmentToItselfChecker} class
 */
public class AssignmentToItselfCheckerTest extends CheckerTestCase {
	// void main() {
	// int x = 0;
	// x = 10;
	// }
	public void testNoErrorConstants() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// int x = 10;
	// int s = 10;
	// x = s;
	// }
	public void testNoErrorVariables() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// int x = 0;
	// x = x;
	// }
	public void testSimpleVariableSelfAssignmentError() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// void main() {
	// char str[] = "hello testing world";
	// int x = 10;
	// str[i] = str[i];
	// }
	public void testArraySelfAssignmentError() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// #define X a = 1
	// void main() {
	//    int a;
	//    X;
	// }
	public void testNoError_Bug321933() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	//    int x = 0; x
	//       = x;
	// }
	public void testMarkerOffset_Bug486610() throws Exception {
		String code = getAboveComment();
		loadCodeAndRun(code);
		IMarker marker = checkErrorLine(2);
		int start = marker.getAttribute(IMarker.CHAR_START, -1);
		int end = marker.getAttribute(IMarker.CHAR_END, -1);
		// The offset should start at the beginning of the expression "x = x"
		assertEquals("x" + NL + "       = x", code.substring(start, end));
	}
}
