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

import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;

public class ReturnTypeDeductionTests extends AST2CPPTestBase {
	private IType getReturnType(String functionName) throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPFunction f = bh.assertNonProblem(functionName);
		return f.getType().getReturnType();
	}

	private void assertReturnType(String functionName, IType returnType) throws Exception {
		assertSameType(getReturnType(functionName), returnType);
	}

	private void assertReturnTypeProblem(String functionName) throws Exception {
		assertInstance(getReturnType(functionName), IProblemType.class);
	}

	private void assertReturnTypeValid(String functionName) throws Exception {
		assertFalse(getReturnType(functionName) instanceof IProblemType);
	}

	private IType getLambdaReturnType(String lambdaName) throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPVariable lambda = bh.assertNonProblem(lambdaName);
		IType lambdaType = lambda.getType();
		assertInstance(lambdaType, CPPClosureType.class);
		ICPPFunction f = ((CPPClosureType) lambdaType).getFunctionCallOperator();
		return f.getType().getReturnType();
	}

	private void assertLambdaReturnType(String lambdaName, IType returnType) throws Exception {
		assertSameType(getLambdaReturnType(lambdaName), returnType);
	}

	private void assertLambdaReturnTypeValid(String lambdaName) throws Exception {
		assertFalse(getLambdaReturnType(lambdaName) instanceof IProblemType);
	}

	//	auto f() { return 42; }
	public void testSingleReturn() throws Exception {
		assertReturnType("f", CommonCPPTypes.int_);
	}

	//	auto f(int x) {
	//		if (x < 10)
	//			return 42;
	//		else
	//			return 0;
	//	}
	public void testMultipleReturnsSameType() throws Exception {
		assertReturnType("f", CommonCPPTypes.int_);
	}

	//	struct S {};
	//	auto f(const S& s, bool c) {
	//		if (c)
	//			return S();
	//		else
	//			return s;
	//	}
	public void testMultipleReturnsDifferingByConst() throws Exception {
		assertReturnTypeValid("f");
	}

	//	auto f(int x) {
	//		if (x < 10)
	//			return 42;
	//		else
	//			return 0.0;
	//	}
	public void testMultipleReturnsDifferentTypes() throws Exception {
		assertReturnTypeProblem("f");
	}

	//	auto f() {
	//		return f();
	//	}
	public void testFullyRecursiveFunction() throws Exception {
		assertReturnTypeProblem("f");
	}

	//	auto sum(int i) {
	//		if (i == 1)
	//			return i;
	//		else
	//			return sum(i - 1) + i;
	//	}
	public void testPartiallyRecursiveFunction() throws Exception {
		assertReturnType("sum", CommonCPPTypes.int_);
	}

	//	template <typename T>
	//	auto f(T t) {
	//		return t;
	//	}
	//	typedef decltype(f(1)) fint_t;
	public void testFunctionTemplate() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ITypedef t = bh.assertNonProblem("fint_t");
		assertSameType(t, CommonCPPTypes.int_);
	}

	//	template <typename T> auto f(T t) { return t; }
	//	template <typename T> auto f(T* t) { return *t; }
	//	void g() { int (*p)(int*) = &f; }
	public void testAddressOfFunction() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPFunctionTemplate f2 = bh.assertNonProblem("f(T*", 1);
		ICPPTemplateInstance fi = bh.assertNonProblem("f;", 1);
		assertSame(f2, fi.getSpecializedBinding());
	}

	//	struct A { static int i; };
	//	auto& f1() { return A::i; }
	//	auto&& f2() { return A::i; }
	//	auto&& f3() { return 42; }
	//	const auto& f4() { return A::i; }
	//	const auto&& f5() { return 42; }
	public void testAutoRef() throws Exception {
		assertReturnType("f1", CommonCPPTypes.referenceToInt);
		assertReturnType("f2", CommonCPPTypes.referenceToInt);
		assertReturnType("f3", CommonCPPTypes.rvalueReferenceToInt);
		assertReturnType("f4", CommonCPPTypes.referenceToConstInt);
		assertReturnType("f5", CommonCPPTypes.rvalueReferenceToConstInt);
	}

	//	struct A { static int i; };
	//	auto* f1() { return &A::i; }
	//	const auto* f2() { return &A::i; }
	public void testAutoPointer() throws Exception {
		assertReturnType("f1", CommonCPPTypes.pointerToInt);
		assertReturnType("f2", CommonCPPTypes.pointerToConstInt);
	}

	//	auto f1() {}
	//	auto& f2() {}
	//	auto* f3() {}
	public void testVoidFunction() throws Exception {
		assertReturnType("f1", CommonCPPTypes.void_);
		assertReturnTypeProblem("f2");
		assertReturnTypeProblem("f3");
	}

	//	struct A { static int i; };
	//	auto f1() -> auto { return 42; }
	//	auto f2() -> auto& { return A::i; }
	//	auto f3() -> auto&& { return A::i; }
	//	auto f4() -> auto&& { return 42; }
	//	auto f5() -> const auto& { return A::i; }
	//	auto f6() -> const auto&& { return 42; }
	//	auto f7() -> auto* { return &A::i; }
	//	auto f8() -> const auto* { return &A::i; }
	public void testAutoInTrailingReturnType() throws Exception {
		assertReturnType("f1", CommonCPPTypes.int_);
		assertReturnType("f2", CommonCPPTypes.referenceToInt);
		assertReturnType("f3", CommonCPPTypes.referenceToInt);
		assertReturnType("f4", CommonCPPTypes.rvalueReferenceToInt);
		assertReturnType("f5", CommonCPPTypes.referenceToConstInt);
		assertReturnType("f6", CommonCPPTypes.rvalueReferenceToConstInt);
		assertReturnType("f7", CommonCPPTypes.pointerToInt);
		assertReturnType("f8", CommonCPPTypes.pointerToConstInt);
	}

	//      int i;
	//      auto f1 = []() -> auto { return 42; };
	//      auto f2 = []() -> auto& { return i; };
	//      auto f3 = []() -> auto&& { return i; };
	//      auto f4 = []() -> auto&& { return 42; };
	public void testAutoInLambdaReturnType() throws Exception {
		assertLambdaReturnType("f1", CommonCPPTypes.int_);
		assertLambdaReturnType("f2", CommonCPPTypes.referenceToInt);
		assertLambdaReturnType("f3", CommonCPPTypes.referenceToInt);
		assertLambdaReturnType("f4", CommonCPPTypes.rvalueReferenceToInt);
	}

	//	struct S {};
	//	auto f = [](const S& s, bool c) {
	//		if (c)
	//			return S();
	//		else
	//			return s;
	//	};
	public void testLambdaWithMultipleReturnsDifferingByConst() throws Exception {
		assertLambdaReturnTypeValid("f");
	}

	//	struct A {
	//		virtual auto f() { return 42; }
	//		virtual decltype(auto) g() { return 42; }
	//	};
	public void testVirtualAutoFunction() throws Exception {
		assertReturnTypeProblem("f");
		assertReturnTypeProblem("g");
	}

	//	auto f() { return {1, 2, 3}; }
	public void testInitializerList() throws Exception {
		assertReturnTypeProblem("f");
	}

	//	int f(int);
	//	int g(int);
	//	template <typename T>
	//	auto foo(bool cond, T arg) {
	//		if (cond) {
	//			return f(arg);
	//		} else {
	//			return g(arg);
	//		}
	//	}
	//	void bar(bool cond) {
	//		foo(cond, 0);
	//	}
	public void _testMultipleDependentReturns() throws Exception {
		// TODO: To pass this test, we need to defer the checking of whether
		//       all paths return the same type, until after instantiation.
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertNonProblem("foo(cond", "foo");
	}

	//	decltype(auto) f() { return 42; }
	//	decltype(auto) g(int* arg) { return *arg; }
	public void testDecltypeAuto() throws Exception {
		assertReturnType("f", CommonCPPTypes.int_);
		assertReturnType("g", CommonCPPTypes.referenceToInt);
	}

	//	auto f() -> decltype(auto) { return 42; }
	//	auto g(int* arg) -> decltype(auto) { return *arg; }
	// 	auto L1 = []() -> decltype(auto) { return 42; };
	//	auto L2 = [](int* arg) -> decltype(auto) { return *arg; };
	public void testDecltypeAutoInTrailingReturnType() throws Exception {
		assertReturnType("f", CommonCPPTypes.int_);
		assertReturnType("g", CommonCPPTypes.referenceToInt);
		assertLambdaReturnType("L1", CommonCPPTypes.int_);
		assertLambdaReturnType("L2", CommonCPPTypes.referenceToInt);
	}

	//	int i;
	//	decltype(auto)& f() { return i; }
	//	decltype(auto)* g() { return &i; }
	//	auto f2() -> decltype(auto)& { return i; }
	//	auto g2() -> decltype(auto)* { return &i; }
	public void testDecltypeAutoWithDecoration() throws Exception {
		assertReturnTypeProblem("f");
		assertReturnTypeProblem("g");
		assertReturnTypeProblem("f2");
		assertReturnTypeProblem("g2");
	}

	//	auto f();
	//	auto waldo = f();
	public void testUseWithoutDefinition() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableTypeProblem("waldo");
	}

	//	auto f();
	//	auto f() { return 42; }
	//	auto waldo = f();
	public void testUseAfterDefinition() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("waldo", CommonCPPTypes.int_);
	}

	//	auto f();
	//	auto waldo = f();
	//	auto f() { return 42; }
	public void _testUseBeforeDefinition() throws Exception {
		// TODO: To pass this test, we need to apply declaredBefore() filtering
		//       to the definition search in CPPFunction.getType().
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableTypeProblem("waldo");
	}

	//	auto f() { return 42; }
	//	int f();
	public void testRedeclaration() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPFunction autoFunction = helper.assertNonProblem("auto f", "f");
		// If we start diagnosing invalid redeclarations as errors, it would be
		// appropriate to start doing assertProblem() for the "f" in "int f" instead.
		ICPPFunction intFunction = helper.assertNonProblem("int f", "f");
		assertNotSame(autoFunction, intFunction);
	}

	//	struct A {
	//		auto f();
	//	};
	//	auto A::f() { return 42; }
	//	auto waldo = A().f();
	public void testOutOfLineMethod() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("waldo", CommonCPPTypes.int_);
	}

	// struct A {
	//     decltype(auto) f() { return (var); }
	//     int var{};
	// };
	public void testParenthesizedIdIsLValueReference_520117() throws Exception {
		assertReturnType("f", CommonCPPTypes.referenceToInt);
	}

	//	struct s{ int v{}; };
	//
	//	decltype(auto) f() {
	//	    return (s{}.v);
	//	}
	public void testParenthesizedXValueIsRValueReference_520117() throws Exception {
		assertReturnType("f", CommonCPPTypes.rvalueReferenceToInt);
	}
}