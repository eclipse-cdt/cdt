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

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


public class ResourceBuildCoreTests extends TestCase {
	private static final boolean boolVal = true;
	private static IProjectType exeType;	
	private static IProjectType libType;
	private static IProjectType dllType;
	
	
	private static final String projectName = "T1";
	private static final String renamedProjectName1 = "T1_1";
	private static final String renamedProjectName2 = "T1_2";
	
	public ResourceBuildCoreTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ResourceBuildCoreTests.class.getName());		
		suite.addTest(new ResourceBuildCoreTests("testResourceConfigurations"));
		suite.addTest(new ResourceBuildCoreTests("testResourceConfigurationReset"));
		suite.addTest(new ResourceBuildCoreTests("testResourceConfigurationBuildInfo"));
		suite.addTest(new ResourceBuildCoreTests("testResourceRename"));
		return suite;
	}

	/**
	 * Creates a couple of resource configurations.
	 * Checks whether the  resource & project default build properties are same or not. 
	 * Overrides project build properties and checks whether they are reflecting at resource level.
	 * Overrides resource build properties and checks whether they are reflecting at project level.
	 * 
	 */
	
	public void testResourceConfigurations() throws Exception {
			
		// Create a new project
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
		IProjectType projType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.exe");
		assertNotNull(projType);
		
		// Create the managed-project (.cdtbuild) for our project that builds an executable.
		IManagedProject newProject = ManagedBuildManager.createManagedProject(project, projType);
		assertEquals(newProject.getName(), projType.getName());
		assertFalse(newProject.equals(projType));
		ManagedBuildManager.setNewProjectVersion(project);
			
		// Create a couple of resources ( 'main.c' & 'bar.c')
		IFile mainFile = project.getProject().getFile( "main.c" );
		if( !mainFile.exists() ){
			mainFile.create( new ByteArrayInputStream( "#include <stdio.h>\n extern void bar(); \n int main() { \nprintf(\"Hello, World!!\"); \n bar();\n return 0; }".getBytes() ), false, null );
		}
		
		IFile barFile = project.getProject().getFile( "bar.c" );
		if( !barFile.exists() ){
			barFile.create( new ByteArrayInputStream( "#include <stdio.h>\n void bar() { \nprintf(\"Hello, bar()!!\");\n return; }".getBytes() ), false, null );
		}
	
		// Get the configurations and make one of them as default configuration.
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

		// Create Resource Configurations for files main.c and bar.c
		IResourceConfiguration resMainConfig = defaultConfig.createResourceConfiguration(mainFile);
		IResourceConfiguration resBarConfig = defaultConfig.createResourceConfiguration(barFile);
		
		// Check whether defaultConfig has two resource configurations or not.
		IResourceConfiguration resConfigs[] = defaultConfig.getResourceConfigurations();
		assertEquals(2,resConfigs.length);
		
		// Get the tools associated with the resource 'main.c'.
		ITool resMainTools[] = resMainConfig.getTools();
		assertNotNull(resMainTools);
		assertEquals(1,resMainTools.length);
		
		// Get the tools associated with the resource 'bar.c'.
		ITool resBarTools[] = resBarConfig.getTools();
		assertNotNull(resBarTools);
		assertEquals(1,resBarTools.length);

		// Get the build properties for the resource main.c
		ITool resMainTool = resMainTools[0];
		
		String resMainBuildProps = resMainTool.getToolFlags();
			
		// Get the build properties for the resource bar.c
		ITool resBarTool = resBarTools[0];
		String resBarBuildProps = resBarTool.getToolFlags();
		
		//	Get file extension.
		String extString = mainFile.getFileExtension();
		
		// Get the project build properties.
		ITool tools[] = defaultConfig.getFilteredTools();
		Tool projTool = null;
		String projBuildProps = new String();
		for (int i = 0; i < tools.length; i++) {
			if( tools[i].buildsFileType(extString) ) {
				// Get the build properties of a project in default configuration
				projTool = (Tool)tools[i];
				projBuildProps = projTool.getToolFlags();
				break;
			}
		}
		
		// Initially, Project build properties and resource build properties are same.
		assertEquals(resMainBuildProps,projBuildProps);
				
		// Initially, build properties of files with same extension ( example , .c files) are equal.
		assertEquals(resMainBuildProps,resBarBuildProps);
		
		// Now modify project build properties and it should reflect in resource build properties also.
			
		IOption projDebugOption = projTool.getOptionById("testgnu.c.compiler.exe.debug.option.debugging.level");
			
		assertNotNull(projDebugOption);
		
		// Override options in the default configuration.
		// Set the debug option value to '-g2' at Project level
		IOption newProjDebugOption = ManagedBuildManager.setOption(defaultConfig,projTool,projDebugOption,"testgnu.c.debugging.level.default");
		
		// Get the option 'id' and 'value'.
		String newProjDebugOptionId = newProjDebugOption.getId();
		String newProjDebugOptionValue  = newProjDebugOption.getStringValue();
		
		// Assert old & new(overridden) debug option values of project are different.
		assertNotSame(projDebugOption.getStringValue(),newProjDebugOptionValue);
		
		// Check whether the overridden option at project level is reflecting at resource level or not.			
		IOption resMainDebugOption = null;
				
		IOption resMainOptions[] = resMainTool.getOptions();
		
		for(int i=0; i< resMainOptions.length; i++){
			IOption opt = resMainOptions[i];
			if( opt != null ) {
				if(opt.getId().equals(newProjDebugOptionId)){
					// Resource Configuration doesnot have overridden value for this option.
					resMainDebugOption = opt;
					break;
				}
				if ( opt.getSuperClass() != null ) {
					if(opt.getSuperClass().getId().equals(newProjDebugOptionId)){
						// Resource Configuration does have overridden value for this option.
						resMainDebugOption = opt;
						break;	
					}	
				}
			}
		}
		
		String resMainDebugOptionValue = resMainDebugOption.getStringValue();
		
		// Assert Debug option values of project and resource are same.
		assertEquals(newProjDebugOptionValue, resMainDebugOptionValue);
		
		// Now, Modify the Debug option at resource level
		// and verify whether the modified option is reflected at project level. 
		// It should not reflect at project level.
		
		IOption newResMainDebugOption = ManagedBuildManager.setOption(resMainConfig,resMainTool,resMainDebugOption,"gnu.c.debugging.level.minimal");		
				
		//Get the latest project Debug option.
		tools = defaultConfig.getFilteredTools();
		projTool = null;
		
		for (int i = 0; i < tools.length; i++) {
			if( tools[i].buildsFileType(extString) ) {
				// Get the build properties of a project in default configuration
				projTool = (Tool)tools[i];
				break;
			}
		}
		
		projDebugOption = projTool.getOptionById(newProjDebugOptionId);
		String projDebugOptionValue = projDebugOption.getStringValue();
		
		String newResMainDebugOptionValue = newResMainDebugOption.getStringValue();
		
		// Assert the debug option values of project and resource are different.		
		assertNotSame(projDebugOptionValue, newResMainDebugOptionValue);
		
		// Close and remove project.
		project.close(null);
		removeProject(projectName);
	}
	
	/*
	 * 	Creates a project and a resource(hello.c).
	 * 	Overrides the build properties in resource configuration, and
	 * 	resets the resource configuration, verifies whether the overridden
	 * 	values still exist or not.
	 */
	
	public void testResourceConfigurationReset() throws Exception {
		
		// Create a new project
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
		IProjectType projType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.exe");
		assertNotNull(projType);
		
		// Create the managed-project (.cdtbuild) for our project that builds an executable.
		IManagedProject newProject = ManagedBuildManager.createManagedProject(project, projType);
		assertEquals(newProject.getName(), projType.getName());
		assertFalse(newProject.equals(projType));
		ManagedBuildManager.setNewProjectVersion(project);
			
		// Create a resource ( 'hello.c')
		IFile helloFile = project.getProject().getFile( "hello.c" );
		if( !helloFile.exists() ){
			helloFile.create( new ByteArrayInputStream( "#include <stdio.h>\n\n int main() { \nprintf(\"Hello, World!!\"); \n bar();\n return 0; }".getBytes() ), false, null );
		}
	
		// Get the configurations and make one of them as default configuration.
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

		// Create Resource Configurations for hello.c
		IResourceConfiguration resConfig = defaultConfig.createResourceConfiguration(helloFile);
		
		// Check whether defaultConfig has the correct number of resource configurations or not.
		IResourceConfiguration resConfigs[] = defaultConfig.getResourceConfigurations();
		assertEquals(1,resConfigs.length);
		
		// Get the tools associated with the resource 'hello.c'.
		ITool resTools[] = resConfig.getTools();
		assertNotNull(resTools);
		assertEquals(1,resTools.length);
		
		// Get the build properties for the resource hello.c
		ITool resTool = resTools[0];
		String defaultResToolFlags = resTool.getToolFlags();
		
		// Get the Debug Option.
		IOption resDebugOption = resTool.getOptionById("testgnu.c.compiler.exe.debug.option.debugging.level");
		
		// Get the default value of debug option for resource.
		String defaultResDebugOptVal = resDebugOption.getStringValue();
		
		// Now, override the value with "gnu.c.debugging.level.minimal"
		IOption newResDebugOption = ManagedBuildManager.setOption(resConfig,resTool,resDebugOption,"gnu.c.debugging.level.minimal");
		
		// Get the overridden value of debug option.
		String newResDebugOptVal = newResDebugOption.getStringValue();
		String newResToolFlags = resTool.getToolFlags();
		
		// Make sure, default and overridden values are different.
		assertNotSame(defaultResDebugOptVal,newResDebugOptVal);
		
		// Reset the resource configuration.
		ManagedBuildManager.resetResourceConfiguration(project,resConfig);
		String resetResToolFlags = resTool.getToolFlags();
		
		assertNotSame(resetResToolFlags,newResToolFlags);
		assertEquals(defaultResToolFlags,resetResToolFlags);
		
		// Close and remove project.
		project.close(null);
		removeProject(projectName);
	}
	
	/*
	 * 	Creates a project and a couple of resources.
	 * 	Overrides the build properties of resources. Saves, closes, and reopens 
	 * 	the project. Then, checks the overridden options. Basically, this function
	 * 	tests persisting overridden resource build properties between project sessions.
	 */
	
	public void testResourceConfigurationBuildInfo() throws Exception {
		
		//	Create a new project
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
		IProjectType projType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.exe");
		assertNotNull(projType);
		
		// Create the managed-project (.cdtbuild) for our project that builds an executable.
		IManagedProject newProject = ManagedBuildManager.createManagedProject(project, projType);
		assertEquals(newProject.getName(), projType.getName());
		assertFalse(newProject.equals(projType));
		ManagedBuildManager.setNewProjectVersion(project);
			
		// Create a couple of resources ( 'main.c' & 'bar.c')
		IFile mainFile = project.getProject().getFile( "main.c" );
		if( !mainFile.exists() ){
			mainFile.create( new ByteArrayInputStream( "#include <stdio.h>\n extern void bar(); \n int main() { \nprintf(\"Hello, World!!\"); \n bar();\n return 0; }".getBytes() ), false, null );
		}
		
		IFile barFile = project.getProject().getFile( "bar.c" );
		if( !barFile.exists() ){
			barFile.create( new ByteArrayInputStream( "#include <stdio.h>\n void bar() { \nprintf(\"Hello, bar()!!\");\n return; }".getBytes() ), false, null );
		}
	
		// Get the configurations and make one of them as default configuration.
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
		
		// Get the default configuration id.
		String defaultConfigId = defaultConfig.getId();

		// Create Resource Configurations for files main.c and bar.c
		IResourceConfiguration resMainConfig = defaultConfig.createResourceConfiguration(mainFile);
		IResourceConfiguration resBarConfig = defaultConfig.createResourceConfiguration(barFile);
		
		// Check whether defaultConfig has two resource configurations or not.
		IResourceConfiguration resConfigs[] = defaultConfig.getResourceConfigurations();
		assertEquals(2,resConfigs.length);

		// Get the paths of resource configurations.
		String resMainPath = resMainConfig.getResourcePath();
		String resBarPath  = resBarConfig.getResourcePath();
		
		// Get the tools associated with the resource 'main.c'.
		ITool resMainTools[] = resMainConfig.getTools();
		assertNotNull(resMainTools);
		assertEquals(1,resMainTools.length);
		
		// Get the tools associated with the resource 'bar.c'.
		ITool resBarTools[] = resBarConfig.getTools();
		assertNotNull(resBarTools);
		assertEquals(1,resBarTools.length);

		// Get the build properties for the resource main.c
		ITool resMainTool = resMainTools[0];
		
		String defaultMainBuildProps = resMainTool.getToolFlags();
			
		// Get the build properties for the resource bar.c
		ITool resBarTool = resBarTools[0];
		String defaultBarBuildProps = resBarTool.getToolFlags();
		
		
		// Now, override debug and optimization options.
		// In Debug Configuration, Currently default values in resource configurations are
		//	optimization : -O0,  debug : -g3
		//  Override the options in the following way.
		//  main.c :    optimization : '-O1'  debug : '-g1'
		//  bar.c  :    optimization : '-O2'  debug : '-g2'
		
		IOption defaultResMainOptOption = resMainTool.getOptionById("testgnu.c.compiler.exe.debug.option.optimization.level");
		String defaultResMainOptVal = defaultResMainOptOption.getStringValue();
		
		IOption resMainOptOption = ManagedBuildManager.setOption(resMainConfig,resMainTool,defaultResMainOptOption,"gnu.c.optimization.level.optimize");
		String resMainOptVal = resMainOptOption.getStringValue();
		
		IOption defaultResMainDebugOption = resMainTool.getOptionById("testgnu.c.compiler.exe.debug.option.debugging.level");
		String defaultResMainDebugVal = defaultResMainDebugOption.getStringValue();
	
		IOption resMainDebugOption = ManagedBuildManager.setOption(resMainConfig,resMainTool,defaultResMainDebugOption,"gnu.c.debugging.level.minimal");
		String resMainDebugVal = resMainDebugOption.getStringValue();
		
		IOption defaultResBarOptOption = resBarTool.getOptionById("testgnu.c.compiler.exe.debug.option.optimization.level");
		String defaultResBarOptVal = defaultResBarOptOption.getStringValue();
		
		IOption resBarOptOption = ManagedBuildManager.setOption(resBarConfig,resBarTool,defaultResBarOptOption,"gnu.c.optimization.level.more");
		String resBarOptVal  = resBarOptOption.getStringValue();
		
		IOption defaultResBarDebugOption = resBarTool.getOptionById("testgnu.c.compiler.exe.debug.option.debugging.level");
		String defaultResBarDebugVal = defaultResBarDebugOption.getStringValue();
		
		IOption resBarDebugOption = ManagedBuildManager.setOption(resBarConfig,resBarTool,defaultResBarDebugOption,"gnu.c.debugging.level.default");
		String resBarDebugVal = resBarDebugOption.getStringValue();
		
		assertNotSame(defaultResMainOptVal, resMainOptVal);
		assertNotSame(defaultResMainDebugVal, resMainDebugVal);
		
		assertNotSame(defaultResBarOptVal, resBarOptVal);
		assertNotSame(defaultResBarDebugVal, resBarDebugVal);
		
		// Save and Close the project.
		ManagedBuildManager.saveBuildInfo(project,false);
		ManagedBuildManager.removeBuildInfo(project);
		
		project.close(null);
		
		// Now reopen the project.
		project.open(null);
		
		// Get the build info.
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject newManagedProj = info.getManagedProject();
		
		// Verify that there are only two configurations.
		IConfiguration[] definedConfigs = newManagedProj.getConfigurations(); 		
		assertEquals(2, definedConfigs.length);
		
		// Get the default configuration and verify whether it is same as before.
		IConfiguration newDefaultConfig = info.getDefaultConfiguration();
		
		assertEquals(defaultConfigId, newDefaultConfig.getId());
		
		// Get the resource configurations in defaultConfig
		IResourceConfiguration newResConfigs[] = newDefaultConfig.getResourceConfigurations();
		
		assertEquals(2, newResConfigs.length);
		
		// Get the resource configuration for main.c using the path '/T1/main.c' 
		IResourceConfiguration newResMainConfig = newDefaultConfig.getResourceConfiguration(resMainPath);
		assertNotNull(newResMainConfig);
		
		ITool newResMainTools[] = newResMainConfig.getTools();
		assertEquals(1,newResMainTools.length);
		
		// Get the Optimization and Debug option values for the resource 'main.c'.
		ITool newResMainTool = newResMainTools[0];
		
		IOption newResMainOptOption = newResMainTool.getOptionById(resMainOptOption.getId());
		assertNotNull(newResMainOptOption);
		String newResMainOptVal = newResMainOptOption.getStringValue();
		
		IOption newResMainDebugOption = newResMainTool.getOptionById(resMainDebugOption.getId());
		assertNotNull(newResMainDebugOption);
		String newResMainDebugVal = newResMainDebugOption.getStringValue();
		
		// Assert that optimization & debug option values for the resource main.c are same between the sessions.
		assertEquals(resMainOptVal, newResMainOptVal);
		assertEquals(resMainDebugVal,newResMainDebugVal);
		
		// Get the resource configuration for bar.c
		IResourceConfiguration newResBarConfig = newDefaultConfig.getResourceConfiguration(resBarPath);
		assertNotNull(newResBarConfig);
		
		ITool newResBarTools[] = newResBarConfig.getTools();
		assertEquals(1,newResBarTools.length);
		
		//Get the Optimization and Debug option values for the resource 'bar.c'
		ITool newResBarTool = newResBarTools[0];
		
		IOption newResBarOptOption = newResBarTool.getOptionById(resBarOptOption.getId());
		assertNotNull(newResBarOptOption);
		String newResBarOptVal = newResBarOptOption.getStringValue();
		
		IOption newResBarDebugOption = newResBarTool.getOptionById(resBarDebugOption.getId());
		assertNotNull(newResBarDebugOption);
		String newResBarDebugVal = newResBarDebugOption.getStringValue();
		
		// Assert that optimization & debug option values for the resource main.c are same between the sessions.
		assertEquals(resBarOptVal, newResBarOptVal);
		assertEquals(resBarDebugVal,newResBarDebugVal);
		
		//	Close and remove project.
		project.close(null);
		removeProject(projectName);
		
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
			project = CCorePlugin.getDefault().createCProject(description, newProjectHandle, new NullProgressMonitor(), MakeCorePlugin.MAKE_PROJECT_ID);
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
		IProjectType[] projTypes = ManagedBuildManager.getDefinedProjectTypes();
		IProjectType projType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.exe");
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

		// Initialize the path entry container
		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(project);
		if (initResult.getCode() != IStatus.OK) {
			fail("Initializing build information failed for: " + project.getName() + " because: " + initResult.getMessage());
		}
		
		// Now test the results out
	//	checkRootManagedProject(newProject, "x");
		
		// Override the "String Option in Category" option value
		configs = newProject.getConfigurations();
		ITool[] tools = configs[0].getTools();
		IOptionCategory topCategory = tools[0].getTopOptionCategory();
		IOptionCategory[] categories = topCategory.getChildCategories();
		Object[][] options = categories[0].getOptions(configs[0]);
		ITool tool = (ITool)options[0][0];
		IOption option = (IOption)options[0][1];
		configs[0].setOption(tool, option, "1");
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
//		checkRootManagedProject(newProject, "z");
		
		// Now test the information the makefile builder needs
	//	checkBuildTestSettings(info);
		ManagedBuildManager.removeBuildInfo(project);
	}
	
	public void testResourceRename() throws Exception {
		// Create a new project
		IProject project = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
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
		IProjectType projType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.exe");
		assertNotNull(projType);
		
		// Create the managed-project (.cdtbuild) for our project that builds an executable.
		IManagedProject newProject = ManagedBuildManager.createManagedProject(project, projType);
		assertEquals(newProject.getName(), projType.getName());
		assertFalse(newProject.equals(projType));
		ManagedBuildManager.setNewProjectVersion(project);
			
		// Create a couple of resources ( 'main.c' & 'bar.c')
		IFile mainFile = project.getProject().getFile( "main.c" );
		if( !mainFile.exists() ){
			mainFile.create( new ByteArrayInputStream( "#include <stdio.h>\n extern void bar(); \n int main() { \nprintf(\"Hello, World!!\"); \n bar();\n return 0; }".getBytes() ), false, null );
		}
		
		IFile aFile = project.getProject().getFile( "a.c" );
		if( !aFile.exists() ){
			aFile.create( new ByteArrayInputStream( "#include <stdio.h>\n void bar() { \nprintf(\"Hello, bar()!!\");\n return; }".getBytes() ), false, null );
		}

		IFolder dirFolder = project.getProject().getFolder( "dir" );
		if(!dirFolder.exists())
			dirFolder.create(true,true,null);
		IFile bFile = dirFolder.getFile( "b.c" );
		if( !bFile.exists() ){
			bFile.create( new ByteArrayInputStream( "#include <stdio.h>\n void bar1() { \nprintf(\"Hello, bar1()!!\");\n return; }".getBytes() ), false, null );
		}

		// Get the configurations and make one of them as default configuration.
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

		// Create Resource Configurations for files main.c and bar.c
		IResourceConfiguration resMainConfig = defaultConfig.createResourceConfiguration(mainFile);
		IResourceConfiguration resAConfig = defaultConfig.createResourceConfiguration(aFile);
		IResourceConfiguration resBConfig = defaultConfig.createResourceConfiguration(bFile);
		
		// Check whether defaultConfig has three resource configurations or not.
		IResourceConfiguration resConfigs[] = defaultConfig.getResourceConfigurations();
		assertEquals(resMainConfig,defaultConfig.getResourceConfiguration(mainFile.getFullPath().toString()));
		assertEquals(resAConfig,defaultConfig.getResourceConfiguration(aFile.getFullPath().toString()));
		assertEquals(resBConfig,defaultConfig.getResourceConfiguration(bFile.getFullPath().toString()));
		assertEquals(3,resConfigs.length);

		
		mainFile.move(mainFile.getFullPath().removeLastSegments(1).append("main1.c"),true,false,null);
		mainFile = (IFile)project.findMember("main1.c");
		assertEquals(resMainConfig,defaultConfig.getResourceConfiguration(mainFile.getFullPath().toString()));
		assertEquals(resAConfig,defaultConfig.getResourceConfiguration(aFile.getFullPath().toString()));
		assertEquals(resBConfig,defaultConfig.getResourceConfiguration(bFile.getFullPath().toString()));
		resConfigs = defaultConfig.getResourceConfigurations();
		assertEquals(3,resConfigs.length);

		dirFolder.move(dirFolder.getFullPath().removeLastSegments(1).append("dir1"),true,false,null);
		dirFolder = (IFolder)project.findMember("dir1");
		bFile = (IFile)dirFolder.findMember("b.c");
		assertEquals(resMainConfig,defaultConfig.getResourceConfiguration(mainFile.getFullPath().toString()));
		assertEquals(resAConfig,defaultConfig.getResourceConfiguration(aFile.getFullPath().toString()));
		assertEquals(resBConfig,defaultConfig.getResourceConfiguration(bFile.getFullPath().toString()));
		resConfigs = defaultConfig.getResourceConfigurations();
		assertEquals(3,resConfigs.length);

		bFile.move(bFile.getFullPath().removeLastSegments(1).append("b1.c"),true,false,null);
		bFile = (IFile)dirFolder.findMember("b1.c");
		assertEquals(resMainConfig,defaultConfig.getResourceConfiguration(mainFile.getFullPath().toString()));
		assertEquals(resAConfig,defaultConfig.getResourceConfiguration(aFile.getFullPath().toString()));
		assertEquals(resBConfig,defaultConfig.getResourceConfiguration(bFile.getFullPath().toString()));
		resConfigs = defaultConfig.getResourceConfigurations();
		assertEquals(3,resConfigs.length);

		IProjectDescription des = project.getDescription();
		des.setName(renamedProjectName1);
		project.move(des,true,null);
		project = (IProject)root.findMember(renamedProjectName1);
		mainFile = (IFile)project.findMember("main1.c");
		aFile = (IFile)project.findMember("a.c");
		dirFolder = (IFolder)project.findMember("dir1");
		bFile = (IFile)dirFolder.findMember("b1.c");
		assertEquals(resMainConfig,defaultConfig.getResourceConfiguration(mainFile.getFullPath().toString()));
		assertEquals(resAConfig,defaultConfig.getResourceConfiguration(aFile.getFullPath().toString()));
		assertEquals(resBConfig,defaultConfig.getResourceConfiguration(bFile.getFullPath().toString()));
		resConfigs = defaultConfig.getResourceConfigurations();
		assertEquals(3,resConfigs.length);
		
		final IResource rcBuf[] = new IResource[5]; 
		rcBuf[0] = project;
		rcBuf[1] = mainFile;
		rcBuf[2] = aFile;
		rcBuf[3] = dirFolder;
		rcBuf[4] = bFile;

		ResourcesPlugin.getWorkspace().run( new IWorkspaceRunnable(){

			public void run(IProgressMonitor monitor) throws CoreException {
				IProject project = (IProject)rcBuf[0];
				IFile mainFile = (IFile)rcBuf[1];
				IFile aFile = (IFile)rcBuf[2];
				IFolder dirFolder = (IFolder)rcBuf[3];
				IFile bFile = (IFile)rcBuf[4];
				// TODO Auto-generated method stub
				mainFile.move(mainFile.getFullPath().removeLastSegments(1).append("main2.c"),true,false,null);
				mainFile = (IFile)project.findMember("main2.c");

				dirFolder.move(dirFolder.getFullPath().removeLastSegments(1).append("dir2"),true,false,null);
				dirFolder = (IFolder)project.findMember("dir2");
				bFile = (IFile)dirFolder.findMember("b1.c");

				bFile.move(bFile.getFullPath().removeLastSegments(1).append("b2.c"),true,false,null);
				bFile = (IFile)dirFolder.findMember("b2.c");

//				project.move(project.getFullPath().removeLastSegments(1).append(renamedProjectName2),true,null);
				IProjectDescription des = project.getDescription();
				des.setName(renamedProjectName2);
				project.move(des,true,null);
				project = (IProject)ResourcesPlugin.getWorkspace().getRoot().findMember(renamedProjectName2);

				mainFile = (IFile)project.findMember("main2.c");
				aFile = (IFile)project.findMember("a.c");
				dirFolder = (IFolder)project.findMember("dir2");
				bFile = (IFile)dirFolder.findMember("b2.c");
				
				rcBuf[0] = project;
				rcBuf[1] = mainFile;
				rcBuf[2] = aFile;
				rcBuf[3] = dirFolder;
				rcBuf[4] = bFile;
			}
			
			}, 
			root,
			IWorkspace.AVOID_UPDATE,
			null);
		
		project = (IProject)rcBuf[0];
		mainFile = (IFile)rcBuf[1];
		aFile = (IFile)rcBuf[2];
		dirFolder = (IFolder)rcBuf[3];
		bFile = (IFile)rcBuf[4];

		assertEquals(resMainConfig,defaultConfig.getResourceConfiguration(mainFile.getFullPath().toString()));
		assertEquals(resAConfig,defaultConfig.getResourceConfiguration(aFile.getFullPath().toString()));
		assertEquals(resBConfig,defaultConfig.getResourceConfiguration(bFile.getFullPath().toString()));
		resConfigs = defaultConfig.getResourceConfigurations();
		assertEquals(3,resConfigs.length);
		
		aFile.delete(true,null);
		// Check whether defaultConfig has two resource configurations or not.
		assertEquals(resMainConfig,defaultConfig.getResourceConfiguration(mainFile.getFullPath().toString()));
		assertEquals(null,defaultConfig.getResourceConfiguration(aFile.getFullPath().toString()));
		assertEquals(resBConfig,defaultConfig.getResourceConfiguration(bFile.getFullPath().toString()));
		resConfigs = defaultConfig.getResourceConfigurations();
		assertEquals(2,resConfigs.length);
		
		dirFolder.delete(true, null);
		// Check whether defaultConfig has one resource configuration or not.
		assertEquals(resMainConfig,defaultConfig.getResourceConfiguration(mainFile.getFullPath().toString()));
		assertEquals(null,defaultConfig.getResourceConfiguration(aFile.getFullPath().toString()));
		assertEquals(null,defaultConfig.getResourceConfiguration(bFile.getFullPath().toString()));
		resConfigs = defaultConfig.getResourceConfigurations();
		assertEquals(1,resConfigs.length);

		// Close and remove project.
		project.close(null);
		removeProject(renamedProjectName2);
	}
	
}	
