/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
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

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

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

	//void test_commaPrecededIf() {
	//	int a, b;
	//	if (a=some_value(), b=some_other_value(), a=b){ // warning here
	//		// do something
	//	}else{
	//		// do something else
	//	}
	//}
	//int some_value(){return 0;}
	//int ome_other_value(){return 1;}
	public void test_commaPrecededIf() throws CoreException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//void test_commaPrecededWhile() {
	//	int NO_ERROR = 0;
	//	int error_code;
	//	while (error_code = read_from_file(), error_code = NO_ERROR) { // warning
	//		// do something
	//	}
	//}
	public void test_commaPrecededWhile() throws CoreException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	//void test_commaPrecededDoWhile() {
	//	int NO_ERROR = 0;
	//	int error_code;
	//	do{
	//		// do something
	//	} while (error_code = read_from_file(), error_code = NO_ERROR); // warning
	//}
	public void test_commaPrecededDoWhile() throws CoreException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6);
	}

	//void test_commaPrecededConditioal(){
	//	int a, b, c;
	//	c = (a=some_value(), b=some_other_value(), a=b)? a : b; // warning here
	//}
	public void test_commaPrecededConditioal() throws CoreException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}
}
