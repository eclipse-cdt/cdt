/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14;

import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;

public class ReturnTypeDeductionTests extends AST2TestBase {
	private BindingAssertionHelper getAssertionHelper() throws Exception {
		return getAssertionHelper(ParserLanguage.CPP);
	}
	
	private void assertReturnType(String functionName, IType returnType) throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPFunction f = bh.assertNonProblem(functionName);
		assertSameType(f.getType().getReturnType(), returnType);
	}
	
	private void assertReturnTypeProblem(String functionName) throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPFunction f = bh.assertNonProblem(functionName);
		assertInstance(f.getType().getReturnType(), IProblemType.class);
	}
	
	private void assertLambdaReturnType(String lambdaName, IType returnType) throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPVariable lambda = bh.assertNonProblem(lambdaName);
		CPPClosureType lambdaType = (CPPClosureType) lambda.getType();
		ICPPFunction f = lambdaType.getFunctionCallOperator();
		assertSameType(f.getType().getReturnType(), returnType);
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
	
	//	struct A {
	//		virtual auto f() { return 42; }
	//	};
	public void testVirtualAutoFunction() throws Exception {
		assertReturnTypeProblem("f");
	}
	
	//	auto f() { return {1, 2, 3}; }
	public void testInitializerList() throws Exception {
		assertReturnTypeProblem("f");
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
	public void testDecltypeAuto() throws Exception {
		assertReturnType("f", CommonCPPTypes.int_);
	}
}