/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *    Felipe Martinez  - ReturnCheckerTest implementation
 *    Tomasz Wesolowski - Bug 348387
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.ReturnChecker;

/**
 * Test for {@see ReturnCheckerTest} class
 *
 */
public class ReturnCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ReturnChecker.RET_NORET_ID,ReturnChecker.RET_ERR_VALUE_ID,ReturnChecker.RET_NO_VALUE_ID);
	}
	//	dummy() {
	//	  return; // no error here on line 2
	//	}
	public void testDummyFunction() {
		checkSampleAbove();
		// because return type if not defined, usually people don't care
	}

	//	void void_function(void) {
	//	  return; // no error here
	//	}
	public void testVoidFunction() {
		checkSampleAbove();
	}

	//	int integer_return_function(void) { // error
	//	  if (global) {
	//		if (global == 100) {
	//			return; // error here on line 4
	//		}
	//	  }
	//	}
	public void testBasicTypeFunction() {
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
	public void testUserDefinedFunction() {
		checkSampleAbove();
	}

	//	 typedef unsigned int uint8_t;
	//
	//	uint8_t return_typedef(void) {
	//	return; // error here on line 4
	//	}
	public void testTypedefReturnFunction() {
		checkSampleAbove();
	}


	//	typedef unsigned int uint8_t;
	//
	//	uint8_t (*return_fp_no_typedef(void))(void)
	//	{
	//			return; // error here on line 5
	//	}
	public void testFunctionPointerReturnFunction() {
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
	public void testInnerFunction_Bug315525() {
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
	public void testInnerFunction_Bug316154() {
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
	public void testConstructorRetValue() {
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
	public void testConstructor_Bug323602() {
		IProblemPreference macro = getPreference(ReturnChecker.RET_NO_VALUE_ID, ReturnChecker.PARAM_IMPLICIT);
		macro.setValue(Boolean.TRUE);
		loadCodeAndRunCpp(getAboveComment());
		checkNoErrors();
	}

	//	void f()
	//	{
	//	    [](int r){return r;}(5);
	//	}
	public void testLambda_Bug332285() {
		checkSampleAbove();
	}
//	void f()
//	{
//	    if ([](int r){return r == 0;}(0))
//	        ;
//	}
	public void testLambda2_Bug332285() {
		checkSampleAbove();
	}

	//	void g()
	//	{
	//		int r;
	//	    ({return r;}); // error
	//	}
	public void testGccExtensions() {
		checkSampleAbove();
	}

	//	auto f() -> void
	//	{
	//	}
	public void testVoidLateSpecifiedReturnType_Bug337677() {
		checkSampleAboveCpp();
	}

	//	auto f() -> void* // error
	//	{
	//	}
	public void testVoidPointerLateSpecifiedReturnType_Bug337677() {
		checkSampleAboveCpp();
	}

//	int f() // error
//	{
//	    if (g())
//	        h();
//	    else
//	        return 0;
//	}
	public void testBranches_Bug342906() {
		checkSampleAbove();
	}

//	int f() // error
//	{
//	    switch (g()) {
//	      case 1: h(); break;
//	      case 2:
//	        return 0;
//	}
	public void testSwitch() {
		checkSampleAbove();
	}

	//int bar(int foo)
	//{
	//    if(foo)
	//        return 0;
	//    else
	//        return 0;
	//}
	public void testBranches_Bug343767() {
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
	public void testBranchesDeadCode_Bug343767() {
		checkSampleAbove();
	}

//	int f() // error
//	{
//	    switch (g()) {
//	      case 1: return 1;
//	      case 2: return 0;
//      }
//	}
	public void testBranchesSwitch_Bug343767a() {
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
	public void testBranchesSwitch_Bug343767b() {
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
	public void testBranches2_Bug343767() {
		checkSampleAbove();
	}
	//int bar(int foo) // error
	//{
	//    while(foo) {
	//        return 0;
	//    }
	//}
	public void testWhile() {
		checkSampleAbove();
	}

	//	int f345687() {
	//		{
	//			return 0;
	//		}
	//	}
	public void testNextedBlock_Bug345687() {
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
	public void testGoto_Bug346559() {
		checkSampleAbove();
	}

	//	int main()
	//	{
	//		char c;  // added so function body is non-empty
	//		// no error since return value in main is optional
	//	}
	public void testMainFunction() {
		checkSampleAbove();
	}

	// #include <vector>
	// std::vector<int> f() {
	//    return {1,2,3};
	// }
	public void testReturnInitializerList() {
		checkSampleAbove();
	}

	//	void f() __attribute__((noreturn));
	//
	//	int test() {
	//    f();
	//	}
	public void testNoReturn() {
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
		checkSampleAbove();
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
}
