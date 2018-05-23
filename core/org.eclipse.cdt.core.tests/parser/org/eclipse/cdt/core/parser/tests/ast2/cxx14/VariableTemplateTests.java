/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
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
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldTemplateSpecialization;

import junit.framework.TestSuite;

public class VariableTemplateTests extends AST2CPPTestBase {

	public static TestSuite suite() {
		return suite(VariableTemplateTests.class);
	}

	// template<typename T> constexpr T pi = T(3);
	public void testVariableTemplate() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPVariableTemplate pi = ah.assertNonProblem("pi", ICPPVariableTemplate.class);
		assertFalse(pi.isMutable());
		assertFalse(pi.isStatic());
	}

	// struct S {
	//   template<typename T> static constexpr T pi = T(3);
	// };
	public void testFieldTemplate() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPFieldTemplate pi = ah.assertNonProblem("pi", ICPPFieldTemplate.class);
		assertTrue(pi.isStatic());
		assertFalse(pi.isMutable());
		assertEquals(ICPPASTVisibilityLabel.v_public, pi.getVisibility());
	}

	// template<typename T> const T c;
	// template<typename T> const T c = T{};
	public void testVariableTemplateDeclaration() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPVariableTemplate decl = ah.assertNonProblem("c;", "c", ICPPVariableTemplate.class);
		ICPPVariableTemplate def = ah.assertNonProblem("c =", "c", ICPPVariableTemplate.class);

		assertEquals(decl, def);
	}

	// struct S{
	//   template<typename T> static const T c;
	// };
	// template<typename T> const T S::c = T{};
	public void testFieldTemplateDeclaration() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPFieldTemplate decl = ah.assertNonProblem("c;", "c", ICPPFieldTemplate.class);
		ICPPFieldTemplate def = ah.assertNonProblem("c =", "c", ICPPFieldTemplate.class);

		assertEquals(decl, def);
	}

	// template<typename T> constexpr T pi = T(3);
	//
	// int foo() { return pi<int>/*1*/; }
	// int bar() { return pi<int>/*2*/; }
	public void testVariableTemplateUse() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPVariableTemplate template = ah.assertNonProblem("pi<int>/*1*/;", "pi", ICPPVariableTemplate.class);
		ICPPVariableInstance inst1 = ah.assertNonProblem("pi<int>/*1*/;", "pi<int>", ICPPVariableInstance.class);
		ICPPVariableInstance inst2 = ah.assertNonProblem("pi<int>/*2*/;", "pi<int>", ICPPVariableInstance.class);

		assertEquals("3", inst1.getInitialValue().toString());
		assertEquals(template, inst1.getSpecializedBinding());
		assertEquals(template, inst2.getSpecializedBinding());
		assertEquals(inst1, inst2);
	}

	// struct S {
	//   template<typename T> static constexpr T pi = T(3);
	// };
	//
	// int foo() { return S::pi<int>; }
	public void testFieldTemplateUse() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPFieldTemplate template = ah.assertNonProblem("pi<int>", "pi", ICPPFieldTemplate.class);
		ICPPVariableInstance inst = ah.assertNonProblem("pi<int>", "pi<int>", ICPPVariableInstance.class,
				ICPPField.class);

		assertEquals("3", inst.getInitialValue().toString());
		assertEquals(template, inst.getSpecializedBinding());
	}

	// template<typename T> constexpr T pi = T(3);
	// template<> constexpr float pi<float> = 4;
	//
	// float f(){ return pi<float>; }
	public void testVariableTemplateSpecialization() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPVariableTemplate template = ah.assertNonProblem("pi<float> =", "pi", ICPPVariableTemplate.class);
		ICPPVariableInstance inst = ah.assertNonProblem("pi<float> =", "pi<float>", ICPPVariableInstance.class);
		ICPPVariableInstance ref = ah.assertNonProblem("pi<float>;", "pi<float>", ICPPVariableInstance.class);

		assertEquals("4", inst.getInitialValue().toString());
		assertEquals("4", ref.getInitialValue().toString());
		assertEquals(template, inst.getSpecializedBinding());
		assertEquals(template, ref.getSpecializedBinding());
		assertEquals(inst, ref);
	}

	// struct S {
	//   template<typename T> static constexpr T pi = T(3);
	// };
	// template<> constexpr int S::pi<int> = 4;
	//
	// float f(){ return S::pi<int>; }
	public void testFieldTemplateSpecialization() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPFieldTemplate template = ah.assertNonProblem("pi<int>", "pi", ICPPFieldTemplate.class);
		ICPPVariableInstance inst = ah.assertNonProblem("pi<int> = ", "pi<int>", ICPPVariableInstance.class,
				ICPPField.class);
		ICPPVariableInstance ref = ah.assertNonProblem("pi<int>;", "pi<int>", ICPPVariableInstance.class);

		assertEquals("4", inst.getInitialValue().toString());
		assertEquals("4", ref.getInitialValue().toString());
		assertEquals(template, inst.getSpecializedBinding());
		assertEquals(template, ref.getSpecializedBinding());
		assertEquals(inst, ref);
	}

	// template<typename T, int I> T c = T(I);
	// template<int I> float c<float, I> = float(I+1);
	// float f() { return c<float, 100>; }
	public void testVariableTemplatePartialSpecialization() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPVariableTemplate template = ah.assertNonProblem("c<float, I>", "c", ICPPVariableTemplate.class);
		ICPPPartialSpecialization spec = ah.assertNonProblem("c<float, I>", ICPPVariableTemplate.class,
				ICPPPartialSpecialization.class);
		ICPPVariableInstance inst = ah.assertNonProblem("c<float, 100>", ICPPVariableInstance.class);

		assertEquals("101", inst.getInitialValue().toString());
		assertEquals(template, spec.getPrimaryTemplate());
		assertEquals(spec, inst.getSpecializedBinding());
	}

	// struct S {
	//   template<typename T, int I> static constexpr T c = T(I);
	// };
	// template<int I> constexpr float S::c<float, I> = float(I+1);
	// float f() { return S::c<float, 100>; }
	public void testFieldTemplatePartialSpecialization() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPFieldTemplate template = ah.assertNonProblem("c<float, I>", "c", ICPPFieldTemplate.class);
		ICPPPartialSpecialization spec = ah.assertNonProblem("c<float, I>", ICPPFieldTemplate.class,
				ICPPPartialSpecialization.class);
		ICPPVariableInstance inst = ah.assertNonProblem("c<float, 100>", ICPPVariableInstance.class, ICPPField.class);

		assertEquals("101", inst.getInitialValue().toString());
		assertEquals(template, spec.getPrimaryTemplate());
		assertEquals(spec, inst.getSpecializedBinding());
	}

	// template<typename T> constexpr T pi = T(3);
	// template constexpr int pi<int>;
	//
	// int f(){ return pi<int>; }
	public void testVariableTemplateInstantiation() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPVariableTemplate template = ah.assertNonProblem("T pi", "pi");
		ICPPVariableTemplate instTemplate = ah.assertNonProblem("int pi<int>", "pi", ICPPVariableTemplate.class);
		ICPPVariableInstance inst = ah.assertNonProblem("int pi<int>", "pi<int>", ICPPVariableInstance.class);
		ICPPVariableInstance use = ah.assertNonProblem("return pi<int>", "pi<int>", ICPPVariableInstance.class);

		assertEquals("3", use.getInitialValue().toString());
		assertEquals(template, instTemplate);
		assertEquals(template, inst.getSpecializedBinding());
		assertEquals(template, use.getSpecializedBinding());
		assertEquals(inst, use);
	}

	// struct S {
	//   template<typename T> static constexpr T pi = T(3);
	// };
	// template constexpr double S::pi<double>;
	//
	// double f(){ return S::pi<double>; }
	public void testFieldTemplateInstantiation() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPFieldTemplate template = ah.assertNonProblem("T pi", "pi");
		ICPPFieldTemplate instTemplate = ah.assertNonProblem("double S::pi<double>", "pi", ICPPFieldTemplate.class);
		ICPPVariableInstance inst = ah.assertNonProblem("double S::pi<double>", "pi<double>",
				ICPPVariableInstance.class);
		ICPPVariableInstance use = ah.assertNonProblem("return S::pi<double>", "pi<double>",
				ICPPVariableInstance.class);

		assertEquals("3", use.getInitialValue().toString());
		assertEquals(template, instTemplate);
		assertEquals(inst, use);
		assertEquals(template, inst.getSpecializedBinding());
		assertEquals(template, use.getSpecializedBinding());
	}

	// template<template<typename> class C, typename T> constexpr C<T> once = C<T>{ T{} };
	//
	// template<typename T> struct Vec{ T t; };
	//
	// void f(){ once<Vec, int>; }
	public void testVariableTemplateWithTemplateTemplateParameter() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPVariableTemplate template = ah.assertNonProblem("once = ", "once");
		ICPPVariableInstance use = ah.assertNonProblem("once<Vec, int>");

		assertEquals(template, use.getSpecializedBinding());
		assertEquals("const Vec<int>", use.getType().toString());
	}

	// template<template<typename> class C>
	// struct S {
	//   template<typename T> static constexpr C<T> once = C<T>{ T{} };
	// };
	//
	// template<typename T> struct Vec{ T t; };
	//
	// void f(){ S<Vec>::once<int>; }
	public void testFieldTemplateInTemplate() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPFieldTemplate template = ah.assertNonProblem("once = ", "once", ICPPField.class,
				ICPPTemplateDefinition.class);
		CPPFieldTemplateSpecialization useName = ah.assertNonProblem("S<Vec>::once<int>", "once");
		ICPPVariableInstance useId = ah.assertNonProblem("S<Vec>::once<int>", ICPPField.class);

		assertEquals(useName, useId.getSpecializedBinding());
		assertEquals("const Vec<int>", useId.getType().toString());
	}

	// template<typename ... T> constexpr int Size = sizeof...(T);
	//
	// void f() {
	//   auto a = Size<>;
	//   auto b = Size<int, float, double>;
	// }
	public void testVariableTemplatePack() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);

		ICPPVariableTemplate template = ah.assertNonProblem("int Size", "Size");
		ICPPVariableTemplate aInstTemplate = ah.assertNonProblem("a = Size", "Size");
		ICPPVariableInstance aInst = ah.assertNonProblem("Size<>");
		ICPPVariableTemplate bInstTemplate = ah.assertNonProblem("b = Size", "Size");
		ICPPVariableInstance bInst = ah.assertNonProblem("Size<int, float, double>");

		assertEquals(template, aInstTemplate);
		assertEquals(template, bInstTemplate);
		assertEquals(template, aInst.getSpecializedBinding());
		assertEquals(template, bInst.getSpecializedBinding());
	}

	//	template <typename T>
	//	struct meta {
	//		static const bool value = true;
	//	};
	//
	//  template <typename T>
	//  constexpr bool var = meta<T>::value;
	//
	//  template <bool> struct S {};
	//
	//  template <typename T>
	//  S<var<T>> foo();
	//
	//	void bar() {
	//		auto waldo = foo<int>();
	//	}
	public void test_bug494216() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);
		ICPPVariable waldo = ah.assertNonProblem("waldo");
		IType type = waldo.getType();
		assertInstance(type, CPPClassInstance.class);
		ICPPTemplateArgument[] args = ((CPPClassInstance) type).getTemplateArguments();
		assertEquals(1, args.length);
		assertValue(args[0].getNonTypeValue(), 1);
	}

	//	template<typename T, typename = class _, typename... P>
	//	constexpr bool type_in_pack{type_in_pack<T, P...>};
	//
	//	template<typename T, typename... P>
	//	constexpr bool type_in_pack<T, T, P...>{true};
	//
	//	template<typename T>
	//	constexpr bool type_in_pack<T>{false};
	//
	//	constexpr bool waldo1 = type_in_pack<int, int, char>;
	//	constexpr bool waldo2 = type_in_pack<int, float, char>;
	public void testStackOverflow_513429() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper ah = getAssertionHelper(ParserLanguage.CPP);
		ICPPVariable waldo1 = ah.assertNonProblem("waldo1");
		assertVariableValue(waldo1, 1);
		ICPPVariable waldo2 = ah.assertNonProblem("waldo2");
		assertVariableValue(waldo2, 0);
	}

	//	template <typename R>
	//	auto L = []{ return R{}; };
	//
	//	decltype(L<int>()) waldo;
	public void testLambdaValue_517670() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("waldo", CommonCPPTypes.int_);
	}
}
