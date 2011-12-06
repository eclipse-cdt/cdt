/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anton Gorenkov   - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.ClassMembersInitializationChecker;

/**
 * Test for {@see ClassMembersInitializationChecker} class
 *
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
		setPreferenceValue(ClassMembersInitializationChecker.ER_ID, ClassMembersInitializationChecker.PARAM_SKIP, false);
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
	public void testInitializationListShouldBeChecked() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int m;
	//   C() { m = 0; }  // No warnings.
	// };
	public void testAssignmentsInConstructorShouldBeChecked() {
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
	public void testBasicTypesShouldBeInitialized() {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(9, 7);
	}

	// class Value {};
	// class C {
	//   int* i;
	//   Value* v;
	//   C() {}  // 2 warnings for: i, v.
	// }
	public void testPointersShouldBeInitialized() {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(5, 2);
	}

	// class Value {};
	// class C {
	//   int& i;
	//   Value& v;
	//   C() {}  // 2 warnings for: i, v.
	// }
	public void testReferencesShouldBeInitialized() {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(5, 2);
	}

	// enum Enum { v1, v2 };
	// class C {
	//   Enum e;
	//   C() {}  // 1 warning for: e.
	// }
	public void testEnumsShouldBeInitialized() {
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
	public void testTypedefsShouldBeInitialized() {
		loadCodeAndRun(getAboveComment());
		checkMultiErrorsOnLine(14, 4);
	}

	// class C {
	//   C() : i1(0) {}     // 1 warning for: i2.
	//   C(int) : i2(0) {}  // 1 warning for: i1.
	//   int i1, i2;
	// };
	public void testAFewConstructorsHandling() {
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
	public void testTemplateClassHandling() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// class C {
	//   template <typename T>
	//   C() : i1(0) {}  // 1 warning for: i2.
	//   int i1, i2;
	// };
	public void testTemplateConstructorHandling() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// class C {
	//   C();  // No warnings.
	//   int i;
	// };
	public void testTemplateConstructorDeclarationOnlyHandling() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   C();
	//   int i1, i2;
	// };
	// C::C() : i1(0) {}  // 1 warning for: i2.
	public void testExternalConstructorHandling() {
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
	// C::C() : i1(0), t1(T1()) {}  // 1 warning for: i2.
	public void testExternalConstructorOfTemplateClassHandling() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(8);
	}

	// class C {
	//   template <typename T>
	//   C();
	//   int i1, i2;
	// };
	// template <typename T>
	// C::C() : i1(0) {}  // 1 warning for: i2.
	public void testExternalTemplateConstructorHandling() {
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
	public void testExternalTemplateConstructorOfTemplateClassHandling() {
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
	public void testNestedClassesHandling() {
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
	public void testNestedClassInConstructorHandling() {
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
	public void testNestedClassInExternalConstructorHandling() {
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
	public void testNestedClassWithMethodNamedAsAnotherClassHandling() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2);
	}

	// class C {
	//   C() {}                         // 1 warning for: i.
	//   int someFunction() { i = 0; }  // No warnings.
	//   int i;
	// };
	public void testAssignmentIsNotInConstructor() {
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
	public void testBaseClassMemberShouldNotBeTakenIntoAccount() {
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
	public void testNoErrorsOnStaticMembers() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void	func(int & a) { a = 0; }
	// class C {
	//   C() { func(i); }  // No warnings.
	//   int i;
	// };
	public void testNoErrorsOnFunctionCallInitialization() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void	func(const int & a) {}
	// class C {
	//   C() { func(i); }  // 1 warning for: i.
	//   int i;
	// };
	public void testNoErrorsOnReadingFunctionCall() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// class C {
	//   C() { (i1) = 0; *&i2 = 0; }  // No warnings.
	//   int i1, i2;
	// };
	public void testNoErrorsOnComplexAssignment() {
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
	public void testNoErrorsOnChainInitialization() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class A { protected: A(){} public: int a; };  // 1 warning for: a.
	// class C: public A {
	//   C() {
	//       a = 1;
	//   }
	// };
	public void testErrorOnProtectedConstructor() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1);
	}

	// struct S {
	//   int i;
	//   S() {}  // 1 warning for: i.
	// };
	public void testCheckStructs() {
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
	public void testSkipUnions() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int c;
	// };
	public void testNoErrorsIfThereIsNoConstructorsDefined() {
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
	public void testNoErrorsIfMemberWasInitializedInOneOfTheIfBranch() {
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
	public void testUsingMethodsInConstructorWithPreference() {
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
	public void testUsingConstMethodsInConstructorWithPreference() {
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
	public void testUsingMethodsInConstructorWithoutPreference() {
		disableSkipConstructorsWithFCalls();
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3,4);
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
	public void testUsingConstMethodsInConstructorWithoutPreference() {
		disableSkipConstructorsWithFCalls();
		loadCodeAndRun(getAboveComment());
		checkErrorLines(8,9,10,11);
	}

}
