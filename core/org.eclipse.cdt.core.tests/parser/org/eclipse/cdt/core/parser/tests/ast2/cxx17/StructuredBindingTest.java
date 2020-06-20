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
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.constInt;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.double_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.float_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.int_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.referenceTo;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.referenceToConstInt;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.referenceToInt;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.rvalueReferenceTo;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.rvalueReferenceToInt;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.volatileOf;

import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes;
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.FloatingPointValue;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPStructuredBindingComposite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

public class StructuredBindingTests extends AST2CPPTestBase {

	//struct S {
	//  int first;
	//  double second;
	//};
	//auto [f1, s1] = S{1, 2};
	//auto f2 = f1;
	//auto s2 = s1;
	public void testFromTemporary() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		IBinding f1Declaration = helper.assertNonProblem("f1, ", 2);
		IBinding f1Reference = helper.assertNonProblem("f1;", 2);
		assertSame(f1Declaration, f1Reference);

		IBinding s1Declaration = helper.assertNonProblem("s1] ", 2);
		IBinding s1Reference = helper.assertNonProblem("s1;", 2);
		assertSame(s1Declaration, s1Reference);
	}

	//struct S {
	//  int first;
	//  double second;
	//};
	//S createS() {
	//  return {1, 2};
	//}
	//auto [f, s] = createS();
	public void testFromReturnValue() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s]", 1), double_);
	}

	//struct S {
	//  int first;
	//  double second;
	//};
	//S createS() {
	//  return {1, 2};
	//}
	//auto [f, s]{createS()};
	public void testBracedInitialization() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s]", 1), double_);
	}

	//struct S {
	//  int first;
	//  double second;
	//};
	//S createS() {
	//  return {1, 2};
	//}
	//auto [f, s](createS());
	public void testCopyInitialization() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s]", 1), double_);
	}

	//struct S {
	//  int first;
	//  int second;
	//  float third;
	//  double fourth;
	//  char fifth;
	//};
	//auto [f, s, t, fo, fif] = S{1, 2, 1.5f, 3.1415, '*'};
	public void testWithManyInitializers() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s,", 1), int_);
		assertType(helper.assertNonProblem("t,", 1), float_);
		assertType(helper.assertNonProblem("fo,", 2), double_);
		assertType(helper.assertNonProblem("fif]", 3), char_);
	}

	//struct Base {
	//  int bi;
	//};
	//struct Sub : Base {
	//  static double sd;
	//};
	//auto [b] = Sub{1};
	public void testWithBaseClass() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("b]", 1), int_);
	}

	//auto f() -> int(&)[2];
	//auto [x, y] = f();
	//auto & [xr, yr] = f();
	public void testStandardExample1() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("xr,", 2), referenceToInt);
		assertType(helper.assertNonProblem("yr]", 2), referenceToInt);
	}

	//struct S {
	//  int x1 : 2;
	//  volatile double y1;
	//};
	//S createS();
	//auto const [x, y] = createS();
	public void testStandardExample2() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("x,", 1), constInt);
		assertType(helper.assertNonProblem("y]", 1), CommonCPPTypes.constVolatileOf(double_));
	}

	//int arr[]{1, 2, 3};
	//auto [f, s, t] = arr;
	public void testFromArray() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s,", 1), int_);
		assertType(helper.assertNonProblem("t]", 1), int_);
	}

	//struct S {
	//  int i;
	//} s{};
	//auto && [f] = s;
	public void testForwardingReferenceWithLvalue() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f]", 1), referenceToInt);
	}

	//struct S {
	//  int i;
	//} s{};
	//auto && [f] = static_cast<S&&>(s);
	public void testForwardingReferenceWithXvalue() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f]", 1), rvalueReferenceToInt);
	}

	//struct S {
	//  int i;
	//};
	//auto && [f] = S{};
	public void testForwardingReferenceWithRvalue() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f]", 1), rvalueReferenceToInt);
	}

	//struct S {
	//  int first;
	//  double second;
	//};
	//
	//namespace std {
	//  template <typename>
	//  struct tuple_size;
	//}
	//auto [f, s] = S{};
	public void testUnspecializedTupleSizeTemplate() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s]", 1), double_);
	}

	//namespace std {
	//  using size_t = unsigned long long;
	//}
	//
	//struct S {
	//  int first() const {
	//    return 1;
	//  }
	//  double second() const {
	//    return 2.0;
	//  }
	//  template <std::size_t V>
	//  auto get() {
	//    if constexpr (V == 0) {
	//      return first();
	//    } else if (V == 1) {
	//      return second();
	//    }
	//    static_assert(V < 2);
	//  }
	//};
	//
	//namespace std {
	//  template <typename>
	//  struct tuple_size;
	//  template <>
	//  struct tuple_size<S> {
	//    constexpr static size_t value = 2;
	//  };
	//  template <std::size_t, typename>
	//  struct tuple_element;
	//  template <>
	//  struct tuple_element<0, S> {
	//      using type = int;
	//  };
	//  template <>
	//  struct tuple_element<1, S> {
	//      using type = double;
	//  };
	//}
	//auto [f, s] = S{};
	public void testFromTupleLikeDecompositionWithMemberGet() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s]", 1), double_);
	}

	//namespace std {
	//  using size_t = unsigned long long;
	//}
	//
	//struct S {
	//  int first() const {
	//    return 1;
	//  }
	//  double second() const {
	//    return 2.0;
	//  }
	//};
	//template <std::size_t V>
	//auto get(S s) {
	//  if constexpr (V == 0) {
	//    return s.first();
	//  } else if (V == 1) {
	//    return s.second();
	//  }
	//  static_assert(V < 2);
	//}
	//
	//namespace std {
	//  template <typename>
	//  struct tuple_size;
	//  template <>
	//  struct tuple_size<S> {
	//    constexpr static size_t value = 2;
	//  };
	//  template <std::size_t, typename>
	//  struct tuple_element;
	//  template <>
	//  struct tuple_element<0, S> {
	//      using type = int;
	//  };
	//  template <>
	//  struct tuple_element<1, S> {
	//      using type = double;
	//  };
	//}
	//auto [f, s] = S{};
	public void testFromTupleLikeDecompositionWithInheritedTupleElementType() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("f,", 1), int_);
		assertType(helper.assertNonProblem("s]", 1), double_);
	}

	//struct S {
	//  int member;
	//} s{1};
	//auto [valueLarg] = s;
	//auto [valueRarg] = S{1};
	//auto const [valueConstLarg] = s;
	//auto const [valueConstRarg] = S{1};
	//auto & [lrefLarg] = s;
	//auto & [lrefRarg] = S{1};
	//auto const & [lrefConstLarg] = s;
	//auto const & [lrefConstRarg] = S{1};
	//auto && [frefLarg] = s;
	//auto && [frefRarg] = S{1};
	//auto const && [rrefConstLarg] = s;
	//auto const && [rrefConstRarg] = S{1};
	//auto const sConst = s;
	//auto & [lrefLConstarg] = sConst;
	//auto && [frefLConstarg] = sConst;
	//S volatile sVolatile{1};
	//auto & [lrefLVolatilearg] = sVolatile;
	//auto && [frefLVolatilearg] = sVolatile;
	public void testResultingTypes() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("valueLarg"), int_);
		assertType(helper.assertNonProblem("valueRarg"), int_);
		assertType(helper.assertNonProblem("valueConstLarg"), constInt);
		assertType(helper.assertNonProblem("valueConstRarg"), constInt);
		assertType(helper.assertNonProblem("lrefLarg"), referenceToInt);
		assertType(helper.assertNonProblem("lrefRarg"), referenceToInt);
		assertType(helper.assertNonProblem("lrefConstLarg"), referenceToConstInt);
		assertType(helper.assertNonProblem("lrefConstRarg"), referenceToConstInt);
		assertType(helper.assertNonProblem("frefLarg"), referenceToInt);
		assertType(helper.assertNonProblem("frefRarg"), rvalueReferenceToInt);
		assertType(helper.assertNonProblem("rrefConstLarg"), rvalueReferenceTo(constInt));
		assertType(helper.assertNonProblem("rrefConstRarg"), rvalueReferenceTo(constInt));
		assertType(helper.assertNonProblem("lrefLConstarg"), referenceToConstInt);
		assertType(helper.assertNonProblem("frefLConstarg"), referenceToConstInt);
		assertType(helper.assertNonProblem("lrefLVolatilearg"), referenceTo(volatileOf(int_)));
		assertType(helper.assertNonProblem("frefLVolatilearg"), referenceTo(volatileOf(int_)));
	}

	//struct Aggregate {
	//  int i;
	//  double d;
	//  auto first() {
	//    auto [field1, _] = *this;
	//    return field1;
	//  }
	//  auto second() {
	//    auto [_, field2] = *this;
	//    return field2;
	//  }
	//};
	public void testThisDecomposition() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();

		assertType(helper.assertNonProblem("field1;", 6), int_);
		assertType(helper.assertNonProblem("field2;", 6), double_);
	}

	//struct S {
	//  int first;
	//  double second;
	//};
	//constexpr S createS() {
	//  return S{1, 2.0};
	//}
	//auto [f, s] = createS();
	public void testIVariablePropertiesOfImplicitNameForInitializer() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType classS = helper.assertNonProblem("S", 1);
		IASTImplicitName[] implicitNames = helper.getImplicitNames("= createS();", 11);
		assertEquals(1, implicitNames.length);
		IASTImplicitName implicitName = implicitNames[0];
		IBinding binding = implicitName.getBinding();
		CPPStructuredBindingComposite variable = assertInstance(binding, CPPStructuredBindingComposite.class);
		assertType(variable, classS);
		IValue initialValue = variable.getInitialValue();
		CompositeValue compositeValue = assertInstance(initialValue, CompositeValue.class);
		ICPPEvaluation[] subvalues = compositeValue.getAllSubValues();
		assertEquals(2, subvalues.length);
		assertEquals(IntegralValue.create(1), subvalues[0].getValue());
		assertEquals(FloatingPointValue.create(2.0), subvalues[1].getValue());
	}
}
