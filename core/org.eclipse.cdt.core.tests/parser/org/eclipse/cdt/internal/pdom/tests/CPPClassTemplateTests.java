/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests PDOM class template related bindings
 */
public class CPPClassTemplateTests extends PDOMInlineCodeTestBase {

	@BeforeEach
	protected void beforeEach2() throws Exception {
		setUpSections(1);
	}

	/*************************************************************************/

	//	template<typename T>
	//	class Foo {};
	//
	//	class A{}; class B{};
	//
	//	template<>
	//	class Foo<A> {};
	//
	//	Foo<A> a;
	//	Foo<B> b;
	@Test
	public void testSpecializations() throws Exception {
		IBinding[] as = pdom.findBindings(new char[][] { { 'a' } }, IndexFilter.ALL, npm());
		IBinding[] bs = pdom.findBindings(new char[][] { { 'b' } }, IndexFilter.ALL, npm());

		assertEquals(1, as.length);
		assertEquals(1, bs.length);
		assertInstanceOf(ICPPVariable.class, as[0]);
		assertInstanceOf(ICPPVariable.class, bs[0]);

		ICPPVariable a = (ICPPVariable) as[0];
		ICPPVariable b = (ICPPVariable) bs[0];

		assertInstanceOf(ICPPSpecialization.class, a.getType());
		assertInstanceOf(ICPPSpecialization.class, b.getType());

		ICPPSpecialization asp = (ICPPSpecialization) a.getType();
		ICPPSpecialization bsp = (ICPPSpecialization) b.getType();

		ICPPTemplateParameterMap aArgs = asp.getTemplateParameterMap();
		ICPPTemplateParameterMap bArgs = bsp.getTemplateParameterMap();
		assertEquals(1, aArgs.getAllParameterPositions().length);
		assertEquals(1, bArgs.getAllParameterPositions().length);

		assertInstanceOf(ICPPClassType.class, aArgs.getArgument(0).getTypeValue());
		assertInstanceOf(ICPPClassType.class, bArgs.getArgument(0).getTypeValue());

		assertEquals("A", ((ICPPClassType) aArgs.getArgument(0).getTypeValue()).getName());
		assertEquals("B", ((ICPPClassType) bArgs.getArgument(0).getTypeValue()).getName());

		assertDeclarationCount(pdom, "a", 1);
		assertDeclarationCount(pdom, "b", 1);
	}

