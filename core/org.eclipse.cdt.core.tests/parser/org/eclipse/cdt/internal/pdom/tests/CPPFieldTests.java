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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C++ class data members.
 */
public class CPPFieldTests extends PDOMTestBase {

	protected PDOM pdom;
	protected ICProject project;

	public static Test suite() {
		return suite(CPPFieldTests.class);
	}
	
	protected void setUp() throws Exception {
		project = createProject("fieldTests");
		pdom = (PDOM) CCorePlugin.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}

	public void testFieldDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class1::c1a", 1);
	}
	
	public void testFieldDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class1::c1a", 1);
	}
	
	public void testFieldReferences() throws Exception {
		assertReferenceCount(pdom, "Class1::c1a", 4);
	}
	
	public void testInheritedFieldDeclarations() throws Exception {
		assertDeclarationCount(pdom, "Class2::c1a", 0);
	}
	
	public void testInheritedFieldDefinitions() throws Exception {
		assertDefinitionCount(pdom, "Class2::c1a", 0);
	}
	
	public void testInheritedFieldReferences() throws Exception {
		assertReferenceCount(pdom, "Class2::c1a", 0);
	}
	
	public void testDefaultPrivateField() throws Exception {
		assertVisibility(pdom, "Class1::defaultField", ICPPMember.v_private);
	}
	
	public void testPrivateField() throws Exception {
		assertVisibility(pdom, "Class1::privateField", ICPPMember.v_private);
	}
	
	public void testProtectedField() throws Exception {
		assertVisibility(pdom, "Class1::protectedField", ICPPMember.v_protected);
	}
	
	public void testPublicField() throws Exception {
		assertVisibility(pdom, "Class1::publicField", ICPPMember.v_public);
	}
	
	public void testMutableField() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::mutableField");
		assertEquals(1, bindings.length);
		ICPPField field = (ICPPField) bindings[0];
		assertTrue(field.isMutable());
	}
	
	public void testStaticField() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "Class1::staticField");
		assertEquals(1, bindings.length);
		ICPPField field = (ICPPField) bindings[0];
		assertTrue(field.isStatic());
	}
	
	public void _testIntField() throws Exception {
		// Type information not yet stored in PDOM.
		assertFieldType(pdom, "Class1::c1a", IBasicType.t_int);
	}

	public void _testDoubleField() throws Exception {
		// Type information not yet stored in PDOM.
		assertFieldType(pdom, "Class1::c1b", IBasicType.t_double);
	}

	public void _testCharField() throws Exception {
		// Type information not yet stored in PDOM.
		assertFieldType(pdom, "Class2::c2a", IBasicType.t_char);
	}

	public void _testFloatField() throws Exception {
		// Type information not yet stored in PDOM.
		assertFieldType(pdom, "Class2::c2b", IBasicType.t_float);
	}

	private void assertFieldType(PDOM pdom, String name, int type) throws CoreException, DOMException {
		IBinding[] bindings = findQualifiedName(pdom, name);
		assertEquals(1, bindings.length);
		IField field = (IField) bindings[0];
		assertEquals(type, ((IBasicType) field.getType()).getType());
	}
}
