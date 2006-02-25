/*******************************************************************************
 * Copyright (c) 2005 Symbian Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.testplugin.CTestPlugin;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

// TODO LK - write test for resource configurations + IOptionCategory.getOptions(config)
// TODO LK - write tests for EVENT_OPEN call-backs
// TODO LK - do not deprecate IOption.getParent() as suggested by Leo

/*
 *  These tests exercise CDT 3.0 shared tool options extensions 
 */
public class ManagedBuildCoreTests_SharedToolOptions extends TestCase {

	class IconComparator {
		static final int None = 0;
		static final int One  = 1;
		static final int Two  = 2;

		// The following uses a numbering scheme defined for
		// testCategory and testOption
		
		// Co-ordinates of option groups 2nd level that have icon 1
		int One_i[] = { 3, 2, 4, 7 };
		int One_j[] = { 1, 2, 1, 1 };
		// Co-ordinates of option groups 2nd level that have icon 1
		int Two_i[] = { 2, 6 };
		int Two_j[] = { 1, 1 };
		
		IconComparator()
		{
			assertEquals(One_i.length, One_j.length);
			assertEquals(Two_i.length, Two_j.length);				
		}
		
		int Compare(int i, int j)
		{
			int k;
			
			// Check for icon 1
			for (k=0; k < One_i.length; k++)
			{
				if ( (i == One_i[k]) && (j == One_j[k]) )
				{
					return One;
				}					
			}
			// Check for icon 2
			for (k=0; k < Two_i.length; k++)
			{
				if ( (i == Two_i[k]) && (j == Two_j[k]) )
				{
					return Two;
				}					
			}
			// None of them
			return None;
		}
	}	

	class ValueHandlerComparator {
		static final int MBS  = 0;
		static final int TEST = 1;

		// The following uses a numbering scheme defined for
		// testCategory and testOption
		
		// Co-ordinates of option groups 2nd level that have a test value handler
		int Test_i[]      = { 2, 2, 3, 5, 7 };
		int Test_j[]      = { 1, 4, 2, 1, 1 };
		String Test_arg[] = { "Option2.1.1", "Option2.2.2", "Option3.1.2", "Option5.1", "Option7.1" };
		String last_arg;
		
		ValueHandlerComparator()
		{
			assertEquals(Test_i.length, Test_j.length);
			assertEquals(Test_i.length, Test_arg.length);
			last_arg = "";
		}
		
		int Compare(int i, int j) {
			int k;
			
			// Check for Test handler
			for (k=0; k < Test_i.length; k++) {
				if ( (i == Test_i[k]) && (j == Test_j[k]) ) {
					last_arg = Test_arg[k];
					return TEST;
				}					
			}
			// None of them
			last_arg = "";
			return MBS;			
		}
		
		String getArg() {
			return last_arg;
		}
	}	
	
	// Constants
	private final String projectName = "test30_sto";
	private final String projectID   = "test30_sto.dummy";
	private final String configID    = "test30_sto.dummy.config";
	private final String configName  = "Configuration for test30_sto";

	// Control variables
	private boolean testExtensionElements = true;
	private boolean testIsSetup = false;
	
	// Chain leading to tool, etc 
	private IProjectType    testProject;
	private IConfiguration  testConfig;	
	private IToolChain      testToolChain;
	
	// Direct children of toolChain
	private ITool           testTools[];
	private IOptionCategory testCategoryTop[];
	private IOption         testOptionTop[];
	
	// 2nd level children (of toolChain and tool) mapped in the following pattern
	// onto the arrays:
	//    tool_1: cat_1.1  cat_1.2 cat_1.3 ...
	//    ...
	//    tool_n: cat_n.1  catn.2 
	// The same 2d-array is used to store top-level categories
	//    n+1:    topcat_1
	//    ...
	//    n+m:    topcat_m
	private Object          testCategory[][];
	private Object          testOption[][];	
	
