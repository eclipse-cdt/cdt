/*******************************************************************************
 * Copyright (c) 2009, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 * 	   Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import static org.eclipse.cdt.core.parser.tests.VisibilityAsserts.assertVisibility;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.AccessContext;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.TestSuite;

public class AccessControlTests extends AST2TestBase {

	protected class AccessAssertionHelper extends AST2AssertionHelper {
		AccessAssertionHelper(String contents) throws ParserException {
			super(contents, true);
		}

		void assertAccessible(String section, int len) {
			IASTName name = findName(section, len);
			IBinding binding = name.resolveBinding();
			assertNotNull(binding);
			assertTrue(AccessContext.isAccessible(binding, name));
		}

		void assertNotAccessible(String section, int len) {
			IASTName name = findName(section, len);
			IBinding binding = name.resolveBinding();
			assertNotNull(binding);
			assertFalse(AccessContext.isAccessible(binding, name));
		}
	}

	public AccessControlTests() {
	}

	public AccessControlTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(AccessControlTests.class);
	}

	private AccessAssertionHelper getAssertionHelper() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, ParserLanguage.CPP);
		return new AccessAssertionHelper(code);
	}

	//	class A {
	//	public:
	//	  int a;
	//    typedef char E;
	//	};
	//	class B : private A {
	//    friend void test();
	//    int b;
	//    typedef char* F;
	//	};
	//  class C : protected B {
	//  };
	//	void test() {
	//    class D : public C {
	//      void m() {
	//        a; //1
	//        b; //1
	//        E(); //1
	//        F(); //1
	//      }
	//	  };
	//    B b;
	//    b.a; //2
	//    b.b; //2
	//    B::E(); //2
	//    B::F(); //2
	//    C c;
	//    c.a; //3
	//    c.b; //3
	//    C::E(); //3
	//    C::F(); //3
	//	}
	public void testFriends() throws Exception {
		AccessAssertionHelper ah = getAssertionHelper();
		ah.assertAccessible("a; //1", 1);
		ah.assertAccessible("b; //1", 1);
		ah.assertAccessible("E(); //1", 1);
		ah.assertAccessible("F(); //1", 1);
		ah.assertAccessible("a; //2", 1);
		ah.assertAccessible("b; //2", 1);
		ah.assertAccessible("E(); //2", 1);
		ah.assertAccessible("F(); //2", 1);
		ah.assertNotAccessible("a; //3", 1);
		ah.assertNotAccessible("b; //3", 1);
		ah.assertNotAccessible("E(); //3", 1);
		ah.assertNotAccessible("F(); //3", 1);
	}

	//	class A {
	//	public:
	//	  int a;
	//	};
	//
	//	class B : public A {
	//	private:
	//	  int a;
	//	};
	//
	//	void test(B x) {
	//	  x.a = 0;
	//	}
	public void testHiddenMember() throws Exception {
		AccessAssertionHelper ah = getAssertionHelper();
		ah.assertNotAccessible("a = 0", 1);
	}

	//	class A0 {
	//		public:
	//			enum Ex {e1};
	//	};
	//
	//	class A : public A0 {
	//		class B {
	//			void test() {
	//				Ex a;  // we compute 'B' as the naming type, whereas it should be 'A'
	//			}
	//		};
	//	};
	public void testEnclosingAsNamingClass_292232() throws Exception {
		AccessAssertionHelper ah = getAssertionHelper();
		ah.assertAccessible("Ex a;", 2);
	}

	// // Example from C++-specification 11.2-3
	// class B {
	//   public:
	//     int mi;
	//     static int si;
	// };
	// class D : private B {};
	// class DD : public D {
	//   void f();
	// };
	//
	// void DD::f() {
	//   mi=3;  // private
	//   si=3;  // private
	//   B b;
	//   b.mi=4;
	//   b.si=4;
	//   B* bp;
	//   bp->mi=5;
	// }
	public void testEnclosingAsNamingClass_292232a() throws Exception {
		AccessAssertionHelper ah = getAssertionHelper();
		ah.assertNotAccessible("mi=3;", 2);
		ah.assertNotAccessible("si=3;", 2);
		ah.assertAccessible("mi=4;", 2);
		ah.assertAccessible("si=4;", 2);
		ah.assertAccessible("mi=5;", 2);
	}

	//	class A {
	//	private:
	//		typedef int Waldo;
	//	};
	//	A::Waldo waldo;
	public void testPrivateTypedef_427730() throws Exception {
		AccessAssertionHelper ah = getAssertionHelper();
		ah.assertNotAccessible("Waldo waldo", 5);
	}

	//	class A {
	//	private:
	//		class B {};
	//	public:
	//		typedef B Waldo;
	//	};
	//	A::Waldo waldo;
	public void testPublicTypedefForPrivateMemberClass_427730() throws Exception {
		AccessAssertionHelper ah = getAssertionHelper();
		ah.assertAccessible("Waldo waldo", 5);
	}

	//	class A {
	//	private:
	//		class B {};
	//		friend class C;
	//	};
	//	class C {
	//	public:
	//		typedef A::B Waldo;
	//	};
	//	C::Waldo waldo;
	public void testPublicTypedefForFriendClass_427730() throws Exception {
		AccessAssertionHelper ah = getAssertionHelper();
		ah.assertAccessible("Waldo waldo", 5);
	}

	//	class Outer {
	//		class Inner {};
	//	protected:
	//		using AliasInner = Inner;
	//		typedef Inner TypedefInner;
	//	};
	public void testAccessibilityForAliasedTypeInSameClass_427730() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPClassType outerClass = bh.assertNonProblem("Outer");
		IBinding aliasInner = bh.assertNonProblem("AliasInner");
		assertVisibility(ICPPClassType.v_protected, outerClass.getVisibility(aliasInner));
		IBinding typedefInner = bh.assertNonProblem("TypedefInner");
		assertVisibility(ICPPClassType.v_protected, outerClass.getVisibility(typedefInner));
	}
}