	// template<typename C>
	// class D {
	// public:
	// int foo(C c) {return 1};
	// };
	@Test
	public void testSimpleDefinition() throws Exception {
		assertDeclarationCount(pdom, "D", 1);
		IIndexFragmentBinding[] b = pdom.findBindings(new char[][] { { 'D' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, b.length);
		assertTrue(b[0] instanceof ICPPClassTemplate);
		ICPPClassTemplate ct = (ICPPClassTemplate) b[0];
		ICPPTemplateParameter[] tp = ct.getTemplateParameters();
		assertEquals(1, tp.length);
		assertTrue(tp[0] instanceof ICPPTemplateTypeParameter);
		ICPPTemplateTypeParameter ctp = (ICPPTemplateTypeParameter) tp[0];
		assertNull(ctp.getDefault());
		assertEquals(0, ct.getPartialSpecializations().length);
	}

	// template<class C=char> /* typename and class are equivalent in template parameter context */
	// class D {
	// public:
	// int foo(C c) {return 1};
	// };
	@Test
	public void testDefinition() throws Exception {
		assertDeclarationCount(pdom, "D", 1);
		IIndexFragmentBinding[] b = pdom.findBindings(new char[][] { { 'D' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, b.length);
		assertTrue(b[0] instanceof ICPPClassTemplate);
		ICPPClassTemplate ct = (ICPPClassTemplate) b[0];
		ICPPTemplateParameter[] tp = ct.getTemplateParameters();
		assertEquals(1, tp.length);
		assertTrue(tp[0] instanceof ICPPTemplateTypeParameter);
		assertEquals("C", tp[0].getName());
		assertArrayEquals(new String[] { "D", "C" }, tp[0].getQualifiedName());
		assertArrayOfArrayEquals(new char[][] { { 'D' }, { 'C' } }, tp[0].getQualifiedNameCharArray());
		ICPPTemplateTypeParameter ctp = (ICPPTemplateTypeParameter) tp[0];
		IType def = ctp.getDefault();
		assertTrue(def instanceof IBasicType);
		assertEquals(0, ct.getPartialSpecializations().length);
	}

	// class TA {};
	// class TC {};
	//
	// template<typename A= TA, typename B, typename C=TC>
	// class E {
	// public:
	// int foo(C c, B b, A a) {return 1};
	// };
	@Test
	public void testDefinition2() throws Exception {
		assertDeclarationCount(pdom, "E", 1);
		IIndexFragmentBinding[] b = pdom.findBindings(new char[][] { { 'E' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, b.length);
		assertTrue(b[0] instanceof ICPPClassTemplate);
		ICPPClassTemplate ct = (ICPPClassTemplate) b[0];
		ICPPTemplateParameter[] tp = ct.getTemplateParameters();
		assertEquals(3, tp.length);

		assertTrue(tp[0] instanceof ICPPTemplateTypeParameter);
		assertEquals("A", tp[0].getName());
		assertArrayEquals(new String[] { "E", "A" }, tp[0].getQualifiedName());
		assertArrayOfArrayEquals(new char[][] { { 'E' }, { 'A' } }, tp[0].getQualifiedNameCharArray());
		ICPPTemplateTypeParameter ctpa = (ICPPTemplateTypeParameter) tp[0];
		IType defa = ctpa.getDefault();
		assertTrue(defa instanceof ICPPClassType);
		ICPPClassType ctdefa = (ICPPClassType) defa;
		assertArrayOfArrayEquals(new char[][] { { 'T', 'A' } }, ctdefa.getQualifiedNameCharArray());

		assertTrue(tp[1] instanceof ICPPTemplateTypeParameter);
		assertEquals("B", tp[1].getName());
		assertArrayEquals(new String[] { "E", "B" }, tp[1].getQualifiedName());
		assertArrayOfArrayEquals(new char[][] { { 'E' }, { 'B' } }, tp[1].getQualifiedNameCharArray());
		ICPPTemplateTypeParameter ctpb = (ICPPTemplateTypeParameter) tp[1];
		IType defb = ctpb.getDefault();
		assertNull(defb);

		assertTrue(tp[2] instanceof ICPPTemplateTypeParameter);
		assertEquals("C", tp[2].getName());
		assertArrayEquals(new String[] { "E", "C" }, tp[2].getQualifiedName());
		assertArrayOfArrayEquals(new char[][] { { 'E' }, { 'C' } }, tp[2].getQualifiedNameCharArray());
		ICPPTemplateTypeParameter ctpc = (ICPPTemplateTypeParameter) tp[2];
		IType defc = ctpc.getDefault();
		assertTrue(defc instanceof ICPPClassType);
		ICPPClassType ctdefc = (ICPPClassType) defc;
		assertArrayOfArrayEquals(new char[][] { { 'T', 'C' } }, ctdefc.getQualifiedNameCharArray());

		assertEquals(0, ct.getPartialSpecializations().length);
	}

	// template<typename T>
	// class Foo {
	//    public:
	//    T (*f)(T);
	// };
	//
	// class A {};
	// Foo<A> foo = *new Foo<A>();
	// void bar() {
	//    foo->f(*new A());
	// }
	@Test
	public void testFunctionPointer() throws Exception {
		IIndexFragmentBinding[] bs = pdom.findBindings(new char[][] { "foo".toCharArray() }, IndexFilter.ALL, npm());
		assertEquals(1, bs.length);
		assertInstanceOf(ICPPVariable.class, bs[0]);
		ICPPVariable var = (ICPPVariable) bs[0];
		assertInstanceOf(ICPPClassType.class, var.getType());
		ICPPClassType ct = (ICPPClassType) var.getType();
		IField[] fields = ClassTypeHelper.getFields(ct);
		assertEquals(1, fields.length);
		assertInstanceOf(IPointerType.class, fields[0].getType());
		IPointerType pt = (IPointerType) fields[0].getType();
		assertInstanceOf(IFunctionType.class, pt.getType());
		IFunctionType ft = (IFunctionType) pt.getType();
		assertInstanceOf(ICPPClassType.class, ft.getReturnType());
		assertEquals(1, ft.getParameterTypes().length);
		assertInstanceOf(ICPPClassType.class, ft.getParameterTypes()[0]);
	}

	// template<typename C>
	// class D {
	// public:
	// int foo(C c) {return 1};
	// };
	//
	// class N {};
	//
	// template<>
	// class D<N> {
	// public:
	// int foo(N n) {return 2;}
	// };
	//
	// D<N> dn;
	// D<int> dint;
	@Test
	public void testExplicitInstantiation() throws Exception {
		{
			// template
			IIndexFragmentBinding[] b = pdom.findBindings(new char[][] { { 'D' } }, IndexFilter.ALL_DECLARED, npm());
			assertEquals(2, b.length);
			assertTrue(!(b[0] instanceof ICPPClassTemplate) || !(b[1] instanceof ICPPClassTemplate));
			int i = b[0] instanceof ICPPClassTemplate ? 0 : 1;

			assertInstanceOf(ICPPClassTemplate.class, b[i]);
			ICPPClassTemplate ct = (ICPPClassTemplate) b[i];
			ICPPTemplateParameter[] tp = ct.getTemplateParameters();
			assertEquals(1, tp.length);
			assertInstanceOf(ICPPTemplateTypeParameter.class, tp[i]);
			ICPPTemplateTypeParameter ctp = (ICPPTemplateTypeParameter) tp[i];
			assertNull(ctp.getDefault());
		}

		{
			assertDeclarationCount(pdom, "dn", 1);
			IIndexFragmentBinding[] b = pdom.findBindings(new char[][] { "dn".toCharArray() }, IndexFilter.ALL, npm());
			assertEquals(1, b.length);
			assertInstanceOf(ICPPVariable.class, b[0]);
			ICPPVariable var = (ICPPVariable) b[0];
			assertInstanceOf(ICPPClassType.class, var.getType());
			assertInstanceOf(ICPPSpecialization.class, var.getType());
			ICPPSpecialization cp = (ICPPSpecialization) var.getType();
			ICPPTemplateParameterMap m = cp.getTemplateParameterMap();
			assertEquals(1, m.getAllParameterPositions().length);
			ICPPTemplateArgument arg = m.getArgument(0);
			assertInstanceOf(ICPPClassType.class, arg.getTypeValue());
			assertArrayEquals(new String[] { "N" }, ((ICPPClassType) arg.getTypeValue()).getQualifiedName());
		}

		{
			assertDeclarationCount(pdom, "dint", 1);
			IIndexFragmentBinding[] b = pdom.findBindings(new char[][] { "dint".toCharArray() }, IndexFilter.ALL,
					npm());
			assertEquals(1, b.length);
			assertTrue(b[0] instanceof ICPPVariable);
			ICPPVariable var = (ICPPVariable) b[0];
			assertInstanceOf(ICPPClassType.class, var.getType());
			assertInstanceOf(ICPPSpecialization.class, var.getType());
			ICPPSpecialization cp = (ICPPSpecialization) var.getType();
			ICPPTemplateParameterMap m = cp.getTemplateParameterMap();
			assertEquals(1, m.getAllParameterPositions().length);
			ICPPTemplateArgument arg = m.getArgument(0);
			assertInstanceOf(IBasicType.class, arg.getTypeValue());
			assertEquals(IBasicType.Kind.eInt, ((IBasicType) arg.getTypeValue()).getKind());
		}
	}

	// template<typename xT>
	// struct S {
	//     xT x;
	// };
	// template<typename aT>
	// using A = S<aT>;
	@Test
	public void testSimpleAliasDefinition() throws Exception {
		assertDeclarationCount(pdom, "A", 1);
		IIndexFragmentBinding[] bindingA = pdom.findBindings(new char[][] { { 'A' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingA.length);
		assertTrue(bindingA[0] instanceof ICPPAliasTemplate);
		ICPPAliasTemplate aliasA = (ICPPAliasTemplate) bindingA[0];
		ICPPTemplateParameter[] aliasParameters = aliasA.getTemplateParameters();
		assertEquals(1, aliasParameters.length);

		assertTrue(aliasParameters[0] instanceof ICPPTemplateTypeParameter);
		ICPPTemplateTypeParameter templateParameterAT = (ICPPTemplateTypeParameter) aliasParameters[0];
		assertEquals("aT", templateParameterAT.getName());
		assertNull(templateParameterAT.getDefault());
		assertEquals(0, templateParameterAT.getTemplateNestingLevel());

		assertDeclarationCount(pdom, "S", 1);
		IIndexFragmentBinding[] bindingS = pdom.findBindings(new char[][] { { 'S' } }, IndexFilter.ALL_DECLARED, npm());
		IType aliasedType = aliasA.getType();
		assertTrue(aliasedType instanceof ICPPDeferredClassInstance);
		ICPPDeferredClassInstance deferredClassInstanceS = (ICPPDeferredClassInstance) aliasedType;
		assertEquals(1, bindingA.length);
		assertEquals(bindingS[0], deferredClassInstanceS.getSpecializedBinding());
	}

	// struct D {
	// };
	// template<typename sT1, typename sT2>
	// struct S {
	//     xT x;
	// };
	// template<typename aT1, typename aT2 = D>
	// using A = S<aT1, aT2>;
	@Test
	public void testSimpleAliasDefinitionDefaultTemplateArgument() throws Exception {
		assertDeclarationCount(pdom, "A", 1);
		IIndexFragmentBinding[] bindingA = pdom.findBindings(new char[][] { { 'A' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingA.length);
		assertTrue(bindingA[0] instanceof ICPPAliasTemplate);
		ICPPAliasTemplate aliasA = (ICPPAliasTemplate) bindingA[0];
		ICPPTemplateParameter[] aliasParameters = aliasA.getTemplateParameters();
		assertEquals(2, aliasParameters.length);

		assertTrue(aliasParameters[0] instanceof ICPPTemplateTypeParameter);
		ICPPTemplateTypeParameter templateParameterAT1 = (ICPPTemplateTypeParameter) aliasParameters[0];
		assertEquals("aT1", templateParameterAT1.getName());
		assertNull(templateParameterAT1.getDefault());
		assertEquals(0, templateParameterAT1.getTemplateNestingLevel());

		assertTrue(aliasParameters[1] instanceof ICPPTemplateTypeParameter);
		ICPPTemplateTypeParameter templateParameterAT2 = (ICPPTemplateTypeParameter) aliasParameters[1];
		assertEquals("aT2", templateParameterAT2.getName());
		IType aT2DefaultArgument = templateParameterAT2.getDefault();
		assertNotNull(aT2DefaultArgument);
		assertDeclarationCount(pdom, "D", 1);
		IIndexFragmentBinding[] bindingD = pdom.findBindings(new char[][] { { 'D' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingD.length);
		assertTrue(bindingD[0] instanceof IType);
		assertTrue(((IType) bindingD[0]).isSameType(aT2DefaultArgument));
		assertEquals(0, templateParameterAT2.getTemplateNestingLevel());

		assertDeclarationCount(pdom, "S", 1);
		IIndexFragmentBinding[] bindingS = pdom.findBindings(new char[][] { { 'S' } }, IndexFilter.ALL_DECLARED, npm());
		IType aliasedType = aliasA.getType();
		assertTrue(aliasedType instanceof ICPPDeferredClassInstance);
		ICPPDeferredClassInstance deferredClassInstanceS = (ICPPDeferredClassInstance) aliasedType;
		assertEquals(1, bindingS.length);
		assertEquals(bindingS[0], deferredClassInstanceS.getSpecializedBinding());
	}

	// template<boolean sT1, int sT2>
	// struct S {
	//     xT x;
	// };
	// template<boolean aT1, int aT2 = 5>
	// using A = S<aT1, aT2>;
	@Test
	public void testSimpleAliasDefinitionValueTemplateArguments() throws Exception {
		assertDeclarationCount(pdom, "A", 1);
		IIndexFragmentBinding[] bindingA = pdom.findBindings(new char[][] { { 'A' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingA.length);
		assertTrue(bindingA[0] instanceof ICPPAliasTemplate);
		ICPPAliasTemplate aliasA = (ICPPAliasTemplate) bindingA[0];
		ICPPTemplateParameter[] aliasParameters = aliasA.getTemplateParameters();
		assertEquals(2, aliasParameters.length);

		assertTrue(aliasParameters[0] instanceof ICPPTemplateNonTypeParameter);
		ICPPTemplateNonTypeParameter templateParameterAT1 = (ICPPTemplateNonTypeParameter) aliasParameters[0];
		assertEquals("aT1", templateParameterAT1.getName());
		assertNull(templateParameterAT1.getDefaultValue());
		assertEquals(0, templateParameterAT1.getTemplateNestingLevel());

		assertTrue(aliasParameters[1] instanceof ICPPTemplateNonTypeParameter);
		ICPPTemplateNonTypeParameter templateParameterAT2 = (ICPPTemplateNonTypeParameter) aliasParameters[1];
		assertEquals("aT2", templateParameterAT2.getName());
		ICPPTemplateArgument aT2DefaultArgument = templateParameterAT2.getDefaultValue();
		assertNotNull(aT2DefaultArgument);
		assertTrue(new CPPBasicType(IBasicType.Kind.eInt, 0).isSameType(aT2DefaultArgument.getTypeOfNonTypeValue()));
		assertEquals(5L, aT2DefaultArgument.getNonTypeValue().numberValue().longValue());
		assertEquals(0, templateParameterAT2.getTemplateNestingLevel());

		assertDeclarationCount(pdom, "S", 1);
		IIndexFragmentBinding[] bindingS = pdom.findBindings(new char[][] { { 'S' } }, IndexFilter.ALL_DECLARED, npm());
		IType aliasedType = aliasA.getType();
		assertTrue(aliasedType instanceof ICPPDeferredClassInstance);
		ICPPDeferredClassInstance deferredClassInstanceS = (ICPPDeferredClassInstance) aliasedType;
		assertEquals(1, bindingS.length);
		assertEquals(bindingS[0], deferredClassInstanceS.getSpecializedBinding());
	}

	// template<typename T>
	// struct S {
	//     T t;
	// };
	// template<template<typename> class TT>
	// using A = S<TT>;
	@Test
	public void testSimpleAliasTemplateParameter() throws Exception {
		assertDeclarationCount(pdom, "A", 1);
		IIndexFragmentBinding[] bindingA = pdom.findBindings(new char[][] { { 'A' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingA.length);
		assertTrue(bindingA[0] instanceof ICPPAliasTemplate);
		ICPPAliasTemplate aliasA = (ICPPAliasTemplate) bindingA[0];
		ICPPTemplateParameter[] aliasParameters = aliasA.getTemplateParameters();
		assertEquals(1, aliasParameters.length);

		assertTrue(aliasParameters[0] instanceof ICPPTemplateTemplateParameter);
		ICPPTemplateTemplateParameter templateParameterTT = (ICPPTemplateTemplateParameter) aliasParameters[0];
		assertEquals("TT", templateParameterTT.getName());
		assertNull(templateParameterTT.getDefaultValue());
		assertEquals(0, templateParameterTT.getTemplateNestingLevel());
	}

	// struct B{};
	// template<typename xT>
	// struct S {
	//     xT x;
	// };
	// template<typename aT>
	// using A = S<aT>;
	// A<B> aB;
	// S<B> sB;
	@Test
	public void testSimpleAliasReference() throws Exception {
		assertDeclarationCount(pdom, "A", 1);
		IIndexFragmentBinding[] bindingA = pdom.findBindings(new char[][] { { 'A' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingA.length);
		assertInstanceOf(ICPPAliasTemplate.class, bindingA[0]);
		ICPPAliasTemplate aliasA = (ICPPAliasTemplate) bindingA[0];
		ICPPTemplateParameter[] aliasParameters = aliasA.getTemplateParameters();
		assertEquals(1, aliasParameters.length);

		assertReferenceCount(pdom, "S", 2);
		assertReferenceCount(pdom, "A", 1);
		assertDeclarationCount(pdom, "aB", 1);
		assertDeclarationCount(pdom, "sB", 1);

		IIndexFragmentBinding[] bindingB = pdom.findBindings(new char[][] { { 'B' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingB.length);
		assertInstanceOf(ICPPClassType.class, bindingB[0]);

		IIndexFragmentBinding[] bindingVarSB = pdom.findBindings(new char[][] { "sB".toCharArray() }, IndexFilter.ALL,
				npm());
		assertEquals(1, bindingVarSB.length);
		assertInstanceOf(ICPPVariable.class, bindingVarSB[0]);
		ICPPVariable variableSB = (ICPPVariable) bindingVarSB[0];
		IType varSBType = variableSB.getType();
		assertInstanceOf(ICPPClassSpecialization.class, varSBType);
		ICPPClassSpecialization templateInstanceSB = (ICPPClassSpecialization) varSBType;

		IIndexFragmentBinding[] bindingVarAB = pdom.findBindings(new char[][] { "aB".toCharArray() }, IndexFilter.ALL,
				npm());
		assertEquals(1, bindingVarAB.length);
		assertTrue(bindingVarAB[0] instanceof ICPPVariable);
		ICPPVariable variableAB = (ICPPVariable) bindingVarAB[0];
		IType varABType = variableAB.getType();
		assertInstanceOf(ICPPAliasTemplateInstance.class, varABType);
		ICPPAliasTemplateInstance aliasInstanceAB = (ICPPAliasTemplateInstance) varABType;
		assertTrue(varABType.isSameType(templateInstanceSB));
		assertTrue(aliasInstanceAB.getTemplateDefinition().isSameType(aliasA));
		assertEquals("A", aliasInstanceAB.getName());
		IType aliasedType = aliasInstanceAB.getType();
		assertInstanceOf(ICPPTemplateInstance.class, aliasedType);
		ICPPTemplateArgument[] args = ((ICPPTemplateInstance) aliasedType).getTemplateArguments();
		assertEquals(1, args.length);
		assertTrue(((ICPPClassType) bindingB[0]).isSameType(args[0].getTypeValue()));
	}

	// template<typename T> class CT {
	//     template<typename T> using A= T;   // nesting level 1
	//     A<int> x;
	// };
	@Test
	public void testPDOMNestedAliasDeclarationNestingLevel() throws Exception {
		IIndexFragmentBinding[] bindingCT = pdom.findBindings(new char[][] { "CT".toCharArray() },
				IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingCT.length);
		assertTrue(bindingCT[0] instanceof ICPPClassTemplate);
		ICPPClassTemplate templateCT = (ICPPClassTemplate) bindingCT[0];

		IField[] fields = templateCT.getFields();
		assertEquals(1, fields.length);
		IField x = fields[0];
		IType xType = x.getType();
		assertTrue(xType instanceof ICPPAliasTemplateInstance);

		ICPPAliasTemplateInstance aliasInstance = (ICPPAliasTemplateInstance) xType;
		ICPPAliasTemplate alias = aliasInstance.getTemplateDefinition();
		ICPPTemplateParameter[] aliasParameters = alias.getTemplateParameters();
		assertEquals(1, aliasParameters.length);
		ICPPTemplateParameter aliasParameterT = aliasParameters[0];
		assertEquals(1, aliasParameterT.getTemplateNestingLevel());
	}

	// template<typename T> class CT;
	// template<typename T> using A= CT<T>;  	// nesting level 0
	// template<typename T> class CT {           // nesting level 0
	//     typedef Alias<T> TYPE;
	// };
	@Test
	public void testPDOMAliasDeclarationNestingLevel() throws Exception {
		assertDeclarationCount(pdom, "A", 1);
		IIndexFragmentBinding[] bindingA = pdom.findBindings(new char[][] { { 'A' } }, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingA.length);
		assertTrue(bindingA[0] instanceof ICPPAliasTemplate);
		ICPPAliasTemplate aliasA = (ICPPAliasTemplate) bindingA[0];
		ICPPTemplateParameter[] aliasParameters = aliasA.getTemplateParameters();
		assertEquals(1, aliasParameters.length);

		assertTrue(aliasParameters[0] instanceof ICPPTemplateTypeParameter);
		ICPPTemplateTypeParameter templateParameterT = (ICPPTemplateTypeParameter) aliasParameters[0];
		assertEquals("T", templateParameterT.getName());
		assertNull(templateParameterT.getDefault());
		assertEquals(0, templateParameterT.getTemplateNestingLevel());

		assertDeclarationCount(pdom, "CT", 2);
		IIndexFragmentBinding[] bindingCT = pdom.findBindings(new char[][] { "CT".toCharArray() },
				IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, bindingCT.length);
		assertTrue(bindingCT[0] instanceof ICPPClassTemplate);
		ICPPClassTemplate templateCT = (ICPPClassTemplate) bindingCT[0];
		ICPPTemplateParameter[] ctParameters = templateCT.getTemplateParameters();
		assertEquals(1, ctParameters.length);

		assertTrue(ctParameters[0] instanceof ICPPTemplateTypeParameter);
		ICPPTemplateTypeParameter templateParameterTofCT = (ICPPTemplateTypeParameter) ctParameters[0];
		assertEquals("T", templateParameterTofCT.getName());
		assertNull(templateParameterTofCT.getDefault());
		assertEquals(0, templateParameterTofCT.getTemplateNestingLevel());
	}

	protected void assertArrayOfArrayEquals(char[][] c1, char[][] c2) {
		if (c1 == null || c2 == null) {
			assertTrue(c1 == c2);
			return;
		}

		assertEquals(c1.length, c2.length);
		for (int i = 0; i < c1.length; i++) {
			assertArrayEquals(c1[i], c2[i]);
		}
	}
}
