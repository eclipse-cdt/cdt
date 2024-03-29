/*******************************************************************************
 * Copyright (c) 2011, 2016 Google, Inc and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.IOException;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeTraits;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.TestSuite;

/**
 * Tests for ClassTypeHelper class.
 */
public class TypeTraitsTests extends AST2TestBase {

	private boolean fUseClang = false;

	public TypeTraitsTests() {
	}

	public TypeTraitsTests(String name) {
		super(name);
	}

	@Override
	protected void tearDown() throws Exception {
		fUseClang = false;
	}

	public static TestSuite suite() {
		return suite(TypeTraitsTests.class);
	}

	protected BindingAssertionHelper getAssertionHelper() throws ParserException, IOException {
		String code = getAboveComment();
		return new AST2AssertionHelper(code, true);
	}

	@Override
	public ScannerInfo createTestScannerInfo(ScannerKind scannerKind) {
		ScannerInfo scannerInfo = super.createTestScannerInfo(scannerKind);
		if (fUseClang) {
			scannerInfo.getDefinedSymbols().put("__clang__", "1");
		}
		return scannerInfo;
	}

	//	struct A {
	//	  A(const A& a);
	//	};
	//
	//	class B {
	//	public:
	//	  B();
	//	  int x;
	//	  A* y;
	//	  const A& z;
	//    static A s;
	//	};
	//
	//	class C {
	//	public:
	//	  A a;
	//	};
	public void testHasTrivialCopyCtor() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblemOnFirstIdentifier("A {");
		assertFalse(TypeTraits.hasTrivialCopyCtor(classA));
		ICPPClassType classB = helper.assertNonProblemOnFirstIdentifier("B {");
		assertTrue(TypeTraits.hasTrivialCopyCtor(classB));
		ICPPClassType classC = helper.assertNonProblemOnFirstIdentifier("C {");
		assertFalse(TypeTraits.hasTrivialCopyCtor(classC));
	}

	//	struct A {
	//	  ~A();
	//	};
	//
	//	class B {
	//	public:
	//	  B();
	//	  B(const B& a);
	//	  int x;
	//	  B* y;
	//	  const B& z;
	//	  static A s;
	//	};
	//
	//	class C {
	//	public:
	//	  A a;
	//	};
	public void testHasTrivialDestructor() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblemOnFirstIdentifier("A {");
		assertFalse(TypeTraits.hasTrivialDestructor(classA));
		ICPPClassType classB = helper.assertNonProblemOnFirstIdentifier("B {");
		assertTrue(TypeTraits.hasTrivialDestructor(classB));
		ICPPClassType classC = helper.assertNonProblemOnFirstIdentifier("C {");
		assertFalse(TypeTraits.hasTrivialDestructor(classC));
	}

	//	struct A {
	//	  static int x;
	//	  ~A();
	//	};
	//
	//	class B : public A {
	//	};
	//
	//	struct C {
	//	  A a;
	//	};
	//
	//	struct D : public A, C {
	//	};
	//
	//	struct E {
	//	  virtual ~E();
	//	};
	//
	//	struct F {
	//	  virtual void m();
	//	};
	//
	//	struct G : public virtual A {
	//	};
	//
	//	typedef const A H;
	//
	//	typedef A* I;
	//
	//	typedef A J[0];
	public void testIsEmpty() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblemOnFirstIdentifier("A {");
		assertTrue(TypeTraits.isEmpty(classA));
		ICPPClassType classB = helper.assertNonProblemOnFirstIdentifier("B :");
		assertTrue(TypeTraits.isEmpty(classB));
		ICPPClassType classC = helper.assertNonProblemOnFirstIdentifier("C {");
		assertFalse(TypeTraits.isEmpty(classC));
		ICPPClassType classD = helper.assertNonProblemOnFirstIdentifier("D :");
		assertFalse(TypeTraits.isEmpty(classD));
		ICPPClassType classE = helper.assertNonProblemOnFirstIdentifier("E {");
		assertFalse(TypeTraits.isEmpty(classE));
		ICPPClassType classF = helper.assertNonProblemOnFirstIdentifier("F {");
		assertFalse(TypeTraits.isEmpty(classF));
		ICPPClassType classG = helper.assertNonProblemOnFirstIdentifier("G :");
		assertFalse(TypeTraits.isEmpty(classG));
		IType typeH = helper.assertNonProblemOnFirstIdentifier("H;");
		assertTrue(TypeTraits.isEmpty(typeH));
		IType typeI = helper.assertNonProblemOnFirstIdentifier("I;");
		assertFalse(TypeTraits.isEmpty(typeI));
		IType typeJ = helper.assertNonProblemOnFirstIdentifier("J[");
		assertFalse(TypeTraits.isEmpty(typeJ));
	}

	//	struct A {
	//	  virtual void m();
	//	};
	//
	//	class B {
	//	public:
	//	  B();
	//	  B(const B& a);
	//	  void m();
	//	  int x;
	//	  B* y;
	//	  const B& z;
	//	};
	//
	//	class C : public A {
	//	};
	public void testIsPolymorphic() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblemOnFirstIdentifier("A {");
		assertTrue(TypeTraits.isPolymorphic(classA));
		ICPPClassType classB = helper.assertNonProblemOnFirstIdentifier("B {");
		assertFalse(TypeTraits.isPolymorphic(classB));
		ICPPClassType classC = helper.assertNonProblemOnFirstIdentifier("C");
		assertTrue(TypeTraits.isPolymorphic(classC));
	}

	//	struct A {
	//	  A* a;
	//	  int b;
	//	  A(A* a, int b);
	//	  A(A& a);
	//	  ~A();
	//	};
	//
	//	class B : public A {
	//	  static int c;
	//	  void m(A* a);
	//	};
	//
	//	class C : public A {
	//	  int c;
	//	};
	//
	//	struct D {
	//	  C c;
	//	};
	//
	//	struct E : public C {
	//	};
	//
	//	struct F : public B {
	//	  virtual ~F();
	//	};
	//
	//	struct G {
	//	  int a;
	//	private:
	//	  int b;
	//	};
	//
	//	struct H {
	//	  int& a;
	//	};
	public void testIsStandardLayout() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblemOnFirstIdentifier("A {");
		assertTrue(TypeTraits.isStandardLayout(classA));
		ICPPClassType classB = helper.assertNonProblemOnFirstIdentifier("B {");
		assertTrue(TypeTraits.isStandardLayout(classB));
		ICPPClassType classC = helper.assertNonProblemOnFirstIdentifier("C :");
		assertFalse(TypeTraits.isStandardLayout(classC));
		ICPPClassType classD = helper.assertNonProblemOnFirstIdentifier("D {");
		assertFalse(TypeTraits.isStandardLayout(classD));
		ICPPClassType classE = helper.assertNonProblemOnFirstIdentifier("E :");
		assertFalse(TypeTraits.isStandardLayout(classE));
		ICPPClassType classF = helper.assertNonProblemOnFirstIdentifier("F :");
		assertFalse(TypeTraits.isStandardLayout(classF));
		ICPPClassType classG = helper.assertNonProblemOnFirstIdentifier("G {");
		assertFalse(TypeTraits.isStandardLayout(classG));
		ICPPClassType classH = helper.assertNonProblemOnFirstIdentifier("H {");
		assertFalse(TypeTraits.isStandardLayout(classH));
	}

	//	struct A {
	//	  A* a;
	//	  int b;
	//	  A(char* s);
	//	  A(const A& a, int b);
	//	  A& operator =(const A& a, A* b);
	//	};
	//
	//	class B : public A {
	//	  A a;
	//	};
	//
	//	struct C {
	//	  C(char* s = 0);
	//	};
	//
	//	struct D {
	//	  D(const D& a, int b = 1);
	//	};
	//
	//	struct E {
	//	  E& operator =(const E& a, E* b = nullptr);
	//	};
	//
	//	struct F {
	//	  ~F();
	//	};
	//
	//	struct G {
	//	  C c;
	//	};
	//
	//	struct H : public C {
	//	};
	//
	//	struct I {
	//	  virtual void m();
	//	};
	public void testIsTrivial() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classA = helper.assertNonProblemOnFirstIdentifier("A {");
		assertTrue(TypeTraits.isTrivial(classA));
		ICPPClassType classB = helper.assertNonProblemOnFirstIdentifier("B :");
		assertTrue(TypeTraits.isTrivial(classB));
		ICPPClassType classC = helper.assertNonProblemOnFirstIdentifier("C {");
		assertFalse(TypeTraits.isTrivial(classC));
		ICPPClassType classD = helper.assertNonProblemOnFirstIdentifier("D {");
		assertFalse(TypeTraits.isTrivial(classD));
		ICPPClassType classE = helper.assertNonProblemOnFirstIdentifier("E {");
		assertFalse(TypeTraits.isTrivial(classE));
		ICPPClassType classF = helper.assertNonProblemOnFirstIdentifier("F {");
		assertFalse(TypeTraits.isTrivial(classF));
		ICPPClassType classG = helper.assertNonProblemOnFirstIdentifier("G {");
		assertFalse(TypeTraits.isTrivial(classG));
		ICPPClassType classH = helper.assertNonProblemOnFirstIdentifier("H :");
		assertFalse(TypeTraits.isTrivial(classH));
		ICPPClassType classI = helper.assertNonProblemOnFirstIdentifier("I {");
		assertFalse(TypeTraits.isTrivial(classI));
	}

	//	int a;
	//	char* const b;
	//	void* volatile c;
	//	char* const d[42];
	//	enum E {} e;
	//	struct F {
	//		int x, y;
	//		F();
	//	} f;
	//	struct G {
	//		int x, y;
	//		G();
	//		G(const G&);
	//	} g;
	public void testIsTriviallyCopyable() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable a = helper.assertNonProblemOnFirstIdentifier("a;");
		assertTrue(TypeTraits.isTriviallyCopyable(a.getType()));
		IVariable b = helper.assertNonProblemOnFirstIdentifier("b;");
		assertTrue(TypeTraits.isTriviallyCopyable(b.getType()));
		IVariable c = helper.assertNonProblemOnFirstIdentifier("c;");
		assertFalse(TypeTraits.isTriviallyCopyable(c.getType()));
		IVariable d = helper.assertNonProblemOnFirstIdentifier("d[");
		assertTrue(TypeTraits.isTriviallyCopyable(d.getType()));
		IVariable e = helper.assertNonProblemOnFirstIdentifier("e;");
		assertTrue(TypeTraits.isTriviallyCopyable(e.getType()));
		IVariable f = helper.assertNonProblemOnFirstIdentifier("f;");
		assertTrue(TypeTraits.isTriviallyCopyable(f.getType()));
		IVariable g = helper.assertNonProblemOnFirstIdentifier("g;");
		assertFalse(TypeTraits.isTriviallyCopyable(g.getType()));
	}

	//	class Foo
	//	{
	//	public:
	//	void func();
	//	};
	//
	//	template<typename>
	//	struct PM_traits {};
	//
	//	template<class T, class U>
	//	struct PM_traits<U T::*> {
	//	    using member_type = U;
	//	};
	//
	//	template<bool>
	//	struct Test {
	//	  static const bool false_var = true;
	//	};
	//
	//	template<>
	//	struct Test<true> {
	//	  static const bool true_var = true;
	//	};
	//
	//	int main()
	//	{
	//	    Test<__is_function(PM_traits<decltype(&Foo::func)>::member_type)>::true_var;
	//
	//	    auto lambda = [](){};
	//	    Test<__is_function(decltype(lambda))>::false_var;
	//	    void (*funcPtr);
	//	    Test<__is_function(decltype(funcPtr))>::false_var;
	//	    Test<__is_function(int)>::false_var;
	//	    Test<__is_function(Foo)>::false_var;
	//	    return 0;
	//	}
	public void testIsFunction() throws Exception {
		fUseClang = true;
		parseAndCheckBindings(getAboveComment(), ParserLanguage.CPP);
	}
}
