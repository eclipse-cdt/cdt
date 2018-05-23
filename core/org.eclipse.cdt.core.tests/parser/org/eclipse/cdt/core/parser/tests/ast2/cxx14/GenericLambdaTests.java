/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14;

import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes;

/**
 * AST tests for C++14 generic lambdas.
 */
public class GenericLambdaTests extends AST2CPPTestBase {
	//	auto Identity = [](auto a){ return a; };
	//	auto three = Identity(3);
	//	auto hello = Identity("hello");
	public void testBasicCall() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("three", CommonCPPTypes.int_);
		helper.assertVariableType("hello", CommonCPPTypes.pointerToConstChar);
	}

	//	// Adapted from the example in [expr.prim.lambda] p7 in the standard.
	//	auto Identity = [](auto a){ return a; };
	//	void f1(int(*)(int));
	//	void f2(char(*)(int));
	//	void g(int(*)(int));
	//	void g(char(*)(char));
	//	void h(int(*)(int));   // #1
	//	void h(char(*)(int));  // #2
	//	void j(int&(*)(int*));
	//	int main() {
	//		f1(Identity);  // ok
	//		f2(Identity);  // error: not convertible
	//		g(Identity);   // error: ambiguous
	//		h(Identity);   // ok: calls #1
	//		j([](auto* a) -> auto& { return *a; });  // ok
	//	}
	public void testConversionToFunctionPointer() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertNonProblem("f1(Id", "f1");
		helper.assertProblem("f2(Id", "f2");
		helper.assertProblem("g(Id", "g");
		IFunction h1 = helper.assertNonProblem("h(int", "h");
		IFunction hCall = helper.assertNonProblem("h(Id", "h");
		assertSame(h1, hCall);
		helper.assertNonProblem("j([]", "j");
	}

	//	template <typename... T>
	//	struct tuple {};
	//
	//	template <typename... T>
	//	tuple<T...> make_tuple(T...);
	//
	//	auto L = [](auto... args) { return make_tuple(args...); };
	//
	//	struct Cat { void meow(); };
	//
	//	Cat foo(tuple<int, char>);
	//	Cat bar(tuple<int, char, float>);
	//
	//	void waldo(Cat);
	//
	//	int main() {
	//	    waldo(foo(L(42, 'x')));
	//	    waldo(bar(L(42, 'x', 42.0f)));
	//	}
	public void testVariadicAutoParameter() throws Exception {
		parseAndCheckBindings();
	}

	//	// Adapted from the example in [expr.prim.lambda] p6 in the standard.
	//	namespace std {
	//		struct ostream {
	//			template <typename T>
	//			ostream& operator<<(const T&);
	//		};
	//		ostream cout;
	//	}
	//	auto vglambda = [](auto printer) {
	//		return [=](auto&&... ts) {
	//			return [=]() {
	//				return printer(ts...);
	//			};
	//		};
	//	};
	//	auto p = vglambda(
	//		[](auto v1, auto v2, auto v3) {
	//			return std::cout << v1 << v2 << v3;
	//		});
	//	auto q = p(1, 'a', 3.14);
	//	void waldo(const std::ostream&);
	//	int main() {
	//		waldo(q());
	//	}
	public void testNestedGenericLambdas() throws Exception {
		parseAndCheckBindings();
	}
}
