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

/**********************************************************************
 * These tests are for a 3.0 style tool integration.
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.projectconverter.UpdateManagedProjectManager;
import org.eclipse.cdt.managedbuilder.testplugin.CTestPlugin;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class ManagedProject30MakefileTests extends TestCase {
	public static final String MBS_TEMP_DIR = "MBSTemp";

	static boolean pathVariableCreated = false;
	
	public ManagedProject30MakefileTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedProject30MakefileTests.class.getName());
		
		suite.addTest(new ManagedProject30MakefileTests("test30SingleFileExe"));
		suite.addTest(new ManagedProject30MakefileTests("test30TwoFileSO"));
		suite.addTest(new ManagedProject30MakefileTests("test30MultiResConfig"));
		suite.addTest(new ManagedProject30MakefileTests("test30LinkedLib"));
		//  TODO: testLinkedFolder fails intermittently saying that it cannot find
		//        the makefiles to compare.  This appears to be a test set issue,
		//        rather than an MBS functionality issue
		//suite.addTest(new ManagedProject30MakefileTests("test30LinkedFolder"));
		suite.addTest(new ManagedProject30MakefileTests("test30CopyandDeploy"));
		suite.addTest(new ManagedProject30MakefileTests("test30DeleteFile"));
		suite.addTest(new ManagedProject30MakefileTests("test30NoFilesToBuild"));
		suite.addTest(new ManagedProject30MakefileTests("testFileWithNoExtension"));
		suite.addTest(new ManagedProject30MakefileTests("testPreAndPostProcessBuildSteps"));
		suite.addTest(new ManagedProject30MakefileTests("testResourceCustomBuildStep"));
		suite.addTest(new ManagedProject30MakefileTests("test30_1"));
		suite.addTest(new ManagedProject30MakefileTests("test30_2"));
		suite.addTest(new ManagedProject30MakefileTests("testTopTC"));
		return suite;
	}

	private IProject[] createProject(String projName, IPath location, String projectTypeId, boolean containsZip){
		ArrayList projectList = null;
		if (containsZip) {
			File testDir = CTestPlugin.getFileInPlugin(new Path("resources/test30Projects/" + projName));
			if(testDir == null) {
				fail("Test project directory " + projName + " is missing.");
				return null;
			}

			File projectZips[] = testDir.listFiles(new FileFilter(){
				public boolean accept(File pathname){
					if(pathname.isDirectory())
						return false;
					return true;
				}
			});
			
			projectList = new ArrayList(projectZips.length);
			for(int i = 0; i < projectZips.length; i++){
				try{
					String projectName = projectZips[i].getName();
					if(!projectName.endsWith(".zip"))
						continue;
					
					projectName = projectName.substring(0,projectName.length()-".zip".length());
					if(projectName.length() == 0)
						continue;
					IProject project = ManagedBuildTestHelper.createProject(projectName, projectZips[i], location, projectTypeId);
					if(project != null)
						projectList.add(project);
				}
				catch(Exception e){
				}
			}
			if(projectList.size() == 0) {
				fail("No projects found in test project directory " + testDir.getName() + ".  The .zip file may be missing or corrupt.");
				return null;
			}
		} else {
			try{
				IProject project = ManagedBuildTestHelper.createProject(projName, null, location, projectTypeId);
				if(project != null)
					projectList = new ArrayList(1);
					projectList.add(project);
			} catch(Exception e){}
		}
		
		return (IProject[])projectList.toArray(new IProject[projectList.size()]);
	}
	
	private IProject[] createProjects(String projName, IPath location, String projectTypeId, boolean containsZip) {
		
		//  In case the projects need to be updated...
		IOverwriteQuery queryALL = new IOverwriteQuery(){
			public String queryOverwrite(String file) {
				return ALL;
			}};
		IOverwriteQuery queryNOALL = new IOverwriteQuery(){
			public String queryOverwrite(String file) {
				return NO_ALL;
			}};
		
		UpdateManagedProjectManager.setBackupFileOverwriteQuery(queryALL);
		UpdateManagedProjectManager.setUpdateProjectQuery(queryALL);
		
		IProject projects[] = createProject(projName, location, projectTypeId, containsZip);
		return projects;
	}
		
	private void buildProjectsWorker(IProject projects[], IPath[] files, boolean compareBenchmark) {	
		if(projects == null || projects.length == 0)
			return;
				
		boolean succeeded = true;
		for (int i = 0; i < projects.length; i++){
			IProject curProject = projects[i];
			
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(curProject);
			
			//check whether the managed build info is converted
			boolean isCompatible = UpdateManagedProjectManager.isCompatibleProject(info);
			assertTrue(isCompatible);
			
			if (isCompatible){
				// Build the project in order to generate the makefiles 
				try{
					curProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,null);
				}
				catch(CoreException e){
					fail(e.getStatus().getMessage());
				}
				catch(OperationCanceledException e){
					fail("the project \"" + curProject.getName() + "\" build was cancelled, exception message: " + e.getMessage());
				}
				
				//compare the generated makefiles to their benchmarks
				if (files != null && files.length > 0) {
					if (i == 0) {
						String configName = info.getDefaultConfiguration().getName();
						IPath buildDir = Path.fromOSString(configName);
						if (compareBenchmark)
						    succeeded = ManagedBuildTestHelper.compareBenchmarks(curProject, buildDir, files);
						else
							succeeded = ManagedBuildTestHelper.verifyFilesDoNotExist(curProject, buildDir, files);
					}
				}
			}
		}
		
		if (succeeded) {	//  Otherwise leave the projects around for comparison
			for (int i = 0; i < projects.length; i++)
				ManagedBuildTestHelper.removeProject(projects[i].getName());
		}
	}

	// Build projects and compare benchmarks
	private void buildProjects(IProject projects[], IPath[] files) {
		buildProjectsWorker(projects, files, true);
	}
	
	// Build projects but don't compare benchmarks because there should be not build files generated
	private void buildDegenerativeProjects(IProject projects[], IPath[] files) {	
		buildProjectsWorker(projects, files, false);
	}
		
	private void createPathVariable(IPath tmpDir) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace = ResourcesPlugin.getWorkspace();
		IPathVariableManager pathMan = workspace.getPathVariableManager();
		String name = MBS_TEMP_DIR;
		try {
			if (pathMan.validateName(name).isOK() && pathMan.validateValue(tmpDir).isOK()) {
			    pathMan.setValue(name, tmpDir);
				assertTrue(pathMan.isDefined(name));
			} else {
				fail("could not create the path variable " + name);
			}
		} catch (Exception e) {fail("could not create the path variable " + name);}
	}
	
	private void createFileLink(IProject project, IPath tmpDir, String linkName, String fileName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String name = MBS_TEMP_DIR;
		if (!pathVariableCreated) {
			createPathVariable(tmpDir);
			pathVariableCreated = true;
		}
			
		try {	
			// Now we can create a linked resource relative to the defined path variable: 
			IFile linkF1 = project.getFile(linkName);
			IPath location = new Path("MBSTemp/" + fileName);
			if (workspace.validateLinkLocation(linkF1, location).isOK()) {
			    linkF1.createLink(location, IResource.NONE, null);
			} else {
				fail("could not create the link to " + name);
			}
		} catch (Exception e) {fail("could not create the link to " + name);}		
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration for a single file executable
	 */
	public void test30SingleFileExe(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("singleFileExe", null, null, true);
		buildProjects(projects, makefiles);
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration for a two file SO
	 */
	public void test30TwoFileSO(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("twoFileSO", null, null, true);
		buildProjects(projects, makefiles);
	}

	/* (non-Javadoc)
	 * tests 3.0 style tool integration for multiple source files & a resource configuration
	 */
	public void test30MultiResConfig(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("source1/subdir.mk"),
				 Path.fromOSString("source2/subdir.mk"),
				 Path.fromOSString("source2/source21/subdir.mk")};
		IProject[] projects = createProjects("multiResConfig", null, null, true);
		buildProjects(projects, makefiles);
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration for linked files
	 */
	public void test30LinkedLib(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 //Path.fromOSString("subdir.mk")   // Can't compare this yet since it contains absolute paths!
				 Path.fromOSString("sources.mk")}; 
		IPath[] linkedFiles = {
				 Path.fromOSString("f1.c"), 
				 Path.fromOSString("f2.c"), 
				 Path.fromOSString("test_ar.h")};
		File srcDirFile = CTestPlugin.getFileInPlugin(new Path("resources/test30Projects/linkedLib/"));
		IPath srcDir = Path.fromOSString(srcDirFile.toString());
		IPath tmpSubDir = Path.fromOSString("CDTMBSTest");
		IPath tmpDir = ManagedBuildTestHelper.copyFilesToTempDir(srcDir, tmpSubDir, linkedFiles);
		try {
			IProject[] projects = createProjects("linkedLib", null, "cdt.managedbuild.target.gnu30.lib", true);
			//  There should be only one project.  Add our linked files to it.
			IProject project = projects[0];
			createFileLink(project, tmpDir, "f1.c", "f1.c");
			createFileLink(project, tmpDir, "f2link.c", "f2.c");
			createFileLink(project, tmpDir, "test_ar.h", "test_ar.h");
			//  Build the project
			buildProjects(projects, makefiles);
		} finally {ManagedBuildTestHelper.deleteTempDir(tmpSubDir, linkedFiles);}
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration for a linked folder
	 */
	public void test30LinkedFolder(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("sources.mk")}; 
		IPath[] linkedFiles = {
				 Path.fromOSString("f1.c"), 
				 Path.fromOSString("f2.c"), 
				 Path.fromOSString("test_ar.h"),
				 Path.fromOSString("Benchmarks/makefile"), 
				 Path.fromOSString("Benchmarks/objects.mk"), 
				 Path.fromOSString("Benchmarks/subdir.mk"),
				 Path.fromOSString("Benchmarks/sources.mk")}; 
		File srcDirFile = CTestPlugin.getFileInPlugin(new Path("resources/test30Projects/linkedFolder/"));
		IPath srcDir = Path.fromOSString(srcDirFile.toString());
		IPath tmpSubDir = Path.fromOSString("CDTMBSTest");
		IPath tmpDir = ManagedBuildTestHelper.copyFilesToTempDir(srcDir, tmpSubDir, linkedFiles);
		if (!pathVariableCreated) {
			createPathVariable(tmpDir);
			pathVariableCreated = true;
		}
		try {
			IPath location = Path.fromOSString(MBS_TEMP_DIR);
			IProject[] projects = createProjects("linkedFolder", location, "cdt.managedbuild.target.gnu30.lib", false);
			//  Build the project
			buildProjects(projects, makefiles);
		} finally {ManagedBuildTestHelper.deleteTempDir(tmpSubDir, linkedFiles);}
	}

	/* (non-Javadoc)
	 * tests 3.0 style tool integration with pre and post process steps added to typical compile & link
	 */
	public void test30CopyandDeploy(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk"), 
				 Path.fromOSString("Functions/subdir.mk")};
		IProject[] projects = createProjects("copyandDeploy", null, null, true);
		buildProjects(projects, makefiles);
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration in the context of deleting a file, to see if the proper behavior
	 * occurs in the managedbuild system
	 */
	public void test30DeleteFile(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("sources.mk")}; 		

		IProject[] projects = createProjects("deleteFile", null, null, true);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		ArrayList resourceList = new ArrayList(1);
		IProject project = projects[0];
		IFile projfile = project.getFile("filetobedeleted.cxx");
		resourceList.add(projfile);
		final IResource[] fileResource = (IResource[])resourceList.toArray(new IResource[resourceList.size()]);
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
			    workspace.delete(fileResource, false, null);
			}
		};
		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
		} catch (Exception e) { 
		    fail("could not delete file in project " + project.getName());
		}
		buildProjects(projects, makefiles);
	}

	/* (non-Javadoc)
	 * tests 3.0 managed build system with a project which has only a single source file that is marked as 
	 * "excluded from build" to see that this degenerative case is handled gracefully
	 */
	public void test30NoFilesToBuild(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("sources.mk")}; 		

		IProject[] projects = createProjects("noFilesToBuild", null, null, true);
		IProject project = projects[0];
		IFile projfile = project.getFile("filetobeexcluded.cxx");
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration config = info.getDefaultConfiguration();
		IResourceConfiguration rconfig = config.createResourceConfiguration(projfile);
		rconfig.setExclude(true);
		buildDegenerativeProjects(projects, makefiles);			
	}

	/**
	 * (non-Javadoc)
	 * tests 3.0 managed build system with a project which has a file with no file extesnion
	 */
	public void testFileWithNoExtension()
	{
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("testFileWithNoExtension", null, null, true);
		buildProjects(projects, makefiles);
	}
	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration: create pre-build and post-build steps and verify that 
	 * the proper commands are generated in the makefile which is created by the managedbuild system
	 */
	public void testPreAndPostProcessBuildSteps(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("sources.mk")}; 		

		IProject[] projects = createProjects("preAndPostBuildSteps", null, null, true);				
		IProject project = projects[0];
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration config = info.getDefaultConfiguration();
		IFile projfile = project.getFile("main.cxx");
		config.setPreannouncebuildStep("Pre-announce Build Step");
		config.setPrebuildStep("echo 'executing Pre-Build Step' ");
		config.setPostannouncebuildStep("Post-announce Build Step");
		config.setPostbuildStep("echo 'executing Post-Build Step' ");
		buildProjects(projects, makefiles);
	}


	/* (non-Javadoc)
	 * tests 3.0 style tool integration: create resource custom build step and verify that 
	 * the proper commands are generated in the makefile which is created by the managedbuild system
	 */
	public void testResourceCustomBuildStep(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("subdir.mk"),
				 Path.fromOSString("sources.mk")}; 		
		ITool rcbsTool;
		IInputType rcbsToolInputType;
		IAdditionalInput rcbsToolInputTypeAdditionalInput;
		IOutputType rcbsToolOutputType;

		IProject[] projects = createProjects("rcbsBasicTest", null, null, true);				
		IProject project = projects[0];
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration config = info.getDefaultConfiguration();
		IFile projfile = project.getFile("rcbsBasicTest.c");
		IResourceConfiguration rconfig = config.createResourceConfiguration(projfile);
		rcbsTool = rconfig.createTool(null,"rcbsBasicTestTool","rcbs Basic Test Tool",false);
		rcbsToolInputType = rcbsTool.createInputType(null,"rcbsToolInputTypeId","rcbsToolInputTypeName",false);
		rcbsToolInputTypeAdditionalInput = rcbsToolInputType.createAdditionalInput("");
		rcbsToolInputTypeAdditionalInput.setKind(IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY);
		rcbsToolOutputType = rcbsTool.createOutputType(null,"rcbsToolOutputTypeId","rcbsToolOutputTypeName",false);
		rcbsToolOutputType.setOutputNames("rcbsBasicTest.o");
		rcbsTool.setCustomBuildStep(true);
		rcbsTool.setToolCommand("gcc -g -c ../rcbsBasicTest.c -o ./rcbsBasicTest.o");
		rcbsTool.setAnnouncement("Now executing custom build step for rcbsBasicTest debug config");
		rconfig.setRcbsApplicability(IResourceConfiguration.KIND_APPLY_RCBS_TOOL_AS_OVERRIDE);
		buildProjects(projects, makefiles);
	}

	
	/* (non-Javadoc)
	 * tests 3.0 style tool integration with pre and post process steps added to typical compile & link
	 */
	public void test30_1(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("test30_1", null, null, true);
		buildProjects(projects, makefiles);
	}

	/* (non-Javadoc)
	 * tests 3.0 style tool integration with multiple input types use Eclipse content types
	 */
	public void test30_2(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		IProject[] projects = createProjects("test30_2", null, null, true);
		buildProjects(projects, makefiles);
	}
	
	/* (non-Javadoc)
	 * tests 3.0 top-level tool-chain definition
	 */
	public void testTopTC(){
		IProject[] projects = createProjects("TopTC", null, "TopTC.target.exe", false);
		//  There should be only one project.
		assertNotNull(projects);
		assertEquals(1, projects.length);
		IProject project = projects[0];
		//  Verify a number of other attributes
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		assertNotNull(info);
		IManagedProject managedProj = info.getManagedProject();
		assertNotNull(managedProj);
		IConfiguration[] configs = managedProj.getConfigurations();
		assertNotNull(configs);
		assertEquals(2, configs.length);
		//  Make sure that each configuration has a tool-chain with all 5 tools
		for (int i=0; i<configs.length; i++) {
			IConfiguration config = configs[i];
			ToolChain tc = (ToolChain)config.getToolChain();
			Iterator iter = tc.getToolList().listIterator();
			int j = 0;
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				j++;
			}
			assertEquals(5, j);
		}
		buildDegenerativeProjects(projects, null);
	}
}
