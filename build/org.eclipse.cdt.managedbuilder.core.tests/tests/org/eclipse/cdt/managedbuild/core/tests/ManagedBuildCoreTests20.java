/**********************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuild.core.tests;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.core.Option;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;

/*
 *  These tests exercise CDT 2.0 manifest file functionality 
 */
public class ManagedBuildCoreTests20 extends TestCase {
	private static final boolean boolVal = true;
	private static final String testConfigId = "test.config.override";
	private static final String testConfigName = "Tester";
	private static final String enumVal = "Another Enum";
	private static final String[] listVal = {"_DEBUG", "/usr/include", "libglade.a"};
	private static final String newExt = "wen";
	private static final String projectName = "ManagedBuildTest";
	private static final String projectName2 = "ManagedBuildTest2";
	private static final String projectRename = "ManagedBuildRedux";
	private static final String rootExt = "toor";
	private static final String stringVal = "-c -Wall";
	private static final String anotherStringVal = "thevalue";
	private static final String subExt = "bus";
		
	public ManagedBuildCoreTests20(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildCoreTests20.class.getName());
		
		suite.addTest(new ManagedBuildCoreTests20("testExtensions"));
		suite.addTest(new ManagedBuildCoreTests20("testProjectCreation"));
		suite.addTest(new ManagedBuildCoreTests20("testConfigurations"));
		suite.addTest(new ManagedBuildCoreTests20("testConfigurationReset"));
		suite.addTest(new ManagedBuildCoreTests20("testConfigBuildArtifact"));
		suite.addTest(new ManagedBuildCoreTests20("testMakeCommandManipulation"));
		suite.addTest(new ManagedBuildCoreTests20("testScannerInfoInterface"));
		suite.addTest(new ManagedBuildCoreTests20("testBug43450"));
		suite.addTest(new ManagedBuildCoreTests20("testProjectRename"));
		suite.addTest(new ManagedBuildCoreTests20("testErrorParsers"));
		suite.addTest(new ManagedBuildCoreTests20("cleanup"));
		
