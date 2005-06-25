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
package org.eclipse.cdt.core.model.tests;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * @author hamer
 *
 */
public class StructuralTemplateTests extends ITemplateTests {
	/**
	 * @param name
	 */
	public StructuralTemplateTests(String name) {
		super(name);
	}
	public static Test suite() {
		TestSuite suite= new TestSuite( StructuralTemplateTests.class.getName() );
		
		// Interface tests:
		suite.addTest( new StructuralTemplateTests("testGetChildrenOfTypeTemplate"));
		suite.addTest( new StructuralTemplateTests("testGetNumberOfTemplateParameters"));
		suite.addTest( new StructuralTemplateTests("testGetTemplateParameterTypes"));
		suite.addTest( new StructuralTemplateTests("testGetTemplateSignature"));
						
		return suite;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.ITemplateTests#testGetChildrenOfTypeTemplate()
	 */
	public void testGetChildrenOfTypeTemplate() throws CModelException {
		setStructuralParse(true);
		ITranslationUnit tu = getTU();
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT);
			String[] myExpectedValues = {
				"Map"
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate)celement;
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], celement.getElementName());
			}
		}
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS);
			String[] myExpectedValues = {
				"nonVector"
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate)celement;
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], celement.getElementName());
			}
		}
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION);
			String[] myExpectedValues = {
				"ArrayOverlay"
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate)celement;
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], celement.getElementName());
			}
		}
		{
			// Methods and Functions are tested together as 
			// Function declarations in Quick Parse mode 
			// are considered Method Declarations in Structural parse mode
			List arrayElements = getTemplateMethods(tu);
			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION));
			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
			String[] myExpectedValues = {
				"fum",
				"scrum",
				"first",
				"fum",
				"IsGreaterThan",
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			for(int i=0; i<myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate)celement;
				assertNotNull( "Failed on "+i, myITemplate);
				assertEquals("Failed on "+i, myExpectedValues[i], celement.getElementName());
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.ITemplateTests#testGetNumberOfTemplateParameters()
	 */
	public void testGetNumberOfTemplateParameters() throws CModelException {
		setStructuralParse(true);
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION ) );
		arrayElements.addAll( getTemplateMethods(tu) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION ) );
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
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
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.ITemplateTests#testGetTemplateParameterTypes()
	 */
	public void testGetTemplateParameterTypes() throws CModelException {
		setStructuralParse(true);
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION ) );
		arrayElements.addAll( getTemplateMethods(tu) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION ) );
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
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
			//"Foo::fum"
			{"Bar"},
			//"IsGreaterThan"
			{"X"},
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
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.ITemplateTests#testGetTemplateSignature()
	 */
	public void testGetTemplateSignature() throws CModelException {
		setStructuralParse(true);
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS ) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION ) );
		arrayElements.addAll( getTemplateMethods(tu) );
		arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION ) );
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
		// TEMPLATE_VARIABLE moved to failed tests
		//arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE ) );
		
		String[] myExpectedValues = {
			"Map<Key, Value, SortAlgorithm>",
			"nonVector<T>",
			"ArrayOverlay<X, Y, int=16>",
			"fum<Bar>(int) : void",
			"scrum<int>(void) : void", // TODO: deduce the rules of () versus (void), compare below.
			// TODO: shouldn't signature indicate const function as well?
			"first<T>() : const T&", // TODO: where should <T> be?
			"fum<Bar>(int) : void",
			"IsGreaterThan<X>(X, X) : bool",
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
