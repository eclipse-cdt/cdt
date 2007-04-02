/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C++ class member functions.
 */
public class MethodTests extends PDOMTestBase {
	protected ICProject project;
	protected PDOM pdom;

	public static Test suite() {
		return suite(MethodTests.class);
	}

	protected void setUp() throws Exception {
		project = createProject("methodTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			CProjectHelper.delete(project);
			project= null;
		}
	}

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
	
	public void testVirtualMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::inheritedMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isVirtual());
	}
	
	public void testVirtualMethodType() throws Exception {
		assertType(pdom, "Class1::inheritedMethod", ICPPFunction.class);
	}

	public void testVirtualMethodDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class1::inheritedMethod", 2);
	}

	public void testVirtualMethodDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class1::inheritedMethod", 1);
	}

	public void testVirtualMethodReferences() throws Exception {
		assertReferenceCount(pdom, "Class1::inheritedMethod", 3);
	}

	public void testInheritedMethodType() throws Exception {
		assertEquals(0, findQualifiedName(pdom, "Class2::inheritedMethod").length);
	}

	public void testInheritedMethodDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class2::inheritedMethod", 0);
	}

	public void testInheritedMethodDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class2::inheritedMethod", 0);
	}

	public void testInheritedMethodReferences() throws Exception {
		assertReferenceCount(pdom, "Class2::inheritedMethod", 0);
	}

	public void testPureVirtualMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::pureVirtualMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isVirtual());
	}
	
	public void testPureVirtualMethodType() throws Exception {
		assertType(pdom, "Class1::pureVirtualMethod", ICPPFunction.class);
		assertType(pdom, "Class2::pureVirtualMethod", ICPPFunction.class);
	}

	public void testPureVirtualMethodDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class1::pureVirtualMethod", 1);
		assertDeclarationCount(pdom, "Class2::pureVirtualMethod", 2);
	}

	public void testPureVirtualMethodDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class1::pureVirtualMethod", 0);
		assertDefinitionCount(pdom, "Class2::pureVirtualMethod", 1);
	}

	public void testPureVirtualMethodReferences() throws Exception {
		assertReferenceCount(pdom, "Class1::pureVirtualMethod", 2);
		assertReferenceCount(pdom, "Class2::pureVirtualMethod", 3);
	}

	public void testOverriddenMethodType() throws Exception {
		assertType(pdom, "Class1::overriddenMethod", ICPPFunction.class);
		assertType(pdom, "Class2::overriddenMethod", ICPPFunction.class);
	}

	public void testOverriddenMethodDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class1::overriddenMethod", 2);
		assertDeclarationCount(pdom, "Class2::overriddenMethod", 2);
	}

	public void testOverriddenMethodDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class1::overriddenMethod", 1);
		assertDefinitionCount(pdom, "Class2::overriddenMethod", 1);
	}

	public void testOverriddenMethodReferences() throws Exception {
		assertReferenceCount(pdom, "Class1::overriddenMethod", 3);
		assertReferenceCount(pdom, "Class2::overriddenMethod", 4);
	}

	public void testDestructor() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::~Class1");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isDestructor());
	}
	
	public void testDefaultPrivateMethod() throws Exception {
		assertVisibility(pdom, "Class3::defaultMethod", ICPPMember.v_private);
	}
	
	public void testPrivateMethod() throws Exception {
		assertVisibility(pdom, "Class3::privateMethod", ICPPMember.v_private);
	}
	
	public void testProtectedMethod() throws Exception {
		assertVisibility(pdom, "Class3::protectedMethod", ICPPMember.v_protected);
	}
	
	public void testPublicMethod() throws Exception {
		assertVisibility(pdom, "Class3::publicMethod", ICPPMember.v_public);
	}
	
	public void testInlineMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::inlineMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isInline());
	}
	
	public void testStaticMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::staticMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.isStatic());
	}
	
	public void testVarArgsMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::varArgsMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		assertTrue(method.takesVarArgs());
	}
	
	public void testConstMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::constMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = (ICPPFunctionType) method.getType();
		assertTrue(type.isConst());
	}
	
	public void testVolatileMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::volatileMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = (ICPPFunctionType) method.getType();
		assertTrue(type.isVolatile());
	}
		
	public void testConstVolatileMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::constVolatileMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = (ICPPFunctionType) method.getType();
		assertTrue(type.isConst());
		assertTrue(type.isVolatile());
	}
	
	public void testNotConstMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::notConstMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = (ICPPFunctionType) method.getType();
		assertFalse(type.isConst());
	}
	
	public void testNotVolatileMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::notVolatileMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = (ICPPFunctionType) method.getType();
		assertFalse(type.isVolatile());
	}
		
	public void testNotConstVolatileMethod() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::notConstVolatileMethod");
		assertEquals(1, bindings.length);
		ICPPMethod method = (ICPPMethod) bindings[0];
		ICPPFunctionType type = (ICPPFunctionType) method.getType();
		assertFalse(type.isConst());
		assertFalse(type.isVolatile());
	}

}
