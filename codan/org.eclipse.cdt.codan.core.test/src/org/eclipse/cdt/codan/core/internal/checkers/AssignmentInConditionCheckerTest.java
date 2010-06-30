/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IMarker;

/**
 * Test for {@see SuggestedParenthesisChecker} class
 * 
 */
public class AssignmentInConditionCheckerTest extends CheckerTestCase {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.test.CodanTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems("org.eclipse.cdt.codan.internal.checkers.AssignmentInConditionProblem");
	}

	//	 main() {
	//	   int a=1,b=3;
	//	   if (a=b) b=4; // error here on line 3
	//	 }	
	public void test_basic() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//	   int a=1,b=3;
	//	   
	//	   if ((a=b)) b--; // no error
	//	 }
	public void test_fixed() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	 main() {
	//     int a=1,b=3;
	//	   if ((a=b)!=0) b=4; // no error here on line 3
	//	 }
	public void test3() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// main() {
	//   int i,a[10];
	//   if (a[i]=0) b=4; // no error here on line 3
	// }
	public void test_array() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//      int i,b=3;
	//	for (i = 0; i=b; i++) { // here
	//	}
	//	 }
	public void test_for() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//      int i,b=3;
	//	while (i=b) { // here
	//	}
	//	 }
	public void test_while() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//      int i,b=3;
	//	(i=b)?i++:b++;  // here
	//	 }
	public void test_tri() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//	   int a=1,b=3;
	//	   if (a=b) b=4; // error here on line 3
	//	 }	
	public void test_basic_params() {
		loadCodeAndRun(getAboveComment());
		IMarker marker = checkErrorLine(3);
		String arg = CodanProblemMarker.getProblemArgument(marker, 0);
		assertEquals("a=b", arg); //$NON-NLS-1$
	}
}
