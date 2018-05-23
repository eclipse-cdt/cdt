/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.char_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.double_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.int_;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.index.tests.IndexBindingResolutionTestBase;

public class StructuredBindingIndexTests extends IndexBindingResolutionTestBase {
	public StructuredBindingIndexTests() {
		setStrategy(new SinglePDOMTestStrategy(true));
	}

	//struct S {
	//  int i;
	//} s{};

	//auto [z] = s;
	public void testLocalStructuredBindingFromMemberOfBasicType_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("z"), int_);
	}

	//struct S {
	//  int i;
	//} s{};
	//auto [z] = s;

	//auto x = z;
	public void testExternalStructuredBindingFromMemberOfBasicType_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("z"), int_);
	}

	//struct T {
	//};
	//struct S {
	//  T t;
	//} s{};

	//auto [z] = s;
	//T localT{};
	public void testLocalStructuredBindingFromMemberOfUserDefinedType_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		IVariable variable = helper.assertNonProblem("z");
		IType variableType = variable.getType();

		IVariable localT = helper.assertNonProblem("localT");
		IType typeT = localT.getType();

		assertSameType(typeT, variableType);
	}

	//struct T {
	//};
	//struct S {
	//  T t;
	//} s{};
	//auto [z] = s;

	//auto x = z;
	//T localT{};
	public void testExternalStructuredBindingFromMemberOfUserDefinedType_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		IVariable variable = helper.assertNonProblem("z");
		IType variableType = variable.getType();

		IVariable localT = helper.assertNonProblem("localT");
		IType typeT = localT.getType();

		assertSameType(typeT, variableType);
	}

	//struct T {
	//};
	//struct Base1 {
	//};
	//struct Base2 {
	//  T t;
	//  int i;
	//  double d;
	//  char c;
	//};
	//struct S : Base1, Base2 {
	//} s{};

	//auto [t, i, d, c] = s;
	//T localT{};
	public void testMultipleVariablesInStructuredBindingFromMembers_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		IVariable variableT = helper.assertNonProblem("t,", 1);
		IType variableTType = variableT.getType();
		IVariable localT = helper.assertNonProblem("localT");
		IType typeT = localT.getType();
		assertSameType(typeT, variableTType);

		assertType(helper.assertNonProblem("i,", 1), int_);
		assertType(helper.assertNonProblem("d,", 1), double_);
		assertType(helper.assertNonProblem("c]", 1), char_);
	}

	//struct T {
	//};
	//struct Base1 {
	//  T t;
	//  int i;
	//  double d;
	//  char c;
	//};
	//struct Base2 : Base1 {
	//};
	//struct S : Base2 {
	//} s{};

	//auto [t, i, d, c] = s;
	//T localT{};
	public void testMultipleVariablesDeepBaseStructure_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		IVariable variableT = helper.assertNonProblem("t,", 1);
		IType variableTType = variableT.getType();
		IVariable localT = helper.assertNonProblem("localT");
		IType typeT = localT.getType();
		assertSameType(typeT, variableTType);

		assertType(helper.assertNonProblem("i,", 1), int_);
		assertType(helper.assertNonProblem("d,", 1), double_);
		assertType(helper.assertNonProblem("c]", 1), char_);
	}

	//struct S {
	//  int i;
	//  static float f;
	//  double d;
	//} s{};

	//auto [i, d] = s;
	public void testStaticFieldsAreNotConsidered_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("i,", 1), int_);
		assertType(helper.assertNonProblem("d]", 1), double_);
	}

	//namespace std {
	//  using size_t = unsigned long long;
	//  template <typename T, size_t N>
	//  struct array {
	//    T elements[N];
	//  };
	//  template <typename T>
	//  struct tuple_size;
	//  template <typename T, size_t N>
	//  struct tuple_size<array<T, N>> {
	//    constexpr static size_t value = N;
	//  };
	//  template <size_t I, typename T>
	//  struct tuple_element;
	//  template <size_t I, typename T, size_t N>
	//  struct tuple_element<I, array<T, N>> {
	//    using type = T;
	//  };
	//}

	//auto [f, s, t] = std::array<int, 3>{1, 2, 3};
	public void testStandardArray_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s,", 1), int_);
		assertType(helper.assertNonProblem("t]", 1), int_);
	}

	//namespace std {
	//  using size_t = unsigned long long;
	//  template <typename T, size_t N>
	//  struct array {
	//    T elements[N];
	//  };
	//  template <typename T>
	//  struct tuple_size;
	//  template <typename T, size_t N>
	//  struct tuple_size<array<T, N>> {
	//    constexpr static size_t value = N;
	//  };
	//  template <size_t I, typename T>
	//  struct tuple_element;
	//  template <size_t I, typename T, size_t N>
	//  struct tuple_element<I, array<T, N>> {
	//    using type = T;
	//  };
	//}
	//struct X {
	//  int first;
	//  int second;
	//};

	//int main() {
	//  auto arr = std::array<X, 3>{X{1,2}, X{3,4}, X{5,6}};
	//  for (auto [firstX, secondX] : arr.elements) {
	//    auto sum = firstX + secondX;
	//  }
	//}
	public void testStandardArrayInLoop_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("firstX,", 6), int_);
		assertType(helper.assertNonProblem("secondX]", 7), int_);
		assertType(helper.assertNonProblem("sum", 3), int_);
	}

	//struct X {
	//  int first;
	//  int second;
	//  void fun();
	//};

	//void X::fun() {
	//  auto [f, s] = *this;
	//}
	public void testBindStarThis_() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s]", 1), int_);
	}
}
