/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.junit.jupiter.api.Test;

/**
 * @author bnicolle
 */
public class IStructureTests extends IntegratedCModelTest {
	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileResource() {
		return "IStructure.cpp";
	}

	@Test
	public void testGetChildrenOfTypeStruct() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		String[] myExpectedStructs = { "testStruct1", "testStruct2", "testStruct3", "testStruct4NoSemicolon",
				/* 2 anonymous structs */ "", "", "testStruct7", "testStruct8" };
		assertEquals(myExpectedStructs.length, arrayStructs.size());
		for (int i = 0; i < myExpectedStructs.length; i++) {
			IStructure myIStruct = (IStructure) arrayStructs.get(i);
			assertNotNull(myIStruct, "Failed on " + i);
			assertEquals(myExpectedStructs[i], myIStruct.getElementName());
		}
	}

	@Test
	public void testGetChildrenOfTypeClass() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayClasses = tu.getChildrenOfType(ICElement.C_CLASS);
		String[] myExpectedClasses = { "testClass1", "testClass2NoSemicolon", "testClass3", "testClass4Abstract",
				"testClass5", "testClass6" };
		assertEquals(myExpectedClasses.length, arrayClasses.size());
		for (int i = 0; i < myExpectedClasses.length; i++) {
			IStructure myIStruct = (IStructure) arrayClasses.get(i);
			assertNotNull(myIStruct, "Failed on " + i);
			assertEquals(myExpectedClasses[i], myIStruct.getElementName());
		}
	}

	@Test
	public void testGetFields() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		IField[] myArrayIField = myIStruct.getFields();
		String[] myExpectedFields = { "m_field1", "m_field2", "m_field3", "m_field4", "m_field5", "m_field6", };
		assertEquals(myExpectedFields.length, myArrayIField.length);
		for (int i = 0; i < myArrayIField.length; i++) {
			assertNotNull(myArrayIField[i], "Failed on " + i);
			assertEquals(myExpectedFields[i], myArrayIField[i].getElementName(), "Failed on " + i);
		}
	}

	// TODO Bug# 38985: remove testGetFieldsHack()
	@Test
	public void testGetFieldsHack() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		String[] myExpectedFields = { "m_field1", "m_field2", "m_field3", "m_field4", "m_field5", "m_field6", };
		List myArrayIField = myIStruct.getChildrenOfType(ICElement.C_FIELD);
		assertEquals(myExpectedFields.length, myArrayIField.size());
		for (int i = 0; i < myArrayIField.size(); i++) {
			IField myIField = (IField) myArrayIField.get(i);
			assertNotNull(myIField, "Failed on " + i);
			assertEquals(myExpectedFields[i], myIField.getElementName(), "Failed on " + i);
		}
	}

	@Test
	public void testGetField() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		String[] myExpectedFields = { "m_field1", "m_field2", "m_field3", "m_field4", "m_field5", "m_field6", };
		for (int i = 0; i < myExpectedFields.length; i++) {
			IField myIField = myIStruct.getField(myExpectedFields[i]);
			assertNotNull(myIField, "Failed on " + i);
		}

		String[] myUnexpectedFields = { "m_field7", "m_field8", "m_field9", };
		for (int i = 0; i < myUnexpectedFields.length; i++) {
			IField myIField = myIStruct.getField(myUnexpectedFields[i]);
			assertNull(myIField, "Failed on " + i);
		}
	}

	@Test
	public void testGetMethods() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		IMethodDeclaration[] myArrayIMethod = myIStruct.getMethods();
		String[] myExpectedMethods = { "method1", "method2", "testStruct1", "~testStruct1" };
		assertEquals(myExpectedMethods.length, myArrayIMethod.length);
		for (int i = 0; i < myArrayIMethod.length; i++) {
			assertNotNull(myArrayIMethod[i], "Failed on " + i);
			assertEquals(myExpectedMethods[i], myArrayIMethod[i].getElementName(), "Failed on " + i);
		}
	}

	// TODO Bug# 38985: remove testGetMethodsHack()
	@Test
	public void testGetMethodsHack() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		List myArrayIMethod = myIStruct.getChildrenOfType(ICElement.C_METHOD_DECLARATION);
		myArrayIMethod.addAll(myIStruct.getChildrenOfType(ICElement.C_METHOD));
		String[] myExpectedMethods = { "method1", "method2", "testStruct1", "~testStruct1" };
		assertEquals(myExpectedMethods.length, myArrayIMethod.size());
		for (int i = 0; i < myArrayIMethod.size(); i++) {
			IMethodDeclaration myIMethod = (IMethodDeclaration) myArrayIMethod.get(i);
			assertNotNull(myIMethod, "Failed on " + i);
			assertEquals(myExpectedMethods[i], myIMethod.getElementName(), "Failed on " + i);
		}
	}

	@Test
	public void testGetMethod() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		String[] myExpectedMethods = { "method1", "method2", "testStruct1", "~testStruct1" };
		for (int i = 0; i < myExpectedMethods.length; i++) {
			IMethodDeclaration myIMethod = myIStruct.getMethod(myExpectedMethods[i]);
			assertNotNull(myIMethod, "Failed on " + i);
		}

		String[] myUnexpectedMethods = { "method7", "method8", "method9", };
		for (int i = 0; i < myUnexpectedMethods.length; i++) {
			IMethodDeclaration myIMethod = myIStruct.getMethod(myUnexpectedMethods[i]);
			assertNull(myIMethod);
		}
	}

	@Test
	public void testIsUnion() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementUnion = null;
		ICElement myElementNonUnion = null;

		myElementUnion = tu.getElement("testUnion1");
		myElementNonUnion = tu.getElement("testStruct1");

		assertNotNull(myElementUnion);
		assertTrue(myElementUnion.getElementType() == ICElement.C_UNION);
		IStructure myStructUnion = (IStructure) myElementUnion;
		assertNotNull(myStructUnion);
		assertTrue(myStructUnion.isUnion());

		assertNotNull(myElementNonUnion);
		assertTrue(myElementNonUnion.getElementType() != ICElement.C_UNION);
		IStructure myStructNonUnion = (IStructure) myElementNonUnion;
		assertNotNull(myStructNonUnion);
		assertFalse(myStructNonUnion.isUnion());
	}

	@Test
	public void testIsStruct() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementStruct = null;
		ICElement myElementNonStruct = null;
		myElementStruct = tu.getElement("testStruct1");
		myElementNonStruct = tu.getElement("testClass1");

		assertNotNull(myElementStruct);
		assertTrue(myElementStruct.getElementType() == ICElement.C_STRUCT);
		IStructure myStructStruct = (IStructure) myElementStruct;
		assertNotNull(myStructStruct);
		assertTrue(myStructStruct.isStruct());

		assertNotNull(myElementNonStruct);
		assertTrue(myElementNonStruct.getElementType() != ICElement.C_STRUCT);
		IStructure myStructNonStruct = (IStructure) myElementNonStruct;
		assertNotNull(myStructNonStruct);
		assertFalse(myStructNonStruct.isStruct());
	}

	@Test
	public void testIsClass() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementClass = null;
		ICElement myElementNonClass = null;

		myElementClass = tu.getElement("testClass1");
		myElementNonClass = tu.getElement("testStruct1");

		assertNotNull(myElementClass);
		assertTrue(myElementClass.getElementType() == ICElement.C_CLASS);
		IStructure myStructClass = (IStructure) myElementClass;
		assertNotNull(myStructClass);
		assertTrue(myStructClass.isClass());

		assertNotNull(myElementNonClass);
		assertTrue(myElementNonClass.getElementType() != ICElement.C_CLASS);
		IStructure myStructNonClass = (IStructure) myElementNonClass;
		assertNotNull(myStructNonClass);
		assertFalse(myStructNonClass.isClass());
	}

	@Test
	public void testIsAbstract() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementAbstract = null;
		ICElement myElementNonAbstract = null;

		myElementAbstract = tu.getElement("testClass4Abstract");
		myElementNonAbstract = tu.getElement("testClass1");

		assertNotNull(myElementAbstract);
		assertTrue(myElementAbstract.getElementType() == ICElement.C_CLASS);
		IStructure myStructAbstract = (IStructure) myElementAbstract;
		assertNotNull(myStructAbstract);
		assertTrue(myStructAbstract.isAbstract());

		assertNotNull(myElementNonAbstract);
		assertTrue(myElementNonAbstract.getElementType() == ICElement.C_CLASS);
		IStructure myStructNonAbstract = (IStructure) myElementNonAbstract;
		assertNotNull(myStructNonAbstract);
		assertFalse(myStructNonAbstract.isAbstract());
	}

	// IInheritance
	@Test
	public void testGetBaseTypes() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementDerived = null;
		String[] myBaseTypes = null;

		myElementDerived = tu.getElement("testClass5"); // throws
		assertNotNull(myElementDerived);
		assertTrue(myElementDerived.getElementType() == ICElement.C_CLASS);
		IStructure myStructDerived = (IStructure) myElementDerived;
		assertNotNull(myStructDerived);
		myBaseTypes = myStructDerived.getSuperClassesNames();

		String[] myExpectedBaseTypes = { "testClass1", "testClass3", "testClass4Abstract" };
		assertEquals(myExpectedBaseTypes.length, myBaseTypes.length);
		for (int i = 0; i < myBaseTypes.length; i++) {
			assertEquals(myExpectedBaseTypes[i], myBaseTypes[i]);
		}
	}

	// IInheritance
	@Test
	public void testGetAccessControl() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementDerived = null;
		String[] myBaseTypes = null;

		myElementDerived = tu.getElement("testClass5"); // throws
		assertNotNull(myElementDerived);
		assertTrue(myElementDerived.getElementType() == ICElement.C_CLASS);
		IStructure myStructDerived = (IStructure) myElementDerived;
		assertNotNull(myStructDerived);
		myBaseTypes = myStructDerived.getSuperClassesNames();

		ASTAccessVisibility[] myExpectedAccessControl = {
				// TODO #38986: expect appropriate access control tags
				ASTAccessVisibility.PUBLIC, ASTAccessVisibility.PROTECTED, ASTAccessVisibility.PRIVATE };
		assertEquals(myExpectedAccessControl.length, myBaseTypes.length);
		for (int i = 0; i < myBaseTypes.length; i++) {
			ASTAccessVisibility myAccessControl = myStructDerived.getSuperClassAccess(myBaseTypes[i]);
			assertEquals(myExpectedAccessControl[i], myAccessControl, "Failed on " + i);
		}

	}

	// getStructureInfo
	@Test
	public void testGetStructureInfo() {
	}

	// TODO: Not tested; Bug# 38958. public void testGetInitializer()
	// TODO: Not tested; Bug# 38958. public void testGetTypeName()
	// TODO: Not tested; Bug# 38958. public void testIsConst()
	// TODO: Not tested; Bug# 38958. public void testIsStatic()
	// TODO: Not tested; Bug# 38958. public void testIsVolatile()
	// TODO: Not tested; Bug# 38958. public void testGetAccessControl_Void()

	//
	// Language Specification Tests
	//

	@Test
	public void testAnonymousStructObject() throws CModelException {
		ITranslationUnit tu = getTU();

		ICElement myElement = tu.getElement("testAnonymousStructObject1");

		assertNotNull(myElement);
		assertEquals(ICElement.C_VARIABLE, myElement.getElementType());
	}

	@Test
	public void testInnerStruct() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElement = null;

		myElement = tu.getElement("testStruct8");

		assertNotNull(myElement);
		IStructure myIStruct = (IStructure) myElement;
		assertNotNull(myIStruct);

		String[] myExpectedInnerStructs = { "testStruct9Inner", "testStruct10Inner" };
		List myInnerStructs = myIStruct.getChildrenOfType(ICElement.C_STRUCT);
		assertEquals(myExpectedInnerStructs.length, myInnerStructs.size());
		for (int i = 0; i < myExpectedInnerStructs.length; i++) {
			IStructure myInnerStruct = (IStructure) myInnerStructs.get(i);
			assertNotNull(myInnerStruct, "Failed on " + i);
			assertEquals(myExpectedInnerStructs[i], myInnerStruct.getElementName(), "Failed on " + i);
		}
	}
}