		return suite;
	}

	/**
	 * Navigates through the build info as defined in the extensions
	 * defined in this plugin
	 */
	public void testExtensions() throws Exception {
		IProjectType testRoot = null;
		IProjectType testSub = null;
		IProjectType testSubSub = null;
		IProjectType testForwardChild = null;
		IProjectType testForwardParent = null;
		IProjectType testForwardGrandchild = null;
		int numTypes = 0;
		
		// Note secret null parameter which means just extensions
		IProjectType[] projTypes = ManagedBuildManager.getDefinedProjectTypes();

		for (int i = 0; i < projTypes.length; ++i) {
			IProjectType type = projTypes[i];
			
			if (type.getName().equals("Test Root")) {
				testRoot = type;
				checkRootProjectType(testRoot);
			} else if (type.getName().equals("Test Sub")) {
				testSub = type;
				checkSubProjectType(testSub);
			} else if (type.getName().equals("Test Sub Sub")) {
				testSubSub = type;
				checkSubSubProjectType(testSubSub);
			} else if (type.getName().equals("Forward Child")) {
				testForwardChild = type;
			} else if (type.getName().equals("Forward Parent")) {
				testForwardParent = type;
			} else if (type.getName().equals("Forward Grandchild")) {
				testForwardGrandchild = type;
			} else if (type.getId().startsWith("test.provider.Test_")) {
				numTypes++;
				checkProviderProjectType(type);
			}
		}
		// check that the forward references are properly resolved.
		assertNotNull(testForwardChild);
		assertNotNull(testForwardParent);
		assertNotNull(testForwardGrandchild);
		checkForwardProjectTypes(testForwardParent, testForwardChild, testForwardGrandchild);
		
		// check that the proper number of projectTypes were dynamically provided
		assertEquals(3, numTypes);
		
		// All these project types are defines in the plugin files, so none
		// of them should be null at this point
		assertNotNull(testRoot);
		assertNotNull(testSub);
		assertNotNull(testSubSub);
	}

	/**
	 * This test exercises the interface the <code>IConfiguration</code> exposes to manipulate 
	 * its make command.
	 */
	public void testMakeCommandManipulation () {
		String oldMakeCmd = "make";
		String newMakeCmd = "Ant";
		
		// Open the test project
		IProject project = null;
		try {
			project = createProject(projectName);
			IProjectDescription description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}
		} catch (CoreException e) {
			fail("Failed to open project in 'testMakeCommandManipulation': " + e.getLocalizedMessage());
		}
		assertNotNull(project);
		
		// Now get the default configuration
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertNotNull(info);
		IManagedProject managedProj = info.getManagedProject();
		assertNotNull(managedProj);
		IConfiguration defaultConfig = info.getDefaultConfiguration();
		assertNotNull(defaultConfig);
		
		// Does it have a default build command
		assertFalse(defaultConfig.hasOverriddenBuildCommand());
		assertEquals(oldMakeCmd, defaultConfig.getBuildCommand());
		
		// Change it
		defaultConfig.setBuildCommand(newMakeCmd);
		assertEquals(newMakeCmd, defaultConfig.getBuildCommand());
		assertTrue(defaultConfig.hasOverriddenBuildCommand());
		
		// Reset it
		defaultConfig.setBuildCommand(null);
		assertFalse(defaultConfig.hasOverriddenBuildCommand());
		assertEquals(oldMakeCmd, defaultConfig.getBuildCommand());
		
		ManagedBuildManager.saveBuildInfo(project, false);
	}
	
	
	/**
	 * The purpose of this test is to exercise the build path info interface.
	 * To get to that point, a new project/config has to be created in the test
	 * project and the default configuration changed.
	 *  
	 * @throws CoreException
	 */
	public void testScannerInfoInterface(){
		// Open the test project
		IProject project = null;
		try {
			project = createProject(projectName);
			IProjectDescription description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}
		} catch (CoreException e) {
			fail("Failed to open project in 'testScannerInfoInterface': " + e.getLocalizedMessage());
		}
		
		//These are the expected path settings
		 final String[] expectedPaths = new String[5];

		 // This first path is a built-in, so it will not be manipulated by build manager
		 expectedPaths[0] = "/usr/gnu/include";
		 expectedPaths[1] = (new Path("/usr/include")).toOSString();
		 expectedPaths[2] = (new Path("/opt/gnome/include")).toOSString();
		 expectedPaths[3] = (new Path("C:\\home\\tester/include")).toOSString();
		 expectedPaths[4] = project.getLocation().append( "Sub Config\\\"..\\includes\"" ).toOSString();
		 
		// Create a new managed project based on the sub project type
		IProjectType projType = ManagedBuildManager.getExtensionProjectType("test.sub");
		assertNotNull(projType);
		
		// Create the managed-project (.cdtbuild) for our project
		IManagedProject newProject = null;
		try {
			newProject = ManagedBuildManager.createManagedProject(project, projType);
		} catch (BuildException e) {
			fail("Failed creating new project: " + e.getLocalizedMessage());
		}
		assertNotNull(newProject);
		ManagedBuildManager.setNewProjectVersion(project);
	
		// Copy over the configs
		IConfiguration[] baseConfigs = projType.getConfigurations();
		for (int i = 0; i < baseConfigs.length; ++i) {
			newProject.createConfiguration(baseConfigs[i], baseConfigs[i].getId() + "." + i);
		}
		
		// Change the default configuration to the sub config
		IConfiguration[] configs = newProject.getConfigurations();
		assertEquals(4, configs.length);
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		buildInfo.setDefaultConfiguration(newProject.getConfiguration(configs[0].getId()));

		// Save, close, reopen
		ManagedBuildManager.saveBuildInfo(project, true);
		ManagedBuildManager.removeBuildInfo(project);
		try {
			project.close(null);
		} catch (CoreException e) {
			fail("Failed on project close: " + e.getLocalizedMessage());
		}
		try {
			project.open(null);
		} catch (CoreException e) {
			fail("Failed on project open: " + e.getLocalizedMessage());
		}
		buildInfo = ManagedBuildManager.getBuildInfo(project);
		
		// Use the plugin mechanism to discover the supplier of the path information
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID + ".ScannerInfoProvider");
		if (extensionPoint == null) {
			fail("Failed to retrieve the extension point ScannerInfoProvider.");
		}

		// Find the first IScannerInfoProvider that supplies build info for the project
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		assertNotNull(provider);
		
		// Now subscribe (note that the method will be called after a change
		provider.subscribe(project, new IScannerInfoChangeListener () {
			public void changeNotification(IResource project, IScannerInfo info) {
				// Test the symbols: expect "BUILTIN" from the manifest, and "DEBUG" and "GNOME=ME"
				// from the overidden settings 
				Map definedSymbols = info.getDefinedSymbols();
				assertTrue(definedSymbols.containsKey("BUILTIN"));
				assertTrue(definedSymbols.containsKey("DEBUG"));
				assertTrue(definedSymbols.containsKey("GNOME"));
				assertTrue(definedSymbols.containsValue("ME"));
				assertEquals((String)definedSymbols.get("BUILTIN"), "");
				assertEquals((String)definedSymbols.get("DEBUG"), "");
				assertEquals((String)definedSymbols.get("GNOME"), "ME");
				// Test the includes path
				String[] actualPaths = info.getIncludePaths();
				assertTrue(Arrays.equals(expectedPaths, actualPaths));
			}
		});

		// Check the build information before we change it
		IScannerInfo currentSettings = provider.getScannerInformation(project);
		
		Map currentSymbols = currentSettings.getDefinedSymbols();
		// It should simply contain the built-in
		assertTrue(currentSymbols.containsKey("BUILTIN"));
		assertEquals((String)currentSymbols.get("BUILTIN"), "");
		String[] currentPaths = currentSettings.getIncludePaths();
		assertTrue(Arrays.equals(expectedPaths, currentPaths));

		// Add some defined symbols programmatically
		String[] expectedSymbols = {"DEBUG", "GNOME = ME "};
		IConfiguration defaultConfig = buildInfo.getDefaultConfiguration();
		ITool[] tools = defaultConfig.getTools();
		ITool subTool = null;
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			if("tool.sub".equalsIgnoreCase(tool.getSuperClass().getId())) {
				subTool = tool;
				break;
			}
		}
		assertNotNull(subTool);
		IOption symbolOpt = null;
		IOption[] opts = subTool.getOptions();
		for (int i = 0; i < opts.length; i++) {
			IOption option = opts[i];
			try {
				if (option.getValueType() == IOption.PREPROCESSOR_SYMBOLS) {
					symbolOpt = option;
					break;
				}
			} catch (BuildException e) {
				fail("Failed getting option value-type: " + e.getLocalizedMessage());
			}
		}
		assertNotNull(symbolOpt);
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertFalse(info.isDirty());
		ManagedBuildManager.setOption(defaultConfig, subTool, symbolOpt, expectedSymbols);
		assertTrue(info.isDirty());
		info.setDirty(false);
		assertFalse(info.isDirty());
	}
	
	/**
	 * Create a new configuration based on one defined in the plugin file.
	 * Overrides all of the configuration settings. Saves, closes, and reopens 
	 * the project. Then calls a method to check the overridden options.
	 * 
	 * Tests creating a new configuration.
	 * Tests setting options.
	 * Tests persisting overridden options between project sessions.
	 * 
	 */
	public void testConfigurations() throws CoreException, BuildException {
		final String rootName = "Root Config";
		final String overrideName = "Root Override Config";
		final String completeOverrideName = "Complete Override Config";
		final String toolCmd = "doIt";
		final String newCmd = "never";
		
		// Open the test project
		IProject project = createProject(projectName);
		IProjectDescription description = project.getDescription();
		// Make sure it has a managed nature
		if (description != null) {
			assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
		}
		
		// Make sure there is a ManagedProject with 3 configs
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject managedProj = info.getManagedProject();
		IConfiguration[] definedConfigs = managedProj.getConfigurations(); 		
		assertEquals(3, definedConfigs.length);
		IConfiguration baseConfig = definedConfigs[0];
		assertEquals(definedConfigs[0].getName(), rootName);
		assertEquals(definedConfigs[1].getName(), overrideName);
		assertEquals(definedConfigs[2].getName(), completeOverrideName);
		
		// Create a new configuration and test the rename function
		IConfiguration newConfig = managedProj.createConfigurationClone(baseConfig, testConfigId);
		assertEquals(4, managedProj.getConfigurations().length);
		newConfig.setName(testConfigName);
		assertEquals(newConfig.getId(), testConfigId);
		assertEquals(newConfig.getName(), testConfigName);

		// There is only one tool
		ITool[] definedTools = newConfig.getTools();
		assertEquals(1, definedTools.length);
		ITool rootTool = definedTools[0];
		
		// Test changing its command
		assertEquals(rootTool.getToolCommand(), toolCmd);
		newConfig.setToolCommand(rootTool, newCmd);
		assertEquals(rootTool.getToolCommand(), newCmd);
		
		// Override options in the new configuration
		IOptionCategory topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		Object[][] options = topCategory.getOptions(newConfig);
		int i;
		for (i=0; i<options.length; i++)
			if (options[i][0] == null) break;
		assertEquals(2, i);
		ITool tool = (ITool)options[0][0];
		IOption option = (IOption)options[0][1];
		ManagedBuildManager.setOption(newConfig, tool, option, listVal);
		option = (IOption)options[1][1];
		ManagedBuildManager.setOption(newConfig, tool, option, boolVal);

		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		options = categories[0].getOptions(newConfig);
		for (i=0; i<options.length; i++)
			if (options[i][0] == null) break;
		assertEquals(4, i);
		tool = (ITool)options[0][0];
		option = (IOption)options[0][1];
		ManagedBuildManager.setOption(newConfig, tool, option, stringVal);
		option = (IOption)options[1][1];
		ManagedBuildManager.setOption(newConfig, tool, option, anotherStringVal);
		option = (IOption)options[2][1];
		ManagedBuildManager.setOption(newConfig, tool, option, enumVal);
		option = (IOption)options[3][1];
		ManagedBuildManager.setOption(newConfig, tool, option, "False");

		// Save, close, reopen and test again
		ManagedBuildManager.saveBuildInfo(project, false);
		ManagedBuildManager.removeBuildInfo(project);
		project.close(null);
		project.open(null);

		// Test the values in the new configuration
		checkOptionReferences(project);
		
		// Now delete the new configuration and test the managed project
		info = ManagedBuildManager.getBuildInfo(project);
		managedProj = info.getManagedProject();
		definedConfigs = managedProj.getConfigurations(); 		
		assertEquals(4, definedConfigs.length);
		managedProj.removeConfiguration(testConfigId);
		definedConfigs = managedProj.getConfigurations(); 		
		assertEquals(3, definedConfigs.length);
		assertEquals(definedConfigs[0].getName(), rootName);
		assertEquals(definedConfigs[1].getName(), overrideName);
		ManagedBuildManager.saveBuildInfo(project, false);
	}
	
	public void testConfigurationReset() {
		// Open the test project
		IProject project = null;
		try {
			project = createProject(projectName);
			IProjectDescription description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}
		} catch (CoreException e) {
			fail("Failed to open project: " + e.getLocalizedMessage());
		}

		// Get the default configuration
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertNotNull(info);
		IManagedProject managedProj = info.getManagedProject();
		assertNotNull(managedProj);
		IConfiguration defaultConfig = info.getDefaultConfiguration();
		assertNotNull(defaultConfig);
		
		// See if it still contains the overridden values (see testProjectCreation())
		try {
			checkRootManagedProject(managedProj, "z");
		} catch (BuildException e1) {
			fail("Overridden root managed project check failed: " + e1.getLocalizedMessage());
		}
		
		// Reset the config and retest
		ManagedBuildManager.resetConfiguration(project, defaultConfig);
		ManagedBuildManager.saveBuildInfo(project, false);
		try {
			checkRootManagedProject(managedProj, "x");
		} catch (BuildException e2) {
			fail("Reset root managed project check failed: " + e2.getLocalizedMessage());
		}
	}
	
	/**
	 * @throws CoreException
	 * @throws BuildException
	 */
	public void testProjectCreation() throws BuildException {
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
		IProjectType projType = ManagedBuildManager.getExtensionProjectType("test.root");
		assertNotNull(projType);
		
		// Create the managed-project (.cdtbuild) for our project that builds a dummy executable
		IManagedProject newProject = ManagedBuildManager.createManagedProject(project, projType);
		assertEquals(newProject.getName(), projType.getName());
		assertFalse(newProject.equals(projType));
		ManagedBuildManager.setNewProjectVersion(project);

		// Copy over the configs
		IConfiguration defaultConfig = null;
		IConfiguration[] configs = projType.getConfigurations();
		for (int i = 0; i < configs.length; ++i) {
			// Make the first configuration the default 
			if (i == 0) {
				defaultConfig = newProject.createConfiguration(configs[i], projType.getId() + "." + i);
			} else {
				newProject.createConfiguration(configs[i], projType.getId() + "." + i);
			}
		}
		ManagedBuildManager.setDefaultConfiguration(project, defaultConfig);

		String buildArtifactName = projectName;
		defaultConfig.setArtifactName(buildArtifactName);
		defaultConfig.setArtifactExtension(newExt);
		
		// Initialize the path entry container
		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(project);
		if (initResult.getCode() != IStatus.OK) {
			fail("Initializing build information failed for: " + project.getName() + " because: " + initResult.getMessage());
		}
		
		// Now test the results out
		checkRootManagedProject(newProject, "x");
		
		// Override the "String Option in Category" option value
		configs = newProject.getConfigurations();
		ITool[] tools = configs[0].getTools();
		IOptionCategory topCategory = tools[0].getTopOptionCategory();
		IOptionCategory[] categories = topCategory.getChildCategories();
		Object[][] options = categories[0].getOptions(configs[0]);
		ITool tool = (ITool)options[0][0];
		IOption option = (IOption)options[0][1];
		configs[0].setOption(tool, option, "z");
		options = categories[0].getOptions((IConfiguration)null);
		tool = (ITool)options[0][0];
		option = (IOption)options[0][1];
		assertEquals("x", option.getStringValue());
		options = categories[0].getOptions(configs[0]);
		tool = (ITool)options[0][0];
		option = (IOption)options[0][1];
		assertEquals("z", option.getStringValue());
		
		// Save, close, reopen and test again
		ManagedBuildManager.saveBuildInfo(project, true);
		ManagedBuildManager.removeBuildInfo(project);
		try {
			project.close(null);
		} catch (CoreException e) {
			fail("Failed on project close: " + e.getLocalizedMessage());
		}
		try {
			project.open(null);
		} catch (CoreException e) {
			fail("Failed on project open: " + e.getLocalizedMessage());
		}
		
		// Test that the default config was remembered
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertEquals(defaultConfig.getId(), info.getDefaultConfiguration().getId());

		// Check the rest of the default information
		checkRootManagedProject(newProject, "z");
		
		// Now test the information the makefile builder needs
		checkBuildTestSettings(info);
		ManagedBuildManager.removeBuildInfo(project);
	}

	/**
	 * Tests that bugzilla 44159 has been addressed. After a project was renamed, the 
	 * build information mistakenly referred to the old project as its owner. This
	 * caused a number of searches on the information to fail. In this bug, it was the 
	 * list of tools that could not be determined. In other cases, the information 
	 * retrieval caused NPEs because the old owner no longer existed.
	 */
	public void testProjectRename() {
		// Open the test project
		IProject project = null;
		try {
			project = createProject(projectName);
			IProjectDescription description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}
		} catch (CoreException e) {
			fail("Failed to open project: " + e.getLocalizedMessage());
		}
		
		// Rename the project
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();	
		IResource newResource = workspaceRoot.findMember(projectRename);
		if (newResource != null) {
			try {
				newResource.delete(IResource.KEEP_HISTORY, new NullProgressMonitor());
			} catch (CoreException e) {
				fail("Failed to delete old project " + projectRename + ": " + e.getLocalizedMessage());
			}		
		}
		IProjectDescription description = null;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			fail("Failed to find project descriptor for " + projectName + ": " + e.getLocalizedMessage());
		}
		description.setName(projectRename);
		try {
			project.move(description, IResource.FORCE | IResource.SHALLOW, new NullProgressMonitor());
		} catch (CoreException e) {
			fail("Failed to rename project: " + e.getLocalizedMessage());
		}
		try {
			project = createProject(projectRename);
			description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}
		} catch (CoreException e) {
			fail("Failed to open renamed project: " + e.getLocalizedMessage());
		}

		// By now the project should have 3 configs
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject managedProj = info.getManagedProject();
		IConfiguration[] definedConfigs = managedProj.getConfigurations(); 		
		assertEquals(4, definedConfigs.length);
		IConfiguration baseConfig = definedConfigs[1];
		
		// There is only one tool
		ITool[] definedTools = baseConfig.getTools();
		assertEquals(1, definedTools.length);
		ITool rootTool = definedTools[0];
		
		// Get the options (2) in top category and (4) in its child
		IOptionCategory topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		Object[][] options = topCategory.getOptions(baseConfig);
		int i;
		for (i=0; i<options.length; i++)
			if (options[i][0] == null) break;
		assertEquals(2, i);
		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		options = categories[0].getOptions(baseConfig);
		for (i=0; i<options.length; i++)
			if (options[i][0] == null) break;
		assertEquals(4, i);
		
		// Set the name back
		newResource = workspaceRoot.findMember(projectName);
		if (newResource != null) {
			try {
				newResource.delete(IResource.KEEP_HISTORY, new NullProgressMonitor());
			} catch (CoreException e) {
				fail("Failed to delete old project " + projectName + ": " + e.getLocalizedMessage());
			}		
		}
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			fail("Failed to find project descriptor for " + projectRename + ": " + e.getLocalizedMessage());
		}
		description.setName(projectName);
		try {
			project.move(description, IResource.FORCE | IResource.SHALLOW, new NullProgressMonitor());
		} catch (CoreException e) {
			fail("Failed to re-rename project: " + e.getLocalizedMessage());
		}
		try {
			project = createProject(projectName);
			description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}
		} catch (CoreException e) {
			fail("Failed to open re-renamed project: " + e.getLocalizedMessage());
		}

		// Do it all again
		info = ManagedBuildManager.getBuildInfo(project);
		managedProj = info.getManagedProject();
		definedConfigs = managedProj.getConfigurations(); 		
		assertEquals(4, definedConfigs.length);
		baseConfig = definedConfigs[1];
		definedTools = baseConfig.getTools();
		assertEquals(1, definedTools.length);
		rootTool = definedTools[0];
		topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		options = topCategory.getOptions(baseConfig);
		for (i=0; i<options.length; i++)
			if (options[i][0] == null) break;
		assertEquals(2, i);
		categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		options = categories[0].getOptions(baseConfig);
		for (i=0; i<options.length; i++)
			if (options[i][0] == null) break;
		assertEquals(4, i);
	}
	
	/**
	 * Tests the tool settings through the interface the makefile generator
	 * uses.
	 * 
	 * @param project
	 */
	private void checkBuildTestSettings(IManagedBuildInfo info) {
		String ext1 = "foo";
		String ext2 = "bar";
		String badExt = "cpp";
		String expectedOutput = "toor";
		String expectedCmd = "doIt";
		
		assertNotNull(info);
		assertEquals(info.getBuildArtifactName(), projectName);
		
		// There should be a default configuration defined for the project
		IManagedProject managedProj = info.getManagedProject();
		assertNotNull(managedProj);
		IConfiguration buildConfig = info.getDefaultConfiguration();
		assertNotNull(buildConfig);
		
		// Check that tool handles resources with extensions foo and bar by building a baz
		assertEquals(info.getOutputExtension(ext1), expectedOutput);
		assertEquals(info.getOutputExtension(ext2), expectedOutput);
		
		// Check that it ignores others based on filename extensions
		assertNull(info.getOutputExtension(badExt));
		
		// Now see what the tool command line invocation is for foo and bar
		assertEquals(info.getToolForSource(ext1), expectedCmd);
		assertEquals(info.getToolForSource(ext2), expectedCmd);
		// Make sure that there is no tool to build files of type foo and bar
		assertNull(info.getToolForConfiguration(ext1));
		assertNull(info.getToolForConfiguration(ext2));
		
		// There is no tool that builds toor
		assertNull(info.getToolForSource(expectedOutput));
		// but there is one that produces it
		assertEquals(info.getToolForConfiguration(expectedOutput), expectedCmd);
		
		// Now check the build flags
		assertEquals(info.getFlagsForSource(ext1), "-La -Lb z -e1 -nob");
		assertEquals(info.getFlagsForSource(ext1), info.getFlagsForSource(ext2));
		
	}
	
	/**
	 * Tests that overridden options are properly read into build model.
	 * Test that option values that are not overridden remain the same.
	 * 
	 * @param project The project to get build model information for.
	 * @throws BuildException
	 */
	private void checkOptionReferences(IProject project) throws BuildException {
		// Get the configs
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject managedProj = info.getManagedProject();
		IConfiguration[] definedConfigs = managedProj.getConfigurations(); 		
		assertEquals(4, definedConfigs.length);
		IConfiguration newConfig = managedProj.getConfiguration(testConfigId);
		assertNotNull(newConfig);

		// Now get the tool options and make sure the values are correct		
		ITool[] definedTools = newConfig.getTools();
		assertEquals(1, definedTools.length);
		ITool rootTool = definedTools[0];

		// Check that the options in the new config contain overridden values
		IOption[] rootOptions = rootTool.getOptions();
		assertEquals(6, rootOptions.length);
		// First is the new list
		assertEquals("List Option in Top", rootOptions[0].getName());
		assertEquals(IOption.STRING_LIST, rootOptions[0].getValueType());
		String[] list = rootOptions[0].getStringListValue();
		assertEquals(3, list.length);
		assertTrue(Arrays.equals(listVal, list));
		assertEquals(rootOptions[0].getCommand(), "-L");
		// Next option is a boolean in top
		assertEquals("Boolean Option in Top", rootOptions[1].getName());
		assertEquals(IOption.BOOLEAN, rootOptions[1].getValueType());
		assertEquals(boolVal, rootOptions[1].getBooleanValue());
		assertEquals("-b", rootOptions[1].getCommand());
		// Next option is a string in category
		assertEquals("String Option in Category", rootOptions[2].getName());
		assertEquals(IOption.STRING, rootOptions[2].getValueType());
		assertEquals(stringVal, rootOptions[2].getStringValue());
		// Next option is a another string in category
		assertEquals("Another String Option in Category", rootOptions[3].getName());
		assertEquals(IOption.STRING, rootOptions[3].getValueType());
		assertEquals(anotherStringVal, rootOptions[3].getStringValue());
		assertEquals("-str", rootOptions[3].getCommand());
		// Next option is an enumerated in category
		assertEquals("Enumerated Option in Category", rootOptions[4].getName());
		assertEquals(IOption.ENUMERATED, rootOptions[4].getValueType());
		String selEnum = rootOptions[4].getSelectedEnum();
		assertEquals(enumVal, selEnum);
		String[] enums = rootOptions[4].getApplicableValues();
		assertEquals(2, enums.length);
		assertEquals("Default Enum", enums[0]);
		assertEquals("Another Enum", enums[1]);
		assertEquals("-e1", rootOptions[4].getEnumCommand(enums[0]));
		assertEquals("-e2", rootOptions[4].getEnumCommand(enums[1]));
		assertEquals("-e2", rootOptions[4].getEnumCommand(selEnum));
		// Final option is a boolean in Category
		assertEquals("Boolean Option in Category", rootOptions[5].getName());
		assertEquals(IOption.BOOLEAN, rootOptions[5].getValueType());
		assertEquals(false, rootOptions[5].getBooleanValue());
		assertEquals("-nob", rootOptions[5].getCommandFalse());
	}
	
	/*
	 * Do a full sanity check on the root project type.
	 */
	private void checkRootProjectType(IProjectType type) throws BuildException {
		// Project stuff
		String expectedCleanCmd = "del /myworld";
		String expectedParserId = "org.eclipse.cdt.core.PE";
		String[] expectedOSList = {"win32"};
		String[] expectedArchList = {"all"};
		assertTrue(type.isTestProjectType());
		IConfiguration[] configs = type.getConfigurations();
		if (configs[0].getArtifactName().equals("ManagedBuildTest")) {
			assertEquals(configs[0].getArtifactExtension(), newExt);
		} else {
			assertEquals(configs[0].getArtifactExtension(), rootExt);
		}
		assertEquals(expectedCleanCmd, configs[0].getCleanCommand());
		assertEquals("make", configs[0].getBuildCommand());
		IToolChain toolChain = configs[0].getToolChain();
		ITargetPlatform targetPlatform = toolChain.getTargetPlatform();
		String[] binaryParsers = targetPlatform.getBinaryParserList();
		assertEquals(binaryParsers.length, 1);
	    assertEquals(binaryParsers[0], expectedParserId);
		assertTrue(Arrays.equals(expectedOSList, toolChain.getOSList()));
		assertTrue(Arrays.equals(expectedArchList, toolChain.getArchList()));
		// This configuration defines no errors parsers.
		assertNull(configs[0].getErrorParserIds());
		assertTrue(Arrays.equals(configs[0].getErrorParserList(), CCorePlugin.getDefault().getAllErrorParsersIDs()));
		
		// Tools
		ITool[] tools = toolChain.getTools();
		// Root Tool
		ITool rootTool = tools[0];
		assertEquals("Root Tool", rootTool.getName());
		// 6 Options are defined in the root tool
		IOption[] options = rootTool.getOptions();
		assertEquals(6, options.length);
		// First option is a 3-element list with 1 built-in
		assertEquals("List Option in Top", options[0].getName());
		assertEquals(IOption.STRING_LIST, options[0].getValueType());
		String[] valueList = options[0].getStringListValue();
		assertEquals(2, valueList.length);
		assertEquals("a", valueList[0]);
		assertEquals("b", valueList[1]);
		String[] builtInList = options[0].getBuiltIns();
		assertEquals(1, builtInList.length);
		assertEquals("c", builtInList[0]);
		assertEquals(options[0].getCommand(), "-L");
		// Next option is a boolean in top
		assertEquals("Boolean Option in Top", options[1].getName());
		assertEquals(IOption.BOOLEAN, options[1].getValueType());
		assertEquals(false, options[1].getBooleanValue());
		assertEquals("-b", options[1].getCommand());
		// Next option is a string category
		assertEquals("String Option in Category", options[2].getName());
		assertEquals(IOption.STRING, options[2].getValueType());
		assertEquals("x", options[2].getStringValue());
		// Next option is another string category
		assertEquals("Another String Option in Category", options[3].getName());
		assertEquals(IOption.STRING, options[3].getValueType());
		assertEquals("", options[3].getStringValue());
		assertEquals("-str", options[3].getCommand());
		// Next option is an enumerated
		assertEquals("Enumerated Option in Category", options[4].getName());
		assertEquals(IOption.ENUMERATED, options[4].getValueType());
		// Post-2.0 enums store the ID, not the string value 
		assertEquals("default.enum.option", options[4].getSelectedEnum());
		assertEquals("-e1", options[4].getEnumCommand("default.enum.option"));
		// Need this methof to populate the UI selection widget
		valueList = options[4].getApplicableValues();
		assertEquals(2, valueList.length);
		assertEquals("Default Enum", valueList[0]);
		assertEquals("Another Enum", valueList[1]);
		// Test compatability with 1.2 scheme of getting the command from the name
		assertEquals("-e1", options[4].getEnumCommand(valueList[0]));
		assertEquals("-e2", options[4].getEnumCommand(valueList[1]));
		// Final option is another boolean
		assertEquals("Boolean Option in Category", options[5].getName());
		assertEquals(IOption.BOOLEAN, options[5].getValueType());
		assertEquals(false, options[5].getBooleanValue());
		assertEquals("", options[5].getCommand());
		assertEquals("-nob", options[5].getCommandFalse());
		
		// Option Categories
		IOptionCategory topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		Object[][] catoptions = topCategory.getOptions(configs[0]);
		int i;
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(2, i);
		assertEquals("List Option in Top", ((IOption)catoptions[0][1]).getName());
		assertEquals("Boolean Option in Top", ((IOption)catoptions[1][1]).getName());
		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		assertEquals("Category", categories[0].getName());
		catoptions = categories[0].getOptions(configs[0]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(4, i);
		assertEquals("String Option in Category", ((IOption)catoptions[0][1]).getName());
		assertEquals("Another String Option in Category", ((IOption)catoptions[1][1]).getName());
		assertEquals("Enumerated Option in Category", ((IOption)catoptions[2][1]).getName());
		assertEquals("Boolean Option in Category", ((IOption)catoptions[3][1]).getName());

		// There should be 3 defined configs
		configs = type.getConfigurations();
		assertEquals(3, configs.length);
		
		// Root Config
		IConfiguration rootConfig = configs[0];
		assertEquals("Root Config", rootConfig.getName());

		// Tool elements
		tools = rootConfig.getTools();
		assertEquals(1, tools.length);
		assertEquals("Root Tool", tools[0].getName());
		assertEquals("-r", tools[0].getOutputFlag());
		assertTrue(tools[0].buildsFileType("foo"));
		assertTrue(tools[0].buildsFileType("bar"));
		assertTrue(tools[0].producesFileType("toor"));
		assertEquals("doIt", tools[0].getToolCommand());
		assertEquals("", tools[0].getOutputPrefix());
		// The root tool defines one valid header file extension
		assertTrue(rootTool.isHeaderFile("baz"));
		assertTrue(tools[0].isHeaderFile("baz"));
		assertEquals(ITool.FILTER_C, rootTool.getNatureFilter());
		
		// Partially Overriden Configuration
		assertEquals("Root Override Config", configs[1].getName());
		tools = configs[1].getTools();
		assertEquals(1, tools.length);
		assertEquals("Root Tool", tools[0].getName());
		topCategory = tools[0].getTopOptionCategory();
		catoptions = topCategory.getOptions(configs[1]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(2, i);
		assertEquals("List Option in Top", ((IOption)catoptions[0][1]).getName());
		valueList = ((IOption)catoptions[0][1]).getStringListValue();
		assertEquals("a", valueList[0]);
		assertEquals("b", valueList[1]);
		assertEquals("Boolean Option in Top", ((IOption)catoptions[1][1]).getName());
		assertEquals(true, ((IOption)catoptions[1][1]).getBooleanValue());
		assertEquals("-b", ((IOption)catoptions[1][1]).getCommand());
		categories = topCategory.getChildCategories();
		catoptions = categories[0].getOptions(configs[1]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(4, i);
		assertEquals("String Option in Category", ((IOption)catoptions[0][1]).getName());
		assertEquals("y", ((IOption)catoptions[0][1]).getStringValue());
		assertEquals("Another String Option in Category", ((IOption)catoptions[1][1]).getName());
		assertEquals("", ((IOption)catoptions[1][1]).getStringValue());
		assertEquals("Enumerated Option in Category", ((IOption)catoptions[2][1]).getName());
		valueList = ((IOption)catoptions[2][1]).getApplicableValues();
		assertEquals(2, valueList.length);
		assertEquals("Default Enum", valueList[0]);
		assertEquals("Another Enum", valueList[1]);
		assertEquals("-e1", ((IOption)catoptions[2][1]).getEnumCommand(valueList[0]));
		assertEquals("-e2", ((IOption)catoptions[2][1]).getEnumCommand(valueList[1]));
		assertEquals(1, tools.length);
		assertEquals("Boolean Option in Category", ((IOption)catoptions[3][1]).getName());
		assertEquals(false, ((IOption)catoptions[3][1]).getBooleanValue());
		assertEquals("", ((IOption)catoptions[3][1]).getCommand());
		assertEquals("-nob", ((IOption)catoptions[3][1]).getCommandFalse());
		assertEquals(1, tools.length);
		ITool tool = tools[0];
		assertNotNull(tool);
		assertEquals("Root Tool", tool.getName());
		assertEquals("-r", tool.getOutputFlag());
		assertTrue(tool.buildsFileType("foo"));
		assertTrue(tool.buildsFileType("bar"));
		assertTrue(tool.producesFileType("toor"));
		assertTrue(tool.isHeaderFile("baz"));
		assertEquals("doIt", tool.getToolCommand());
		assertEquals("-La -Lb -b y -e1 -nob", tool.getToolFlags());
		
		// Completely Overridden configuration
		assertEquals("Complete Override Config", configs[2].getName());
		tools = configs[2].getTools();
		assertEquals(1, tools.length);
		assertEquals("Root Tool", tools[0].getName());
		topCategory = tools[0].getTopOptionCategory();
		catoptions = topCategory.getOptions(configs[2]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(2, i);
		// Check that there's an string list with totally new values 
		assertEquals("List Option in Top", ((IOption)catoptions[0][1]).getName());
		assertEquals(IOption.STRING_LIST, ((IOption)catoptions[0][1]).getValueType());
		valueList = ((IOption)catoptions[0][1]).getStringListValue();
		assertTrue(valueList.length == 3);
		assertEquals("d", valueList[0]);
		assertEquals("e", valueList[1]);
		assertEquals("f", valueList[2]);		
		assertEquals("-L", ((IOption)catoptions[0][1]).getCommand());
		// and a true boolean (commands should not have changed)
		assertEquals("Boolean Option in Top", ((IOption)catoptions[1][1]).getName());
		assertEquals(IOption.BOOLEAN, ((IOption)catoptions[1][1]).getValueType());
		assertEquals(true, ((IOption)catoptions[1][1]).getBooleanValue());
		assertEquals("-b", ((IOption)catoptions[1][1]).getCommand());
		// Check that there's an overridden enumeration and string
		categories = topCategory.getChildCategories();
		catoptions = categories[0].getOptions(configs[2]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(4, i);
		assertEquals("String Option in Category", ((IOption)catoptions[0][1]).getName());
		assertEquals(IOption.STRING, ((IOption)catoptions[0][1]).getValueType());
		assertEquals("overridden", ((IOption)catoptions[0][1]).getStringValue());
		assertEquals("Another String Option in Category", ((IOption)catoptions[1][1]).getName());
		assertEquals(IOption.STRING, ((IOption)catoptions[1][1]).getValueType());
		assertEquals("alsooverridden", ((IOption)catoptions[1][1]).getStringValue());
		assertEquals("Enumerated Option in Category", ((IOption)catoptions[2][1]).getName());
		assertEquals(IOption.ENUMERATED, ((IOption)catoptions[2][1]).getValueType());
		assertEquals("another.enum.option", ((IOption)catoptions[2][1]).getSelectedEnum());
		assertEquals("Boolean Option in Category", ((IOption)catoptions[3][1]).getName());
		assertEquals(IOption.BOOLEAN, ((IOption)catoptions[3][1]).getValueType());
		assertEquals(true, ((IOption)catoptions[3][1]).getBooleanValue());
		tool = tools[0];
		assertEquals("-Ld -Le -Lf -b overridden -stralsooverridden -e2", tool.getToolFlags());
		
		// Make sure that the build manager returns the default makefile generator (not null)
		assertNotNull(ManagedBuildManager.getBuildfileGenerator(configs[0]));
	}
	
	/*
	 * Do a full sanity check on the root managed project.
	 */
	private void checkRootManagedProject(IManagedProject managedProj, String testValue) throws BuildException {
		String expectedCleanCmd = "del /myworld";
		String expectedParserId = "org.eclipse.cdt.core.PE";
		String[] expectedOSList = {"win32"};
		String[] expectedArchList = {"all"};
		assertTrue(managedProj.getProjectType().isTestProjectType());
		IConfiguration[] configs = managedProj.getConfigurations();
		if (configs[0].getArtifactName().equals("ManagedBuildTest")) {
			assertEquals(configs[0].getArtifactExtension(), newExt);
		} else {
			assertEquals(configs[0].getArtifactExtension(), rootExt);
		}
		assertEquals(expectedCleanCmd, configs[0].getCleanCommand());
		assertEquals("make", configs[0].getBuildCommand());
		IToolChain toolChain = configs[0].getToolChain();
		ITargetPlatform targetPlatform = toolChain.getTargetPlatform();
		String[] binaryParsers = targetPlatform.getBinaryParserList();
		assertEquals(binaryParsers.length, 1);
	    assertEquals(binaryParsers[0], expectedParserId);
		assertTrue(Arrays.equals(expectedOSList, toolChain.getOSList()));
		assertTrue(Arrays.equals(expectedArchList, toolChain.getArchList()));
		// This configuration defines no errors parsers.
		assertNull(configs[0].getErrorParserIds());
		assertTrue(Arrays.equals(configs[0].getErrorParserList(), CCorePlugin.getDefault().getAllErrorParsersIDs()));
		
		// Tools
		ITool[] tools = configs[0].getTools();
		// Root Tool
		ITool rootTool = tools[0];
		assertEquals("Root Tool", rootTool.getName());
		// 6 Options are defined in the root tool
		IOption[] options = rootTool.getOptions();
		assertEquals(6, options.length);
		// First option is a 3-element list with 1 built-in
		assertEquals("List Option in Top", options[0].getName());
		assertEquals(IOption.STRING_LIST, options[0].getValueType());
		String[] valueList = options[0].getStringListValue();
		assertEquals(2, valueList.length);
		assertEquals("a", valueList[0]);
		assertEquals("b", valueList[1]);
		String[] builtInList = options[0].getBuiltIns();
		assertEquals(1, builtInList.length);
		assertEquals("c", builtInList[0]);
		assertEquals(options[0].getCommand(), "-L");
		// Next option is a boolean in top
		assertEquals("Boolean Option in Top", options[1].getName());
		assertEquals(IOption.BOOLEAN, options[1].getValueType());
		assertEquals(false, options[1].getBooleanValue());
		assertEquals("-b", options[1].getCommand());
		// Next option is a string category
		assertEquals("String Option in Category", options[2].getName());
		assertEquals(IOption.STRING, options[2].getValueType());
		assertEquals(testValue, options[2].getStringValue());
		// Next option is another string category
		assertEquals("Another String Option in Category", options[3].getName());
		assertEquals(IOption.STRING, options[3].getValueType());
		assertEquals("", options[3].getStringValue());
		assertEquals("-str", options[3].getCommand());
		// Next option is an enumerated
		assertEquals("Enumerated Option in Category", options[4].getName());
		assertEquals(IOption.ENUMERATED, options[4].getValueType());
		// Post-2.0 enums store the ID, not the string value 
		assertEquals("default.enum.option", options[4].getSelectedEnum());
		assertEquals("-e1", options[4].getEnumCommand("default.enum.option"));
		// Need this methof to populate the UI selection widget
		valueList = options[4].getApplicableValues();
		assertEquals(2, valueList.length);
		assertEquals("Default Enum", valueList[0]);
		assertEquals("Another Enum", valueList[1]);
		// Test compatability with 1.2 scheme of getting the command from the name
		assertEquals("-e1", options[4].getEnumCommand(valueList[0]));
		assertEquals("-e2", options[4].getEnumCommand(valueList[1]));
		// Final option is another boolean
		assertEquals("Boolean Option in Category", options[5].getName());
		assertEquals(IOption.BOOLEAN, options[5].getValueType());
		assertEquals(false, options[5].getBooleanValue());
		assertEquals("", options[5].getCommand());
		assertEquals("-nob", options[5].getCommandFalse());
		
		// Option Categories
		IOptionCategory topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		Object[][] catoptions = topCategory.getOptions(configs[0]);
		int i;
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(2, i);
		IOption catOption = (IOption)catoptions[0][1]; 
		assertEquals("List Option in Top", catOption.getName());
		catOption = (IOption)catoptions[1][1]; 
		assertEquals("Boolean Option in Top", catOption.getName());
		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		assertEquals("Category", categories[0].getName());
		catoptions = categories[0].getOptions(configs[0]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(4, i);
		catOption = (IOption)catoptions[0][1]; 
		assertEquals("String Option in Category", catOption.getName());
		catOption = (IOption)catoptions[1][1]; 
		assertEquals("Another String Option in Category", catOption.getName());
		catOption = (IOption)catoptions[2][1]; 
		assertEquals("Enumerated Option in Category", catOption.getName());
		catOption = (IOption)catoptions[3][1]; 
		assertEquals("Boolean Option in Category", catOption.getName());

		// There should be 3 defined configs
		assertEquals(3, configs.length);
		
		// Root Config
		IConfiguration rootConfig = configs[0];
		assertEquals("Root Config", rootConfig.getName());

		// Tool elements
		tools = rootConfig.getTools();
		assertEquals(1, tools.length);
		assertEquals("Root Tool", tools[0].getName());
		assertEquals("-r", tools[0].getOutputFlag());
		assertTrue(tools[0].buildsFileType("foo"));
		assertTrue(tools[0].buildsFileType("bar"));
		assertTrue(tools[0].producesFileType("toor"));
		assertEquals("doIt", tools[0].getToolCommand());
		assertEquals("", tools[0].getOutputPrefix());
		// The root tool defines one valid header file extension
		assertTrue(rootTool.isHeaderFile("baz"));
		assertTrue(tools[0].isHeaderFile("baz"));
		assertEquals(ITool.FILTER_C, rootTool.getNatureFilter());
		
		// Partially Overriden Configuration
		assertEquals("Root Override Config", configs[1].getName());
		tools = configs[1].getTools();
		assertEquals(1, tools.length);
		assertEquals("Root Tool", tools[0].getName());
		topCategory = tools[0].getTopOptionCategory();
		catoptions = topCategory.getOptions(configs[1]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(2, i);
		catOption = (IOption)catoptions[0][1]; 
		assertEquals("List Option in Top", catOption.getName());
		valueList = catOption.getStringListValue();
		assertEquals("a", valueList[0]);
		assertEquals("b", valueList[1]);
		catOption = (IOption)catoptions[1][1]; 
		assertEquals("Boolean Option in Top", catOption.getName());
		assertEquals(true, catOption.getBooleanValue());
		assertEquals("-b", catOption.getCommand());
		categories = topCategory.getChildCategories();
		catoptions = categories[0].getOptions(configs[1]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(4, i);
		catOption = (IOption)catoptions[0][1]; 
		assertEquals("String Option in Category", catOption.getName());
		assertEquals("y", catOption.getStringValue());
		catOption = (IOption)catoptions[1][1]; 
		assertEquals("Another String Option in Category", catOption.getName());
		assertEquals("", catOption.getStringValue());
		catOption = (IOption)catoptions[2][1]; 
		assertEquals("Enumerated Option in Category", catOption.getName());
		valueList = catOption.getApplicableValues();
		assertEquals(2, valueList.length);
		assertEquals("Default Enum", valueList[0]);
		assertEquals("Another Enum", valueList[1]);
		catOption = (IOption)catoptions[2][1]; 
		assertEquals("-e1", catOption.getEnumCommand(valueList[0]));
		assertEquals("-e2", catOption.getEnumCommand(valueList[1]));
		assertEquals(1, tools.length);
		catOption = (IOption)catoptions[3][1]; 
		assertEquals("Boolean Option in Category", catOption.getName());
		assertEquals(false, catOption.getBooleanValue());
		assertEquals("", catOption.getCommand());
		assertEquals("-nob", catOption.getCommandFalse());
		assertEquals(1, tools.length);
		ITool tool = tools[0];
		assertNotNull(tool);
		assertEquals("Root Tool", tool.getName());
		assertEquals("-r", tool.getOutputFlag());
		assertTrue(tool.buildsFileType("foo"));
		assertTrue(tool.buildsFileType("bar"));
		assertTrue(tool.producesFileType("toor"));
		assertTrue(tool.isHeaderFile("baz"));
		assertEquals("doIt", tool.getToolCommand());
		assertEquals("-La -Lb -b y -e1 -nob", tool.getToolFlags());
		
		// Completely Overridden configuration
		assertEquals("Complete Override Config", configs[2].getName());
		tools = configs[2].getTools();
		assertEquals(1, tools.length);
		assertEquals("Root Tool", tools[0].getName());
		topCategory = tools[0].getTopOptionCategory();
		catoptions = topCategory.getOptions(configs[2]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(2, i);
		// Check that there's an string list with totally new values 
		catOption = (IOption)catoptions[0][1]; 
		assertEquals("List Option in Top", catOption.getName());
		assertEquals(IOption.STRING_LIST, catOption.getValueType());
		valueList = catOption.getStringListValue();
		assertTrue(valueList.length == 3);
		assertEquals("d", valueList[0]);
		assertEquals("e", valueList[1]);
		assertEquals("f", valueList[2]);		
		assertEquals("-L", catOption.getCommand());
		// and a true boolean (commands should not have changed)
		catOption = (IOption)catoptions[1][1]; 
		assertEquals("Boolean Option in Top", catOption.getName());
		assertEquals(IOption.BOOLEAN, catOption.getValueType());
		assertEquals(true, catOption.getBooleanValue());
		assertEquals("-b", catOption.getCommand());
		// Check that there's an overridden enumeration and string
		categories = topCategory.getChildCategories();
		catoptions = categories[0].getOptions(configs[2]);
		for (i=0; i<catoptions.length; i++)
			if (catoptions[i][0] == null) break;
		assertEquals(4, i);
		catOption = (IOption)catoptions[0][1]; 
		assertEquals("String Option in Category", catOption.getName());
		assertEquals(IOption.STRING, catOption.getValueType());
		assertEquals("overridden", catOption.getStringValue());
		catOption = (IOption)catoptions[1][1]; 
		assertEquals("Another String Option in Category", catOption.getName());
		assertEquals(IOption.STRING, catOption.getValueType());
		assertEquals("alsooverridden", catOption.getStringValue());
		catOption = (IOption)catoptions[2][1]; 
		assertEquals("Enumerated Option in Category", catOption.getName());
		assertEquals(IOption.ENUMERATED, catOption.getValueType());
		assertEquals("another.enum.option", catOption.getSelectedEnum());
		catOption = (IOption)catoptions[3][1]; 
		assertEquals("Boolean Option in Category", catOption.getName());
		assertEquals(IOption.BOOLEAN, catOption.getValueType());
		assertEquals(true, catOption.getBooleanValue());
		tool = tools[0];
		assertEquals("-Ld -Le -Lf -b overridden -stralsooverridden -e2", tool.getToolFlags());
		
		// Make sure that the build manager returns the default makefile generator (not null)
		assertNotNull(ManagedBuildManager.getBuildfileGenerator(configs[0]));
	}

	/*
	 * The Sub Sub project type has a reference to a tool that is defined  
	 * independently from the project type itself. This is a common pattern 
	 * for tools that are shared between many project types.
	 * 
	 * The tool itself is defined as having two option categories, with 
	 * one option in each category. To test that the reference is properly 
	 * inheritted, the project type overrides the default value of the boolean 
	 * option. 
	 * 
	 * The test confirms that the basic settings are inheritted through the 
	 * reference, and that the overridden value is used instead of the 
	 * default. It also tests that the command can be overidden through a 
	 * tool reference.
	 * 
	 * Finally, the string option in the configuration is overridden and the 
	 * test confirms that it contains both the overridden boolean that the 
	 * project type provides, and the overridden string that it provides.   
	 *  
	 * @param testSubSub
	 */
	private void checkSubSubProjectType(IProjectType projType) {
		final String indyToolName = "Target Independent Tool";
		final String indyToolCommand = "RC.EXE";
		final String indyToolInputExt = "rc";
		final String indyToolOutputExt = "free";
		final String indyToolOutFlag = "/fo";
		final String indyToolHeader = "h";
		final String indyToolHeaderNot = "j";
		final String indyCatOne = "Free";
		final String indyCatTwo = "Chained";
		final String freeOptName = "String in Free";
		final String chainedOptName = "Boolean in Chained";
		final String freeOptValue = "Live free or die";
		final String newCmd = "Let the Wookie win";
		final String stringOverride = "The future language of slaves";
		
		IConfiguration[] configs = projType.getConfigurations();
		// Check the inherited clean command
		assertEquals("rm -yourworld", configs[0].getCleanCommand());
		// Check that the make command is overridden from parent
		assertEquals("nmake", configs[0].getBuildCommand());
		// Make sure we get the proper binary parser
		IToolChain toolChain = configs[0].getToolChain();
		ITargetPlatform targetPlatform = toolChain.getTargetPlatform();
		assertEquals("org.eclipse.cdt.core.ELF", targetPlatform.getBinaryParserList()[0]);
		// Make sure the os list is inherited
		String[] expectedOSList = {"win32","linux","solaris"};
		assertTrue(Arrays.equals(expectedOSList, toolChain.getOSList()));
		// Make sure the arch list is inherited
		String[] expectedArchList = {"x86", "ppc"}; 
		assertTrue(Arrays.equals(expectedArchList, toolChain.getArchList()));

		// Get the 5 configurations (3 from test, 1 from test sub and 1 from this)
		assertEquals(5, configs.length);
		
		// Check the tools. We should have 3 (1 from each parent and the one referenced).
		ITool[] tools = configs[0].getTools();
		assertEquals(3, tools.length);
		ITool toolRef = tools[0];
		
		// Make sure we get all the tool settings
		assertEquals(toolRef.getName(), indyToolName);
		assertEquals(toolRef.getToolCommand(), indyToolCommand);
		assertTrue(toolRef.buildsFileType(indyToolInputExt));
		assertEquals(toolRef.getOutputExtension(indyToolInputExt), indyToolOutputExt);
		assertEquals(toolRef.getOutputFlag(), indyToolOutFlag);
		assertTrue(toolRef.isHeaderFile(indyToolHeader));
		assertFalse(toolRef.isHeaderFile(indyToolHeaderNot));
		assertEquals(toolRef.getNatureFilter(), ITool.FILTER_BOTH);
		// Check out the referenced tool and make sure we get all option categories
		IOptionCategory topCategory = toolRef.getTopOptionCategory();
		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		assertEquals(categories[0].getName(), indyCatOne);
		IOptionCategory[] subCategories = categories[0].getChildCategories();
		// Is the chained category a subcategory
		assertEquals(1, subCategories.length);
		assertEquals(subCategories[0].getName(), indyCatTwo);
		// Make sure the option in the top category is correct
		Object[][] optsInCat = categories[0].getOptions(configs[0]);
		int i;
		for (i=0; i<optsInCat.length; i++)
			if (optsInCat[i][0] == null) break;
		assertEquals(1, i);
		IOption optCat = (IOption)optsInCat[0][1];
		assertEquals(freeOptName, optCat.getName());
		try {
			// We get the option categories and options from the tool itself, but the 
			// tool reference will have a set of 0 to n option references that contain 
			// overridden settings. In this case, the string is inheritted and should 
			// not be reference
			assertEquals(IOption.STRING, optCat.getValueType());
			IOption stringOpt = toolRef.getOptionById(optCat.getId());
			assertTrue(stringOpt instanceof Option);
			assertEquals(freeOptValue, stringOpt.getStringValue());
		} catch (BuildException e1) {
			fail("Failed getting string value in subsub :" + e1.getLocalizedMessage());
		}

		// Do the same for the options in the child cat
		Object[][] optsInSubCat = subCategories[0].getOptions(configs[0]);
		for (i=0; i<optsInSubCat.length; i++)
			if (optsInSubCat[i][0] == null) break;
		assertEquals(1, i);
		IOption booleanRef = toolRef.getOptionById(((IOption)optsInSubCat[0][1]).getId());
		assertEquals(chainedOptName, booleanRef.getName());
		try {
			assertEquals(IOption.BOOLEAN, booleanRef.getValueType());
			assertTrue(booleanRef.getBooleanValue());
		} catch (BuildException e) {
			fail("Failure getting boolean value in subsub: " + e.getLocalizedMessage());
		}
		
		// Test that the tool command can be changed through the reference
		toolRef.setToolCommand(newCmd);
		assertEquals(toolRef.getToolCommand(), newCmd);
		
		// Muck about with the options in the local config
		IConfiguration subSubConfig = projType.getConfiguration("sub.sub.config");
		assertNotNull(subSubConfig);
		ITool[] configTools = subSubConfig.getTools();
		// This tool ref is inherited from parent, so it does not belong to the config
		ITool configToolRef = configTools[0];
		assertNotNull(configToolRef);
		optCat = (IOption)optsInCat[0][1];
		IOption configStringOpt = configToolRef.getOptionById(optCat.getId());
		assertNotNull(configStringOpt);
		// Override the string option		
		try {
			subSubConfig.setOption(configToolRef, configStringOpt, stringOverride);
		} catch (BuildException e) {
			fail("Failure setting string value in subsubconfiguration: " + e.getLocalizedMessage());
		}
		// Now the config should have a tool ref to the independent tool
		configTools = subSubConfig.getTools();
		configToolRef = configTools[0];
		assertNotNull(configToolRef);
		
		// Test that the string option is overridden in the configuration
		optsInCat = categories[0].getOptions(configs[0]);
		for (i=0; i<optsInCat.length; i++)
			if (optsInCat[i][0] == null) break;
		assertEquals(1, i);
		optCat = (IOption)optsInCat[0][1];
		assertEquals(freeOptName, optCat.getName());
		configStringOpt = configToolRef.getOptionById(optCat.getId());
		try {
			assertEquals(stringOverride, configStringOpt.getStringValue());
		} catch (BuildException e) {
			fail("Failure getting string value in subsubconfiguration: " + e.getLocalizedMessage());
		}
		// The tool should also contain the boolean option set to true
		IOption optSubCat = (IOption)optsInSubCat[0][1];
		IOption configBoolOpt = configToolRef.getOptionById(optSubCat.getId());
		assertNotNull(configBoolOpt);
		try {
			assertTrue(configBoolOpt.getBooleanValue());
		} catch (BuildException e) {
			fail("Failure getting boolean value in subsubconfiguration: " + e.getLocalizedMessage());
		}
			
		// Override it in config and retest
		try {
			subSubConfig.setOption(configToolRef, configBoolOpt, false);
		} catch (BuildException e) {
			fail("Failure setting boolean value in subsubconfiguration: " + e.getLocalizedMessage());
		}
		optsInSubCat = subCategories[0].getOptions(configs[0]);
		for (i=0; i<optsInSubCat.length; i++)
			if (optsInSubCat[i][0] == null) break;
		assertEquals(1, i);
		configBoolOpt = configToolRef.getOptionById(((IOption)optsInSubCat[0][1]).getId());
		assertEquals(chainedOptName, booleanRef.getName());
		try {
			assertFalse(configBoolOpt.getBooleanValue());
		} catch (BuildException e) {
			fail("Failure getting boolean value in subsubconfiguration: " + e.getLocalizedMessage());
		}
	}

	/*
	 * Do a sanity check on the values in the sub-project type. Most of the
	 * sanity on the how build model entries are read is performed in 
	 * the root project type check, so these tests just verify that the the sub 
	 * project type properly inherits from its parent. For the new options
	 * in the sub project type, the test does a sanity check just to be complete.
	 */
	private void checkSubProjectType(IProjectType projType) throws BuildException {
		final String expectedFlags = "-I/usr/include -I/opt/gnome/include -IC:\\home\\tester/include -I\"../includes\" x y z";
		
		IConfiguration[] configs = projType.getConfigurations();
		// Check the overridden clean command
		assertEquals("rm -yourworld", configs[0].getCleanCommand());
		// Make sure the projType inherits the make command
		assertEquals("make", configs[0].getBuildCommand());
		// Make sure the binary parser is hard-coded and available
		IToolChain toolChain = configs[0].getToolChain();
		ITargetPlatform targetPlatform = toolChain.getTargetPlatform();
		assertEquals("org.eclipse.cdt.core.PE", targetPlatform.getBinaryParserList()[0]);
		String[] expectedOSList = {"win32","linux","solaris"};
		assertTrue(Arrays.equals(expectedOSList, toolChain.getOSList()));
		// Make sure the list is overridden
		String[] expectedArchList = {"x86", "ppc"};
		assertTrue(Arrays.equals(expectedArchList, toolChain.getArchList()));

		// Make sure this is a test projType
		assertTrue(projType.isTestProjectType());
		// Make sure the build artifact extension is there
		assertEquals(configs[0].getArtifactExtension(), subExt);
				
		// Get the tools for this projType
		ITool[] tools = configs[0].getTools();
		// Do we inherit properly from parent
		ITool rootTool = tools[0];
		assertEquals("Root Tool", rootTool.getName());
		// Now get the tool defined for this projType
		ITool subTool = tools[1];
		assertEquals("Sub Tool", subTool.getName());
		// Confirm that it has four options
		IOption[] subOpts = subTool.getOptions();
		assertEquals(5, subOpts.length);
		assertEquals("", subTool.getOutputFlag());
		assertTrue(subTool.buildsFileType("yarf"));
		assertTrue(subTool.producesFileType("bus"));
		assertEquals("", subTool.getToolCommand());
		assertEquals("lib", subTool.getOutputPrefix());
		assertTrue(subTool.isHeaderFile("arf"));
		assertTrue(subTool.isHeaderFile("barf"));
		assertEquals(ITool.FILTER_BOTH, subTool.getNatureFilter());
		
		// Do a sanity check on the options 
		assertEquals("Include Paths", subOpts[0].getName());
		assertEquals(IOption.INCLUDE_PATH, subOpts[0].getValueType());
		String[] incPath = subOpts[0].getIncludePaths();
		assertEquals(2, incPath.length);
		assertEquals("/usr/include", incPath[0]);
		assertEquals("/opt/gnome/include", incPath[1]);
		String[] builtInPaths = subOpts[0].getBuiltIns();
		assertEquals(1, builtInPaths.length);
		assertEquals("/usr/gnu/include", builtInPaths[0]);
		assertEquals("-I", subOpts[0].getCommand());
		assertEquals(IOption.BROWSE_DIR, subOpts[0].getBrowseType());
				
		// There are no user-defined preprocessor symbols
		assertEquals("Defined Symbols", subOpts[1].getName());
		assertEquals(IOption.PREPROCESSOR_SYMBOLS, subOpts[1].getValueType());
		String[] defdSymbols = subOpts[1].getDefinedSymbols();
		assertEquals(0, defdSymbols.length);
		assertEquals("-D", subOpts[1].getCommand());
		// But there is a builtin
		String[] builtInSymbols = subOpts[1].getBuiltIns();
		assertEquals(1, builtInSymbols.length);
		assertEquals("BUILTIN", builtInSymbols[0]);
		// Broswe type should be none
		assertEquals(IOption.BROWSE_NONE, subOpts[1].getBrowseType());

		assertEquals("More Includes", subOpts[2].getName());
		assertEquals(IOption.INCLUDE_PATH, subOpts[2].getValueType());
		String[] moreIncPath = subOpts[2].getIncludePaths();
		assertEquals(2, moreIncPath.length);
		assertEquals("C:\\home\\tester/include", moreIncPath[0]);
		assertEquals("-I", subOpts[2].getCommand());
		assertEquals(IOption.BROWSE_DIR, subOpts[2].getBrowseType());
		
		// Check the user object option
		assertEquals("User Objects", subOpts[3].getName());
		assertEquals(IOption.OBJECTS, subOpts[3].getValueType());
		String[] objs = subOpts[3].getUserObjects();
		assertEquals(2, objs.length);
		assertEquals("obj1.o", objs[0]);
		assertEquals("obj2.o", objs[1]);
		assertEquals(IOption.BROWSE_FILE, subOpts[3].getBrowseType());
		assertEquals("", subOpts[3].getCommand());
		
		// There should be a string list with no command
		assertEquals("No Command StringList", subOpts[4].getName());
		assertEquals(IOption.STRING_LIST, subOpts[4].getValueType());
		
		// Make sure the tool flags look right
		assertEquals(subTool.getToolFlags(), expectedFlags);
		
		// Get the configs for this projType; it should inherit all the configs defined for the parent
		assertEquals(4, configs.length);
		assertEquals("Sub Config", configs[0].getName());
		assertEquals("Root Config", configs[1].getName());
		assertEquals("Root Override Config", configs[2].getName());
		assertEquals("Complete Override Config", configs[3].getName());
	}

	private void checkForwardProjectTypes(IProjectType parent, IProjectType child, IProjectType grandchild) {
		// check that the projType parent reference has been resolved.
		assertEquals(parent, child.getSuperClass());
		assertEquals(child, grandchild.getSuperClass());
		
		// get the parent tool
		IConfiguration[] parentConfigs = parent.getConfigurations();
		ITool[] parentTools = parentConfigs[0].getTools();
		assertEquals(1, parentTools.length);
		ITool parentTool = parentTools[0];
		assertNotNull(parentTool);

		// check option categories
		IOption option = parentTool.getOptionById("test.forward.option");
		assertNotNull(option);
		IOptionCategory[] firstLevel = parentTool.getTopOptionCategory()
			.getChildCategories();
		assertEquals(1, firstLevel.length);
		IOptionCategory[] secondLevel = firstLevel[0].getChildCategories();
		assertEquals(1, secondLevel.length);
		assertEquals(0, secondLevel[0].getChildCategories().length);
		Object[][] optList = secondLevel[0].getOptions(parentConfigs[0]);
		int i;
		for (i=0; i<optList.length; i++)
			if (optList[i][0] == null) break;
		assertEquals(1, i);
		assertEquals(option, optList[0][1]);
		
		// get the tool reference from the child
		IConfiguration[] childConfigs = child.getConfigurations();
		ITool[] childTools = childConfigs[0].getTools();
		assertEquals(1, childTools.length);
		ITool childToolRef = childTools[0];
		assertEquals(parentTool.getSuperClass(), childToolRef.getSuperClass());
		
		// get and check the option reference
		IOption optRef = childToolRef.getOptionById("test.forward.option");
		assertEquals(option, optRef);
		
		// get the tool reference from the grandchild
		IConfiguration[] grandConfigs = grandchild.getConfigurations();
		ITool[] grandTools = grandConfigs[0].getTools();
		assertEquals(1, grandTools.length);
		ITool grandToolRef = grandTools[0];
		assertEquals(parentTool.getSuperClass(), grandToolRef.getSuperClass());
		
	}
	
	public void checkProviderProjectType(IProjectType projType) throws Exception {
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream("test_commands"));

		// check that this projType is in the file
		String command = props.getProperty(projType.getId());
		assertNotNull(command);
		
		IProjectType parent = projType.getSuperClass();
		assertNotNull(parent);
		assertEquals("test.forward.parent.target", parent.getId());
		
		IConfiguration[] configs = projType.getConfigurations();
		ITool toolRef = configs[0].getFilteredTools()[0];
		assertEquals(command, toolRef.getToolCommand());
	}
	
	/**
	 * Remove all the project information associated with the project used during test.
	 */
	public void cleanup() {
		removeProject(projectName);
		removeProject(projectName2);
	}
	
	/* (non-Javadoc)
	 * Create a new project named <code>name</code> or return the project in 
	 * the workspace of the same name if it exists.
	 * 
	 * @param name The name of the project to create or retrieve.
	 * @return 
	 * @throws CoreException
	 */
	private IProject createProject(String name) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject newProjectHandle = root.getProject(name);
		IProject project = null;
		
		if (!newProjectHandle.exists()) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			//description.setLocation(root.getLocation());
			project = CCorePlugin.getDefault().createCProject(description, newProjectHandle, new NullProgressMonitor(), MakeCorePlugin.MAKE_PROJECT_ID);
		} else {
			newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);
			project = newProjectHandle;
		}
        
		// Open the project if we have to
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}
				
		return project;	
	}
	
	/**
	 * Remove the <code>IProject</code> with the name specified in the argument from the 
	 * receiver's workspace.
	 *  
	 * @param name
	 */
	private void removeProject(String name) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		if (project.exists()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			} finally {
				try {
					System.gc();
					System.runFinalization();
					project.delete(true, true, null);
				} catch (CoreException e2) {
					assertTrue(false);
				}
			}
		}
	}
	/**
	 * @throws CoreException
	 * @throws BuildException
	 */
	public void testErrorParsers() throws BuildException {
		// Create new project
		IProject project = null;
		try {
			project = createProject(projectName2);
			// Now associate the builder with the project
			ManagedBuildTestHelper.addManagedBuildNature(project);
			IProjectDescription description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}
		} catch (CoreException e) {
			fail("Test failed on error parser project creation: " + e.getLocalizedMessage());
		}
		
		// Find the base project Type definition
		IProjectType projType = ManagedBuildManager.getProjectType("test.error.parsers");
		assertNotNull(projType);
		
		// Create the target for our project that builds a dummy executable
		IManagedProject newProj = ManagedBuildManager.createManagedProject(project, projType);
		assertEquals(newProj.getName(), projType.getName());
		ManagedBuildManager.setNewProjectVersion(project);

		// Initialize the path entry container
		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(project);
		if (initResult.getCode() != IStatus.OK) {
			fail("Initializing build information failed for: " + project.getName() + " because: " + initResult.getMessage());
		}
		
		// Copy over the configs
		IConfiguration[] baseConfigs = projType.getConfigurations();
		for (int i = 0; i < baseConfigs.length; ++i) {
			newProj.createConfiguration(baseConfigs[i], baseConfigs[i].getId() + "." + i);
		}
		
		// Test this out
		checkErrorParsersProject(newProj);
		
		// Save, close, reopen and test again
		ManagedBuildManager.saveBuildInfo(project, true);
		ManagedBuildManager.removeBuildInfo(project);
		try {
			project.close(null);
		} catch (CoreException e) {
			fail("Failed on error parser project close: " + e.getLocalizedMessage());
		}
		try {
			project.open(null);
		} catch (CoreException e) {
			fail("Failed on error parser project open: " + e.getLocalizedMessage());
		}
		
		// Test that the default config was remembered
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);

		// Check the rest of the default information
		checkErrorParsersProject(info.getManagedProject());
		ManagedBuildManager.removeBuildInfo(project);
	}
	
	/*
	 * Do a sanity check on the error parsers target.
	 */
	private void checkErrorParsersProject(IManagedProject proj) throws BuildException {
		// Target stuff
		String expectedBinParserId = "org.eclipse.cdt.core.PE";
		IConfiguration[] configs = proj.getConfigurations();
		IToolChain toolChain = configs[0].getToolChain();
		ITargetPlatform targetPlatform = toolChain.getTargetPlatform();
		assertEquals(expectedBinParserId, targetPlatform.getBinaryParserList()[0]);
		// This target defines errors parsers.  Check that the error parsers
		// have been assigned.
		assertEquals("org.eclipse.cdt.core.MakeErrorParser;org.eclipse.cdt.core.GCCErrorParser;org.eclipse.cdt.core.GLDErrorParser", configs[0].getErrorParserIds());
		
		// Tool
		ITool[] tools = configs[0].getTools();
		ITool rootTool = tools[0];
		assertEquals(1, tools.length);
		assertEquals("EP Tool", tools[0].getName());
		assertEquals("-o", tools[0].getOutputFlag());
		assertTrue(tools[0].buildsFileType("y"));
		assertTrue(tools[0].buildsFileType("x"));
		assertTrue(tools[0].producesFileType("xy"));
		assertEquals("EP", tools[0].getToolCommand());
		assertEquals(ITool.FILTER_C, rootTool.getNatureFilter());

		// There should be one defined configs
		assertEquals(1, configs.length);
	}
	
	/**
	 * Test that the build artifact of a <code>ITarget</code> can be modified
	 * programmatically.
	 */
	public void testConfigBuildArtifact () throws CoreException {
		// Open the test project
		IProject project = createProject(projectName);
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertNotNull(info);
		IManagedProject managedProj = info.getManagedProject();
		assertNotNull(managedProj);
		IConfiguration defaultConfig = info.getDefaultConfiguration();
		assertNotNull(defaultConfig);
		
		// Set the build artifact of the configuration
		String ext = defaultConfig.getArtifactExtension();
		String name = project.getName() + "." + ext;
		defaultConfig.setArtifactName(name);
		
		// Save, close, reopen and test again
		ManagedBuildManager.saveBuildInfo(project, false);
		ManagedBuildManager.removeBuildInfo(project);
		project.close(null);
		project.open(null);

		// Check the artifact name
		info = ManagedBuildManager.getBuildInfo(project);
		assertNotNull(info);
		managedProj = info.getManagedProject();
		assertNotNull(managedProj);
		defaultConfig = info.getDefaultConfiguration();
		assertNotNull(defaultConfig);
		assertEquals(name, defaultConfig.getArtifactName());
	}

	public void testThatAlwaysFails() {
		assertTrue(false);
	}
	
	public void testBug43450 () throws Exception{
		IProject project = createProject( projectName );
		
		IFolder folder = project.getProject().getFolder( "includes" );
		if( !folder.exists() ){
			folder.create( false, true, null );
		}
		
		IFile file = project.getProject().getFile( "includes/header.h" );
		if( !file.exists()   ){
			file.create( new ByteArrayInputStream( "class A { public : static int i; };".getBytes() ), false, null );
		}
		
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		IScannerInfo info = provider.getScannerInformation( project );
		ISourceElementRequestor callback = new NullSourceElementRequestor();
		
		IScanner scanner = ParserFactory.createScanner( new CodeReader( "#include <header.h>\n int A::i = 1;".toCharArray() ), 
														info, ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, callback, new NullLogService(), null);
		
		IParser parser = ParserFactory.createParser( scanner, callback, ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, null );
		assertTrue( parser.parse() );
	}
	
}

