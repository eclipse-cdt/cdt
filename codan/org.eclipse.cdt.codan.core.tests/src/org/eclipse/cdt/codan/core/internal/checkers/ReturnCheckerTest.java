/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
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
 *    Felipe Martinez  - ReturnCheckerTest implementation
 *    Tomasz Wesolowski - Bug 348387
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.ReturnChecker;

/**
 * Test for {@see ReturnCheckerTest} class
 */
public class ReturnCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ReturnChecker.RET_NORET_ID, ReturnChecker.RET_ERR_VALUE_ID, ReturnChecker.RET_NO_VALUE_ID,
				ReturnChecker.RET_LOCAL_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//	void dummy() {
	//	  return; // no error here on line 2
	//	}
	public void testDummyFunction() throws Exception {
		checkSampleAbove();
		// because return type if not defined, usually people don't care
	}

	//	void void_function(void) {
	//	  return; // no error here
	//	}
	public void testVoidFunction() throws Exception {
		checkSampleAbove();
	}

	//	int integer_return_function(void) { // error
	//	  if (global) {
	//		if (global == 100) {
	//			return; // error here on line 4
	//		}
	//	  }
	//	}
	public void testBasicTypeFunction() throws Exception {
		checkSampleAbove();
	}

	//
	//	struct My_Struct {
	//	int a;
	//	};
	//
	//	 struct My_Struct struct_return_function(void) {
	//	return; // error here on line 6
	//	}
	public void testUserDefinedFunction() throws Exception {
		checkSampleAbove();
	}

	//	 typedef unsigned int uint8_t;
	//
	//	uint8_t return_typedef(void) {
	//	return; // error here on line 4
	//	}
	public void testTypedefReturnFunction() throws Exception {
		checkSampleAbove();
	}

	//	typedef unsigned int uint8_t;
	//
	//	uint8_t (*return_fp_no_typedef(void))(void)
	//	{
	//			return; // error here on line 5
	//	}
	public void testFunctionPointerReturnFunction() throws Exception {
		checkSampleAbove();
	}

	//	void test() {
	//		  class A {
	//		   public:
	//		    void m() {
	//		      return; // should not be an error here
	//		    }
	//		  };
	//		}
	public void testInnerFunction_Bug315525() throws Exception {
		checkSampleAbove();
	}

	//	void test() {
	//		  class A {
	//		   public:
	//		    int m() {
	//		      return; // error here
	//		    }
	//		  };
	//		}
	public void testInnerFunction_Bug316154() throws Exception {
		checkSampleAbove();
	}

	//	class c {
	//		c() {
	//			return 0;
	//		}
	//
	//		~c() {
	//			return;
	//		}
	//	};
	public void testConstructorRetValue() throws Exception {
		loadCodeAndRunCpp(getAboveComment());
		checkErrorLine(3, ReturnChecker.RET_ERR_VALUE_ID);
	}

	//	class c {
	//		c() {
	//			return;
	//		}
	//
	//		~c() {
	//			return;
	//		}
	//	};
	public void testConstructor_Bug323602() throws Exception {
		IProblemPreference macro = getPreference(ReturnChecker.RET_NO_VALUE_ID, ReturnChecker.PARAM_IMPLICIT);
		macro.setValue(Boolean.TRUE);
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	//	void f()
	//	{
	//	    [](int r){return r;}(5);
	//	}
	public void testLambda_Bug332285() throws Exception {
		checkSampleAbove();
	}

	//	void f()
	//	{
	//	    if ([](int r){return r == 0;}(0))
	//	        ;
	//	}
	public void testLambda2_Bug332285() throws Exception {
		checkSampleAbove();
	}

	//	void g()
	//	{
	//		int r;
	//	    ({return r;}); // error
	//	}
	public void testGccExtensions() throws Exception {
		checkSampleAbove();
	}

	//	auto f() -> void
	//	{
	//	}
	public void testVoidLateSpecifiedReturnType_Bug337677() throws Exception {
		checkSampleAboveCpp();
	}

	//	auto f() -> void* // error
	//	{
	//	}
	public void testVoidPointerLateSpecifiedReturnType_Bug337677() throws Exception {
		checkSampleAboveCpp();
	}

	//	int f() // error
	//	{
	//	    if (g())
	//	        h();
	//	    else
	//	        return 0;
	//	}
	public void testBranches_Bug342906() throws Exception {
		checkSampleAbove();
	}

	//	int f() // error
	//	{
	//	    switch (g()) {
	//	      case 1: h(); break;
	//	      case 2:
	//	        return 0;
	//	}
	public void testSwitch() throws Exception {
		checkSampleAbove();
	}

	//int bar(int foo)
	//{
	//    if(foo)
	//        return 0;
	//    else
	//        return 0;
	//}
	public void testBranches_Bug343767() throws Exception {
		checkSampleAbove();
	}

	//int bar(int foo)
	//{
	//    if(foo)
	//        return 0;
	//    else
	//        return 0;
	//    foo++;
	//}
	public void testBranchesDeadCode_Bug343767() throws Exception {
		checkSampleAbove();
	}

	//	int f() // error
	//	{
	//	    switch (g()) {
	//	      case 1: return 1;
	//	      case 2: return 0;
	//      }
	//	}
	public void testBranchesSwitch_Bug343767a() throws Exception {
		checkSampleAbove();
	}

	//	int f()
	//	{
	//	    switch (g()) {
	//	      case 1: return 1;
	//	      case 2: return 0;
	//	      default: return -1;
	//      }
	//	}
	public void testBranchesSwitch_Bug343767b() throws Exception {
		checkSampleAbove();
	}

	//int bar(int foo)
	//{
	//    if(foo)
	//        return 0;
	//    else
	//        if (g()) return 0;
	//        else return 1;
	//}
	public void testBranches2_Bug343767() throws Exception {
		checkSampleAbove();
	}

	//int bar(int foo) // error
	//{
	//    while(foo) {
	//        return 0;
	//    }
	//}
	public void testWhile() throws Exception {
		checkSampleAbove();
	}

	//	int f345687() {
	//		{
	//			return 0;
	//		}
	//	}
	public void testNextedBlock_Bug345687() throws Exception {
		checkSampleAbove();
	}

	//	int
	//	fp_goto(int a)
	//	{
	//	if (a) {
	//	goto end;
	//	}
	//	end:
	//	return (a);
	//	}
	public void testGoto_Bug346559() throws Exception {
		checkSampleAbove();
	}

	//	int main()
	//	{
	//		char c;  // added so function body is non-empty
	//		// no error since return value in main is optional
	//	}
	public void testMainFunction() throws Exception {
		checkSampleAbove();
	}

	// #include <vector>
	// std::vector<int> f() {
	//    return {1,2,3};
	// }
	public void testReturnInitializerList() throws Exception {
		checkSampleAbove();
	}

	//	void f() __attribute__((noreturn));
	//
	//	int test() {
	//    f();
	//	}
	public void testNoReturn() throws Exception {
		checkSampleAbove();
	}

	//	struct A {
	//	  A();
	//	  ~A() __attribute__((noreturn));
	//	};
	//
	//	int test() {
	//	  A();
	//	}
	public void testNoReturnInDestructor_461538() throws Exception {
		checkSampleAboveCpp();
	}

	//	int try1() {
	//		try {
	//			return 5;
	//		} catch (...) {
	//			return 5;
	//		}
	//	}
	public void testTryBlock1() throws Exception {
		// bug 348387
		checkSampleAboveCpp();
	}

	//	int try2() { // error
	//		try {
	//			return 5;
	//		} catch (int) {
	//		}
	//	}
	public void testTryBlock2() throws Exception {
		checkSampleAboveCpp();
	}

	//	int try3() { // error
	//		try {
	//		} catch (int a) {
	//			return 5;
	//		}
	//	}
	public void testTryBlock3() throws Exception {
		checkSampleAboveCpp();
	}

	//	int retindead() {
	//			return 5;
	//  ;
	//	}
	public void testRetInDeadCode1() throws Exception {
		// bug 348386
		checkSampleAbove();
	}

	//	int retindead() {
	//			throw 42;
	//  ;
	//	}
	public void testRetInDeadCodeThrow() throws Exception {
		// bug 356908
		checkSampleAboveCpp();
	}

	//	bool func( int i )
	//	{
	//	    switch( i )
	//	    {
	//	    case 0:
	//	        return true;
	//	    default:
	//	        return false;
	//	        break;
	//	    }
	//	}
	public void testRetInDeadCodeCase() throws Exception {
		// Bug 350168
		checkSampleAboveCpp();
	}

	//	int test1() {
	//	    do {
	//	        return 1;
	//	    } while (0);
	//	}
	public void testNoRetInfinitLoop() throws Exception {
		// Bug 394521
		checkSampleAbove();
	}

	//	int test1_f()    // WARNING HERE: "No return, in function returning non-void"
	//	{
	//	    while (1)
	//	    {
	//	    }
	//	}
	public void testNoRetInfinitLoop2() throws Exception {
		// Bug 394521
		checkSampleAboveCpp();
	}

	//	int foo() { // error
	//	    int waldo = waldo();
	//	    if (waldo);
	//	}
	public void testSelfReferencingVariable_452325() throws Exception {
		// Just check that codan runs without any exceptions being thrown.
		checkSampleAboveCpp();
	}

	//	int bar(int x) { return x; }
	//	int foo() { // error
	//	    int waldo = bar(waldo);
	//	    if (bar(waldo));
	//	}
	public void testSelfReferencingVariable_479638() throws Exception {
		// Just check that codan runs without any exceptions being thrown.
		checkSampleAboveCpp();
	}

	//	int foo(int x) {  // error
	//	    switch (x) {
	//	    }
	//	}
	public void testEmptySwitch_455828() throws Exception {
		checkSampleAbove();
	}

	//	int foo(int x) { // error
	//	    switch (x) {
	//	        case 0:
	//	            return 42;;
	//	        default:
	//	    }
	//	}
	public void testDoubleSemicolonInSwitchCase_455828() throws Exception {
		checkSampleAboveCpp();
	}

	//	auto f() {}
	public void testReturnTypeDeduction_540112() throws Exception {
		checkSampleAboveCpp();
	}

	//	void waldo() {
	//	  return 5; // error here on line 2
	//	}
	public void testNonTemplateFunctionReturn_509751() throws Exception {
		checkSampleAboveCpp();
	}

	//	template <typename T>
	//	void waldoT() {
	//	  return 5;  // error here on line 3
	//	}
	public void testTemplateFunctionReturn_509751a() throws Exception {
		checkSampleAboveCpp();
	}

	//	template <typename T>
	//	T waldoT() {
	//		return 5;
	//	}
	public void testTemplateFunctionReturn_509751b() throws Exception {
		checkSampleAboveCpp();
	}

	//	[[noreturn]] void throwMe()	{
	//		throw 1;
	//	}
	//	int foo(int bar) {
	//	  switch(bar) {
	//	  case 0:
	//	    return 1;
	//	  case 1:
	//	    return 0;
	//	  default:
	//	    throwMe();
	//	  }
	//	}
	public void testFunctionWithAttribute_519105() throws Exception {
		checkSampleAboveCpp();
	}

	//	template <typename T>
	//	[[noreturn]] void foo(const T& e) {
	//		throw e;
	//	}
	//	int bar() {
	//		foo<int>(5);
	//	}
	public void testTemplateFunctionNoReturn() throws Exception {
		checkSampleAboveCpp();
	}

	//int foo() {
	//	int errcode = -1;
	//	errcode = 0;
	//	cleanup:
	//	return errcode;
	//	barf:
	//	goto cleanup;
	//}
	public void testNoReturnWithGoto_Bug492878() throws Exception {
		checkSampleAboveCpp();
	}

	//	int& bar() {
	//		int a = 0;
	//		return a; //error here
	//	}
	public void testReturnByRef() throws Exception {
		checkSampleAboveCpp();
	}

	//	int* bar() {
	//		int a = 0;
	//		return &a; //error here
	//	}
	public void testReturnByPtr() throws Exception {
		checkSampleAboveCpp();
	}

	//	int& bar() {
	//		int a = 0;
	//		return reinterpret_cast<int&>(a); //error here
	//	}
	public void testReturnByCastRef() throws Exception {
		checkSampleAboveCpp();
	}

	//	int* bar() {
	//		int a = 0;
	//		return reinterpret_cast<int*>(a);
	//	}
	public void testReturnByCastPtr() throws Exception {
		checkSampleAboveCpp();
	}

	//	int* bar() {
	//		int a = 0, b = 0;
	//		bool cond = true;
	//		return cond ? &a : b; //error here
	//	}
	public void testReturnByTernary() throws Exception {
		checkSampleAboveCpp();
	}

	//	struct S { int a; }
	//	int& bar() {
	//		struct S s;
	//		return s.a; //error here
	//	}
	public void testReturnLocalStructField() throws Exception {
		checkSampleAboveCpp();
	}

	//	class Test {
	//	private:
	//		int field;
	//	public:
	//		int& bar() {
	//			return field;
	//		}
	//	}
	public void testReturnClassField() throws Exception {
		checkSampleAboveCpp();
	}

	//	class Test {
	//	private:
	//		int field;
	//	public:
	//		void foo(double*);
	//		void (Test::*op_func)(double*) bar() {
	//			return foo;
	//		}
	//	}
	public void testReturnClassMethod() throws Exception {
		checkSampleAboveCpp();
	}

	//int& foo() {
	//	int* a = new int;
	//	return *a;
	//}
	public void testReturnRefUsingDerefPtr() throws Exception {
		checkSampleAboveCpp();
	}

	//void foo() {
	//	int local;
	//	auto s = [&local]() {
	//	    return &local;  // ok
	//	};
	//	int* ptr = s();
	//}
	public void testReturnLambda() throws Exception {
		checkSampleAboveCpp();
	}
}
