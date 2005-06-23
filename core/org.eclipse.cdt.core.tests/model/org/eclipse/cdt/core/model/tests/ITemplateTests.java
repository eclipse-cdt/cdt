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
 * Created on Jun 17, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import org.eclipse.cdt.core.model.*;

import junit.framework.*;
import java.util.ArrayList;
import java.util.List;



/**
 * Class for testing ITemplate interface
 * @author bnicolle
 *
 */
public class ITemplateTests extends IntegratedCModelTest {
	/**
	 * @param name
	 */
	public ITemplateTests(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IntegratedCModelTest#getSourcefileSubdir()
	 */
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IntegratedCModelTest#getSourcefileResource()
	 */
	public String getSourcefileResource() {
		return "ITemplate.cpp";
	}
	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite( IStructureTests.class.getName() );
		
		// Interface tests:
		suite.addTest( new ITemplateTests("testGetChildrenOfTypeTemplate"));
		suite.addTest( new ITemplateTests("testGetNumberOfTemplateParameters"));
		suite.addTest( new ITemplateTests("testGetTemplateParameterTypes"));
		suite.addTest( new ITemplateTests("testGetTemplateSignature"));
		
		// Language Specification tests:		
		// TBD.
				
		return suite;
	}

	public List getTemplateMethods(ITranslationUnit tu) throws CModelException
	{
		IStructure myElem = null;
		try {
			myElem = (IStructure) tu.getElement("TemplateContainer");
		}
		catch( CModelException c ) {
			assertNotNull( c );							
		}
		assertNotNull(myElem);
		return myElem.getChildrenOfType(ICElement.C_TEMPLATE_METHOD);
	}

	public void testGetChildrenOfTypeTemplate() throws CModelException {
		ITranslationUnit tu = getTU();
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT);
			String[] myExpectedValues = {
				"Map"
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ITemplate myITemplate = (ITemplate) arrayElements.get(i);
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], myITemplate.getElementName());
			}
		}
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS);
			String[] myExpectedValues = {
				"nonVector"
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ITemplate myITemplate = (ITemplate) arrayElements.get(i);
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], myITemplate.getElementName());
			}
		}
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION);
			String[] myExpectedValues = {
				"ArrayOverlay"
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ITemplate myITemplate = (ITemplate) arrayElements.get(i);
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], myITemplate.getElementName());
			}
		}
		{
			// Methods and Functions are tested together as 
			// Function declarations in Quick Parse mode 
			// are considered Method Declarations in Structural parse mode
			List arrayElements = getTemplateMethods(tu);
			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION));
			String[] myExpectedValues = {
				"fum",
				"scrum",
				"nonVector<T>::first",
				"IsGreaterThan",
				"Foo::fum"
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ITemplate myITemplate = (ITemplate) arrayElements.get(i);
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], myITemplate.getElementName());
			}
		}
/*
 		// TEMPLATE_VARIABLE moved to failed tests
 		{
			ArrayList arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE);
			String[] myExpectedValues = {
				"default_alloc_template<threads,inst>::S_start_free"
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ITemplate myITemplate = (ITemplate) arrayElements.get(i);
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], myITemplate.getElementName());
			}
		}
*/	}


	public void testGetNumberOfTemplateParameters() throws CModelException
	{
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION ) );
		arrayElements.addAll( getTemplateMethods(tu) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION ) );
		// TEMPLATE_VARIABLE moved to failed tests
		//arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE ) );
		
		int[] myExpectedNumbers = {
//			3,1,3,1,1,3
			3,1,3,1,1,1,1,1/*,2*/
		};
		assertEquals(myExpectedNumbers.length, arrayElements.size());
		for(int i=0; i<myExpectedNumbers.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull( "Failed on "+i, myTemplate );
			assertEquals( "Failed on "+i, myExpectedNumbers[i],
				myTemplate.getNumberOfTemplateParameters());
		}
	}
	public void testGetTemplateParameterTypes() throws CModelException
	{
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION ) );
		arrayElements.addAll( getTemplateMethods(tu) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION ) );
		// TEMPLATE_VARIABLE moved to failed tests
		//arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE ) );
		
		String[][] myExpectedValues = {
			//"Map"
			{"Key", "Value", "SortAlgorithm"},
			//"nonVector"
			{"T"},
			//"ArrayOverlay"
			{"X","Y","int=16"},
			//"TemplateContainer::fum"
			{"Bar"},
		  	//"TemplateParameter::scrum"
			{"int"},
			//"nonVector::first"
			{"T"},
			//"IsGreaterThan"
			{"X"},
			//"Foo::fum"
			{"Bar"},
			/*
			//"default_alloc_template::S_start_free"
			{"bool", "int"},*/
		};
		assertEquals(myExpectedValues.length, arrayElements.size());
		for(int i=0; i<myExpectedValues.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull( "Failed on "+i, myTemplate );
			String[] myExpectedParams = myExpectedValues[i];
			String[] myParams = myTemplate.getTemplateParameterTypes();
			assertEquals( "Failed on "+i, myExpectedParams.length, myParams.length );
			for(int j=0; j<myExpectedParams.length; j++) {
				assertEquals( "Failed on "+i+","+j, myExpectedParams[j], myParams[j] );
			}
		}
	}
	public void testGetTemplateSignature() throws CModelException
	{
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION ) );
		arrayElements.addAll( getTemplateMethods(tu) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION ) );
		// TEMPLATE_VARIABLE moved to failed tests
		//arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE ) );
		
		String[] myExpectedValues = {
			"Map<Key, Value, SortAlgorithm>",
			"nonVector<T>",
			"ArrayOverlay<X, Y, int=16>",
			"fum<Bar>(int) : void",
			"scrum<int>(void) : void", // TODO: deduce the rules of () versus (void), compare below.
			"nonVector<T>::first<T>() : const T&", // TODO: where should <T> be?
			// TODO: shouldn't signature indicate const function as well?
			"IsGreaterThan<X>(X, X) : bool",
			"Foo::fum<Bar>(int) : void",
			/*"default_alloc_template<threads,inst>::S_start_free<bool, int> : char*",*/
		};
		assertEquals(myExpectedValues.length, arrayElements.size());
		for(int i=0; i<myExpectedValues.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull( "Failed on "+i, myTemplate );
			assertEquals( "Failed on "+i, myExpectedValues[i],
				myTemplate.getTemplateSignature() );
		}
	}
}
