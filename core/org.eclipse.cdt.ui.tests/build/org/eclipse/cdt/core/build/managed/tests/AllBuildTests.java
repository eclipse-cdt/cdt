/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.build.managed.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.cdt.core.build.managed.ManagedBuildManager;

/**
 * 
 */
public class AllBuildTests extends TestCase {

	public AllBuildTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTest(new AllBuildTests("testExtensions"));
		
		return suite;
	}

	public void testThatAlwaysFails() {
		assertTrue(false);
	}
	
	/**
	 * Navigates through the build info as defined in the extensions
	 * defined in this plugin
	 */
	public void testExtensions() {
		ITarget testRoot = null;
		ITarget testSub = null;
		
		// Note secret null parameter which means just extensions
		ITarget[] targets = ManagedBuildManager.getDefinedTargets(null);

		for (int i = 0; i < targets.length; ++i) {
			ITarget target = targets[i];
			
			if (target.getName().equals("Test Root")) {
				testRoot = target;
				
				// Tools
				ITool[] tools = testRoot.getTools();
				// Root Tool
				ITool rootTool = tools[0];
				assertEquals("Root Tool", rootTool.getName());
				// Options
				IOption[] options = rootTool.getOptions();
				assertEquals(2, options.length);
				assertEquals("Option in Top", options[0].getName());
				assertEquals("Option in Category", options[1].getName());
				// Option Categories
				IOptionCategory topCategory = rootTool.getTopOptionCategory();
				assertEquals("Root Tool", topCategory.getName());
				options = topCategory.getOptions(rootTool);
				assertEquals(1, options.length);
				assertEquals("Option in Top", options[0].getName());
				IOptionCategory[] categories = topCategory.getChildCategories();
				assertEquals(1, categories.length);
				assertEquals("Category", categories[0].getName());
				options = categories[0].getOptions(rootTool);
				assertEquals(1, options.length);
				assertEquals("Option in Category", options[0].getName());
				
				// Configs
				IConfiguration[] configs = testRoot.getConfigurations();
				// Root Config
				IConfiguration rootConfig = configs[0];
				assertEquals("Root Config", rootConfig.getName());
				
			} else if (target.getName().equals("Test Sub")) {
				testSub = target;
				
				// Tools
				ITool[] tools = testSub.getTools();
				// Root Tool
				ITool rootTool = tools[0];
				assertEquals("Root Tool", rootTool.getName());
				// Sub Tool
				ITool subTool = tools[1];
				assertEquals("Sub Tool", subTool.getName());

				// Configs
				IConfiguration[] configs = testSub.getConfigurations();
				// Root Config
				IConfiguration rootConfig = configs[0];
				assertEquals("Root Config", rootConfig.getName());
				// Sub Config
				IConfiguration subConfig = configs[1];
				assertEquals("Sub Config", subConfig.getName());
			}
		}
		
		assertNotNull(testRoot);
		assertNotNull(testSub);
	}
}
