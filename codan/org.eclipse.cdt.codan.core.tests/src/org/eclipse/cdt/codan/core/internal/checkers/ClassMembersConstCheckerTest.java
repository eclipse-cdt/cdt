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

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.ClassMembersConstChecker;

/**
 * Test for {@see ClassMembersInitializationChecker} class
 *
 */
public class ClassMembersConstCheckerTest extends CheckerTestCase {
	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ClassMembersConstChecker.ER_ID_MemberCannotBeUsedInStatic,
				ClassMembersConstChecker.ER_ID_MethodShouldBeStatic,
				ClassMembersConstChecker.ER_ID_MemberCannotBeWritten,
				ClassMembersConstChecker.ER_ID_MethodShouldBeConst);
	}

	// class C {
	//   int i;
	//   C() { int v = i; }  // No warnings.
	//   C(int) : i(0) {}    // No warnings.
	//   C(bool) { i = 0; }  // No warnings.
	// };
	public void testNoWarningOnConstructor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C1 {
	//   int i;
	//   ~C() { int v = i; }  // No warnings.
	// };
	// class C2 {
	//   int i;
	//   ~C2() { i = 0; }  // No warnings.
	// };
	public void testNoWarningOnDestructor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   static int i;
	//   static void fRead()  { int v = i; }  // No warnings.
	//   static void fWrite() { i = 0; }      // No warnings.
	//   static void fCall()  { fRead(); }    // No warnings.
	//   static void f2Read();
	//   static void f2Write();
	//   static void f2Call();
	// };
	// void C::f2Read()  { int v = i; }  // No warnings.
	// void C::f2Write() { i = 0; }      // No warnings.
	// void C::f2Call()  { fRead(); }    // No warnings.
	public void testNoWarningOnStaticMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int i;
	//   void fField() const { int v = i; }  // No warnings.
	//   void fMethod() const { fField(); }     // No warnings.
	//   void f2Field() const;
	//   void f2Method() const;
	// };
	// void C::f2Field() const { int v = i; }   // No warnings.
	// void C::f2Method() const { f2Field(); }  // No warnings.
	public void testNoWarningOnConstMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   static int i;
	//   int m;
	//   static void fStatic() {}
	//   int fField() const { int v = i; i = 0; return m; }  // No warnings.
	//   int fMethod() const { fStatic(); return m; }        // No warnings.
	//   int f2Field() const;
	//   int f2Method() const;
	// };
	// int C::f2Field() const { int v = i; i = 0; return m; }  // No warnings.
	// int C::f2Method() const { fStatic(); return m; }        // No warnings.
	public void testNoWarningOnConstMethodWithStatic() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   mutable int i;
	//   void fField() const { i = 0; }  // No warnings.
	//   void f2Field() const;
	// };
	// void C::f2Field() const { i = 0; }   // No warnings.
	public void testNoWarningOnConstMethodWritingMutableVariables() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int i;
	//   void fField()  { i = 0; }     // No warnings.
	//   void fMethod() { fField(); }  // No warnings.
	//   void f2Field();
	//   void f2Method();
	// };
	// void C::f2Field()  { i = 0; }      // No warnings.
	// void C::f2Method() { f2Field(); }  // No warnings.
	public void testNoWarningOnNonConstMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int i;
	//   void fConst() const { int v = i; fConst(); }  // No warnings.
	//   void fNonConst()  { i = 0; fNonConst(); }     // No warnings.
	//   void f2Const() const;
	//   void f2NonConst();
	//	 static void staticRecursive(int i) { i--; staticRecursive(i); } // No warnings.
	// };
	// void C::f2Const() const { int v = i; f2Const(); }  // No warnings.
	// void C::f2NonConst()  { i = 0; f2NonConst(); }     // No warnings.
	public void testNoWarningOnRecursiveMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int i;
	//   void f() {
	//     class NestedC {
	//       int i;
	//       void fConst() const { int v = i; }  // No warnings.
	//       void fNonConst()  { i = 0; }        // No warnings.
	//     };
	//     i = 0;
	//   }
	// };
	public void testNoWarningOnNestedClass() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class Base {
	//   int i;
	//   virtual void fField() = 0;
	//   virtual void fMethod() = 0;
	// };
	// class C : public Base {
	//	 void test();
	//   void fField()  { int v = i; }    // No warnings.
	//   void fMethod() { test(); }  // No warnings.
	// };
	public void testNoWarningOnVirtualMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {
	//   int i;
	//   void testConst() const { int v = i; }
	//   void testNonConst() { i = 0; }
	//   static void fRead()  { int v = i; }           // Class member 'i' cannot be used in static method 'fRead()'.
	//   static void fWrite() { i = 0; }               // Class member 'i' cannot be used in static method 'fWrite()'.
	//   static void fConst()  { testConst(); }        // Class member 'testConst()' cannot be used in static method 'fConst()'.
	//   static void fNonConst()  { testNonConst(); }  // Class member 'testNonConst()' cannot be used in static method 'fNonConst()'.
	//   static void f2Read();
	//   static void f2Write();
	//   static void f2Const();
	//   static void f2NonConst();
	// };
	// void C::f2Read()  { int v = i; }           // Similar warning.
	// void C::f2Write() { i = 0; }               // Similar warning.
	// void C::f2Const()  { testConst(); }        // Similar warning.
	// void C::f2NonConst()  { testNonConst(); }  // Similar warning.
	public void testWarningOnStaticMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(5, 6, 7, 8, 14, 15, 16, 17);
	}

	// class C {
	//   int i;
	//   void test() { i = 0; }
	//   void f() const { i = 0; }    // Class member 'i' cannot be written in constant method 'f()'.
	//   void f2() const;
	// };
	// void C::f2() const { i = 0; }    // Similar warning.
	public void testWarningOnConstMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4, 7);
	}

	// class C {
	//   void fConst() const { int a = 0; }  // Class member 'fConst()' should be static.
	//   void fNonConst() { int a = 0; }     // Class member 'fNonConst()' should be static.
	//   void f2Const() const;
	//   void f2NonConst();
	// };
	// void C::f2Const() const { int a = 0; }  // Similar warning.
	// void C::f2NonConst() { int a = 0; }     // Similar warning.
	public void testWarningOnMethodThatShouldBeStatic() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2, 3, 7, 8);
	}

	// class C {
	//   int i;
	//   void test() const { int v = i; }
	//   void fField()  { int v = i; }  // Class member 'fField()' should be constant.
	//   void fMethod() { test(); }     // Class member 'fMethod()' should be constant.
	//   void f2Field();
	//   void f2Method();
	// };
	// void C::f2Field()  { int v = i; }  // Similar warning.
	// void C::f2Method() { test(); }     // Similar warning.
	public void testWarningOnNonConstMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4, 5, 9, 10);
	}

	// class C {
	//   static int i;
	//   int m;
	//   static void test() {}
	//   int fField()  { i = 0; return m; }   // Class member 'fField()' should be constant.
	//   int fMethod() { test(); return m; }  // Class member 'fMethod()' should be constant.
	//   int f2Field();
	//   int f2Method();
	// };
	// int C::f2Field()  { i = 0; return m; }   // Similar warning.
	// int C::f2Method() { test(); return m; }  // Similar warning.
	public void testWarningOnNonConstMethodWithStatic() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(5, 6, 10, 11);
	}

	// class C {
	//   mutable int i;
	//   void f() { i = 0; }  // Class member 'f()' should be constant.
	//   void f2();
	// };
	// void C::f2() { i = 0; }  // Similar warning.
	public void testWarningOnNonConstMethodWithMutableWriting() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3, 6);
	}

	// class C {
	//   int i;
	//   void fConst() const { i = 0; fConst(); }       // Class member 'i' cannot be written in constant method 'fConst()'.
	//   void fNonConst()  { int v = i; fNonConst(); }  // Class member 'fNonConst()' should be constant.
	//   void f2Const() const;
	//   void f2NonConst();
	// };
	// void C::f2Const() const { i = 0; f2Const(); }       // Similar warning.
	// void C::f2NonConst()  { int v = i; f2NonConst(); }  // Similar warning.
	public void testWarningOnRecursiveMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3, 4, 8, 9);
	}

	// class C {
	//   int i;
	//   void f() {
	//     class NestedC {
	//       int i;
	//       void fConst() const { i = 0; }    // Class member 'i' cannot be written in constant method 'fConst()'.
	//       void fNonConst()  { int v = i; }  // Class member 'fNonConst()' should be constant.
	//     };
	//     i = 0;
	//   }
	// };
	public void testWarningOnNestedClass() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6, 7);
	}

	// class C1 {
	//   int i;
	//   void f1() const;
	//   void f2() const;
	// };
	// class C2 {
	//   int i;
	//   void f1() const;
	//   void f2() const;
	// };
	// void C1::f1() const { i = 0; }      // Class member 'i' cannot be written in constant method 'f1()'.
	// void C2::f1() const { int v = i; }  // No warnings.
	// void C1::f2() const { i = 0; }      // Class member 'i' cannot be written in constant method 'f1()'.
	// void C2::f2() const { int v = i; }  // No warnings.
	public void testWarningOnAFewClassesWithMixedWrittenMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(11, 13);
	}

	// class Base {
	//   int i;
	//   void testNonConst() { i = 0; }
	//   void testConst() const { int v = i; }
	// };
	// class C1 : public Base {
	//   void fField() const { i = 0; }            // Class member 'i' cannot be written in constant method 'fField()'.
	// };
	// class C2 : public Base {
	//   void fField()  { int v = i; }    // Class member 'fField()' should be constant.
	//   void fMethod() { testConst(); }  // Class member 'testConst()' should be constant.
	// };
	public void testWarningOnBaseClassMembersUsing() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(7, 10, 11);
	}

	// class Base {
	//   int i;
	//   virtual void f() const = 0;
	// };
	// class C : public Base {
	//   void f() const { i = 0; }  // Class member 'i' cannot be written in constant method 'f()'.
	// };
	public void testWarningOnVirtualMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(6);
	}

	//class Waldo {
	//      int location;
	//      static int findOther(Waldo other) {
	//          return other.location;
	//      }
	//      void moveOther(Waldo other) const {
	//          other.location++;
	//      }
	//};
	public void testNoWarningOnNamesNotBelongingToInstance() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeUsedInStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeWritten);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeConst);
		checkErrorLine(6, ClassMembersConstChecker.ER_ID_MethodShouldBeStatic);
	}

	//class Array {
	//     int operator[](int index);
	//      int at(int index) {
	//          return (*this)[index];
	//      }
	//  };
	public void testNoWarningWithImplicit() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeUsedInStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeWritten);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeStatic);
		checkErrorLine(3, ClassMembersConstChecker.ER_ID_MethodShouldBeConst);
	}

	//class Waldo {
	//	int location;
	//	void test() const {
	//		int v = location;
	//		const_cast<Waldo*>(this)->location = 1;
	//	}
	//};
	public void testWithConstCast() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeUsedInStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeWritten);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeConst);
	}

	//struct A {
	//	A& operator=(const A&) = delete;
	//	A& operator=(A&&) = default;
	//;
	public void testWithDeleteDefault() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeUsedInStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeWritten);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeConst);
	}

	//struct A {
	//	template <typename T>
	//	T operator()(T t) {
	//		return t;
	//	}
	//};
	public void testWithOverloadMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeUsedInStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeWritten);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeStatic);
		checkErrorLine(3, ClassMembersConstChecker.ER_ID_MethodShouldBeConst);
	}

	//struct B {
	//    int member;
	//    int& get_member_ref() {
	//        return member;
	//    }
	//};
	public void testWithReturnByNoConstRef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeUsedInStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeWritten);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeConst);
	}

	//struct B {
	//    int member[10];
	//    void set() {
	//        member[0] = 1;
	//    }
	//};
	public void testWithArray() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeUsedInStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MemberCannotBeWritten);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeStatic);
		checkNoErrorsOfKind(ClassMembersConstChecker.ER_ID_MethodShouldBeConst);
	}
}
