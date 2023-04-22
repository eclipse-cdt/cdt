/*******************************************************************************
 * Copyright (c) 2023 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.char_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.double_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.int_;

import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;

/**
 * AST tests for C++17 deduction guides.
 */
public class DeductionGuideTest extends AST2CPPTestBase {

	//  template<typename T> struct S {
	//    template<typename U> S(S<U> s) : value(s.value) {}
	//    S(T v) : value(v) {}
	//    T value;
	//  };
	//
	//  //template<typename T> S(T t) -> S<double>;
	//  template<typename T> S(char) -> S<char>; // invalid candidate, T cannot be deduced
	//  S(int) -> S<double>;
	//
	//  void f() {
	//    auto dchar = S('1').value;
	//    auto dint = S(1).value;
	//    double dv = S<double>(1).value;
	//    int iv = S(1).value;
	//
	//    auto sdouble = S<double>();
	//    auto ddouble = S(1);
	//
	//    S copy = ddouble;
	//    S init{ddouble};
	//    S convert(ddouble);
	//  }
	public void testDeductionGuideBasic() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper(ParserLanguage.CPP, ScannerKind.STD);
		assertType(bh.assertNonProblem("dchar = S", 5), char_);
		assertType(bh.assertNonProblem("dint = S", 4), double_);
		assertType(bh.assertNonProblem("dv = S", 2), double_);
		assertType(bh.assertNonProblem("iv = S", 2), int_);

		IVariable varDouble = bh.assertNonProblem("sdouble = S", 7);
		assertType(bh.assertNonProblem("ddouble = S", 7), varDouble.getType());

		assertType(bh.assertNonProblem("copy = ddouble", 4), varDouble.getType());

		assertType(bh.assertNonProblem("init{", 4), varDouble.getType());

		assertType(bh.assertNonProblem("convert(", 7), varDouble.getType());
	}

	//  template<typename T, typename U = bool> struct S {
	//    S(T v) : value(v) {}
	//    T value;
	//  };
	//
	//  //template<typename T> S(T t) -> S<double>;
	//  template<typename T> S(char) -> S<char>;
	//  S(int) -> S<double>;
	//
	//  void f() {
	//    auto dchar = S('1').value;
	//    auto dint = S(1).value;
	//    double dv = S<double>(1).value;
	//    int iv = S(1).value;
	//  }
	public void testDeductionGuideWithDefaultTemplateArg() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper(ParserLanguage.CPP, ScannerKind.STD);
		assertType(bh.assertNonProblem("dchar = S", 5), char_);
		assertType(bh.assertNonProblem("dint = S", 4), double_);
		assertType(bh.assertNonProblem("dv = S", 2), double_);
		assertType(bh.assertNonProblem("iv = S", 2), int_);
	}

	//  template<typename T> struct S; // No definition
	//
	//  S(char) -> S<char>;
	//  S(int) -> S<double>;
	//
	//  void f() {
	//    auto schar = S<char>();
	//    auto dchar = S('1');
	//    auto sdouble = S<double>();
	//    auto ddouble = S(1);
	//    S copy = ddouble;
	//    S init{ddouble};
	//    S convert(ddouble);
	//  }
	public void testDeductionGuideWithTemplateDeclaration() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper(ParserLanguage.CPP, ScannerKind.STD);

		IVariable varChar = bh.assertNonProblem("schar = S", 5);
		assertType(bh.assertNonProblem("dchar = S", 5), varChar.getType());

		IVariable varDouble = bh.assertNonProblem("sdouble = S", 7);
		assertType(bh.assertNonProblem("ddouble = S", 7), varDouble.getType());

		assertType(bh.assertNonProblem("copy = ddouble", 4), varDouble.getType());

		assertType(bh.assertNonProblem("init{", 4), varDouble.getType());

		assertType(bh.assertNonProblem("convert(", 7), varDouble.getType());
	}

	//  template<class T>
	//  struct UniquePtr
	//  {
	//    UniquePtr(T* t);
	//  };
	//
	//  UniquePtr dp{new auto(2.0)};
	//  auto da = dp;
	public void testMinimal() throws Exception {
		parseAndCheckBindings(ScannerKind.STD);
	}

	//  template<class T> struct S {
	//    S();
	//  };
	//
	//  S<int> sInt;
	//
	//  S s1;
	//
	//  S() -> S<int>;
	//
	//  S s2;
	public void testDeduceFromEmptyInitializer() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper(ParserLanguage.CPP, ScannerKind.STD);
		IVariable varSInt = bh.assertNonProblem("sInt", 4);
		bh.assertProblem("S s1", 1);
		bh.assertNonProblem("S s2", 1);
		assertType(bh.assertNonProblem("s2", 2), varSInt.getType());
	}

	//  template<class T>
	//  struct S
	//  {
	//    S(T t);
	//    template <typename U> S(U u);
	//  };
	//
	//	S(char c) -> S<long>;
	//  S(int i) -> S<double>;
	//
	//  auto v = S(1);
	public void testViaFunctionSetFromConstructors() throws Exception {
		parseAndCheckBindings(ScannerKind.STD);
	}

	//  template<class T> struct S{};
	//
	//  S* pointer;
	public void testNoDeductionForPointer() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper(ParserLanguage.CPP, ScannerKind.STD);
		IType pointedType = ((IPointerType) ((IVariable) bh.assertNonProblem("pointer")).getType()).getType();
		assertTrue(pointedType instanceof IProblemType);
		bh.assertProblem("S*", 1);
	}

	//  template<class T> struct S;
	//
	//  S* pointer;
	public void testNoDeductionForPointerNoDefinition() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper(ParserLanguage.CPP, ScannerKind.STD);
		IType pointedType = ((IPointerType) ((IVariable) bh.assertNonProblem("pointer")).getType()).getType();
		assertTrue(pointedType instanceof IProblemType);
		bh.assertProblem("S*", 1);
	}
}
