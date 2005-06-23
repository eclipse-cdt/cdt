/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 9, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import org.eclipse.cdt.core.model.*;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

import junit.framework.*;

import java.util.List;

/**
 * @author bnicolle
 *
 */
public class IStructureTests extends IntegratedCModelTest {
	/**
	 * @param name
	 */
	public IStructureTests(String name) {
		super(name);
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileResource() {
		return "IStructure.c";
	}
	
	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite( IStructureTests.class.getName() );
		
		// TODO check C-only behaviour using C_NATURE vs CC_NATURE
		
		// Interface tests:
		suite.addTest( new IStructureTests("testGetChildrenOfTypeStruct"));
		suite.addTest( new IStructureTests("testGetChildrenOfTypeClass")); // C++ only
		suite.addTest( new IStructureTests("testGetFields"));
		//Bug# 38985: solved. suite.addTest( new IStructureTests("testGetFieldsHack"));
		suite.addTest( new IStructureTests("testGetField"));
		suite.addTest( new IStructureTests("testGetMethods")); // C++ only
		//Bug# 38985: solved. suite.addTest( new IStructureTests("testGetMethodsHack")); // C++ only
		suite.addTest( new IStructureTests("testGetMethod")); // C++ only
		suite.addTest( new IStructureTests("testIsStruct"));
		suite.addTest( new IStructureTests("testIsClass")); // C++ only
		suite.addTest( new IStructureTests("testIsUnion"));
		suite.addTest( new IStructureTests("testIsAbstract")); // C++ only
		suite.addTest( new IStructureTests("testGetBaseTypes")); // C++ only
		suite.addTest( new IStructureTests("testGetAccessControl")); // C++ only
		
		// Language Specification tests:		
		suite.addTest( new IStructureTests("testAnonymousStructObject"));
		suite.addTest( new IStructureTests("testInnerStruct"));
				
		return suite;
	}

