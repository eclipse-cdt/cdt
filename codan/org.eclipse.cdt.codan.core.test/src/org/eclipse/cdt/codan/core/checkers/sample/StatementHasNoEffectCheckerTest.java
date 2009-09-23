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
package org.eclipse.cdt.codan.core.checkers.sample;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;

/**
 * Test for {@see StatementHasNoEffectChecker} class
 * 
 */
public class StatementHasNoEffectCheckerTest extends CheckerTestCase {
	/*-
	 <code file="test1.c">
	 main() {
	   int a;
	   +a; // error here on line 3
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
	   int a,b;
	   
	   b+a; // error here on line 4
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
	   int a,b;
	   
	   a=b+a; // no error here
	 }
	 </code>
	 */
	public void test3() {
		load("test3.c");
		runOnFile();
		checkNoErrors();
	}
}