	// Helper classes
	private IconComparator  iconComparator;
	private ValueHandlerComparator valueHandlerComparator;
	
	public ManagedBuildCoreTests_SharedToolOptions(String name) {
		super(name);
		
		iconComparator = new IconComparator();
		valueHandlerComparator = new ValueHandlerComparator();
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildCoreTests_SharedToolOptions.class.getName());
		
		suite.addTest(new ManagedBuildCoreTests_SharedToolOptions("testIcons"));
		suite.addTest(new ManagedBuildCoreTests_SharedToolOptions("testValueHandlers"));
		suite.addTest(new ManagedBuildCoreTests_SharedToolOptions("testOptions"));
		suite.addTest(new ManagedBuildCoreTests_SharedToolOptions("testConfiguration"));
		
		return suite;
	}

	private void assertCorrectId(String s1, String s2) {
		if (testExtensionElements == true) {
			// A strict comparison is required
			assertTrue(s1.equals(s2));
		} else {
			// Compare for non-extension element Id's
			assertTrue(s1.startsWith(s2+"."));			
		}
	}
	
	private IProject createProject(String name) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject newProjectHandle = root.getProject(name);
		IProject project = null;
		
		if (!newProjectHandle.exists()) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			//description.setLocation(root.getLocation());
			project = CCorePlugin.getDefault().createCProject(description, newProjectHandle, new NullProgressMonitor(), ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID);
		} else {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			project = newProjectHandle;
		}
        
		// Open the project if we have to
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}
				
		return project;	
	}
	
	/**
	 * Sets up the test environment for the default project
	 */
	private void setupDefaultProject() throws Exception {

		// The assertCorrectId() call needs to be set up 
		testExtensionElements = true;
		// Get all the key structures of our test and do some sanity checking 
		//
		testProject = ManagedBuildManager.getProjectType(projectID);
		assertNotNull(testProject);		
		assertTrue(testProject.isTestProjectType());
		assertFalse(testProject.isAbstract());
		
		// Get and check project configurations - only one
		//
		setupConfiguration();
		assertTrue(testConfig.isExtensionElement());
	}
	
	/**
	 * Sets up the test configuration from the project set
	 * in the test
	 */
	private void setupConfiguration() throws Exception {
		
		IConfiguration[] configs = testProject.getConfigurations();
		assertNotNull(configs);
		assertEquals(configs.length, 1);
		testConfig = configs[0]; 
	    assertEquals(testConfig.getId(), configID);		     
	    assertEquals(testConfig.getName(), configName);		
	}
	
	/**
	 * Sets up the test environment for a project created from the
	 * default project
	 */
	private void setupProject() throws Exception {

		// The assertCorrectId() call needs to be set up 
		testExtensionElements = false;
		// Create new project
		IProject project = null;
		try {
			project = createProject(projectName);
			// Now associate the builder with the project
			ManagedBuildTestHelper.addManagedBuildNature(project);
			IProjectDescription description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}

		} catch (CoreException e) {
			fail("Test failed on project creation: " + e.getLocalizedMessage());
		}
		// Find the base project type definition
		IProjectType[] projTypes = ManagedBuildManager.getDefinedProjectTypes();
		IProjectType projType = ManagedBuildManager.getProjectType(projectID);
		assertNotNull(projType);
		// Create a managed project		
		IManagedProject newProject = ManagedBuildManager.createManagedProject(project, projType);
		assertEquals(newProject.getName(), projType.getName());
		assertFalse(newProject.equals(projType));
		ManagedBuildManager.setNewProjectVersion(project);
		// Set up the environment
		testProject = newProject.getProjectType();
		IConfiguration config = testProject.getConfiguration(configID);
		testConfig = newProject.createConfiguration(config, configID + ".12345678");
	}
	
	/**
	 * Sets up the test environment and does some initial checking.
	 * Do not do this in the constructor, as it is part of the test.
	 */
	private void setupTestEnvironment() throws Exception {
		
		int i;
		
		// Test ID's
		//
		String configID        = "test30_sto.dummy.config";
		String configName      = "Configuration for test30_sto";
		String toolChainID     = "test30_sto.dummy.toolchain";
		String toolChainName   = "Toolchain for test30_sto";
		// Toolchain Info
		int numTools           = 4;
		String toolIDs         = "test30_sto.dummy.tool.";
		int firstToolID        = 1;
		// Top level option categories and groups 
		int numTopCategories   = 3;
		String topCategoryIDs  = "test30_sto.dummy.category.";
		int firstTopCategoryID = 5;
		
		// Sizes of some arrays
		int sizeCategoryTop = 0;
		int sizeOptionTop   = 0;
		int sizeTools       = 0;		
	     
	    // Fetch toolchain
	    //
		testToolChain = testConfig.getToolChain();		
		assertNotNull(testToolChain);
	    assertEquals(testToolChain.getName(), toolChainName);
		assertCorrectId(testToolChain.getId(), toolChainID);

		// Fetch and check tools list
		//
		testTools = testToolChain.getTools();
		assertNotNull(testTools);
		assertEquals(testTools.length, numTools);
		int toolNo = firstToolID;
		sizeTools = numTools;
		for (i=0; i < sizeTools; i++) {
			assertCorrectId(testTools[i].getId(), toolIDs+toolNo);
			toolNo++;
		}
		
		// Fetch and check top level option categories and options
		//
		testCategoryTop = testToolChain.getChildCategories();
		testOptionTop = testToolChain.getOptions();
		
		if (testCategoryTop != null) {
			int categoryNo = firstTopCategoryID;
			sizeCategoryTop = testCategoryTop.length;
			for (i=0; i < sizeCategoryTop; i++, categoryNo++)
			{
				String ID = ((IOptionCategory)testCategoryTop[i]).getId();
				// Categories are always extension elements, so check
				// for an identical match
				assertEquals(ID, topCategoryIDs+categoryNo);
			}
		}
		if (testOptionTop != null) {
			sizeOptionTop = testOptionTop.length;
		}

		// Fetch and check 2nd level level option categories and options
		//
		testCategory = new Object[testTools.length+sizeCategoryTop][];
		testOption   = new Object[testTools.length+sizeOptionTop][];	
		for (i=0; i < sizeTools; i++) {
			testCategory[i] = testTools[i].getChildCategories();
			testOption[i]   = testTools[i].getOptions();
			// Make the arrays safe in case we have null references
			if ( testCategory[i] == null ) {
				testCategory[i] = new Object[0];
			}
			if ( testOption[i] == null ) {
				testOption[i] = new Object[0];				
			}				
		}
		// Add top level categories and options to test arrays
		//
		for (i=0; i < sizeCategoryTop; i++) {
			testCategory[sizeTools+i] = new Object[1];
			testCategory[sizeTools+i][0] = testCategoryTop[i];			
		}
		for (i=0; i < sizeOptionTop; i++) {
			testOption[sizeTools+i] = new Object[1];
			testOption[sizeTools+i][0] = testOptionTop[i];			
		}
	}
	
	/**
	 * Check, whether icon paths in tool1 - tool3 have been created.
	 * Check, whether icon paths in category 2.1 - 3.1 have been created
	 */
	public void testIcons() throws Exception {
			
		// Set up the environment
		if ( testIsSetup == false ) {
			setupDefaultProject();
			setupTestEnvironment();
		}
		
		// Get path's of icons to compare against 
		CTestPlugin me = CTestPlugin.getDefault();
		URL icon1 = Platform.asLocalURL( me.find(new Path("icons/one.gif"), null));
		URL icon2 = Platform.asLocalURL( me.find(new Path("icons/two.gif"), null));

		// Check the icons on tools
		assertToolIcon(testTools[0], icon1);
		assertToolIcon(testTools[1], null);
		assertToolIcon(testTools[2], icon2);
		
		// Check the top level and 2nd level categories
		int i;
		for (i=0; i < testCategory.length; i++) {
			
			int j;
			for (j=0; j < testCategory[i].length; j++) {
				URL url = ((IOptionCategory)testCategory[i][j]).getIconPath();

				switch ( iconComparator.Compare(i+1, j+1) ) {
					case IconComparator.None:
						assertEquals(url, null);
						break;
					case IconComparator.One:
						assertEquals(url, icon1);
						break;
					case IconComparator.Two:
						assertEquals(url, icon2);
						break;
				}
			}
		}		
	}
	
	private void assertToolIcon(ITool tool, URL url)
	{
		assertTrue(tool instanceof IOptionCategory);		
		IOptionCategory toolCategory = (IOptionCategory)tool;
		assertEquals(toolCategory.getIconPath(), url);
	}
	
	/**
	 * Test whether option objects have value handlers as expected
	 */
	public void testValueHandlers() throws Exception {
			
		// Set up the environment
		if ( testIsSetup == false ) {
			setupDefaultProject();
			setupTestEnvironment();
		}
		
		int i;
		for (i=0; i < testOption.length; i++) {
			
			int j;
			for (j=0; j < testOption[i].length; j++) {
				IOption option = (IOption)testOption[i][j];
				IManagedOptionValueHandler handler = option.getValueHandler();
				String handlerExtraArg = option.getValueHandlerExtraArgument();
				
				switch ( valueHandlerComparator.Compare(i+1, j+1) ) {
					case ValueHandlerComparator.MBS:
						assertEquals(ManagedOptionValueHandler.getManagedOptionValueHandler(), handler);
						break;
					case ValueHandlerComparator.TEST:
						assertTrue(handler instanceof TestValueHandler);
						assertEquals(valueHandlerComparator.getArg(), handlerExtraArg);
						break;
				}
			}
		}		
	}
	
	/**
	 * Test whether OptionCatgeory.getOptions(IConfiguration) works as expected
	 */
	public void testOptions() throws Exception {
			
		// Set up the environment
		if ( testIsSetup == false ) {
			setupDefaultProject();
			setupTestEnvironment();
		}
		// Go over all option Categories
		int i;
		int j;
		Object[][] results;
		for (i=0; i < testCategory.length; i++) {			
			for (j=0; j < testCategory[i].length; j++) {
				results = ((IOptionCategory)testCategory[i][j]).getOptions(testConfig);
				// Go over results and check the following: 
				// A) results[k][0] must be the parent tool or toolChain
				// B) results[k][1] must be in testOption[i]
				for (int k=0; k < results[0].length; k++) {
					if (results[k][0] == null) {
						assertNull(results[k][1]);
						break;
					}
					// A) results[k][0] must be the parent tool or toolChain
					switch (i) {
					case 0:
					case 1:
					case 2:
					case 3:
						// Tool
						assertTrue(results[k][0] instanceof ITool);
						assertEquals(results[k][0], testTools[i]);
						break;
					default:
						// ToolChain
						assertTrue(results[k][0] instanceof IToolChain);
						assertEquals(results[k][0], testToolChain);
						break;
					}					
					// B) results[k][1] must be in testOption[i]
					//    and its parent must be testCategory[i][j]
					boolean found = false;
					for (int l=0; l < testOption[i].length; l++) {
						if (testOption[i][l] == results[k][1])
						{
							found = true;
							break;
						}
					}
					assertTrue(found);
					assertEquals(((IOption)results[k][1]).getCategory(), testCategory[i][j]);
				}
			}
		}		
	}
	
	/**
	 * Test whether all the other tests work as expected for 
	 * non-extension configurations.
	 */
	public void testConfiguration() throws Exception {
		
		// Set up the environment
		setupProject();
		setupTestEnvironment();
		// Rerun the other tests, without setting the test up again
		testIsSetup = true;		
		testIcons();
		testValueHandlers();
		testOptions();
	}	
}
