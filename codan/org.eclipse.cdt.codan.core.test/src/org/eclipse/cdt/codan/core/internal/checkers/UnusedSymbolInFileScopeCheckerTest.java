/*******************************************************************************
 * Copyright (c) 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import java.io.IOException;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.UnusedSymbolInFileScopeChecker;

/**
 * Test for {@see UnusedSymbolInFileScopeChecker} class
 *
 */
public class UnusedSymbolInFileScopeCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(
				UnusedSymbolInFileScopeChecker.ER_UNUSED_VARIABLE_DECLARATION_ID,
				UnusedSymbolInFileScopeChecker.ER_UNUSED_FUNCTION_DECLARATION_ID,
				UnusedSymbolInFileScopeChecker.ER_UNUSED_STATIC_FUNCTION_ID);
	}

	////////////////////////////////////////////////////////////////////////////
	// extern function declarations
	////////////////////////////////////////////////////////////////////////////

	// int test_fun();
	// extern int test_efun();
	public void testExternFunction_Declaration_Unused() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
		checkErrorLine(2);
	}

	// int test_fun();
	// void fun() {
	//   test_fun();
	// }
	public void testExternFunction_Declaration_Used() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void test_fun();
	// void test_fun() {}
	public void testExternFunction_Declaration_FollowedByDefinition() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	////////////////////////////////////////////////////////////////////////////
	// extern function definitions
	////////////////////////////////////////////////////////////////////////////

	// void test_fun(void) {}
	public void testExternFunction_Definition() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	////////////////////////////////////////////////////////////////////////////
	// Static function declarations
	////////////////////////////////////////////////////////////////////////////

	// static void test_fun(void);
	public void testStaticFunction_Declaration_Unused() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static void test_fun(void);
	// static void test_fun(void) {}
	public void testStaticFunction_Declaration_FollowedByDefinition() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// static void test_fun(void);
	// void fun() {
	//   test_fun();
	// }
	public void testStaticFunction_Declaration_Used() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	////////////////////////////////////////////////////////////////////////////
	// Static function definitions
	////////////////////////////////////////////////////////////////////////////

	// static void test_fun(void) {}
	public void testStaticFunction_Definition_Unused() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static void test_fun(void);
	// static void test_fun(void) {}
	public void testStaticFunction_Definition_Unused_WithDeclaration() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// static void test_fun(void) {}
	// void fun() {
	//   test_fun();
	// }
	public void testStaticFunction_Definition_Used() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void fun() {
	//   test_fun();
	// }
	// static int test_fun(void) {}
	public void testStaticFunction_Definition_UsedBeforeDefinition() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// static int test_fun(void) {}
	// static int test_fun(int) {}
	// void fun() {
	//   test_fun(0);
	// }
	public void testStaticFunction_Definition_Signature() throws IOException {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorLine(1);
	}

	// static int test_fun(void) {}
	// void fun() {
	//   int test_fun=0;
	// }
	public void testStaticFunction_Definition_SynonymLocalScope() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int test_fun(void) {}
	// void fun(int test_fun) {
	// }
	public void testStaticFunction_Definition_SynonymArgs() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int (test_fun) ();
	public void testStaticFunction_Definition_InParentheses() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	////////////////////////////////////////////////////////////////////////////
	// Extern variables declaration
	////////////////////////////////////////////////////////////////////////////

	// extern int test_var;
	public void testExternVariable_Declaration_Unused() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// extern int test_var;
	// void fun() {
	//   test_var=0;
	// }
	public void testExternVariable_Declaration_Used() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// extern int i,
	//   test_var;
	public void testExternVariable_Declaration_Combined() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
		checkErrorLine(2);
	}

	////////////////////////////////////////////////////////////////////////////
	// Extern variables definition
	////////////////////////////////////////////////////////////////////////////

	// int test_var;
	// int test_var2=0;
	public void testExternVariable_Definition() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	////////////////////////////////////////////////////////////////////////////
	// Static variables
	////////////////////////////////////////////////////////////////////////////

	// static int test_var;
	public void testStaticVariable_Unused() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int (*test_var)(float, char, char);
	public void testStaticVariable_Unused_FunctionPointer() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int test_var;
	// int i=test_var;
	public void testStaticVariable_Used_GlobalScope() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// static int test_var;
	// void fun() {
	//   int i=test_var;
	// }
	public void testStaticVariable_Used_LocalScope() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class Class;
	// static Class test_var; // constructor is called here
	public void testStaticVariable_Used_Constructor() throws IOException {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// static X test_var; // avoid possible false positive, binding checker would complain anyway
	public void testExternVariable_Declaration_IgnoreUnresolved() throws IOException {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

}
