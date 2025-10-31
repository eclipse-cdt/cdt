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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.junit.jupiter.api.Test;

/**
 * @author hamer
 *
 */
public class StructuralTemplateTests extends ITemplateTests {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.ITemplateTests#testGetChildrenOfTypeTemplate()
	 */
	@Override
	@Test
	public void testGetChildrenOfTypeTemplate() throws CModelException {
		setStructuralParse(true);
		ITranslationUnit tu = getTU();
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT);
			String[] myExpectedValues = { "Map" };
			assertEquals((long) myExpectedValues.length, (long) arrayElements.size());
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull(myITemplate, "Failed on " + i);
				assertEquals(myExpectedValues[i], celement.getElementName(), "Failed on " + i);
			}
		}
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS);
			String[] myExpectedValues = { "nonVector" };
			assertEquals((long) myExpectedValues.length, (long) arrayElements.size());
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull(myITemplate, "Failed on " + i);
				assertEquals(myExpectedValues[i], celement.getElementName(), "Failed on " + i);
			}
		}
		{
			List arrayElements = tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION);
			String[] myExpectedValues = { "ArrayOverlay" };
			assertEquals((long) myExpectedValues.length, (long) arrayElements.size());
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull(myITemplate, "Failed on " + i);
				assertEquals(myExpectedValues[i], celement.getElementName(), "Failed on " + i);
			}
		}
		{
			// Methods and Functions are tested together as
			// Function declarations in Quick Parse mode
			// are considered Method Declarations in Structural parse mode
			List arrayElements = getTemplateMethods(tu);
			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION));
			arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
			String[] myExpectedValues = { "fum", "scrum", "nonVector<T>::first", "Foo::fum", "IsGreaterThan", };
			assertEquals((long) myExpectedValues.length, (long) arrayElements.size());
			for (int i = 0; i < myExpectedValues.length; i++) {
				ICElement celement = (ICElement) arrayElements.get(i);
				ITemplate myITemplate = (ITemplate) celement;
				assertNotNull(myITemplate, "Failed on " + i);
				assertEquals(myExpectedValues[i], celement.getElementName(), "Failed on " + i);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.ITemplateTests#testGetNumberOfTemplateParameters()
	 */
	@Override
	@Test
	public void testGetNumberOfTemplateParameters() throws CModelException {
		setStructuralParse(true);
		ITranslationUnit tu = getTU();
		ArrayList arrayElements = new ArrayList();
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_CLASS));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_UNION));
		arrayElements.addAll(getTemplateMethods(tu));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION));
		arrayElements.addAll(tu.getChildrenOfType(ICElement.C_TEMPLATE_METHOD));
		// TEMPLATE_VARIABLE moved to failed tests
		//arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE ) );

		int[] myExpectedNumbers = {
				//			3,1,3,1,1,3
				3, 1, 3, 1, 1, 1, 1, 1/*,2*/
		};
		assertEquals((long) myExpectedNumbers.length, (long) arrayElements.size());
		for (int i = 0; i < myExpectedNumbers.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull(myTemplate, "Failed on " + i);
			assertEquals((long) myExpectedNumbers[i], (long) myTemplate.getNumberOfTemplateParameters(),
					"Failed on " + i);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.ITemplateTests#testGetTemplateParameterTypes()
	 */
	@Override
	@Test
	public void testGetTemplateParameterTypes() throws CModelException {
		setStructuralParse(true);
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
		//arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE ) );

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
				/*
				//"default_alloc_template::S_start_free"
				{"bool", "int"},*/
		};
		assertEquals((long) myExpectedValues.length, (long) arrayElements.size());
		for (int i = 0; i < myExpectedValues.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull(myTemplate, "Failed on " + i);
			String[] myExpectedParams = myExpectedValues[i];
			String[] myParams = myTemplate.getTemplateParameterTypes();
			assertEquals((long) myExpectedParams.length, (long) myParams.length, "Failed on " + i);
			for (int j = 0; j < myExpectedParams.length; j++) {
				assertEquals(myExpectedParams[j], myParams[j], "Failed on " + i + "," + j);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.tests.ITemplateTests#testGetTemplateSignature()
	 */
	@Override
	@Test
	public void testGetTemplateSignature() throws CModelException {
		setStructuralParse(true);
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
		//arrayElements.addAll( tu.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE ) );

		String[] myExpectedValues = { "Map<Key, Value, SortAlgorithm>", "nonVector<T>", "ArrayOverlay<X, Y, int=16>",
				"fum<Bar>(int) : void", "scrum<int>(void) : void", // TODO: deduce the rules of () versus (void), compare below.
				"nonVector<T>::first<T>() const : const T&", // TODO: where should <T> be?
				"Foo::fum<Bar>(int) : void", "IsGreaterThan<X>(X, X) : bool",
				/*"default_alloc_template<threads,inst>::S_start_free<bool, int> : char*",*/
		};
		assertEquals((long) myExpectedValues.length, (long) arrayElements.size());
		for (int i = 0; i < myExpectedValues.length; i++) {
			ITemplate myTemplate = (ITemplate) arrayElements.get(i);
			assertNotNull(myTemplate, "Failed on " + i);
			assertEquals(myExpectedValues[i], myTemplate.getTemplateSignature(), "Failed on " + i);
		}
	}
}
