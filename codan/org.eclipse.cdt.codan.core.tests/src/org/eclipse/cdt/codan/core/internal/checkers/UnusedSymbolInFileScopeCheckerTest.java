/*******************************************************************************
 * Copyright (c) 2011, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.UnusedSymbolInFileScopeChecker;

/**
 * Test for {@see UnusedSymbolInFileScopeChecker} class
 */
public class UnusedSymbolInFileScopeCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(UnusedSymbolInFileScopeChecker.ER_UNUSED_VARIABLE_DECLARATION_ID,
				UnusedSymbolInFileScopeChecker.ER_UNUSED_FUNCTION_DECLARATION_ID,
				UnusedSymbolInFileScopeChecker.ER_UNUSED_STATIC_FUNCTION_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	// extern function declarations
	////////////////////////////////////////////////////////////////////////////

	// int test_fun();
	// extern int test_efun();
	public void testExternFunction_Declaration_Unused() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
		checkErrorLine(2);
	}

	// int test_fun();
	// void fun() {
	//   test_fun();
	// }
	public void testExternFunction_Declaration_Used() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void test_fun();
	// void test_fun() {}
	public void testExternFunction_Declaration_FollowedByDefinition() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	////////////////////////////////////////////////////////////////////////////
	// extern function definitions
	////////////////////////////////////////////////////////////////////////////

	// void test_fun(void) {}
	public void testExternFunction_Definition() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	////////////////////////////////////////////////////////////////////////////
	// Static function declarations
	////////////////////////////////////////////////////////////////////////////

	// static void test_fun(void);
	public void testStaticFunction_Declaration_Unused() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static void test_fun(void);
	// static void test_fun(void) {}
	public void testStaticFunction_Declaration_FollowedByDefinition() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// static void test_fun(void);
	// void fun() {
	//   test_fun();
	// }
	public void testStaticFunction_Declaration_Used() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	////////////////////////////////////////////////////////////////////////////
	// Static function definitions
	////////////////////////////////////////////////////////////////////////////

	// static void test_fun(void) {}
	public void testStaticFunction_Definition_Unused() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static void test_fun(void);
	// static void test_fun(void) {}
	public void testStaticFunction_Definition_Unused_WithDeclaration() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// static void test_fun(void) {}
	// void fun() {
	//   test_fun();
	// }
	public void testStaticFunction_Definition_Used() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void fun() {
	//   test_fun();
	// }
	// static int test_fun(void) {}
	public void testStaticFunction_Definition_UsedBeforeDefinition() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// static int test_fun(void) {}
	// static int test_fun(int) {}
	// void fun() {
	//   test_fun(0);
	// }
	public void testStaticFunction_Definition_Signature() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorLine(1);
	}

	// static int test_fun(void) {}
	// void fun() {
	//   int test_fun=0;
	// }
	public void testStaticFunction_Definition_SynonymLocalScope() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int test_fun(void) {}
	// void fun(int test_fun) {
	// }
	public void testStaticFunction_Definition_SynonymArgs() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int (test_fun) ();
	public void testStaticFunction_Definition_InParentheses() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int test_fun(int i) {}
	// int i = test_fun(X);
	public void testStaticFunction_Definition_UnknownParameterType() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// static void test_fun(void) {}
	// void Class::fun() {
	//   test_fun();
	// }
	public void testStaticFunction_Definition_InQualifiedFunction() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// static int test_fun(X) {}
	// int i = test_fun(X);
	public void testStaticFunction_Definition_UnknownArgumentType() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	////////////////////////////////////////////////////////////////////////////
	// Extern variables declaration
	////////////////////////////////////////////////////////////////////////////

	// extern int test_var;
	public void testExternVariable_Declaration_Unused() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// extern int test_var;
	// void fun() {
	//   test_var=0;
	// }
	public void testExternVariable_Declaration_Used() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// extern int i,
	//   test_var;
	public void testExternVariable_Declaration_Combined() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
		checkErrorLine(2);
	}

	////////////////////////////////////////////////////////////////////////////
	// Extern variables definition
	////////////////////////////////////////////////////////////////////////////

	// int test_var;
	// int test_var2=0;
	public void testGlobalVariable_Definition() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// extern const int test_var=0; // not quite legal but some compilers allow that
	public void testExternVariable_Definition1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// extern const int test_var;
	// const int test_var = 0;
	public void testExternVariable_Definition2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	////////////////////////////////////////////////////////////////////////////
	// Static variables
	////////////////////////////////////////////////////////////////////////////

	// static int test_var;
	public void testStaticVariable_Unused() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int (*test_var)(float, char, char);
	public void testStaticVariable_Unused_FunctionPointer() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1);
	}

	// static int test_var;
	// int i=test_var;
	public void testStaticVariable_Used_GlobalScope() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// static int test_var;
	// void fun() {
	//   int i=test_var;
	// }
	public void testStaticVariable_Used_LocalScope() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// static int test_var;
	// void Class::fun() {
	//   test_var = 0;
	// }
	public void testStaticVariable_Used_InQualifiedFunction() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// class Class;
	// static Class test_var; // constructor is called here
	public void testStaticVariable_Used_Constructor() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// static X test_var; // avoid possible false positive, binding checker would complain anyway
	public void testExternVariable_Declaration_IgnoreUnresolved() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// static char* test_var="$Id: UnusedSymbolInFileScopeCheckerTest.java,v 1.2 2011/04/29 11:17:42 agvozdev Exp $";
	public void testExternVariable_Declaration_CvsIdent() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// static char* test_var="@(#) $Header: /cvsroot/tools/org.eclipse.cdt/codan/org.eclipse.cdt.codan.core.test/src/org/eclipse/cdt/codan/core/internal/checkers/UnusedSymbolInFileScopeCheckerTest.java,v 1.2 2011/04/29 11:17:42 agvozdev Exp $";
	public void testExternVariable_Declaration_SccsIdent() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// static char test_var[]("@(#) $Id: UnusedSymbolInFileScopeCheckerTest.java,v 1.2 2011/04/29 11:17:42 agvozdev Exp $");
	public void testExternVariable_Declaration_CvsIdentInitializer() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// static int v1 __attribute__((unused));
	// static int __attribute__((unused)) v2;
	// int f1() __attribute__((__unused__));
	// extern int f2() __attribute__((unused));
	// static void f3() __attribute__((unused));
	// void f3() {}
	// static void f4() __attribute__((unused));
	// static void f4() __attribute__((unused)) {}
	// static void __attribute__((unused)) f5();
	// static void f6() __attribute__((unused));
	// static void f6() {}
	// static void __attribute__((unused)) f7();
	// void f7() {}
	// static void __attribute__((unused)) f8();
	// static void f8() {}
	public void testAttributeUnused() throws Exception {
		loadCodeAndRunC(getAboveComment());
		checkNoErrors();

		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	// static void __attribute__((constructor)) Ctor() {}
	// static void __attribute__((destructor)) Dtor();
	// static void Dtor2() __attribute__((destructor));
	// static void Dtor3() __attribute__((destructor));
	// static void Dtor() {}
	// static void Dtor2() {}
	// void Dtor3() {}
	public void testAttributeConstructorDestructor_bug389577() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	extern int* pxCurrentTCB;
	//
	//	int main() {
	//	    asm ("lds r26, pxCurrentTCB\n\t");
	//	}
	public void testUseInAsm_bug393129() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	static void foo(int) {}
	//	static void foo(float) {}
	//
	//	template <typename T>
	//	void bar(T t) {
	//	    foo(t);
	//	}
	//
	//	int main() {
	//	    bar(0);
	//	}
	public void testOverloadedStaticFunctionUsedInTemplate_bug358694() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	//	class S {};
	//	S operator+(S, S);
	//
	//	int main() {
	//		S a, b;
	//		a + b;
	//	}
	public void testOverloadedOperator_536268() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}
}
