/*******************************************************************************
 * Copyright (c) 2010, 2011 Meisam Fathi and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Meisam Fathi  - base API
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;

/**
 * Test for {@see FormatStringChecker} class
 */
public class FormatStringCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems("org.eclipse.cdt.codan.internal.checkers.ScanfFormatStringSecurityProblem"); //$NON-NLS-1$
	}

	// int f(){
	// return 0;
	// }
	public void testBase() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int f(){
	// char inputstr[5];
	// scanf("%s", inputstr); // here
	// return 0;
	// }
	public void testSimple() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// int main(void) {
	// char inputstr1[5];
	// int inputval;
	// int i = 5;
	// scanf("%i %4s", inputval, inputstr1); // no error here
	// printf("%d" ,i);
	// return 0;
	// }
	public void testIntRight() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int main(void) {
	// char inputstr[5];
	// int inputval;
	// int i = 5;
	// scanf("%d %9s", inputval, inputstr);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testIntWrong() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

	// int main(void) {
	// char inputstr1[5];
	// int inputval;
	// int i = 5;
	// scanf("%4s %i", inputstr1, inputval);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testRightInt() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int main(void) {
	// char inputstr1[5];
	// char inputstr2[5];
	// int i = 5;
	// scanf("%4s %9s", inputstr1, inputstr2);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testRightWrong() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

	// int main(void) {
	// char inputstr1[5];
	// int inputval;
	// int i = 5;
	// scanf("%9s %i", inputstr1, inputval);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testWrongInt() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

	// int main(void) {
	// char inputstr1[5];
	// char inputstr2[5];
	// int i = 5;
	// scanf("%9s %4s", inputstr1, inputstr2);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testWrongRight() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

	// int main(void) {
	// char inputstr[5];
	// int i = 5;
	// scanf("%s", inputstr);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testInfiniteSize() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// int main(void) {
	// puts("Enter a string whose length is bellow 5:");
	// char inputstr[5];
	// int i = 5;
	// scanf("%3s", inputstr);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testRight() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int main(void) {
	// char inputstr1[5];
	// char inputstr2[5];
	// int i = 5;
	// scanf("%3s %4s", inputstr1, inputstr2);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testRightRight() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int main(void) {
	// char inputstr1[5];
	// char inputstr2[5];
	// int i = 5;
	// scanf("%8s %9s", inputstr1, inputstr2);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testWrongWrong() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

	// char inputstr[5];
	// int foo(void){
	// char inputstr[15];
	// return 0;
	// }
	// int main(void) {
	// puts("Enter a string whose length is bellow 5:");
	// int i = 5;
	// scanf("%10s", inputstr);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testGlobalBeforeWrong() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(9);
	}

	// int main(void) {
	// puts("Enter a string whose length is bellow 5:");
	// char inputstr[15];
	// int i = 5;
	// scanf("%10s", inputstr);
	// printf("%d" ,i);
	// return 0;
	// }
	// int foo(void){
	// char inputstr[5];
	// return 0;
	// }
	public void testNonglobalAfterRight() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int main(void) {
	// char inputstr[5];
	// int i = 5;
	// scanf("%10s", inputstr);
	// printf("%d" ,i);
	// return EXIT_SUCCESS;
	// }
	// int foo(void){
	// char inputstr[15];
	// return 0;
	// }
	public void testNonglobalAfterWrong() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// int foo(void){
	// char inputstr[5];
	// return 0;
	// }
	// int main(void) {
	// char inputstr[15];
	// int i = 5;
	// scanf("%10s", inputstr);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testNonglobalBeforeRight() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// int foo(void){
	// char inputstr[15];
	// return 0;
	// }
	// int main(void) {
	// char inputstr[5];
	// int i = 5;
	// scanf("%10s", inputstr);
	// printf("%d" ,i);
	// return 0;
	// }
	public void testNonglobalBeforeWrong() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(8);
	}

	// int main(void) {
	// char inputstr1[5];
	// int inputval;
	// int i = 5;
	// scanf("%i %as", inputval, inputstr1); // no error here
	// printf("%d" ,i);
	// return 0;
	// }
	public void testGaurdedRight() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}
}