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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
/*
 * Created on Jun 17, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariable;

import junit.framework.Test;
import junit.framework.TestSuite;

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
	@Override
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.IntegratedCModelTest#getSourcefileResource()
	 */
	@Override
	public String getSourcefileResource() {
		return "ITemplate.cpp";
	}

	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(IStructureTests.class.getName());

		// Interface tests:
		suite.addTest(new ITemplateTests("testGetChildrenOfTypeTemplate"));
		suite.addTest(new ITemplateTests("testGetNumberOfTemplateParameters"));
		suite.addTest(new ITemplateTests("testGetTemplateParameterTypes"));
		suite.addTest(new ITemplateTests("testGetTemplateSignature"));

		// Language Specification tests:
		// TBD.

		return suite;
	}

	public List getTemplateMethods(ITranslationUnit tu) throws CModelException {
		IStructure myElem = null;
		try {
			myElem = (IStructure) tu.getElement("TemplateContainer");
		} catch (CModelException c) {
			assertNotNull(c);
		}
		assertNotNull(myElem);
		List list = myElem.getChildrenOfType(ICElement.C_TEMPLATE_METHOD_DECLARATION);
		list.addAll((myElem.getChildrenOfType(ICElement.C_TEMPLATE_METHOD)));
		return list;
	}

	public void testGetChildrenOfTypeTemplate() throws CModelException {
		ITranslationUnit tu = getTU();
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT);
			String[] myExpectedValues = { "Map" };
			assertEquals(myExpectedValues.length, arrayElements.size());
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull("Failed on " + i, myITemplate);
				assertEquals("Failed on " + i, myExpectedValues[i], celement.getElementName());
			}
		}
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS);
			String[] myExpectedValues = { "nonVector" };
			assertEquals(myExpectedValues.length, arrayElements.size());
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull("Failed on " + i, myITemplate);
				assertEquals("Failed on " + i, myExpectedValues[i], celement.getElementName());
			}
		}
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION);
			String[] myExpectedValues = { "ArrayOverlay" };
			assertEquals(myExpectedValues.length, arrayElements.size());
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull("Failed on " + i, myITemplate);
				assertEquals("Failed on " + i, myExpectedValues[i], celement.getElementName());
			}
		}
		{
			// Method from the TemplateContainer
			List arrayElements = getTemplateMethods(tu);
			String[] myExpectedValues = { "fum", "scrum", };
			assertEquals(myExpectedValues.length, arrayElements.size());
			// This test is no correct there is no guaranty on the order
			// for this particular case
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull("Failed on " + i, myITemplate);
				assertEquals("Failed on " + i, myExpectedValues[i], celement.getElementName());
			}
		}
		{
			// Check the template function
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION);
			// actually, none of the two are function templates, but method templates
			String[] myExpectedValues = {
					//					"nonVector<T>::first",
					//					"Foo::fum",
			};
			assertEquals(myExpectedValues.length, arrayElements.size());
			// This test is no correct there is no guaranty on the order
			// for this particular case
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull("Failed on " + i, myITemplate);
				assertEquals("Failed on " + i, myExpectedValues[i], celement.getElementName());
			}

		}
		{
			// Check the template method
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD);
			String[] myExpectedValues = { "nonVector<T>::first", "Foo::fum", };
			assertEquals(myExpectedValues.length, arrayElements.size());
			// This test is no correct there is no guaranty on the order
			// for this particular case
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull("Failed on " + i, myITemplate);
				assertEquals("Failed on " + i, myExpectedValues[i], celement.getElementName());
			}

		}
		{
			// Template function declation
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION);
			String[] myExpectedValues = { "IsGreaterThan" };
			assertEquals(myExpectedValues.length, arrayElements.size());
			// This test is no correct there is no guaranty on the order
			// for this particular case
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull("Failed on " + i, myITemplate);
				assertEquals("Failed on " + i, myExpectedValues[i], celement.getElementName());
			}
		}
		{
			//			// Methods and Functions are tested together as
			//			// Function declarations in Quick Parse mode
			//			// are considered Method Declarations in Structural parse mode
			//			List arrayElements = getTemplateMethods(tu);
			//			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION));
			//			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
			//			String[] myExpectedValues = {
			//				"fum",
			//				"scrum",
			//				"nonVector<T>::first",
			//				"Foo::fum",
			//				"IsGreaterThan"
			//			};
			//			assertEquals(myExpectedValues.length, arrayElements.size());
			//			// This test is no correct there is no guaranty on the order
			//			// for this particular case
			//			for(int i=0; i<myExpectedValues.length; i++) {
			//				ICElement celement = (ICElement) arrayElements.get(i);
			//				ITemplate myITemplate = (ITemplate)celement;
			//				assertNotNull( "Failed on "+i, myITemplate);
			//				assertEquals("Failed on "+i, myExpectedValues[i], celement.getElementName());
			//			}
		}

		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE);
			String[] myExpectedValues = { "default_alloc_template<threads,inst>::S_start_free" };
			assertEquals(myExpectedValues.length, arrayElements.size());
			for (int i = 0; i < myExpectedValues.length; i++) {
				IVariable myITemplate = (IVariable) arrayElements.get(i);
				assertNotNull("Failed on " + i, myITemplate);
				assertEquals("Failed on " + i, myExpectedValues[i], myITemplate.getElementName());
			}
		}
	}

	public void testGetNumberOfTemplateParameters() throws CModelException {
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION));
		arrayElements.addAll(getTemplateMethods(tu));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE));

		int[] myExpectedNumbers = { 3, 1, 3, 1, 1, 1, 1, 1, 2 };
		assertEquals(myExpectedNumbers.length, arrayElements.size());
		for (int i = 0; i < myExpectedNumbers.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull("Failed on " + i, myTemplate);
			assertEquals("Failed on " + i, myExpectedNumbers[i], myTemplate.getNumberOfTemplateParameters());
		}
	}

	public void testGetTemplateParameterTypes() throws CModelException {
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION));
		arrayElements.addAll(getTemplateMethods(tu));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE));

		String[][] myExpectedValues = {
				//"Map"
				{ "Key", "Value", "SortAlgorithm" },
				//"nonVector"
				{ "T" },
				//"ArrayOverlay"
				{ "X", "Y", "int=16" },
				//"TemplateContainer::fum"
				{ "Bar" },
				//"TemplateParameter::scrum"
				{ "int" },
				//"nonVector::first"
				{ "T" },
				//"Foo::fum"
				{ "Bar" },
				//"IsGreaterThan"
				{ "X" },
				//"default_alloc_template::S_start_free"
				{ "bool", "int" }, };
		assertEquals(myExpectedValues.length, arrayElements.size());
		for (int i = 0; i < myExpectedValues.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull("Failed on " + i, myTemplate);
			String[] myExpectedParams = myExpectedValues[i];
			String[] myParams = myTemplate.getTemplateParameterTypes();
			assertEquals("Failed on " + i, myExpectedParams.length, myParams.length);
			for (int j = 0; j < myExpectedParams.length; j++) {
				assertEquals("Failed on " + i + "," + j, myExpectedParams[j], myParams[j]);
			}
		}
	}

	public void testGetTemplateSignature() throws CModelException {
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION));
		arrayElements.addAll(getTemplateMethods(tu));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
		// TEMPLATE_VARIABLE moved to failed tests
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE));

		String[] myExpectedValues = { "Map<Key, Value, SortAlgorithm>", "nonVector<T>", "ArrayOverlay<X, Y, int=16>",
				"fum<Bar>(int) : void", "scrum<int>(void) : void", // TODO: deduce the rules of () versus (void), compare below.
				"nonVector<T>::first<T>() const : const T&", // TODO: where should <T> be?
				"Foo::fum<Bar>(int) : void",
				// TODO: shouldn't signature indicate const function as well?
				"IsGreaterThan<X>(X, X) : bool",
				"default_alloc_template<threads,inst>::S_start_free<bool, int> : char*", };
		assertEquals(myExpectedValues.length, arrayElements.size());
		for (int i = 0; i < myExpectedValues.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull("Failed on " + i, myTemplate);
			assertEquals("Failed on " + i, myExpectedValues[i], myTemplate.getTemplateSignature());
		}
	}
}
