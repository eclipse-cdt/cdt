/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.implementmethod;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.implementmethod.ImplementMethodRefactoring;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import junit.framework.Test;

/**
 * Tests for Extract Local Variable refactoring.
 */
public class ImplementMethodRefactoringTest extends RefactoringTestBase {

	public ImplementMethodRefactoringTest() {
		super();
	}

	public ImplementMethodRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(ImplementMethodRefactoringTest.class);
	}

	@Override
	protected CRefactoring createRefactoring() {
		return new ImplementMethodRefactoring(getSelectedTranslationUnit(), getSelection(), getCProject());
	}

	//A.h
	//class X {
	//public:
	//	bool /*$*/a(int = 100)/*$$*/ const;
	//};
	//====================
	//class X {
	//public:
	//	bool a(int = 100) const;
	//};
	//
	//inline bool X::a(int int1) const {
	//}
	public void testParameterWithDefaultValue() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//class X {
	//public:
	//	bool /*$*/xy(int, int i)/*$$*/ const;
	//};
	//====================
	//class X {
	//public:
	//	bool xy(int, int i) const;
	//};
	//
	//inline bool X::xy(int int1, int i) const {
	//}
	public void testConstMethod() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//template<class T>
	//class A {
	//public:
	//	/*$*/void test();/*$$*/
	//};
	//====================
	//template<class T>
	//class A {
	//public:
	//	void test();
	//};
	//
	//template<class T>
	//inline void A<T>::test() {
	//}

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	public void testTestIfTemplateMethodStaysInHeader() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//template<class T>
	//class A {
	//public:
	//    A();
	//    /*$*/void test();/*$$*/
	//};
	//
	//template<class T>
	//A<T>::A() {
	//}
	//====================
	//template<class T>
	//class A {
	//public:
	//    A();
	//    void test();
	//};
	//
	//template<class T>
	//A<T>::A() {
	//}
	//
	//template<class T>
	//inline void A<T>::test() {
	//}
	public void testClassTemplateMemberFunctions() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class Demo {
	//	class SubClass {
	//		/*$*/void test();/*$$*/
	//	};
	//};
	//

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void Demo::SubClass::test() {
	//}
	public void testMemberClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//public:
	//	/*$*/void test();/*$$*/
	//};
	//====================
	//class A {
	//public:
	//	void test();
	//};
	//
	//inline void A::test() {
	//}
	public void testNoImplementationFile() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//public:
	//	/*$*/void test();/*$$*/
	//};
	//====================
	//class A {
	//public:
	//	void test();
	//};

	//A.cpp
	//====================
	//void A::test() {
	//}
	public void testDeclaredInOtherwiseEmptyClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace Namespace {
	//
	//class ClassInNamespace {
	//public:
	//	int test();
	//	/*$*/void test2();/*$$*/
	//};
	//
	//}

	//A.cpp
	//#include "A.h"
	//
	//namespace Namespace {
	//
	//int ClassInNamespace::test() {
	//	return 5;
	//}
	//
	//}
	//====================
	//#include "A.h"
	//
	//namespace Namespace {
	//
	//int ClassInNamespace::test() {
	//	return 5;
	//}
	//
	//void ClassInNamespace::test2() {
	//}
	//
	//}
	public void testExistingNamespace() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	/*$*/virtual void foo();/*$$*/
	//	~A();
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//void A::foo() {
	//}
	//
	//A::~A() {
	//}
	public void testVirtualMethodBetweenCtorAndDtor() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	///*$*/void function();/*$$*/
	//void function_with_impl();

	//A.cpp
	//#include "A.h"
	//void function_with_impl() {
	//}
	//====================
	//#include "A.h"
	//
	//void function() {
	//}
	//
	//void function_with_impl() {
	//}
	public void testFunctionAtStartOfSourceFile() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	/*$*/void foo();/*$$*/
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//A::A() {
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//void A::foo() {
	//}
	public void testLastMethodInClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	/*$*/void foo() const;/*$$*/
	//	A();
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//A::A() {
	//}
	//====================
	//#include "A.h"
	//
	//void A::foo() const {
	//}
	//
	//A::A() {
	//}
	public void testFirstMethodInClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	/*$*/int foo();/*$$*/
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//A::A() {
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//int A::foo() {
	//}
	public void testWithIntReturnValue() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	/*$*/int foo(int param1, int param2);/*$$*/
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//A::A() {
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//int A::foo(int param1, int param2) {
	//}
	public void testWithTwoIntParameters() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//public:
	//	A();
	//	/*$*/void test();/*$$*/
	//};
	//
	//A::A() {
	//}
	//====================
	//class A {
	//public:
	//	A();
	//	void test();
	//};
	//
	//A::A() {
	//}
	//
	//inline void A::test() {
	//}
	public void testDefinedInHeader() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//void function_with_impl();
	///*$*/void function();/*$$*/

	//A.cpp
	//void function_with_impl() {
	//}
	//====================
	//void function_with_impl() {
	//}
	//
	//void function() {
	//}
	public void testFunctionAtEndOfSourceFile() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace Namespace  {
	//
	//class ClassInNamespace {
	//public:
	//	int other_test();
	//	/*$*/void test();/*$$*/
	//};
	//
	//}

	//A.cpp
	//#include "A.h"
	//void Namespace::ClassInNamespace::other_test() {
	//}
	//====================
	//#include "A.h"
	//void Namespace::ClassInNamespace::other_test() {
	//}
	//
	//void Namespace::ClassInNamespace::test() {
	//}
	public void testNamespace() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace OuterSpace {
	//namespace Namespace {
	//
	//int test();
	///*$*/int test2();/*$$*/
	//
	//}
	//}

	//A.cpp
	//#include "A.h"
	//namespace OuterSpace {
	//
	//int Namespace::test() {
	//}
	//
	//}
	//====================
	//#include "A.h"
	//namespace OuterSpace {
	//
	//int Namespace::test() {
	//}
	//
	//int Namespace::test2() {
	//}
	//
	//}
	public void testFunctionWithinNamespace() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace OuterSpace {
	//namespace Namespace	{
	//
	//int test();
	///*$*/int test2();/*$$*/
	//
	//}
	//}

	//A.cpp
	//#include "A.h"
	//namespace OuterSpace {
	//namespace Namespace {
	//
	//int test() {
	//}
	//
	//}
	//}
	//====================
	//#include "A.h"
	//namespace OuterSpace {
	//namespace Namespace {
	//
	//int test() {
	//}
	//
	//int test2() {
	//}
	//
	//}
	//}
	public void testFunctionWithinNamespaces() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//template<class T, class U>
	//class A {
	//public:
	//    A();
	//    /*$*/void test();/*$$*/
	//};
	//
	//template<class T, class U>
	//A<T, U>::A() {
	//}
	//====================
	//template<class T, class U>
	//class A {
	//public:
	//    A();
	//    void test();
	//};
	//
	//template<class T, class U>
	//A<T, U>::A() {
	//}
	//
	//template<class T, class U>
	//inline void A<T, U>::test() {
	//}
	public void testMethodOfTemplateClass() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class Class {
	//public:
	//	/*$*/void test(int param1, int param2 = 5, int param3 = 10);/*$$*/
	//};
	//

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void Class::test(int param1, int param2, int param3) {
	//}
	public void testWithDefaultParameters() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class Class {
	//public:
	//	/*$*/static void test();/*$$*/
	//};
	//

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void Class::test() {
	//}
	public void testStaticMethod() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class TestClass {
	//public:
	//	/*$*/int* get(char *val);/*$$*/
	//};
	//

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//int* TestClass::get(char *val) {
	//}
	public void testPointerReturnValue_Bug238253() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class Test {
	//public:
	//	/*$*/void doNothing(void);/*$$*/
	//};
	//

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void Test::doNothing(void) {
	//}
	public void testVoidParameter_Bug238554() throws Exception {
		assertRefactoringSuccess();
	}

	//TestClass.h
	//#ifndef TESTCLASS_H_
	//#define TESTCLASS_H_
	//
	//namespace nspace {
	//
	//class TestClass {
	//	void /*$*/testMethod()/*$$*/;
	//};
	//
	//}
	//
	//#endif /* TESTCLASS_H_ */
	//====================
	//#ifndef TESTCLASS_H_
	//#define TESTCLASS_H_
	//
	//namespace nspace {
	//
	//class TestClass {
	//	void testMethod();
	//};
	//
	//}
	//
	//inline void nspace::TestClass::testMethod() {
	//}
	//
	//#endif /* TESTCLASS_H_ */
	public void testNameQualification_Bug282989() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//namespace n1 {
	//namespace n2 {
	//
	//class A {
	//public:
	//	A();
	//	~A();
	//	void testmethod(int x);
	//
	//protected:
	//	class B {
	//	public:
	//		void /*$*/testmethod2()/*$$*/;
	//	};
	//};
	//
	//}
	//}
	//
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//namespace n1 {
	//namespace n2 {
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//}
	//}
	//====================
	//#include "A.h"
	//
	//namespace n1 {
	//namespace n2 {
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//}
	//}
	//
	//void n1::n2::A::B::testmethod2() {
	//}
	public void testNestedClass_Bug290110() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class TestClass {
	//public:
	//	/*$*/void foo();/*$$*/
	//};
	//

	//A.cxx
	//====================
	//void TestClass::foo() {
	//}
	public void testEmptyImplementationFile_Bug337040() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//
	///*$*/template<typename T>
	//void func(T&);/*$$*/
	//====================
	//
	//template<typename T>
	//void func(T&);
	//
	//template<typename T>
	//inline void func(T&) {
	//}
	public void testTemplateFunction_Bug355006() throws Exception {
		expectedFinalInfos = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class TestClass {
	//public:
	//	/*$*/explicit TestClass();/*$$*/
	//};
	//

	//A.cpp
	//====================
	//TestClass::TestClass() {
	//}
	public void testExplicitConstructor_Bug363111() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class TestClass {
	//public:
	//	/*$*/void foo() throw ();/*$$*/
	//};
	//

	//A.cpp
	//====================
	//void TestClass::foo() throw () {
	//}
	public void testEmptyThowsClause_Bug393833() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class TestClass {
	//public:
	//	/*$*/void foo() throw (TestClass);/*$$*/
	//};
	//

	//A.cpp
	//====================
	//void TestClass::foo() throw (TestClass) {
	//}
	public void testNonEmptyThowsClause_Bug393833() throws Exception {
		assertRefactoringSuccess();
	}

}
