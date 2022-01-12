/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.templateengine;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

/**
 * Test the functionality of Tempalte Class.
 */
public class TestTemplateCore extends BaseTestCase {

	public TemplateCore[] templates = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		templates = TemplateEngineTestsHelper.getTestTemplates();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Constructor for Template.
	 * @param name
	 */
	public TestTemplateCore(String name) {
		super(name);
	}

	/**
	 * check that the Template contains a Non Null ValueStore
	 */
	public void testValueStoreNotNull() {
		for (int i = 0; i < templates.length; i++) {
			assertNotNull(templates[i].getValueStore());
		}
	}

	/**
	 * Check the IDs to be persisited in SharedDefaults.
	 */
	public void testPersistTrueIDs() {
		for (int i = 0; i < templates.length; i++) {
			assertNotNull(templates[i].getPersistTrueIDs());
		}
	}

	public void testGetAllMissingMacrosInProcesses() {
		for (int i = 0; i < templates.length; i++) {
			assertNotNull(templates[i].getAllMissingMacrosInProcesses());
			assertTrue(templates[i].getAllMissingMacrosInProcesses().size() > 0);
		}
	}

}
