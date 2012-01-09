/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.templateengine;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateDescriptor;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

/**
 * Test the functionality of the ValueStore class.
 */
public class TestValueStore extends BaseTestCase {
	/**
	 * setUp is called before execution of test method.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * release resources held.
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public TestValueStore(String name) {
		super(name);
    }

	/**
	 * Test ValueStore for Not Null condition.
	 */
	public void testValueStoreNotNull(){
		TemplateCore[] templates = TemplateEngineTestsHelper.getTestTemplates();
		for (int i = 0; i < templates.length; i++) {
			Map<String, String> valueStore = templates[i].getValueStore();
			assertNotNull(valueStore);
		}
	}
	
	/**
	 * ValueStore is expected to consist all the IDs from 
	 * FactoryDefaults. Test the same.
	 */
	public void testCompareValueStoreWithTemplateDefaluts(){
		TemplateCore[] templates = TemplateEngineTestsHelper.getTestTemplates();
		for (int i = 0; i < templates.length; i++) {
			Map<String, String> valueStore = templates[i].getValueStore();
			TemplateDescriptor templateDescriptor = templates[i].getTemplateDescriptor();
	        Map<String, String> templateDefaults = templateDescriptor.getTemplateDefaults(templateDescriptor.getRootElement());

	        Iterator<String> defaultsIterator = templateDefaults.keySet().iterator();
			while (defaultsIterator.hasNext()){
			  String key = defaultsIterator.next();
			  assertNotNull(valueStore.get(key));
			}
		}
	}
}
