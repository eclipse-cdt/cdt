/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

/**********************************************************************
 * These tests are for the default dependency calculators
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.projectconverter.UpdateManagedProjectManager;
import org.eclipse.cdt.managedbuilder.testplugin.CTestPlugin;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class ManagedBuildDependencyCalculatorTests extends TestCase {
	
	public ManagedBuildDependencyCalculatorTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildDependencyCalculatorTests.class.getName());
		
		suite.addTest(new ManagedBuildDependencyCalculatorTests("test1DepCalc2"));
		suite.addTest(new ManagedBuildDependencyCalculatorTests("test1DepCalc3"));
		suite.addTest(new ManagedBuildDependencyCalculatorTests("test1DepCalcPreBuild"));
		return suite;
	}

	private IProject[] createProject(String projName, IPath location, String projectTypeId, boolean containsZip){
		ArrayList projectList = null;
		if (containsZip) {
			File testDir = CTestPlugin.getFileInPlugin(new Path("resources/depCalcProjects/" + projName));
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
	

	/* (non-Javadoc)
	 * test for dependency calculation as a side-effect of compilation
	 */
	public void test1DepCalc2(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk"),
				 //  This file is different using Cygwin vs GCC
				 //Path.fromOSString("main.d"),
				 Path.fromOSString("Sources/subdir.mk"),
				 Path.fromOSString("Sources/func1.d"),
				 Path.fromOSString("Sources/func2.d"),
				 Path.fromOSString("Sources/func4.d"),
				 Path.fromOSString("Sources/sub sources/func 3.d"),
				 Path.fromOSString("Sources/sub sources/subdir.mk")};
		IProject[] projects = createProjects("test1DepCalc2", null, null, true);
		buildProjects(projects, makefiles);
	}
	

	/* (non-Javadoc)
	 * test for dependency calculation using Echo, a 2nd conmpilation step, and post-processing
	 */
	public void test1DepCalc3(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk"),
				 //  This file is different using Cygwin vs GCC
				 //Path.fromOSString("main.d"),
				 Path.fromOSString("Sources/subdir.mk"),
				 Path.fromOSString("Sources/func1.d"),
				 Path.fromOSString("Sources/func2.d"),
				 Path.fromOSString("Sources/func4.d"),
				 Path.fromOSString("Sources/sub sources/func 3.d"),
				 Path.fromOSString("Sources/sub sources/subdir.mk")};
		IProject[] projects = createProjects("test1DepCalc3", null, null, true);
		buildProjects(projects, makefiles);
	}
	

	/* (non-Javadoc)
	 * test for dependency calculation that uses a separate, pre-build, step to generate dependency files
	 */
	public void test1DepCalcPreBuild(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk"),
				 //  This file is different using Cygwin vs GCC
				 //Path.fromOSString("main.d"),
				 Path.fromOSString("Sources/subdir.mk"),
				 Path.fromOSString("Sources/func1.d"),
				 Path.fromOSString("Sources/func2.d"),
				 Path.fromOSString("Sources/func4.d"),
				 Path.fromOSString("Sources/sub sources/func 3.d"),
				 Path.fromOSString("Sources/sub sources/subdir.mk")};
		IProject[] projects = createProjects("test1DepCalcPreBuild", null, null, true);
		buildProjects(projects, makefiles);
	}
	
}