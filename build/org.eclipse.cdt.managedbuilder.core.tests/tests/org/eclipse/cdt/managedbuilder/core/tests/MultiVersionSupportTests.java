/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;


import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.runtime.NullProgressMonitor;


public class MultiVersionSupportTests extends TestCase {
	static IProject proj = null;
	static IManagedProject mproj = null;
	
	IConfiguration[] cfgs = null;	
	private IWorkspace worksp;	
	private boolean windows;
	
	public MultiVersionSupportTests() { super(); }
	public MultiVersionSupportTests(String name) { super(name); }

	public static Test suite() {
		TestSuite suite = new TestSuite(MultiVersionSupportTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTest(new MultiVersionSupportTests("testProjectConverterConvenienceRoutines"));	//$NON-NLS-1$
		suite.addTest(new MultiVersionSupportTests("testConfigurationNameProvider"));	//$NON-NLS-1$
		suite.addTest(new MultiVersionSupportTests("testConfigurationDescription"));	//$NON-NLS-1$
		suite.addTest(new MultiVersionSupportTests("testVersionInfo"));	//$NON-NLS-1$
		suite.addTest(new MultiVersionSupportTests("testVersionsSupportedAttribute"));	//$NON-NLS-1$

		//$JUnit-END$
		return suite;
	}

	
	/*
	 * This function tests the functionality of Project converter convenience routines
	 * defined in ManagedBuildManager class. These convenience routines are generic routines
	 * anyone can use them 
	 * 		  i) To find out whether any converters are available for a given Build Object,
	 * 		 ii) To get a list of converters
	 * 		iii) To convert a Build Object
	 */
	
	public void testProjectConverterConvenienceRoutines() throws Exception {
		String projectName = "TestProjectConverterConvenienceRoutines";			//$NON-NLS-1$
		String projectTypeId = "testProjectConverterConvenienceRoutines.exe";	//$NON-NLS-1$

		doInit(projectName, projectTypeId);
		
		// It should contain only one configuration.
		assertEquals(cfgs.length, 1);
		
		IToolChain toolChain = cfgs[0].getToolChain();
		
		// Check whether this tool chain has converters using generic convenience routine.
		boolean hasConverters = ManagedBuildManager.hasTargetConversionElements(toolChain);
		
		// Expected value is 'true'
		assertEquals(hasConverters, true);
		
		// Get the list of available converters for ths tool chain using generic convenience routine
		Map converters = ManagedBuildManager.getConversionElements(toolChain);
		
		// Expected value for the number of converters available is '1'
		assertEquals(converters.size(), 1);

		return;
	}
	
	/*
	 * This function tests the functionality of configuration name provider.
	 * Using this functionality, Tool Integrator can provide configuration names dynamically
	 * based on the OS, architecture.
	 * 
	 * In plugin manifest file, there are 4 configurations defined for the project type
	 * "cdt.managedbuild.target.testMultipleVersions.exe". Two configurations are defined with "Debug"
	 * as their name and other two are defined with "Release" as their name. But user will see the 
	 * configuration names as "Debug", "Release", "Debug_ia64" and "Release_ia64" while creating a project.
	 * 
	 */
	public void testConfigurationNameProvider()throws Exception {
		String [] expectedConfigNames = { "Debug","Release","Debug_ia64","Release_ia64" };	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String [] actualConfigNames = new String[4];
		// create managed project
		String projectName = "TestConfigurationNameProvider";	//$NON-NLS-1$
		String projectTypeId = "cdt.managedbuild.target.testMultipleVersions.exe";	//$NON-NLS-1$
		doInit(projectName, projectTypeId);
		
		// Check the configuration names
		cfgs = mproj.getConfigurations();
		
		// Get the actual configuration names
		for (int i = 0; i < cfgs.length; i++) {
			actualConfigNames[i] = cfgs[i].getName();		
		}
		
		// Compare the expected and actual,
		for (int i = 0; i < expectedConfigNames.length; i++) {
			assertTrue( isArrayContains(actualConfigNames, expectedConfigNames[i]) );			
		}
		return;
	}
	
	/*
	 * This function tests whether the newly added attribute 'description' is retrived from plugin
	 * manifest file correctly or not. There are 4 configurations defined with different description
	 * each configuration. This function checks the description of each configuration against
	 * expected description after creating a project.
	 */
	
	public void testConfigurationDescription()throws Exception {
		String [] expectedConfigDescriptions = { "debug configuration for IA32 windows","release configuration for IA32 windows","debug configuration for IA64 windows","release configuration for IA64 windows" };	//$NON-NLS-1$
		String [] actualConfigDescriptions = new String[4];
		// create managed project
		String projectName = "TestConfigurationDescription";	//$NON-NLS-1$
		String projectTypeId = "cdt.managedbuild.target.testMultipleVersions.exe";	//$NON-NLS-1$

		doInit(projectName, projectTypeId);
		
		// Check the configuration descriptions
		cfgs = mproj.getConfigurations();
		
		// Get the actual configuration names
		for (int i = 0; i < cfgs.length; i++) {
			actualConfigDescriptions[i] = cfgs[i].getDescription();		
		}
		
		// Compare the expected and actual,
		for (int i = 0; i < expectedConfigDescriptions.length; i++) {
			assertTrue( isArrayContains(actualConfigDescriptions, expectedConfigDescriptions[i]) );			
		}
		return;
	}

	
	/*
	 * This function tests whether the version information stored with in the 'id' of ToolChain/Tool/Builder
	 * is retrived from plugin manifest file correctly or not. 
	 */
	
	public void testVersionInfo()throws Exception {

		String projectName = "TestVersionInfo";	//$NON-NLS-1$
		String projectTypeId = "cdt.managedbuild.target.testMultipleVersions.exe";	//$NON-NLS-1$

		// create managed project
		doInit(projectName, projectTypeId);
		
		// Check the configuration descriptions
		cfgs = mproj.getConfigurations();
		
		// Check the version information of ToolChain/Tool/Builder defined under 'Release' Configuration.
		
		// It should return the version as '0.0.4' as there is no version information defined
		// for ToolChain/Tool/Builder. The version '0.0.4' is the default version for CDT3.0
		
		// Release configuration 'id' : "cdt.managedbuild.config.testMultipleVersions.exe.release.1"			
		IConfiguration releaseConfig = mproj.getConfiguration("cdt.managedbuild.config.testMultipleVersions.exe.release.1");	//$NON-NLS-1$

		// the toolchain version should be '0.0.4' as this is default value
		assertEquals(releaseConfig.getToolChain().getVersion().toString(), "0.0.4");	//$NON-NLS-1$
		
		// Get the tools of this toolChain and verify the version of each tool
		// Each tool version in release configuration should be "0.0.4"
		ITool [] tools = releaseConfig.getToolChain().getTools();
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			assertEquals(tool.getVersion().toString(),"0.0.4");	//$NON-NLS-1$
		}
		
		// check the builder version
		IBuilder builder = releaseConfig.getToolChain().getBuilder();
		assertEquals(builder.getVersion().toString(),"0.0.4");	//$NON-NLS-1$
		
		// Check the versions of ToolChain/Tool/Builder in Debug Configuration
		// Expected version: "1.1.0" for all ToolChain/Tool(s)/Builder.
		
		// Get the 'Debug' Configuration
		// Debug configuration 'id' : "cdt.managedbuild.config.testMultipleVersions.exe.debug.0"			
		IConfiguration debugConfig = mproj.getConfiguration("cdt.managedbuild.config.testMultipleVersions.exe.debug.0");	//$NON-NLS-1$

		// the toolchain version should be '1.1.0' as that's what defined in plugin manifest file
		assertEquals(debugConfig.getToolChain().getVersion().toString(), "1.1.0");	//$NON-NLS-1$
		
		// Get the tools of this toolChain and verify the version of each tool
		// Each tool version in debug configuration should be "1.1.0"
		tools = debugConfig.getToolChain().getTools();
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			assertEquals(tool.getVersion().toString(),"1.1.0");	//$NON-NLS-1$
		}
		
