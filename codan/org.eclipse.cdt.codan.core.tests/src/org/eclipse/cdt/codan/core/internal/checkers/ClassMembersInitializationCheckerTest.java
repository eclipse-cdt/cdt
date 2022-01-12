/*******************************************************************************
 * Copyright (c) 2011, 2013 Anton Gorenkov and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov   - initial implementation
 *     Marc-Andre Laperle
 *     Nathan Ridge
 *     Danny Ferreira
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.ClassMembersInitializationChecker;

/**
 * Test for {@see ClassMembersInitializationChecker} class
 */
public class ClassMembersInitializationCheckerTest extends CheckerTestCase {
	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ClassMembersInitializationChecker.ER_ID);
	}

	private void disableSkipConstructorsWithFCalls() {
		setPreferenceValue(ClassMembersInitializationChecker.ER_ID, ClassMembersInitializationChecker.PARAM_SKIP,
				false);
	}

	public void checkMultiErrorsOnLine(int line, int count) {
		for (int i = 0; i < count; ++i) {
			checkErrorLine(line);
		}
		assertEquals(count, markers.length);
	}

	// class C {
	//   int m;
	//   C() : m(0) {}  // No warnings.
	// };
	public void testInitializationListShouldBeChecked() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int m;
	//   C() { m = 0; }  // No warnings.
	// };
	public void testAssignmentsInConstructorShouldBeChecked() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int m;
	//   unsigned int ui;
	//   float f;
	//   double d;
	//   bool b;
	//   char c;
	//   long l;
	//   C() {}  // 7 warnings for: m, ui, f, d, b, c, l.
	// };
	public void testBasicTypesShouldBeInitialized() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(9, 7);
	}

	// class Value {};
	// class C {
	//   int* i;
	//   Value* v;
	//   C() {}  // 2 warnings for: i, v.
	// }
	public void testPointersShouldBeInitialized() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(5, 2);
	}

	// class Value {};
	// class C {
	//   int& i;
	//   Value& v;
	//   C() {}  // 2 warnings for: i, v.
	// }
	public void testReferencesShouldBeInitialized() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(5, 2);
	}

	// enum Enum { v1, v2 };
	// class C {
	//   Enum e;
	//   C() {}  // 1 warning for: e.
	// }
	public void testEnumsShouldBeInitialized() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(4, 1);
	}

	// enum Enum { v1, v2 };
	// class Value {};
	// typedef int IntTypedef;
	// typedef int* IntPtrTypedef;
	// typedef int& IntRefTypedef;
	// typedef Enum EnumTypedef;
	// typedef Value ValueTypedef;
	// class C {
	//   IntTypedef i;
	//   IntPtrTypedef ip;
	//   IntRefTypedef ir;
	//   EnumTypedef e;
	//   ValueTypedef v;
	//   C() {}  // 5 warnings for: i, ip, ir, e.
	// }
	public void testTypedefsShouldBeInitialized() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(14, 4);
	}

	// class C {
	//   C() : i1(0) {}     // 1 warning for: i2.
	//   C(int) : i2(0) {}  // 1 warning for: i1.
	//   int i1, i2;
	// };
	public void testAFewConstructorsHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2, 3);
	}

	// template <typename T1, typename T2>
	// class C {
	//   C() : i1(0), t1(T1()) {}  // 1 warning for: i2.
	//   int i1, i2;
	//   T1 t1;
	//   T2 t2;
	// };
	public void testTemplateClassHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// class C {
	//   template <typename T>
	//   C() : i1(0) {}  // 1 warning for: i2.
	//   int i1, i2;
	// };
	public void testTemplateConstructorHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// class C {
	//   C();  // No warnings.
	//   int i;
	// };
	public void testTemplateConstructorDeclarationOnlyHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   C();
	//   int i1, i2;
	// };
	// C::C() : i1(0) {}  // 1 warning for: i2.
	public void testExternalConstructorHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(5);
	}

	// template <typename T1, typename T2>
	// class C {
	//   C();
	//   int i1, i2;
	//   T1 t1;
	//   T2 t2;
	// };
	// template <typename T1, typename T2>
	// C<T1,T2>::C() : i1(0), t1(T1()) {}  // 1 warning for: i2.
	public void testExternalConstructorOfTemplateClassHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(9);
	}

	// class C {
	//   template <typename T>
	//   C();
	//   int i1, i2;
	// };
	// template <typename T>
	// C::C() : i1(0) {}  // 1 warning for: i2.
	public void testExternalTemplateConstructorHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(7);
	}

	// template <typename T1, typename T2>
	// class C {
	//   template <typename T>
	//   C();
	//   int i1, i2;
	//   T1 t1;
	//   T2 t2;
	// };
	// template <typename T1, typename T2>
	// template <typename T>
	// C<T1,T2>::C() : i1(0), t1(T1()) {}  // 1 warning for: i2.
	public void testExternalTemplateConstructorOfTemplateClassHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(11);
	}

	// class C {
	//   class NestedC {
	//     NestedC() : i(0) {} // No warnings.
	//     int i;
	//   };
	//   C() {}  // 1 warning for: i.
	//   int i;
	// };
	public void testNestedClassesHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6);
	}

	// class C {
	//   C()  // 1 warning for: i.
	//   {
	//     class NestedC {
	//       NestedC() { i = 0; } // No warnings.
	//       int i;
	//     };
	//   }
	//   int i;
	// };
	public void testNestedClassInConstructorHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2);
	}

	// class C {
	//   C();
	//   int i;
	// };
	//
	// C::C()  // 1 warning for: i.
	// {
	//   class NestedC {  // No warnings.
	//     NestedC() { i = 0; }
	//     int i;
	//   };
	// }
	public void testNestedClassInExternalConstructorHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6);
	}

	// class C {
	//   C()  // 1 warning for: i.
	//   {
	// 	   class NestedC {
	// 	     NestedC() { i = 0; } // No warnings.
	// 	     void C() { i = 0; }      // No warnings.
	// 	     int i;
	// 	   };
	//   }
	//   int i;
	// };
	public void testNestedClassWithMethodNamedAsAnotherClassHandling() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2);
	}

	// class C {
	//   C() {}                         // 1 warning for: i.
	//   int someFunction() { i = 0; }  // No warnings.
	//   int i;
	// };
	public void testAssignmentIsNotInConstructor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2);
	}

	// class CBase {
	//   CBase() : i1(0) {}     // No warnings.
	//   int i1;
	// };
	// class CDerived : public CBase {
	//   CDerived() : i2(0) {}  // No warnings.
	//   int i2;
	// };
	public void testBaseClassMemberShouldNotBeTakenIntoAccount() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   C() {}  // No warnings.
	//   static int i1, i2;
	// };
	// int C::i1 = 0;
	// // NOTE: Static members are always initialized with 0, so there should not be warning on C::i2
	// int C::i2;
	public void testNoErrorsOnStaticMembers() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	// public:
	//   C(const C& c) = delete;
	//   int i1, i2;
	// };
	public void testNoErrorsOnDeletedConstructor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void	func(int & a) { a = 0; }
	// class C {
	//   C() { func(i); }  // No warnings.
	//   int i;
	// };
	public void testNoErrorsOnFunctionCallInitialization() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void	func(const int & a) {}
	// class C {
	//   C() { func(i); }  // 1 warning for: i.
	//   int i;
	// };
	public void testNoErrorsOnReadingFunctionCall() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// class C {
	//   C() { (i1) = 0; *&i2 = 0; }  // No warnings.
	//   int i1, i2;
	// };
	public void testNoErrorsOnComplexAssignment() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   C() : i1(0) {  // No warnings.
	//     i2 = i1;
	//     int someVar = 0;
	//     i3 = someVar;
	//   }
	//   int i1, i2, i3;
	// };
	public void testNoErrorsOnChainInitialization() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class A { protected: A(){} public: int a; };  // 1 warning for: a.
	// class C: public A {
	//   C() {
	//       a = 1;
	//   }
	// };
	public void testErrorOnProtectedConstructor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1);
	}

	// struct S {
	//   int i;
	//   S() {}  // 1 warning for: i.
	// };
	public void testCheckStructs() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// union U {
	//   int a;
	//   char b;
	//   U() {  // No warnings.
	//      a=0;
	//   }
	// };
	public void testSkipUnions() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int c;
	// };
	public void testNoErrorsIfThereIsNoConstructorsDefined() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int i;
	//   C(bool b) {  // No warnings.
	//     if (b)
	//       i = 0;
	//     // else - 'i' will be not initialized
	//   }
	// };
	public void testNoErrorsIfMemberWasInitializedInOneOfTheIfBranch() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class A {
	//   int a;
	//   A(int a) { setA(a); }  // No warnings.
	//   A() { getA(); }        // 1 warning for: a.
	//   void setA(int a) {
	//     this->a = a;
	//   }
	//   int getA() const {
	//     return a;
	//   }
	// };
	public void testUsingMethodsInConstructorWithPreference() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// class A;
	// void initializeA1(A*);
	// void initializeA2(A**);
	// void initializeA3(A&);
	//
	// class A {
	//   int a;
	//   A() { initializeA1(this); }               // No warnings.
	//   A(int a) { initializeA2(&this); }         // No warnings.
	//   A(float a) { initializeA3(*this); }       // No warnings.
	//   A(double a) { initializeA3(*(this)); }    // No warnings.
	// };
	public void testUsingConstMethodsInConstructorWithPreference() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class A {
	//   int a;
	//   A(int a) { setA(a); }  // 1 warning for: a.
	//   A() { getA(); }        // 1 warning for: a.
	//   void setA(int a) {
	//     this->a = a;
	//   }
	//   int getA() const {
	//     return a;
	//   }
	// };
	public void testUsingMethodsInConstructorWithoutPreference() throws Exception {
		disableSkipConstructorsWithFCalls();
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3, 4);
	}

	// class A;
	// void initializeA1(A*);
	// void initializeA2(A**);
	// void initializeA3(A&);
	//
	// class A {
	//   int a;
	//   A() { initializeA1(this); }               // 1 warning for: a.
	//   A(int a) { initializeA2(&this); }         // 1 warning for: a.
	//   A(float a) { initializeA3(*this); }       // 1 warning for: a.
	//   A(double a) { initializeA3(*(this)); }    // 1 warning for: a.
	// };
	public void testUsingConstMethodsInConstructorWithoutPreference() throws Exception {
		disableSkipConstructorsWithFCalls();
		loadCodeAndRun(getAboveComment());
		checkErrorLines(8, 9, 10, 11);
	}

	// @file:test.h
	// class C {
	//	 int field;
	// C();
	// void initObject();
	//};

	// @file:test.cpp
	// #include "test.h"
	// C::C() {
	//    initObject();
	// }
	public void testMethodDeclarationInOtherFile_368419() throws Exception {
		CharSequence[] code = getContents(2);
		loadcode(code[0].toString());
		loadcode(code[1].toString());
		runOnProject();
		checkNoErrors();
	}

	//class D {
	//  int field;
	//  D(const D& toBeCopied) {
	//    *this = toBeCopied;
	//  };
	//};
	public void testAssignThis_368420() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//class D {
	//  int field;
	//  D(const D& toBeCopied) {
	//    *(&(*this)) = toBeCopied;
	//  };
	//};
	public void testAssignThisUnaryExpressions_368420() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//class D {
	//  int field;
	//  D(const D& toBeCopied) {
	//    this = toBeCopied;
	//  };
	//};
	public void testAssignThisNonLValue_368420() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	//class D {
	//  int field;
	//  D();
	//  D(const D& toBeCopied) {
	//    D temp;
	//    temp = *(&(*this)) = toBeCopied;
	//  };
	//};
	public void testAssignThisMultiBinaryExpressions_368420() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//@file:test.h
	//template <typename>
	//struct B;

	//@file:test.cpp
	//#include "test.h"
	//
	//template <typename>
	//struct A {
	//};
	//
	//template <typename valueT>
	//struct B<A<valueT> > {
	//    const A<valueT>& obj;
	//    B(const A<valueT>& o) : obj(o) {}
	//};
	public void testTemplatePartialSpecialization_368611() throws Exception {
		CharSequence[] code = getContents(2);
		loadcode(code[0].toString());
		loadcode(code[1].toString());
		runOnProject();
		checkNoErrors();
	}

	//	struct S {
	//	    int i;
	//	    S() = default;
	//	};
	public void testDefaultConstructor_365498() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//	struct S {
	//	    int i;
	//
	//	    S(S&) = default;
	//	    S(const S&) = default;
	//	    S(volatile S&) = default;
	//	    S(const volatile S&) = default;
	//	    S(S&&) = default;
	//	    S(const S&&) = default;
	//	    S(volatile S&&) = default;
	//	    S(const volatile S&&) = default;
	//	};
	public void testDefaultCopyOrMoveConstructor_395018() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	template <typename T>
	//	struct S {
	//	    int i;
	//
	//	    S(const S&) = default;
	//	    S(S&&) = default;
	//	};
	public void testDefaultCopyOrMoveConstructorInTemplate_408303() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	struct A {
	//	    A(int n) : waldo(n) {}
	//	    A() : A(42) {}
	//	    int waldo;
	//	};
	public void testDelegatingConstructor_402607() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	struct A {
	//      typedef A B;
	//	    A(int n) : waldo(n) {}
	//	    A() : B(42) {}
	//	    int waldo;
	//	};
	public void testDelegatingConstructorTypedef_402607() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	template <typename T>
	//	class TemplateWithWarning {
	//	public:
	//	  TemplateWithWarning(int number) {
	//	    internalNumber = number;
	//	  }
	//
	//	  TemplateWithWarning(int number, bool someFlag)
	//	     : TemplateWithWarning(number) {}
	//
	//	protected:
	//	  int internalNumber;
	//	};
	public void testDelegatingConstructorWithTemplate_519311() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	struct A {
	//	    A() {};
	//	    int x = 0;
	//	};
	public void testNonstaticDataMemberInitializer_400673() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	class ClassWithWarning {
	//	public:
	//	  ClassWithWarning(char* n) {
	//	    this->initClass(n);
	//	  }
	//
	//	private:
	//	  char *name;
	//	  void initClass(char *name) {
	//	    this->name = name;
	//	  }
	//	};
	public void testMemberFunctionCalling_519473() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//	class Waldo {
	//	private:
	//		int location;
	//	public:
	//	  Waldo() {
	//	    Waldo d;
	//		d.location = 1;
	//	  }
	//	};
	public void testOtherInstance_Bug519473() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}
}
