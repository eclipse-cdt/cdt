/*******************************************************************************
 * Copyright (c) 2010 Severin Gehwolf 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Severin Gehwolf  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.core.resources.IMarker;

/**
 * Test for {@see AssignmentToItselfChecker} class
 * 
 */
public class AssignmentToItselfCheckerTest extends CheckerTestCase {
	// void main() {
	// int x = 0;
	// x = 10;
	// }
	public void testNoErrorConstants() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// int x = 10;
	// int s = 10;
	// x = s;
	// }
	public void testNoErrorVariables() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// int x = 0;
	// x = x;
	// }
	public void testSimpleVariableSelfAssignmentError() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// void main() {
	// char str[] = "hello testing world";
	// int x = 10;
	// str[i] = str[i];
	// }
	public void testArraySelfAssignmentError() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// #define X a = 1
	// void main() {
	//    int a;
	//    X;
	// }
	public void testNoError_Bug321933() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// int x = 0;
	// int y = 0;
	// y *= 2; x =
	// x;
	// }
	public void testStartOffset() { // Bug 486610
		loadCodeAndRun(getAboveComment());
		IMarker marker = checkErrorLine(4);
		// The Offset should start at the beginning of the expression
		assertTrue(marker.getAttribute(IMarker.CHAR_START, -1) == 46);
	}
}
