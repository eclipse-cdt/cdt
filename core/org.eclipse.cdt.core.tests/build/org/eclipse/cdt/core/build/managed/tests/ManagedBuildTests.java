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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
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
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.core.OptionReference;
import org.eclipse.cdt.managedbuilder.internal.core.ToolReference;
import org.eclipse.cdt.testplugin.FileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ManagedBuildTests extends TestCase {
	private static final boolean boolVal = true;
	private static final String testConfigId = "test.config.override";
	private static final String testConfigName = "Tester";
	private static final String enumVal = "Another Enum";
	private static final String[] listVal = {"_DEBUG", "/usr/include", "libglade.a"};
	private static final String newExt = "wen";
	private static final String projectName = "ManagedBuildTest";
	private static final String projectRename = "ManagedBuildRedux";
	private static final String rootExt = "toor";
	private static final String stringVal = "-c -Wall";
	private static final String subExt = "bus";

	public ManagedBuildTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildTests.class.getName());
		
		suite.addTest(new ManagedBuildTests("testExtensions"));
		suite.addTest(new ManagedBuildTests("testProjectCreation"));
		suite.addTest(new ManagedBuildTests("testConfigurations"));
		suite.addTest(new ManagedBuildTests("testConfigurationReset"));
		suite.addTest(new ManagedBuildTests("testTargetBuildArtifact"));
		suite.addTest(new ManagedBuildTests("testMakeCommandManipulation"));
		suite.addTest(new ManagedBuildTests("testScannerInfoInterface"));
		suite.addTest(new ManagedBuildTests("testBug43450"));
		suite.addTest(new ManagedBuildTests("testProjectRename"));
		suite.addTest(new ManagedBuildTests("cleanup"));
		
		return suite;
	}

	/**
	 * Navigates through the build info as defined in the extensions
	 * defined in this plugin
	 */
	public void testExtensions() throws Exception {
		ITarget testRoot = null;
		ITarget testSub = null;
		ITarget testSubSub = null;
		
		// Note secret null parameter which means just extensions
		ITarget[] targets = ManagedBuildManager.getDefinedTargets(null);

		for (int i = 0; i < targets.length; ++i) {
			ITarget target = targets[i];
			
			if (target.getName().equals("Test Root")) {
				testRoot = target;
				checkRootTarget(testRoot);
			} else if (target.getName().equals("Test Sub")) {
				testSub = target;
				checkSubTarget(testSub);
			} else if (target.getName().equals("Test Sub Sub")) {
				testSubSub = target;
				checkSubSubTarget(testSubSub);
			}
		}
		// All these targets are defines in the plugin files, so none
		// of them should be null at this point
		assertNotNull(testRoot);
		assertNotNull(testSub);
		assertNotNull(testSubSub);
	}

	/**
	 * This test exercises the interface the <code>ITarget</code> exposes to manipulate 
	 * its make command.
	 */
	public void testMakeCommandManipulation () {
		String oldMakeCmd = "make";
		String newMakeCmd = "Ant";
		
		// Open the test project
		IProject project = null;
		try {
			project = createProject(projectName);
		} catch (CoreException e) {
			fail("Failed to open project in 'testMakeCommandManipulation': " + e.getLocalizedMessage());
		}
		assertNotNull(project);
		
		// Now open the root target
		ITarget[] targets = ManagedBuildManager.getTargets(project);
		assertEquals(1, targets.length);
		
		// Does it have a default make command
		assertFalse(targets[0].hasOverridenMakeCommand());
		assertEquals(oldMakeCmd, targets[0].getMakeCommand());
		
		// Change it
		targets[0].setMakeCommand(newMakeCmd);
		assertEquals(newMakeCmd, targets[0].getMakeCommand());
		assertTrue(targets[0].hasOverridenMakeCommand());
		
		// Reset it
		targets[0].resetMakeCommand();
		assertFalse(targets[0].hasOverridenMakeCommand());
		assertEquals(oldMakeCmd, targets[0].getMakeCommand());
	}
	
	
	/**
	 * The purpose of this test is to exercise the build path info interface.
	 * To get to that point, a new target/config has to be created in the test
	 * project and the default configuration changed.
	 *  
	 * @throws CoreException
	 */
	public void testScannerInfoInterface(){
		// Open the test project
		IProject project = null;
		try {
			project = createProject(projectName);
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
		 
		// Create a new target in the project based on the sub target
		ITarget baseTarget = ManagedBuildManager.getTarget(project, "test.sub");
		assertNotNull(baseTarget);
		ITarget newTarget = null;
		try {
			newTarget = ManagedBuildManager.createTarget(project, baseTarget);
		} catch (BuildException e) {
			fail("Failed adding new target to project: " + e.getLocalizedMessage());
		}
		assertNotNull(newTarget);
		// Copy over the configs
		IConfiguration[] baseConfigs = baseTarget.getConfigurations();
		for (int i = 0; i < baseConfigs.length; ++i) {
			newTarget.createConfiguration(baseConfigs[i], baseConfigs[i].getId() + "." + i);
		}
				
		// Change the default configuration to the sub config
		IConfiguration[] configs = newTarget.getConfigurations();
		assertEquals(4, configs.length);
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		buildInfo.setDefaultConfiguration(newTarget.getConfiguration(configs[3].getId()));

		// Use the plugin mechanism to discover the supplier of the path information
		IExtensionPoint extensionPoint = CCorePlugin.getDefault().getDescriptor().getExtensionPoint("ScannerInfoProvider");
		if (extensionPoint == null) {
			fail("Failed to retrieve the extension point ScannerInfoProvider.");
		}

		// Find the first IScannerInfoProvider that supplies build info for the project
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		assertNotNull(provider);
		
		// Check the build information right away
		IScannerInfo currentSettings = provider.getScannerInformation(project);
		Map currentSymbols = currentSettings.getDefinedSymbols();
		// It should simply contain the built-in
		assertTrue(currentSymbols.containsKey("BUILTIN"));
		assertEquals((String)currentSymbols.get("BUILTIN"), "");
		String[] currentPaths = currentSettings.getIncludePaths();
		assertTrue(Arrays.equals(expectedPaths, currentPaths));
		
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
		
		// Add some defined symbols programmatically
		String[] expectedSymbols = {"DEBUG", "GNOME = ME "};
		IConfiguration defaultConfig = buildInfo.getDefaultConfiguration(newTarget);
		ITool[] tools = defaultConfig.getTools();
		ITool subTool = null;
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			if("tool.sub".equalsIgnoreCase(tool.getId())) {
				subTool = tool;
				break;
			}
		}
		assertNotNull(subTool);
		IOption symbolOpt = null;
		IOption[] opts = subTool.getOptions();
		for (int i = 0; i < opts.length; i++) {
			IOption option = opts[i];
			if (option.getValueType() == IOption.PREPROCESSOR_SYMBOLS) {
				symbolOpt = option;
				break;
			}
		}
		assertNotNull(symbolOpt);
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertFalse(info.isDirty());
		ManagedBuildManager.setOption(defaultConfig, symbolOpt, expectedSymbols);
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
		String rootName = "Root Config";
		String overrideName = "Root Override Config";
		String completeOverrideName = "Complete Override Config";
		
		// Open the test project
		IProject project = createProject(projectName);
		
		// Make sure there is one and only one target with 2 configs
		ITarget[] definedTargets = ManagedBuildManager.getTargets(project);
		assertEquals(1, definedTargets.length);
		ITarget rootTarget = definedTargets[0];
		IConfiguration[] definedConfigs = rootTarget.getConfigurations(); 		
		assertEquals(3, definedConfigs.length);
		IConfiguration baseConfig = definedConfigs[0];
		assertEquals(definedConfigs[0].getName(), rootName);
		assertEquals(definedConfigs[1].getName(), overrideName);
		assertEquals(definedConfigs[2].getName(), completeOverrideName);
		
		// Create a new configuration and test the rename function
		IConfiguration newConfig = rootTarget.createConfiguration(baseConfig, testConfigId);
		assertEquals(4, rootTarget.getConfigurations().length);
		newConfig.setName(testConfigName);
		assertEquals(newConfig.getId(), testConfigId);
		assertEquals(newConfig.getName(), testConfigName);

		// There is only one tool
		ITool[] definedTools = newConfig.getTools();
		assertEquals(1, definedTools.length);
		ITool rootTool = definedTools[0];
		
		// Override options in the new configuration
		IOptionCategory topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		IOption[] options = topCategory.getOptions(null);
		assertEquals(2, options.length);
		ManagedBuildManager.setOption(newConfig, options[0], listVal);
		ManagedBuildManager.setOption(newConfig, options[1], boolVal);

		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		options = categories[0].getOptions(null);
		assertEquals(2, options.length);
		ManagedBuildManager.setOption(newConfig, options[0], stringVal);
		ManagedBuildManager.setOption(newConfig, options[1], enumVal);

		// Save, close, reopen and test again
		ManagedBuildManager.saveBuildInfo(project);
		project.close(null);
		ManagedBuildManager.removeBuildInfo(project);
		project.open(null);

		// Test the values in the new configuration
		checkOptionReferences(project);
		
		// Now delete the new configuration and test the target
		definedTargets = ManagedBuildManager.getTargets(project);
		assertEquals(1, definedTargets.length);
		rootTarget = definedTargets[0];
		definedConfigs = rootTarget.getConfigurations(); 		
		assertEquals(4, definedConfigs.length);
		rootTarget.removeConfiguration(testConfigId);
		definedConfigs = rootTarget.getConfigurations(); 		
		assertEquals(3, definedConfigs.length);
		assertEquals(definedConfigs[0].getName(), rootName);
		assertEquals(definedConfigs[1].getName(), overrideName);
	}
	
	public void testConfigurationReset() {
		// Open the test project
		IProject project = null;
		try {
			project = createProject(projectName);
		} catch (CoreException e) {
			fail("Failed to open project: " + e.getLocalizedMessage());
		}

		// Get the default configuration
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertNotNull(info);
		ITarget defaultTarget = info.getDefaultTarget();
		assertNotNull(defaultTarget);
		IConfiguration defaultConfig = info.getDefaultConfiguration(defaultTarget);
		assertNotNull(defaultConfig);
		
		// See if it still contains the overridden values (see testProjectCreation())
		try {
			checkRootTarget(defaultTarget);
		} catch (BuildException e1) {
			fail("Overridden root target check failed: " + e1.getLocalizedMessage());
		}
		
		// Reset the config and retest
		ManagedBuildManager.resetConfiguration(project, defaultConfig);
		try {
			checkRootTarget(defaultTarget);
		} catch (BuildException e2) {
			fail("Reset root target check failed: " + e2.getLocalizedMessage());
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
			addManagedBuildNature(project);
		} catch (CoreException e) {
			fail("Test failed on project creation: " + e.getLocalizedMessage());
		}
		// There should not be any targets defined for this project yet
		assertEquals(0, ManagedBuildManager.getTargets(project).length);
		
		// Find the base target definition
		ITarget targetDef = ManagedBuildManager.getTarget(project, "test.root");
		assertNotNull(targetDef);
		
		// Create the target for our project that builds a dummy executable
		ITarget newTarget = ManagedBuildManager.createTarget(project, targetDef);
		assertEquals(newTarget.getName(), targetDef.getName());
		assertFalse(newTarget.equals(targetDef));
		String buildArtifactName = projectName;
		newTarget.setArtifactName(buildArtifactName);
		newTarget.setArtifactExtension(newExt);
		ITarget[] targets = ManagedBuildManager.getTargets(project);
		assertEquals(1, targets.length);
		ITarget target = targets[0];
		assertEquals(target, newTarget);
		assertFalse(target.equals(targetDef));

		// Copy over the configs
		IConfiguration defaultConfig = null;
		IConfiguration[] configs = targetDef.getConfigurations();
		for (int i = 0; i < configs.length; ++i) {
			// Make the first configuration the default 
			if (i == 0) {
				defaultConfig = target.createConfiguration(configs[i], target.getId() + "." + i);
			} else {
				target.createConfiguration(configs[i], target.getId() + "." + i);
			}
		}
		ManagedBuildManager.setDefaultConfiguration(project, defaultConfig);		
		checkRootTarget(target);
		
		// Override the "String Option in Category" option value
		configs = target.getConfigurations();
		ITool[] tools = configs[0].getTools();
		IOptionCategory topCategory = tools[0].getTopOptionCategory();
		IOptionCategory[] categories = topCategory.getChildCategories();
		IOption[] options = categories[0].getOptions(configs[0]);
		configs[0].setOption(options[0], "z");
		options = categories[0].getOptions(null);
		assertEquals("x", options[0].getStringValue());
		options = categories[0].getOptions(configs[0]);
		assertEquals("z", options[0].getStringValue());
		
		// Save, close, reopen and test again
		ManagedBuildManager.saveBuildInfo(project);
		try {
			project.close(null);
		} catch (CoreException e) {
			fail("Failed on project close: " + e.getLocalizedMessage());
		}
		ManagedBuildManager.removeBuildInfo(project);
		try {
			project.open(null);
		} catch (CoreException e) {
			fail("Failed on project open: " + e.getLocalizedMessage());
		}
		
		// Test that the default config was remembered
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertEquals(defaultConfig.getId(), info.getDefaultConfiguration(target).getId());

		// Get the targets
		targets = ManagedBuildManager.getTargets(project);
		assertEquals(1, targets.length);
		// See if the artifact name is remembered
		assertEquals(targets[0].getArtifactName(), buildArtifactName);
		// Check the rest of the default information
		checkRootTarget(targets[0]);
		
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
		} catch (CoreException e) {
			fail("Failed to open renamed project: " + e.getLocalizedMessage());
		}

		// By now there should be 2 targets with 3 configs
		ITarget[] definedTargets = ManagedBuildManager.getTargets(project);
		assertEquals(2, definedTargets.length);
		ITarget rootTarget = definedTargets[0];
		IConfiguration[] definedConfigs = rootTarget.getConfigurations(); 		
		assertEquals(3, definedConfigs.length);
		IConfiguration baseConfig = definedConfigs[0];
		
		// There is only one tool
		ITool[] definedTools = baseConfig.getTools();
		assertEquals(1, definedTools.length);
		ITool rootTool = definedTools[0];
		
		// Get the options 2 in top category and 2 in its child
		IOptionCategory topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		IOption[] options = topCategory.getOptions(null);
		assertEquals(2, options.length);
		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		options = categories[0].getOptions(null);
		assertEquals(2, options.length);
		
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
		} catch (CoreException e) {
			fail("Failed to open re-renamed project: " + e.getLocalizedMessage());
		}

		// Do it all again
		definedTargets = ManagedBuildManager.getTargets(project);
		assertEquals(2, definedTargets.length);
		rootTarget = definedTargets[0];
		definedConfigs = rootTarget.getConfigurations(); 		
		assertEquals(3, definedConfigs.length);
		baseConfig = definedConfigs[0];
		definedTools = baseConfig.getTools();
		assertEquals(1, definedTools.length);
		rootTool = definedTools[0];
		topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		options = topCategory.getOptions(null);
		assertEquals(2, options.length);
		categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		options = categories[0].getOptions(null);
		assertEquals(2, options.length);
	}
	
	private void addManagedBuildNature (IProject project) {
		// Add the managed build nature
		try {
			ManagedCProjectNature.addManagedNature(project, new NullProgressMonitor());
		} catch (CoreException e) {
			fail("Test failed on adding managed build nature: " + e.getLocalizedMessage());
		}

		// Associate the project with the managed builder so the clients can get proper information
		try {
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project);
			desc.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
			desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
		} catch (CoreException e) {
			fail("Test failed on adding managed builder as scanner info provider: " + e.getLocalizedMessage());
		}
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
		ITarget buildTarget = info.getDefaultTarget();
		assertNotNull(buildTarget);
		IConfiguration buildConfig = info.getDefaultConfiguration(buildTarget);
		assertNotNull(buildConfig);
				
		// The default target should be the same as the one-and-only target in the project
		List targets = info.getTargets();
		assertEquals(targets.size(), 1);
		ITarget target = (ITarget) targets.get(0);
		assertEquals(target, buildTarget);
		
		// Check that tool handles resources with extensions foo and bar by building a baz
		assertEquals(info.getOutputExtension(ext1), expectedOutput);
		assertEquals(info.getOutputExtension(ext2), expectedOutput);
		
		// Check that it ignores others based on filename extensions
		assertNull(info.getOutputExtension(badExt));
		
		// Now see what the tool command line invocation is for foo and bar
		assertEquals(info.getToolForSource(ext1), expectedCmd);
		assertEquals(info.getToolForSource(ext2), expectedCmd);
		// Make sure that there is no tool to build files of type foo and bar
		assertNull(info.getToolForTarget(ext1));
		assertNull(info.getToolForTarget(ext2));
		
		// There is no target that builds toor
		assertNull(info.getToolForSource(expectedOutput));
		// but there is one that produces it
		assertEquals(info.getToolForTarget(expectedOutput), expectedCmd);
		
		// Now check the build flags
		assertEquals(info.getFlagsForSource(ext1), "-La -Lb z -e1");
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
		// Get the targets out of the project
		ITarget[] definedTargets = ManagedBuildManager.getTargets(project);
		assertEquals(1, definedTargets.length);
		ITarget rootTarget = definedTargets[0];

		// Now get the configs
		IConfiguration[] definedConfigs = rootTarget.getConfigurations(); 		
		assertEquals(4, definedConfigs.length);
		IConfiguration newConfig = rootTarget.getConfiguration(testConfigId);
		assertNotNull(newConfig);

		// Now get the tool options and make sure the values are correct		
		ITool[] definedTools = newConfig.getTools();
		assertEquals(1, definedTools.length);
		ITool rootTool = definedTools[0];

		// Check that the options in the new config contain overridden values
		IOption[] rootOptions = rootTool.getOptions();
		assertEquals(4, rootOptions.length);
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
		// Next option is a string category
		assertEquals("String Option in Category", rootOptions[2].getName());
		assertEquals(IOption.STRING, rootOptions[2].getValueType());
		assertEquals(stringVal, rootOptions[2].getStringValue());
		// Final option is an enumerated
		assertEquals("Enumerated Option in Category", rootOptions[3].getName());
		assertEquals(IOption.ENUMERATED, rootOptions[3].getValueType());
		String selEnum = rootOptions[3].getSelectedEnum();
		assertEquals(enumVal, selEnum);
		String[] enums = rootOptions[3].getApplicableValues();
		assertEquals(2, enums.length);
		assertEquals("Default Enum", enums[0]);
		assertEquals("Another Enum", enums[1]);
		assertEquals("-e1", rootOptions[3].getEnumCommand(enums[0]));
		assertEquals("-e2", rootOptions[3].getEnumCommand(enums[1]));
		assertEquals("-e2", rootOptions[3].getEnumCommand(selEnum));
	}
	
	/*
	 * Do a full sanity check on the root target.
	 */
	private void checkRootTarget(ITarget target) throws BuildException {
		// Target stuff
		String expectedCleanCmd = "del /myworld";
		String expectedParserId = "org.eclipse.cdt.core.PE";
		String[] expectedOSList = {"win32"};
		assertTrue(target.isTestTarget());
		if (target.getArtifactName().equals("ManagedBuildTest")) {
			assertEquals(target.getArtifactExtension(), newExt);
		} else {
			assertEquals(target.getArtifactExtension(), rootExt);
		}
		assertEquals(expectedCleanCmd, target.getCleanCommand());
		assertEquals("make", target.getMakeCommand());
		assertEquals(expectedParserId, target.getBinaryParserId());
		assertTrue(Arrays.equals(expectedOSList, target.getTargetOSList()));
		
		// Tools
		ITool[] tools = target.getTools();
		// Root Tool
		ITool rootTool = tools[0];
		assertEquals("Root Tool", rootTool.getName());
		// 4 Options are defined in the root tool
		IOption[] options = rootTool.getOptions();
		assertEquals(4, options.length);
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
		// Final option is an enumerated
		assertEquals("Enumerated Option in Category", options[3].getName());
		assertEquals(IOption.ENUMERATED, options[3].getValueType());
		assertEquals("Default Enum", options[3].getSelectedEnum());
		valueList = options[3].getApplicableValues();
		assertEquals(2, valueList.length);
		assertEquals("Default Enum", valueList[0]);
		assertEquals("Another Enum", valueList[1]);
		assertEquals("-e1", options[3].getEnumCommand(valueList[0]));
		assertEquals("-e2", options[3].getEnumCommand(valueList[1]));
		
		// Option Categories
		IOptionCategory topCategory = rootTool.getTopOptionCategory();
		assertEquals("Root Tool", topCategory.getName());
		options = topCategory.getOptions(null);
		assertEquals(2, options.length);
		assertEquals("List Option in Top", options[0].getName());
		assertEquals("Boolean Option in Top", options[1].getName());
		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		assertEquals("Category", categories[0].getName());
		options = categories[0].getOptions(null);
		assertEquals(2, options.length);
		assertEquals("String Option in Category", options[0].getName());
		assertEquals("Enumerated Option in Category", options[1].getName());

		// There should be 3 defined configs
		IConfiguration[] configs = target.getConfigurations();
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
		assertTrue(tools[0] instanceof ToolReference);
		assertEquals("Root Tool", tools[0].getName());
		topCategory = tools[0].getTopOptionCategory();
		options = topCategory.getOptions(configs[1]);
		assertEquals(2, options.length);
		assertEquals("List Option in Top", options[0].getName());
		valueList = options[0].getStringListValue();
		assertEquals("a", valueList[0]);
		assertEquals("b", valueList[1]);
		assertEquals("Boolean Option in Top", options[1].getName());
		assertEquals(true, options[1].getBooleanValue());
		assertEquals("-b", options[1].getCommand());
		categories = topCategory.getChildCategories();
		options = categories[0].getOptions(configs[1]);
		assertEquals(2, options.length);
		assertEquals("String Option in Category", options[0].getName());
		assertEquals("y", options[0].getStringValue());
		assertEquals("Enumerated Option in Category", options[1].getName());
		valueList = options[1].getApplicableValues();
		assertEquals(2, valueList.length);
		assertEquals("Default Enum", valueList[0]);
		assertEquals("Another Enum", valueList[1]);
		assertEquals("-e1", options[1].getEnumCommand(valueList[0]));
		assertEquals("-e2", options[1].getEnumCommand(valueList[1]));
		assertEquals(1, tools.length);
		assertEquals("Root Tool", tools[0].getName());
		assertEquals("-r", tools[0].getOutputFlag());
		assertTrue(tools[0].buildsFileType("foo"));
		assertTrue(tools[0].buildsFileType("bar"));
		assertTrue(tools[0].producesFileType("toor"));
		assertTrue(tools[0].isHeaderFile("baz"));
		assertEquals("doIt", tools[0].getToolCommand());
		
		// Completely Overridden configuration
		assertEquals("Complete Override Config", configs[2].getName());
		tools = configs[2].getTools();
		assertEquals(1, tools.length);
		assertTrue(tools[0] instanceof ToolReference);
		assertEquals("Root Tool", tools[0].getName());
		topCategory = tools[0].getTopOptionCategory();
		options = topCategory.getOptions(configs[2]);
		assertEquals(2, options.length);
		// Check that there's an empty string list and a true boolean (commands should not have changed)
		assertTrue(options[0] instanceof OptionReference);
		assertEquals("List Option in Top", options[0].getName());
		assertEquals(IOption.STRING_LIST, options[0].getValueType());
		valueList = options[0].getStringListValue();
		assertTrue(valueList.length == 0);
		assertEquals("-L", options[0].getCommand());
		assertEquals("Boolean Option in Top", options[1].getName());
		assertTrue(options[1] instanceof OptionReference);
		assertEquals("Boolean Option in Top", options[1].getName());
		assertEquals(IOption.BOOLEAN, options[1].getValueType());
		assertEquals(true, options[1].getBooleanValue());
		assertEquals("-b", options[1].getCommand());
		// Check that there's an overridden enumeration and string
		categories = topCategory.getChildCategories();
		options = categories[0].getOptions(configs[2]);
		assertEquals(2, options.length);
		assertTrue(options[0] instanceof OptionReference);
		assertEquals("String Option in Category", options[0].getName());
		assertEquals(IOption.STRING, options[0].getValueType());
		assertEquals("overridden", options[0].getStringValue());
		assertTrue(options[1] instanceof OptionReference);
		assertEquals("Enumerated Option in Category", options[1].getName());
		assertEquals(IOption.ENUMERATED, options[1].getValueType());
		assertEquals("-e2", options[1].getSelectedEnum());
	}

	/*
	 * @param testSubSub
	 */
	private void checkSubSubTarget(ITarget target) {
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
		
		// Check the inherited clean command
		assertEquals("rm -yourworld", target.getCleanCommand());
		// Check that the make command is overridden from parent
		assertEquals("nmake", target.getMakeCommand());
		// Make sure we get the proper binary parser
		assertEquals("org.eclipse.cdt.core.ELF", target.getBinaryParserId());
		// Make sure the list is inherited
		String[] expectedOSList = {"win32","linux","solaris"};
		assertTrue(Arrays.equals(expectedOSList, target.getTargetOSList()));

		// Get the 4 configurations
		IConfiguration[] configs = target.getConfigurations();
		assertEquals(4, configs.length);
		
		// Check the tools. We should have 3 (1 from each parent and the one referenced).
		ITool[] tools = target.getTools();
		assertEquals(3, tools.length);
		// Make sure we get all the tool settings
		assertEquals(tools[2].getName(), indyToolName);
		assertEquals(tools[2].getToolCommand(), indyToolCommand);
		assertTrue(tools[2].buildsFileType(indyToolInputExt));
		assertEquals(tools[2].getOutputExtension(indyToolInputExt), indyToolOutputExt);
		assertEquals(tools[2].getOutputFlag(), indyToolOutFlag);
		assertTrue(tools[2].isHeaderFile(indyToolHeader));
		assertFalse(tools[2].isHeaderFile(indyToolHeaderNot));
		assertEquals(tools[2].getNatureFilter(), ITool.FILTER_BOTH);
		// Check out the referenced tool and make sure we get all option categories
		IOptionCategory topCategory = tools[2].getTopOptionCategory();
		IOptionCategory[] categories = topCategory.getChildCategories();
		assertEquals(1, categories.length);
		assertEquals(categories[0].getName(), indyCatOne);
		IOptionCategory[] subCategories = categories[0].getChildCategories();
		// Is the chained category a subcategory
		assertEquals(1, subCategories.length);
		assertEquals(subCategories[0].getName(), indyCatTwo);
		// Make sure the option in the top category is correct
		IOption[] optsInCat = categories[0].getOptions(null);
		assertEquals(1, optsInCat.length);
		assertEquals(freeOptName, optsInCat[0].getName());
		assertEquals(IOption.STRING, optsInCat[0].getValueType());
		try {
			assertEquals(freeOptValue, optsInCat[0].getStringValue());
		} catch (BuildException e1) {
			fail("Failed getting string value in subsubtarget :" + e1.getLocalizedMessage());
		}

		// Do the same for the options in the child cat
		IOption[] optsInSubCat = subCategories[0].getOptions(null);
		assertEquals(1, optsInSubCat.length);
		assertEquals(chainedOptName, optsInSubCat[0].getName());
		assertEquals(IOption.BOOLEAN, optsInSubCat[0].getValueType());
		try {
			assertFalse(optsInSubCat[0].getBooleanValue());
		} catch (BuildException e) {
			fail("Failure getting boolean value in subsubtarget: " + e.getLocalizedMessage());
		}
	}

	/*
	 * Do a sanity check on the values in the sub-target. Most of the
	 * sanity on the how build model entries are read is performed in 
	 * the root target check, so these tests just verify that the the sub 
	 * target properly inherits from its parent. For the new options
	 * in the sub target, the test does a sanity check just to be complete.
	 */
	private void checkSubTarget(ITarget target) throws BuildException {
		// Check the overridden clean command
		assertEquals("rm -yourworld", target.getCleanCommand());
		// Make sure the target inherits the make command
		assertEquals("make", target.getMakeCommand());
		// Make sure the binary parser is hard-coded and available
		assertEquals("org.eclipse.cdt.core.PE", target.getBinaryParserId());
		String[] expectedOSList = {"win32","linux","solaris"};
		assertTrue(Arrays.equals(expectedOSList, target.getTargetOSList()));

		// Make sure this is a test target
		assertTrue(target.isTestTarget());
		// Make sure the build artifact extension is there
		assertEquals(target.getArtifactExtension(), subExt);
				
		// Get the tools for this target
		ITool[] tools = target.getTools();
		// Do we inherit properly from parent
		ITool rootTool = tools[0];
		assertEquals("Root Tool", rootTool.getName());
		// Now get the tool defined for this target
		ITool subTool = tools[1];
		assertEquals("Sub Tool", subTool.getName());
		// Confirm that it has four options
		IOption[] subOpts = subTool.getOptions();
		assertEquals(4, subOpts.length);
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
		assertEquals("More Includes", subOpts[2].getName());
		assertEquals(IOption.INCLUDE_PATH, subOpts[2].getValueType());
		String[] moreIncPath = subOpts[2].getIncludePaths();
		assertEquals(2, moreIncPath.length);
		assertEquals("C:\\home\\tester/include", moreIncPath[0]);
		assertEquals("-I", subOpts[2].getCommand());
		// Check the user object option
		assertEquals("User Objects", subOpts[3].getName());
		assertEquals(IOption.OBJECTS, subOpts[3].getValueType());
		String[] objs = subOpts[3].getUserObjects();
		assertEquals(2, objs.length);
		assertEquals("obj1.o", objs[0]);
		assertEquals("obj2.o", objs[1]);
		
		// Get the configs for this target; it should inherit all the configs defined for the parent
		IConfiguration[] configs = target.getConfigurations();
		assertEquals(4, configs.length);
		IConfiguration rootConfig = configs[0];
		assertEquals("Root Config", rootConfig.getName());
		assertEquals("Root Override Config", configs[1].getName());
		assertEquals("Complete Override Config", configs[2].getName());
		assertEquals("Sub Config", configs[3].getName());
	}

	/**
	 * Remove all the project information associated with the project used during test.
	 */
	public void cleanup() {
		removeProject(projectName);
	}
	
	/**
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
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			//description.setLocation(root.getLocation());
			project = CCorePlugin.getDefault().createCProject(description, newProjectHandle, new NullProgressMonitor(), MakeCorePlugin.MAKE_PROJECT_ID);
		} else {
			newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);
			project = newProjectHandle;
		}
        
		if (!project.isOpen()) {
			project.open(null);
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
				project.delete(true, false, null);
			} catch (CoreException e) {
				assertTrue(false);
			}
		}
	}
	
	/**
	 * Test that the build artifact of a <code>ITarget</code> can be modified
	 * programmatically.
	 */
	public void testTargetBuildArtifact () throws CoreException {
		// Open the test project
		IProject project = createProject(projectName);
		
		// Make sure there is one and only one target
		ITarget[] definedTargets = ManagedBuildManager.getTargets(project);
		assertEquals(1, definedTargets.length);
		ITarget rootTarget = definedTargets[0];
		
		// Set the build artifact of the target
		String ext = rootTarget.getArtifactExtension();
		String name = project.getName() + "." + ext;
		rootTarget.setArtifactName(name);
		
		// Save, close, reopen and test again
		ManagedBuildManager.saveBuildInfo(project);
		project.close(null);
		ManagedBuildManager.removeBuildInfo(project);
		project.open(null);

		// Make sure there is one and only one target
		definedTargets = ManagedBuildManager.getTargets(project);
		assertEquals(1, definedTargets.length);
		rootTarget = definedTargets[0];
		assertEquals(name, rootTarget.getArtifactName());
	}

	public void testThatAlwaysFails() {
		assertTrue(false);
	}
	
	public void testBug43450 () throws Exception{
		IProject project = createProject( projectName );
		
		FileManager fileManager = new FileManager();

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
		
		IScanner scanner = ParserFactory.createScanner( new StringReader( "#include <header.h>\n int A::i = 1;" ), 
														"TEST", info, ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, callback, new NullLogService());
		
		IParser parser = ParserFactory.createParser( scanner, callback, ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, null );
		assertTrue( parser.parse() );
	}
	
}
