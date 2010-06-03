/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *    Felipe Martinez  - ReturnCheckerTest implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;

/**
 * Test for {@see ReturnCheckerTest} class
 * 
 */
public class ReturnCheckerTest extends CheckerTestCase {


//	dummy() {
//	  return; // error here on line 2
//	}
	public void testDummyFunction() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors(); // because return type if not defined, usually people don't care
	}


//	void void_function(void) {
//	  return; // no error here
//	}
	public void testVoidFunction() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}


//	int integer_return_function(void) {
//	  if (global) {
//		if (global == 100) {
//			return; // error here on line 4
//		}
//	  }
//	}	 
	public void testBasicTypeFunction() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

//
//	struct My_Struct {
//	int a;
//	};
//
//	 struct My_Struct struct_return_function(void) {
//	return; // error here on line 6
//	}
	public void testUserDefinedFunction() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6);
	}


//	 typedef unsigned int uint8_t;
//	 
//	uint8_t return_typedef(void) {
//	return; // error here on line 4
//	}
	public void testTypedefReturnFunction() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}


//	typedef unsigned int uint8_t;
//	 	 
//	uint8_t (*return_fp_no_typedef(void))(void)
//	{
//			return; // error here on line 5
//	}
	public void testFunctionPointerReturnFunction() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

}