		// check the builder version
		builder = debugConfig.getToolChain().getBuilder();
		assertEquals(builder.getVersion().toString(),"1.1.0");	//$NON-NLS-1$

		// Get the 'Debug64' Configuration
		// Debug configuration 'id' : "cdt.managedbuild.config.testMultipleVersions.exe.debug.0"			
		IConfiguration debug64Config = mproj.getConfiguration("cdt.managedbuild.config.testMultipleVersions.exe.debug64.2");	//$NON-NLS-1$

		// the toolchain version should be '2.2.0' as that's what defined in plugin manifest file
		assertEquals(debug64Config.getToolChain().getVersion().toString(), "2.2.0");	//$NON-NLS-1$
		
		// Get the tools of this toolChain and verify the version of each tool
		// Each tool version in debug64 configuration should be "5.0.9" (defined in plugin manifest file)
		tools = debug64Config.getToolChain().getTools();
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			assertEquals(tool.getVersion().toString(),"5.0.9");	//$NON-NLS-1$
		}
		
		// check the builder version , expected value : "3.2.1"  defined in plugin manifest file
		builder = debug64Config.getToolChain().getBuilder();
		assertEquals(builder.getVersion().toString(),"3.2.1");	//$NON-NLS-1$

		return;
	}

	/*
	 * This function tests whether the attribute 'versionsSupported' of
	 * ToolChain/Tool/Builder elements is retrived from plugin manifest file correctly or not.
	 */
	
	public void testVersionsSupportedAttribute() throws Exception {
		// create managed project
		String projectName = "TestVersionSupportAttribute";	//$NON-NLS-1$
		String projectTypeId = "cdt.managedbuild.target.testMultipleVersions.exe";	//$NON-NLS-1$

		doInit(projectName, projectTypeId);

		// Check the configuration descriptions
		cfgs = mproj.getConfigurations();
		
		// Check the version information of ToolChain/Tool/Builder defined under 'Release' Configuration.
		
		// It should return the version as '0.0.4' as there is no version information defined
		// for ToolChain/Tool/Builder. The version '0.0.4' is the default version for CDT3.0
		
		// Release configuration 'id' : "cdt.managedbuild.config.testMultipleVersions.exe.release64.3"			
		IConfiguration releaseConfig = mproj.getConfiguration("cdt.managedbuild.config.testMultipleVersions.exe.release64.3");	//$NON-NLS-1$

		// Get the tool chain
		IToolChain toolChain = releaseConfig.getToolChain();

		//Get the 'versionsSupported' attribute of toolChain
		String toolChainVersionsSupported = toolChain.getVersionsSupported();
		
		// Compare with the expected value "1.2.0,2.0.0,2.1.3"
		assertEquals(toolChainVersionsSupported,"1.2.0,2.0.0,2.1.3");	//$NON-NLS-1$
		
		// Get the builder 
		IBuilder builder = releaseConfig.getToolChain().getBuilder();
		
		// Get the 'versionsSupported' attribute of builder
		String versionsSupported = builder.getVersionsSupported();
		
		// Compare with expected value "2.0.0,2.1.1,3.0.3"
		assertEquals(versionsSupported,"2.0.0,2.1.1,3.0.3");	//$NON-NLS-1$

	}
	
	static void createManagedProject(String name, String projectTypeId) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		proj = root.getProject(name); 
	
		if (proj.exists()) {
			mproj = ManagedBuildManager.getBuildInfo(proj).getManagedProject();
		} else {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			try {
				workspace.setDescription(workspaceDesc);
				proj = CCorePlugin.getDefault().createCProject(workspace.newProjectDescription(proj.getName()), 
					proj, new NullProgressMonitor(), MakeCorePlugin.MAKE_PROJECT_ID);
			
				// 	add ManagedBuildNature
				IManagedBuildInfo info = ManagedBuildManager.createBuildInfo(proj);
				info.setValid(true);
				ManagedCProjectNature.addManagedNature(proj, null);
				ManagedCProjectNature.addManagedBuilder(proj, null);

				ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(proj, true);
				desc.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
				desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
				desc.saveProjectData();
			} catch (CoreException e) {
				fail("Cannot create project: " + e.getLocalizedMessage()); //$NON-NLS-1$
			}				
			// Call this function just to avoid init problems in getProjectType();   
			IProjectType[] projTypes = ManagedBuildManager.getDefinedProjectTypes();
			IProjectType projType = ManagedBuildManager.getProjectType(projectTypeId);
			assertNotNull(projType);
			try {
				mproj = ManagedBuildManager.createManagedProject(proj, projType);
			} catch (BuildException e) {}
			ManagedBuildManager.setNewProjectVersion(proj);
			IConfiguration[] cfgs = projType.getConfigurations();
			
			for (int i = 0; i < cfgs.length; i++) { // sic ! from 1
				mproj.createConfiguration(cfgs[i], cfgs[i].getId() + "." + i); //$NON-NLS-1$
			}
			if( cfgs.length > 0)
				ManagedBuildManager.setDefaultConfiguration(proj, cfgs[0]);
			else
				ManagedBuildManager.setDefaultConfiguration(proj, null);
		}
		// open project w/o progress monitor; no action performed if it's opened
		try {
			proj.open(null);
		} catch (CoreException e) {}				
	}

	/*
	 *  isArrayContains() 
	 */
	private boolean isArrayContains(String[] actualConfigNames, String name) {
		if (actualConfigNames != null) {
			for (int i = 0; i < actualConfigNames.length; i++) {
				if ( ( actualConfigNames[i] != null) && (actualConfigNames[i].equals(name)) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 *  doInit() - call it at the beginning of every test
	 *
	 */
	private void doInit(String projectName, String projectTypeId) {
		createManagedProject(projectName, projectTypeId);
		assertNotNull(proj);
		assertNotNull(mproj);
		worksp = proj.getWorkspace();
		assertNotNull(worksp);
		cfgs = mproj.getConfigurations();
		assertNotNull(cfgs);		
	}
}
