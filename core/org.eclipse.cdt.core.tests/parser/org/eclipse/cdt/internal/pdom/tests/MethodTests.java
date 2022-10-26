/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C++ class member functions.
 */
public class MethodTests extends PDOMTestBase {
	protected ICProject project;
	protected PDOM pdom;

	@BeforeEach
	protected void beforeEach() throws Exception {
		project = createProject("methodTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	@AfterEach
	protected void afterEach() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			CProjectHelper.delete(project);
			project = null;
		}
	}

	@Test
	public void testMethodParameters() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::normalMethod");
		assertEquals(1, bindings.length);
		ICPPFunction function = (ICPPFunction) bindings[0];
		IParameter[] parameters = function.getParameters();
		assertEquals(IBasicType.t_int, ((ICPPBasicType) parameters[0].getType()).getType());
		assertEquals("p1", parameters[0].getName());
		assertEquals(IBasicType.t_char, ((ICPPBasicType) parameters[1].getType()).getType());
		assertEquals("p2", parameters[1].getName());
		assertEquals(IBasicType.t_float, ((ICPPBasicType) parameters[2].getType()).getType());
		assertEquals("p3", parameters[2].getName());
	}

	@Test
	public void testVirtualMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::inheritedMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isVirtual());
	}

	@Test
	public void testVirtualMethodType() throws Exception {
		assertType(pdom, "Class1::inheritedMethod", ICPPFunction.class);
	}

	@Test
	public void testVirtualMethodDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class1::inheritedMethod", 2);
	}

	@Test
	public void testVirtualMethodDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class1::inheritedMethod", 1);
	}

	@Test
	public void testVirtualMethodReferences() throws Exception {
		assertReferenceCount(pdom, "Class1::inheritedMethod", 3);
	}

	@Test
	public void testInheritedMethodType() throws Exception {
		assertEquals(0, findQualifiedName(pdom, "Class2::inheritedMethod").length);
	}

	@Test
	public void testInheritedMethodDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class2::inheritedMethod", 0);
	}

	@Test
	public void testInheritedMethodDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class2::inheritedMethod", 0);
	}

	@Test
	public void testInheritedMethodReferences() throws Exception {
		assertReferenceCount(pdom, "Class2::inheritedMethod", 0);
	}

	@Test
	public void testPureVirtualMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::pureVirtualMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isVirtual());
		assertTrue(method.isPureVirtual());
	}

	@Test
	public void testPureVirtualMethodType() throws Exception {
		assertType(pdom, "Class1::pureVirtualMethod", ICPPFunction.class);
		assertType(pdom, "Class2::pureVirtualMethod", ICPPFunction.class);
	}

	@Test
	public void testPureVirtualMethodDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class1::pureVirtualMethod", 1);
		assertDeclarationCount(pdom, "Class2::pureVirtualMethod", 2);
	}

	@Test
	public void testPureVirtualMethodDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class1::pureVirtualMethod", 0);
		assertDefinitionCount(pdom, "Class2::pureVirtualMethod", 1);
	}

	@Test
	public void testPureVirtualMethodReferences() throws Exception {
		assertReferenceCount(pdom, "Class1::pureVirtualMethod", 2);
		assertReferenceCount(pdom, "Class2::pureVirtualMethod", 3);
	}

	@Test
	public void testOverriddenMethodType() throws Exception {
		assertType(pdom, "Class1::overriddenMethod", ICPPFunction.class);
		assertType(pdom, "Class2::overriddenMethod", ICPPFunction.class);
	}

	@Test
	public void testOverriddenMethodDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class1::overriddenMethod", 2);
		assertDeclarationCount(pdom, "Class2::overriddenMethod", 2);
	}

	@Test
	public void testOverriddenMethodDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class1::overriddenMethod", 1);
		assertDefinitionCount(pdom, "Class2::overriddenMethod", 1);
	}

	@Test
	public void testOverriddenMethodReferences() throws Exception {
		assertReferenceCount(pdom, "Class1::overriddenMethod", 3);
		assertReferenceCount(pdom, "Class2::overriddenMethod", 4);
	}

	@Test
	public void testDestructor() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::~Class1");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isDestructor());
	}

	@Test
	public void testDefaultPrivateMethod() throws Exception {
		assertCPPMemberVisibility(pdom, "Class3::defaultMethod", ICPPMember.v_private);
	}

	@Test
	public void testPrivateMethod() throws Exception {
		assertCPPMemberVisibility(pdom, "Class3::privateMethod", ICPPMember.v_private);
	}

	@Test
	public void testProtectedMethod() throws Exception {
		assertCPPMemberVisibility(pdom, "Class3::protectedMethod", ICPPMember.v_protected);
	}

	@Test
	public void testPublicMethod() throws Exception {
		assertCPPMemberVisibility(pdom, "Class3::publicMethod", ICPPMember.v_public);
	}

	@Test
	public void testInlineMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::inlineMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isInline());
	}

	@Test
	public void testStaticMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::staticMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isStatic());
	}

	@Test
	public void testVarArgsMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::varArgsMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.takesVarArgs());
	}

	@Test
	public void testConstMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::constMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = method.getType();
		assertTrue(type.isConst());
	}

	@Test
	public void testVolatileMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::volatileMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = method.getType();
		assertTrue(type.isVolatile());
	}

	@Test
	public void testConstVolatileMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::constVolatileMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = method.getType();
		assertTrue(type.isConst());
		assertTrue(type.isVolatile());
	}

	@Test
	public void testNotConstMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::notConstMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = method.getType();
		assertFalse(type.isConst());
	}

	@Test
	public void testNotVolatileMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::notVolatileMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = method.getType();
		assertFalse(type.isVolatile());
	}

	@Test
	public void testNotConstVolatileMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::notConstVolatileMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = method.getType();
		assertFalse(type.isConst());
		assertFalse(type.isVolatile());
	}

	@Test
	public void testNoExceptionSpecification() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::noExceptionSpecMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		IType[] exceptionSpec = method.getExceptionSpecification();
		assertNull(exceptionSpec);
	}

	@Test
	public void testEmptyExceptionSpecification() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::emptyExceptionSpecMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		IType[] exceptionSpec = method.getExceptionSpecification();
		assertEquals(0, exceptionSpec.length);
	}

	@Test
	public void testNonEmptyExceptionSpecification() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::nonEmptyExceptionSpecMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		IType[] exceptionSpec = method.getExceptionSpecification();
		assertEquals(1, exceptionSpec.length);
		assertEquals(IBasicType.t_int, ((ICPPBasicType) exceptionSpec[0]).getType());
	}

	@Test
	public void testImplicitCtorExceptionSpec() throws Exception {
		IBinding[] bindings = findQualifiedPossiblyImplicit(pdom, "D::D");
		// get both default ctor + copy ctor
		assertEquals(2, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		IType[] exceptionSpec = method.getExceptionSpecification();
		assertNull(exceptionSpec);
	}

	@Test
	public void testImplicitCopyCtorExceptionSpec() throws Exception {
		IBinding[] bindings = findQualifiedPossiblyImplicit(pdom, "D::D");
		// get both default ctor + copy ctor
		assertEquals(2, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[1];
		IType[] exceptionSpec = method.getExceptionSpecification();
		assertEquals(0, exceptionSpec.length);
	}

	@Test
	public void testImplicitDtorExceptionSpec() throws Exception {
		IBinding[] bindings = findQualifiedPossiblyImplicit(pdom, "D::~D");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		IType[] exceptionSpec = method.getExceptionSpecification();
		assertEquals(2, exceptionSpec.length);
		int t1 = ((ICPPBasicType) exceptionSpec[0]).getType();
		int t2 = ((ICPPBasicType) exceptionSpec[1]).getType();
		assertEquals(IBasicType.t_int, Math.min(t1, t2));
		assertEquals(IBasicType.t_double, Math.max(t1, t2));
	}

	@Test
	public void testVirtualMemberFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "E::virtualMemberFunction");
		assertEquals(1, bindings.length);
		assertInstanceOf(ICPPMethod.class, bindings[0]);
		ICPPMethod virtMemFun = (ICPPMethod) bindings[0];
		assertFalse(virtMemFun.isOverride());
		assertFalse(virtMemFun.isFinal());
	}

	@Test
	public void testOverrideVirtualMemberFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "F::virtualMemberFunction");
		assertEquals(1, bindings.length);
		assertInstanceOf(ICPPMethod.class, bindings[0]);
		ICPPMethod virtMemFun = (ICPPMethod) bindings[0];
		assertTrue(virtMemFun.isOverride());
		assertFalse(virtMemFun.isFinal());
	}

	@Test
	public void testOverrideFinalVirtualMemberFunction() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "G::virtualMemberFunction");
		assertEquals(1, bindings.length);
		assertInstanceOf(ICPPMethod.class, bindings[0]);
		ICPPMethod virtMemFun = (ICPPMethod) bindings[0];
		assertTrue(virtMemFun.isOverride());
		assertTrue(virtMemFun.isFinal());
	}
}
