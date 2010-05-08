/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *    Felipe Martinez  - ExpressionRequiredInReturnCheckerTest implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;

/**
 * Test for {@see ExpressionRequiredInReturnCheckerTest} class
 * 
 */
public class ExpressionRequiredInReturnCheckerTest extends CheckerTestCase {
	/*-
	 <code file="test1.c">
	dummy() {
	return; // error here on line 2
	}
	 </code>
	 */
	public void testDummyFunction() {
		load("test1.c");
		runOnFile();
		checkNoErrors(); // because return type if not defined, usually people don't care
	}

	/*-
	 <code file="test2.c">
	void void_function(void) {
	return; // no error here
	}
	 </code>
	 */
	public void testVoidFunction() {
		load("test2.c");
		runOnFile();
		checkNoErrors();
	}

	/*-
	 <code file="test3.c">
	int integer_return_function(void) {
	if (global) {
		if (global == 100) {
			return; // error here on line 4
		}
	}
	}	 
	 </code>
	 */
	public void testBasicTypeFunction() {
		load("test3.c");
		runOnFile();
		checkErrorLine(4);
	}

	/*-
	 <code file="test4.c">
	struct My_Struct {
	int a;
	};

	 struct My_Struct struct_return_function(void) {
	return; // error here on line 6
	}

	 </code>
	 */
	public void testUserDefinedFunction() {
		load("test4.c");
		runOnFile();
		checkErrorLine(6);
	}

	/*-
	 <code file="test5.c">
	 typedef unsigned int uint8_t;
	 
	uint8_t return_typedef(void) {
	return; // error here on line 4
	}
	 </code>
	 */
	public void testTypedefReturnFunction() {
		load("test5.c");
		runOnFile();
		checkErrorLine(4);
	}

	/*-
	 <code file="test6.c">
	 	 typedef unsigned int uint8_t;
	 	 
	uint8_t (*return_fp_no_typedef(void))(void)
		{
			return; // error here on line 5
		}
	 </code>
	 */
	public void testFunctionPointerReturnFunction() {
		load("test6.c");
		runOnFile();
		checkErrorLine(5);
	}

}