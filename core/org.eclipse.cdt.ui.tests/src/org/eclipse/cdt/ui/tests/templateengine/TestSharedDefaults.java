/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.templateengine;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.cdt.core.templateengine.SharedDefaults;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* Executes all the test cases of SharedDefaults backend functionality
*/

public class TestSharedDefaults extends BaseTestCase {
	private SharedDefaults sharedDefaults;
	private final String TEST_KEY = "org.eclipse.cdt.templateengine.project.HelloWorld.basename";
	private final String TEST_VALUE = "Astala Vista";
	private final String TEST_VALUE_UPDATED = "Astala Vista Updated";

	/*
	 * @see TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		sharedDefaults = SharedDefaults.getInstance();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() {
		sharedDefaults = null;
	}

	/**
	 * Get a value from the backend storage
	 *
	 * @return the value for this key or null if no value exist for this key
	 */
	private String getValueFromBackEndStorate(String key) throws Exception {
		File parsedXML = TemplateEngineHelper.getSharedDefaultLocation("shareddefaults.xml");

		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(parsedXML.toURI().toURL().openStream());

		List<Element> sharedElementList = TemplateEngine.getChildrenOfElement(document.getDocumentElement());
		int listSize = sharedElementList.size();
		for (int i = 0; i < listSize; i++) {
			Element xmlElement = sharedElementList.get(i);
			String key2 = xmlElement.getAttribute(TemplateEngineHelper.ID);
			String value2 = xmlElement.getAttribute(TemplateEngineHelper.VALUE);
			if (key.equals(key2)) {
				return value2;
			}
		}

		return null;
	}

	/**
	 * This test checks if data gets added to the back end
	 * New data gets persisted in SharedDefault XML file
	 */
	public void testAddToBackEndStorage() throws Exception {
		sharedDefaults.addToBackEndStorage(TEST_KEY, TEST_VALUE);
		assertTrue(sharedDefaults.getSharedDefaultsMap().containsKey(TEST_KEY));
		assertEquals(TEST_VALUE, sharedDefaults.getSharedDefaultsMap().get(TEST_KEY));

		assertEquals(TEST_VALUE, getValueFromBackEndStorate(TEST_KEY));
	}

	/**
	 * This tests the updateToBackEndStorage of SharedDefaults
	 * to verify whether the key-value pair gets updated with new value
	 * New data gets persisted in SharedDefault XML file
	 */
	public void testUpdateToBackEndStorage() throws Exception {
		sharedDefaults.addToBackEndStorage(TEST_KEY, TEST_VALUE);
		assertTrue(sharedDefaults.getSharedDefaultsMap().containsKey(TEST_KEY));
		sharedDefaults.updateToBackEndStorage(TEST_KEY, TEST_VALUE_UPDATED);
		assertEquals(TEST_VALUE_UPDATED, sharedDefaults.getSharedDefaultsMap().get(TEST_KEY));
		assertEquals(TEST_VALUE_UPDATED, getValueFromBackEndStorate(TEST_KEY));
	}

	/**
	 * This tests the deleteBackEndStorage of SharedDefaults
	 * to verify whether the key-value pair gets deleted at the backend
	 */
	public void testDeleteBackEndStorage() throws Exception {
		sharedDefaults.addToBackEndStorage(TEST_KEY, TEST_VALUE);
		assertTrue(sharedDefaults.getSharedDefaultsMap().containsKey(TEST_KEY));
		sharedDefaults.deleteBackEndStorage(new String[] { TEST_KEY });
		assertFalse(sharedDefaults.getSharedDefaultsMap().containsKey(TEST_KEY));
		assertNull(getValueFromBackEndStorate(TEST_KEY));
	}
}
