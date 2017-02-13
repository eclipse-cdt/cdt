/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IMarker;

/**
 * Test for {@see AssignmentInConditionChecker} class
 */
public class AssignmentInConditionCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems("org.eclipse.cdt.codan.internal.checkers.AssignmentInConditionProblem");
	}

	//	 main() {
	//	   int a=1,b=3;
	//	   if (a=b) b=4; // error here on line 3
	//	 }
	public void test_basic() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//	   int a=1,b=3;
	//
	//	   if ((a=b)) b--; // no error
	//	 }
	public void test_fixed() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	 main() {
	//     int a=1,b=3;
	//	   if ((a=b)!=0) b=4; // no error here on line 3
	//	 }
	public void test3() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// main() {
	//   int i,a[10];
	//   if (a[i]=0) b=4; // no error here on line 3
	// }
	public void test_array() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//      int i,b=3;
	//	for (i = 0; i=b; i++) { // here
	//	}
	//	 }
	public void test_for() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//      int i,b=3;
	//	while (i=b) { // here
	//	}
	//	 }
	public void test_while() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//      int i,b=3;
	//	(i=b)?i++:b++;  // here
	//	 }
	public void test_tri() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	 main() {
	//	   int a=1,b=3;
	//	   if (a=b) b=4; // error here on line 3
	//	 }
	public void test_basic_params() throws Exception {
		loadCodeAndRun(getAboveComment());
		IMarker marker = checkErrorLine(3);
		String arg = CodanProblemMarker.getProblemArgument(marker, 0);
		assertEquals("a=b", arg); //$NON-NLS-1$
	}

	//	main() {
	//      int i;
	//	    while (i=b()) { // @suppress("Assignment in condition")
	//	    }
	//	}
	public void test_while2supp() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	main() {
	//      int i;
	//	    while (i=b()) { /* @suppress("Assignment in condition") */
	//	    }
	//	}
	public void test_while3supp() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	#define LOOP() while (i=b() /* @suppress("Assignment in condition") */ ) {  }
	//  main() {
	//      int i;
	//	    LOOP();
	//	}
	public void test_whileMacroSupp() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4); // TODO: suppression does not work in macro body now
	}

	//	#define LOOP() while (i=b()) { }
	//  main() {
	//      int i;
	//	    LOOP(); // err
	//	}
	public void test_whileMacro() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}
}
