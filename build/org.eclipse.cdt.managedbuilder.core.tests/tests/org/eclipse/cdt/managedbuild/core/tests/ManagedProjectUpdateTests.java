/**********************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuild.core.tests;

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

public class ManagedProjectUpdateTests extends TestCase {
	public ManagedProjectUpdateTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedProjectUpdateTests.class.getName());
		
		suite.addTest(new ManagedProjectUpdateTests("testProjectUpdate12_Update"));
		suite.addTest(new ManagedProjectUpdateTests("testProjectUpdate20_Update"));
		suite.addTest(new ManagedProjectUpdateTests("testProjectUpdate21_Update"));
		suite.addTest(new ManagedProjectUpdateTests("testProjectUpdate12_NoUpdate"));
		suite.addTest(new ManagedProjectUpdateTests("testProjectUpdate20_NoUpdate"));
		suite.addTest(new ManagedProjectUpdateTests("testProjectUpdate21_NoUpdate"));
		
		return suite;
	}
	
	private File getVersionProjectsDir(String version){
		return CTestPlugin.getFileInPlugin(new Path("resources/oldTypeProjects/"+version));
	}

	private IProject[] createVersionProjects(String version){
		File file = getVersionProjectsDir(version);
		if(file == null) {
			fail("Test project directory " + file.getName() + " is missing.");
			return null;
		}
		
		File projectZips[] = file.listFiles(new FileFilter(){
			public boolean accept(File pathname){
				if(pathname.isDirectory())
					return false;
				return true;
			}
		});
		
		ArrayList projectList = new ArrayList(projectZips.length);
		for(int i = 0; i < projectZips.length; i++){
			try{
				String projectName = projectZips[i].getName();
				if(!projectName.endsWith(".zip"))
					continue;
				
				projectName = projectName.substring(0,projectName.length()-".zip".length());
				if(projectName.length() == 0)
					continue;
				IProject project = ManagedBuildTestHelper.createProject(projectName, projectZips[i], null, null);
				if(project != null)
					projectList.add(project);
			}
			catch(Exception e){
			}
		}
		if(projectList.size() == 0) {
			fail("No projects found in test project directory " + file.getName() + ".  The .zip file may be missing or corrupt.");
			return null;
		}
		return (IProject[])projectList.toArray(new IProject[projectList.size()]);
	}
	
	private void doTestProjectUpdate(String version, boolean updateProject, boolean overwriteBackupFiles, 
			IPath[] files){
		IOverwriteQuery queryALL = new IOverwriteQuery(){
			public String queryOverwrite(String file) {
				return ALL;
			}};
		IOverwriteQuery queryNOALL = new IOverwriteQuery(){
			public String queryOverwrite(String file) {
				return NO_ALL;
			}};
		
		UpdateManagedProjectManager.setBackupFileOverwriteQuery(overwriteBackupFiles ? queryALL : queryNOALL);
		UpdateManagedProjectManager.setUpdateProjectQuery(updateProject ? queryALL : queryNOALL);
		
		IProject projects[] = createVersionProjects(version);
		if(projects == null || projects.length == 0)
			return;
		for(int i = 0; i < projects.length; i++){
			IProject curProject = projects[i];
			
			//the project conversion occures the first time 
			//ManagedBuildManager.getBuildInfo gets called
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(curProject);
			
			//check whether the managed build info is converted
			boolean isCompatible = UpdateManagedProjectManager.isCompatibleProject(info);
			assertTrue(isCompatible);
			
			if(isCompatible){
				//check for correct update
				if(!updateProject){
					//TODO: if the user has chosen not to update the project the .cdtbuild file should not change
				}

				//check whether the project builds without errors
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
						ManagedBuildTestHelper.compareBenchmarks(curProject, buildDir, files);
					}
				}
			}
		}
		
		for(int i = 0; i < projects.length; i++)
			ManagedBuildTestHelper.removeProject(projects[i].getName());
	}
	
	/* (non-Javadoc)
	 * tests project v1.2 update 
	 * in case when user chooses to update the project 
	 */
	public void testProjectUpdate12_Update(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		doTestProjectUpdate("1.2", true, true, makefiles);
	}

	/* (non-Javadoc)
	 * tests project v2.0 update 
	 * in case when user chooses to update the project 
	 */
	public void testProjectUpdate20_Update(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk")};
		doTestProjectUpdate("2.0", true, true, makefiles);
	}

	/* (non-Javadoc)
	 * tests project v2.1 update 
	 * in case when user chooses to update the project 
	 */
	public void testProjectUpdate21_Update(){
		IPath[] makefiles = {
				 Path.fromOSString("makefile"), 
				 Path.fromOSString("objects.mk"), 
				 Path.fromOSString("sources.mk"), 
				 Path.fromOSString("subdir.mk"), 
				 Path.fromOSString("Functions/subdir.mk")};
		doTestProjectUpdate("2.1", true, true, makefiles);
	}

	/* (non-Javadoc)
	 * tests project v1.2 update 
	 * in case when user chooses not to update the project 
	 */
	public void testProjectUpdate12_NoUpdate(){
		doTestProjectUpdate("1.2", false, true, null);
	}

	/* (non-Javadoc)
	 * tests project v2.0 update 
	 * in case when user chooses not to update the project 
	 */
	public void testProjectUpdate20_NoUpdate(){
		doTestProjectUpdate("2.0", false, true, null);
	}

	/* (non-Javadoc)
	 * tests project v2.1 update 
	 * in case when user chooses not to update the project 
	 */
	public void testProjectUpdate21_NoUpdate(){
		doTestProjectUpdate("2.1", false, true, null);
	}
}