	public void testGetChildrenOfTypeStruct() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		String[] myExpectedStructs = {
			"testStruct1", "testStruct2", "testStruct3",
			 /* 2 anonymous structs */ "", "", "testStruct7",
			 "testStruct8"
		};
		assertEquals(myExpectedStructs.length,arrayStructs.size());
		for(int i=0; i<myExpectedStructs.length; i++) {
			IStructure myIStruct = (IStructure) arrayStructs.get(i);
			assertNotNull( "Failed on "+i, myIStruct);
			assertEquals(myExpectedStructs[i], myIStruct.getElementName());
		}
	}
	public void testGetChildrenOfTypeClass() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayClasses = tu.getChildrenOfType(ICElement.C_CLASS);
		String[] myExpectedClasses = {
			"testClass1", "testClass3", "testClass4Abstract",
			"testClass5", "testClass6" };
		assertEquals(myExpectedClasses.length,arrayClasses.size());
		for(int i=0; i<myExpectedClasses.length; i++) {
			IStructure myIStruct = (IStructure) arrayClasses.get(i);
			assertNotNull( "Failed on "+i, myIStruct);
			assertEquals(myExpectedClasses[i], myIStruct.getElementName());
		}
	}

	public void testGetFields() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		IField[] myArrayIField = myIStruct.getFields();
		String[] myExpectedFields = {
			"m_field1","m_field2","m_field3",
			"m_field4","m_field5","m_field6",
		};
		assertEquals(myExpectedFields.length, myArrayIField.length);
		for(int i=0; i<myArrayIField.length; i++) {
			assertNotNull( "Failed on "+i, myArrayIField[i]);
			assertEquals("Failed on "+i,
				myExpectedFields[i], myArrayIField[i].getElementName());
		}		
	}

	// TODO Bug# 38985: remove testGetFieldsHack()
	public void testGetFieldsHack() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		String[] myExpectedFields = {
			"m_field1","m_field2","m_field3",
			"m_field4","m_field5","m_field6",
		};
		List myArrayIField = myIStruct.getChildrenOfType(ICElement.C_FIELD);
		assertEquals(myExpectedFields.length, myArrayIField.size());
		for(int i=0; i<myArrayIField.size(); i++) {
			IField myIField = (IField) myArrayIField.get(i);
			assertNotNull( "Failed on "+i, myIField );
			assertEquals("Failed on "+i,
				myExpectedFields[i], myIField.getElementName());
		}		
	}
	public void testGetField() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		String[] myExpectedFields = {
			"m_field1","m_field2","m_field3",
			"m_field4","m_field5","m_field6",
		};
		for(int i=0; i<myExpectedFields.length; i++) {
			IField myIField = myIStruct.getField( myExpectedFields[i] );
			assertNotNull( "Failed on "+i, myIField);
		}		
		
		String[] myUnexpectedFields = {
			"m_field7","m_field8","m_field9",
		};
		for(int i=0; i<myUnexpectedFields.length; i++) {
			IField myIField = myIStruct.getField( myUnexpectedFields[i] );
			assertNull( "Failed on "+i, myIField);
		}		
	}
	public void testGetMethods() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		IMethodDeclaration[] myArrayIMethod = myIStruct.getMethods();
		String[] myExpectedMethods = {
			"method1","method2","testStruct1","~testStruct1"
		};
		assertEquals(myExpectedMethods.length, myArrayIMethod.length);
		for(int i=0; i<myArrayIMethod.length; i++) {
			assertNotNull( "Failed on "+i, myArrayIMethod[i]);
			assertEquals("Failed on "+i,
				myExpectedMethods[i], myArrayIMethod[i].getElementName());
		}		
	}
	// TODO Bug# 38985: remove testGetMethodsHack()
	public void testGetMethodsHack() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		List myArrayIMethod = myIStruct.getChildrenOfType(ICElement.C_METHOD_DECLARATION);
		myArrayIMethod.addAll( myIStruct.getChildrenOfType(ICElement.C_METHOD) );
		String[] myExpectedMethods = {
			"method1","method2","testStruct1","~testStruct1"
		};
		assertEquals(myExpectedMethods.length, myArrayIMethod.size());
		for(int i=0; i<myArrayIMethod.size(); i++) {
			IMethodDeclaration myIMethod = (IMethodDeclaration) myArrayIMethod.get(i);
			assertNotNull( "Failed on "+i, myIMethod);
			assertEquals("Failed on "+i,
				myExpectedMethods[i], myIMethod.getElementName());
		}		
	}
	public void testGetMethod() throws CModelException {
		ITranslationUnit tu = getTU();
		List myArrayStructs = tu.getChildrenOfType(ICElement.C_STRUCT);
		IStructure myIStruct = (IStructure) myArrayStructs.get(0);
		String[] myExpectedMethods = {
			"method1","method2","testStruct1","~testStruct1"
		};
		for(int i=0; i<myExpectedMethods.length; i++) {
			IMethodDeclaration myIMethod = myIStruct.getMethod( myExpectedMethods[i] );
			assertNotNull( "Failed on "+i, myIMethod);
		}		
		
		String[] myUnexpectedMethods = {
			"method7","method8","method9",
		};
		for(int i=0; i<myUnexpectedMethods.length; i++) {
			IMethodDeclaration myIMethod = myIStruct.getMethod( myUnexpectedMethods[i] );
			assertNull( "Failed on "+i, myIMethod);
		}		
	}
	
	public void testIsUnion() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementUnion = null;
		ICElement myElementNonUnion = null;
		try {
			myElementUnion = tu.getElement("testUnion1");
			myElementNonUnion = tu.getElement("testStruct1");
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}
		assertNotNull( myElementUnion );
		assertTrue( myElementUnion.getElementType()==ICElement.C_UNION );		
		IStructure myStructUnion = (IStructure) myElementUnion;
		assertNotNull( myStructUnion );
		assertTrue( myStructUnion.isUnion() );

		assertNotNull( myElementNonUnion );
		assertTrue( myElementNonUnion.getElementType()!=ICElement.C_UNION );		
		IStructure myStructNonUnion = (IStructure) myElementNonUnion;
		assertNotNull( myStructNonUnion );
		assertFalse( myStructNonUnion.isUnion() );
	}
	public void testIsStruct() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementStruct = null;
		ICElement myElementNonStruct = null;
		try {
			myElementStruct = tu.getElement("testStruct1");
			myElementNonStruct = tu.getElement("testClass1");
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}
		assertNotNull( myElementStruct );
		assertTrue( myElementStruct.getElementType()==ICElement.C_STRUCT );		
		IStructure myStructStruct = (IStructure) myElementStruct;
		assertNotNull( myStructStruct );
		assertTrue( myStructStruct.isStruct() );

		assertNotNull( myElementNonStruct );
		assertTrue( myElementNonStruct.getElementType()!=ICElement.C_STRUCT );		
		IStructure myStructNonStruct = (IStructure) myElementNonStruct;
		assertNotNull( myStructNonStruct );
		assertFalse( myStructNonStruct.isStruct() );
	}
	
	public void testIsClass() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementClass = null;
		ICElement myElementNonClass = null;
		try {
			myElementClass = tu.getElement("testClass1");
			myElementNonClass = tu.getElement("testStruct1");
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}
		assertNotNull( myElementClass );
		assertTrue( myElementClass.getElementType()==ICElement.C_CLASS );		
		IStructure myStructClass = (IStructure) myElementClass;
		assertNotNull( myStructClass );
		assertTrue( myStructClass.isClass() );

		assertNotNull( myElementNonClass );
		assertTrue( myElementNonClass.getElementType()!=ICElement.C_CLASS );		
		IStructure myStructNonClass = (IStructure) myElementNonClass;
		assertNotNull( myStructNonClass );
		assertFalse( myStructNonClass.isClass() );
	}
	
	public void testIsAbstract() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElementAbstract = null;
		ICElement myElementNonAbstract = null;
		try {
			myElementAbstract = tu.getElement("testClass4Abstract");
			myElementNonAbstract = tu.getElement("testClass1");
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}
		assertNotNull( myElementAbstract );
		assertTrue( myElementAbstract.getElementType()==ICElement.C_CLASS );		
		IStructure myStructAbstract = (IStructure) myElementAbstract;
		assertNotNull( myStructAbstract );
		assertTrue( myStructAbstract.isAbstract() );

		assertNotNull( myElementNonAbstract );
		assertTrue( myElementNonAbstract.getElementType() == ICElement.C_CLASS );		
		IStructure myStructNonAbstract = (IStructure) myElementNonAbstract;
		assertNotNull( myStructNonAbstract );
		assertFalse( myStructNonAbstract.isAbstract() );
	}

	// IInheritance
	public void testGetBaseTypes() {
		ITranslationUnit tu = getTU();
		ICElement myElementDerived = null;
		String[] myBaseTypes = null;
		try {
			myElementDerived = tu.getElement("testClass5"); // throws
			assertNotNull( myElementDerived );
			assertTrue( myElementDerived.getElementType()==ICElement.C_CLASS );		
			IStructure myStructDerived = (IStructure) myElementDerived;
			assertNotNull( myStructDerived );
			myBaseTypes = myStructDerived.getSuperClassesNames();
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}
		
		String[] myExpectedBaseTypes = {
			"testClass1","testClass3","testClass4Abstract"
		};
		assertEquals( myExpectedBaseTypes.length, myBaseTypes.length );
		for(int i=0; i<myBaseTypes.length; i++) {
			assertEquals( "Failed on "+i, myExpectedBaseTypes[i], myBaseTypes[i] );
		}		
	}

	// IInheritance
	public void testGetAccessControl() {
		ITranslationUnit tu = getTU();
		ICElement myElementDerived = null;
		String[] myBaseTypes = null;
		try {
			myElementDerived = tu.getElement("testClass5"); // throws
			assertNotNull( myElementDerived );
			assertTrue( myElementDerived.getElementType()==ICElement.C_CLASS );		
			IStructure myStructDerived = (IStructure) myElementDerived;
			assertNotNull( myStructDerived );
			myBaseTypes = myStructDerived.getSuperClassesNames();
		
			ASTAccessVisibility[] myExpectedAccessControl = {
				// TODO #38986: expect appropriate access control tags 
				ASTAccessVisibility.PUBLIC,
				ASTAccessVisibility.PROTECTED,
				ASTAccessVisibility.PRIVATE
			};
			assertEquals( myExpectedAccessControl.length, myBaseTypes.length );
			for(int i=0; i<myBaseTypes.length; i++) {
				ASTAccessVisibility myAccessControl = myStructDerived.getSuperClassAccess(myBaseTypes[i]);
				assertEquals( "Failed on "+i, myExpectedAccessControl[i], myAccessControl );
			}		
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}
	}

	// getStructureInfo
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
	
	public void testAnonymousStructObject() {
		ITranslationUnit tu = getTU();
		ICElement myElement = null;
		try {
			myElement = tu.getElement("testAnonymousStructObject1");
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}
		assertNotNull( myElement );
		assertEquals( ICElement.C_VARIABLE, myElement.getElementType() );
	}
	
	public void testInnerStruct() throws CModelException {
		ITranslationUnit tu = getTU();
		ICElement myElement = null;
		try {
			myElement = tu.getElement("testStruct8");
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}
		assertNotNull( myElement );
		IStructure myIStruct = (IStructure) myElement;
		assertNotNull( myIStruct );

		String[] myExpectedInnerStructs = {
			"testStruct9Inner", "testStruct10Inner"
		};
		List myInnerStructs = myIStruct.getChildrenOfType(ICElement.C_STRUCT);
		assertEquals( myExpectedInnerStructs.length, myInnerStructs.size() );
		for(int i=0; i<myExpectedInnerStructs.length; i++) {
			IStructure myInnerStruct = (IStructure) myInnerStructs.get(i);
			assertNotNull( "Failed on "+i, myInnerStruct );
			assertEquals( "Failed on "+i, myExpectedInnerStructs[i], myInnerStruct.getElementName() );
		}		
	}
}
