/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
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

/**
 * Test for {@see SuggestedParenthesisChecker} class
 * 
 */
public class SuggestedParenthesisCheckerTest extends CheckerTestCase {
	/*-
	 <code file="test1.c">
	 main() {
	   int a=1,b=3;
	   if (!a<10) b=4; // error here on line 3
	 }
	 </code>
	 */
	public void test1() {
		load("test1.c");
		runOnFile();
		checkErrorLine(3);
	}
	
	/*-
	 <code file="test2.c">
	 main() {
	   int a=1,b=3;
	   
	   if (b+a && a>b || b-a) b--; // error here on line 4
	 }
	 </code>
	 */
	public void test2() {
		load("test2.c");
		runOnFile();
		checkErrorLine(4);
	}
	
	/*-
	 <code file="test3.c">
	 main() {
       int a=1,b=3;
	   if (!(a<10)) b=4; // no error here on line 3
	 }
	 </code>
	 */
	public void test3() {
		load("test3.c");
		runOnFile();
		checkNoErrors();
	}
	/*-
	 <code file="test4.c">
	 main() {
      int a=1,b=3;
	   if (a && !b) b=4; // no error here on line 3
	 }
	 </code>
	 */
	public void test_lastnot() {
		load("test4.c");
		runOnFile();
		checkNoErrors();
	}
	
	/*-
	 <code file="test5.c">
	 main() {
       int a=1,b=3;
	   if ((!a) && 10) b=4; // no error here on line 3
	 }
	 </code>
	 */
	public void test_fixed() {
		load("test5.c");
		runOnFile();
		checkNoErrors();
	}
	
	/*-
	 <code file="test6.c">
	 main() {
      int a=1,b=3;
	   if (a && b & a) b=4; //  error here on line 3
	 }
	 </code>
	 */
	public void test_mixedbin() {
		load("test6.c");
		runOnFile();
		checkErrorLine(3);
	}
}
