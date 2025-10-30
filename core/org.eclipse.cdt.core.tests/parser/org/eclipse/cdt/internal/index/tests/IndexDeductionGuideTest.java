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
package org.eclipse.cdt.internal.index.tests;

import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.char_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.double_;
import static org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes.int_;

import org.eclipse.cdt.core.dom.ast.IVariable;
import org.junit.jupiter.api.Test;

/**
 * AST tests for C++17 deduction guides via PDOM.
 */
public abstract class IndexDeductionGuideTest extends IndexBindingResolutionTestBase {
	public static class IndexDeductionGuideTestSingleProject extends IndexDeductionGuideTest {
		public IndexDeductionGuideTestSingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true /* cpp */));
		}
	}

	public static class IndexDeductionGuideTestSingleProjectReindexed extends IndexDeductionGuideTest {
		public IndexDeductionGuideTestSingleProjectReindexed() {
			setStrategy(new SinglePDOMReindexedTestStrategy(true /* cpp */));
		}
	}

	public static class IndexDeductionGuideTestProjectWithDepProj extends IndexDeductionGuideTest {
		public IndexDeductionGuideTestProjectWithDepProj() {
			setStrategy(new ReferencedProject(true /* cpp */));
		}
	}

	//  template<typename T> struct S {
	//    template<typename U> S(S<U> s) : value(s.value) {}
	//    S(T v) : value(v) {}
	//    T value;
	//  };
	//
	//  //template<typename T> S(T t) -> S<double>;
	//  template<typename T> S(char) -> S<char>; // invalid candidate, T cannot be deduced
	//  S(int) -> S<double>;

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
	//
	//  constexpr size_t value = sizeof(ddouble.value);
	@Test
	public void testDeductionGuideBasicHeader() throws Exception {
		assertType(getBindingFromASTName("dchar = S", 5), char_);
		assertType(getBindingFromASTName("dint = S", 4), double_);
		assertType(getBindingFromASTName("dv = S", 2), double_);
		assertType(getBindingFromASTName("iv = S", 2), int_);

		IVariable varDouble = (IVariable) getBindingFromASTName("sdouble = S", 7);
		assertType(getBindingFromASTName("ddouble = S", 7), varDouble.getType());

		assertType(getBindingFromASTName("copy = ddouble", 4), varDouble.getType());

		assertType(getBindingFromASTName("init{", 4), varDouble.getType());

		assertType(getBindingFromASTName("convert(", 7), varDouble.getType());
	}

	//	#pragma once
	//
	//	namespace test
	//	{
	//	typedef double result_t;
	//	template<typename _Tp = void> struct Dependent;
	//	}
	//
	//	template<typename _Key, typename _Equal> struct Intermediate
	//	{
	//	public:
	//	    typedef test::result_t result_type;
	//	};
	//
	//	template<typename _Key, typename _Dependent = test::Dependent<_Key>>
	//	class Base {
	//	    typedef Intermediate<_Key, _Dependent> _Intermediate;
	//
	//	public:
	//	    typedef typename _Intermediate::result_type result_type;
	//	};

	//	using BaseType = Base<int>::result_type; // test marker
	//
	//	namespace test_std_alternative {
	//	template<typename A, typename B>
	//	class Unrelated;
	//
	//	  using namespace test;
	//	  template<typename _KeyInUnrelated>
	//	  Unrelated(_KeyInUnrelated)
	//	    -> Unrelated<_KeyInUnrelated, Dependent<_KeyInUnrelated>>;
	//	}
	@Test
	public void testDeductionGuideTemplateIssue438() throws Exception {
		getBindingFromASTName("Base<int>::result_type; // test marker", 22);
	}
}
