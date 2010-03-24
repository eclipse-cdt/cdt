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
	public void testUnaryExpression() {
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
	public void testBinaryExpression() {
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
	public void testNormalAssignment() {
		load("test3.c");
		runOnFile();
		checkNoErrors();
	}
	/*-
	 <code file="test4.c">
	 main() {
	   int a,b;
	   
	   (a=b); // no errors here
	   a+=b;
	   a<<=b;
	   a-=b;
	   a++;
	   b--;
	   --a;
	   ++b;
	   a%=2;
	   a>>=2;
	 }
	 </code>
	 */
	public void testFalsePositives() {
		load("test4.c");
		runOnFile();
		checkNoErrors();
	}
	
	/*-
	 <code file="test5.c">
	 main() {
	   int a;
	   a; // error here on line 3
	 }
	 </code>
	 */
	public void testIdExpression() {
		load("test5.c");
		runOnFile();
		checkErrorLine(3);
	}
}
