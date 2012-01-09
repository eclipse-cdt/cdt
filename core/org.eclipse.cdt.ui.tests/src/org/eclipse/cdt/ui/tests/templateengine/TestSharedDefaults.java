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
import java.util.Set;

import org.eclipse.cdt.core.templateengine.SharedDefaults;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

/**
* Executes all the test cases of SharedDefaults backend functionality
*/

public class TestSharedDefaults extends BaseTestCase {
	private SharedDefaults sharedDefaults;
	
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
	protected void tearDown(){
		sharedDefaults = null;
	}
	
	/**
	 * This test checks if data gets added to the back end
	 * New data gets persisted in SharedDefault XML file 
	 */
	public void testAddToBackEndStorage() {
		Map<String, String> actualSharedDefaults= sharedDefaults.getSharedDefaultsMap();
		
		actualSharedDefaults.put("provider.name","eclipse"); //$NON-NLS-1$ //$NON-NLS-2$
		actualSharedDefaults.put("copyright","Symbian Software Ltd."); //$NON-NLS-1$ //$NON-NLS-2$
		actualSharedDefaults.put("author","Bala Torati"); //$NON-NLS-1$ //$NON-NLS-2$

		Map<String, String> expectedSharedDefaults= sharedDefaults.getSharedDefaultsMap();
		
		assertEquals("Contents are different  :", //$NON-NLS-1$
				expectedSharedDefaults,
				actualSharedDefaults);
	}
	
	/**
	 * This tests the updateToBackEndStorage of SharedDefaults
	 * to verify whether the key-value pair gets updated with new value
	 * New data gets persisted in SharedDefault XML file 
	 */
	public void testUpdateToBackEndStorage() {
		Map<String, String> actualSharedDefaults = sharedDefaults.getSharedDefaultsMap();
	 	
	 	for (Map.Entry<String, String> entry : actualSharedDefaults.entrySet()) {
	 		String key = entry.getKey();
	 		String value = entry.getValue();
	 		if (key.equals("org.eclipse.cdt.templateengine.project.HelloWorld.basename")) { //$NON-NLS-1$
	 			entry.setValue("Astala Vista"); //$NON-NLS-1$
		 		sharedDefaults.updateToBackEndStorage("org.eclipse.cdt.templateengine.project.HelloWorld.basename", value); //$NON-NLS-1$
		 		break;
	 		}
	 	}
		
		Map<String, String> expectedSharedDefaults=sharedDefaults.getSharedDefaultsMap();
		
		assertEquals("Contents are different  :", //$NON-NLS-1$
				expectedSharedDefaults,
				actualSharedDefaults);
	}
	
	/**
	 * This tests the deleteBackEndStorage of SharedDefaults
	 * to verify whether the key-value pair gets deleted at the backend
	 */
	public void testDeleteBackEndStorage() {
		Map<String, String> actualSharedDefaults= sharedDefaults.getSharedDefaultsMap();		
		Set<String> keySet = actualSharedDefaults.keySet();
	 	Iterator<String> iterator = keySet.iterator();
	 	String keyName = null;
	 	
	 	while (iterator.hasNext()) {
	 		String key = iterator.next();
	 		keyName = key;
	  		if (keyName.equals("org.eclipse.cdt.templateengine.project.HelloWorld.basename")) { //$NON-NLS-1$
	 			actualSharedDefaults.remove(keyName);
	 			break;
	 		}	 			
	 	}
	 	
	 	sharedDefaults.deleteBackEndStorage(new String[] { keyName });
		Map<String, String> expectedSharedDefaults= sharedDefaults.getSharedDefaultsMap();
				
		assertEquals("Contents are different  :", //$NON-NLS-1$
				expectedSharedDefaults,
				actualSharedDefaults);
	}
}
