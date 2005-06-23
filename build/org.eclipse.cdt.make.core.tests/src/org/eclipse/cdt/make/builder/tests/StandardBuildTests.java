package org.eclipse.cdt.make.builder.tests;
/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class StandardBuildTests extends TestCase {
	private static final boolean OFF = false;
	private static final boolean ON = true;
	private static final String OVR_BUILD_ARGS = "-f";
	private static final String OVR_BUILD_COMMAND = "/home/tester/bin/nmake";
	private static final String OVR_BUILD_LOCATION = "src";
	private static final String PROJECT_NAME = "StandardBuildTest";

	/**
	 * @param name
	 */
	public StandardBuildTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(StandardBuildTests.class.getName());
		
		// Add the relevant tests to the suite
		suite.addTest(new StandardBuildTests("testProjectCreation"));
		suite.addTest(new StandardBuildTests("testProjectSettings"));
		suite.addTest(new StandardBuildTests("testProjectConversion"));
		suite.addTest(new StandardBuildTests("testProjectCleanup"));
		return suite;
	}
	
	private void checkDefaultProjectSettings(IProject project) throws Exception {
		assertNotNull(project);

		IMakeBuilderInfo defaultInfo = MakeCorePlugin.createBuildInfo(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID, true);
		
		IMakeBuilderInfo builderInfo = MakeCorePlugin.createBuildInfo(project, MakeBuilder.BUILDER_ID);
		// Check the rest of the project information
		assertEquals(defaultInfo.isDefaultBuildCmd(), builderInfo.isDefaultBuildCmd());
		assertEquals(defaultInfo.isStopOnError(), builderInfo.isStopOnError());
		assertEquals(defaultInfo.getBuildCommand(), builderInfo.getBuildCommand());
		assertEquals(defaultInfo.getBuildArguments(), builderInfo.getBuildArguments());
		assertEquals(defaultInfo.getBuildLocation(), builderInfo.getBuildLocation());	

		assertEquals(defaultInfo.isAutoBuildEnable(), builderInfo.isAutoBuildEnable());	
		assertEquals(defaultInfo.getAutoBuildTarget(), builderInfo.getAutoBuildTarget()); 
		assertEquals(defaultInfo.isIncrementalBuildEnabled(), builderInfo.isIncrementalBuildEnabled());	
		assertEquals(defaultInfo.getIncrementalBuildTarget(), builderInfo.getIncrementalBuildTarget()); 
		assertEquals(defaultInfo.isFullBuildEnabled(), builderInfo.isFullBuildEnabled());	
		assertEquals(defaultInfo.getFullBuildTarget(), builderInfo.getFullBuildTarget());
		assertEquals(defaultInfo.isCleanBuildEnabled(), builderInfo.isCleanBuildEnabled());	
		assertEquals(defaultInfo.getCleanBuildTarget(), builderInfo.getCleanBuildTarget());
		
	}
	
	private void checkOverriddenProjectSettings(IProject project) throws Exception {
		assertNotNull(project);
		
		// Check the rest of the project information
		IMakeBuilderInfo builderInfo = MakeCorePlugin.createBuildInfo(project, MakeBuilder.BUILDER_ID);
		assertEquals(OFF, builderInfo.isDefaultBuildCmd());
		assertEquals(ON, builderInfo.isStopOnError());
		assertEquals(new Path(OVR_BUILD_COMMAND), builderInfo.getBuildCommand());
		assertEquals(OVR_BUILD_ARGS, builderInfo.getBuildArguments());
		assertEquals(new Path(OVR_BUILD_LOCATION), builderInfo.getBuildLocation());
	}

	/**
	 * Create a new project named <code>name</code> or return the project in 
	 * the workspace of the same name if it exists.
	 * 
	 * @param name The name of the project to create or retrieve.
	 * @return 
	 * @throws CoreException
	 */
	private IProject createProject(final String name) throws CoreException {
		final Object[] result = new Object[1];
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IProject project = root.getProject(name);
				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
		        
				if (!project.isOpen()) {
					project.open(null);
				}
				CCorePlugin.getDefault().convertProjectToC(project, new NullProgressMonitor(), MakeCorePlugin.MAKE_PROJECT_ID);
				result[0] = project;
			}
		}, null);
		return (IProject)result[0];	
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
	 * Remove all the project information associated with the project used during test.
	 */
	public void testProjectCleanup() {
		removeProject(PROJECT_NAME);
	}

	public void testProjectConversion() throws Exception {
		// Open the project
		IProject project = null;
		try {
			project = createProject(PROJECT_NAME);
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectConversion failed opening project: " + e.getLocalizedMessage());
		}
		assertNotNull(project);
		
		// Check the settings (they should be the override values)
		checkOverriddenProjectSettings(project);
		
		// Now convert the project
		try {
			CCorePlugin.getDefault().convertProjectFromCtoCC(project, new NullProgressMonitor());
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectConversion failed to convert project: " + e.getLocalizedMessage());
		}

		// Close, and Reopen the project
		try {
			project.close(new NullProgressMonitor());
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectConversion failed to close project " + e.getLocalizedMessage());
		}
		try {
			project.open(new NullProgressMonitor());
		} catch (CoreException e) {
			fail ("StandardBuildTest testProjectConversion failed to open project " + e.getLocalizedMessage());
		}

		// Make sure it has a CCNature
		try {
			project.hasNature(CCProjectNature.CC_NATURE_ID);
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectConversion failed getting nature: " + e.getLocalizedMessage());
		}
		
		// Nothing should have changed in the settings
		checkOverriddenProjectSettings(project);
	}

	/**
	 * 
	 */
	public void testProjectCreation() throws Exception  {
		// Create a new project
		IProject project = null;
		try {
			project = createProject(PROJECT_NAME); 
			// Convert the new project to a standard make project
			MakeProjectNature.addNature(project, null);
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectCreation failed creating project: " + e.getLocalizedMessage());
		}
		assertNotNull(project);

		// Make sure it has a CNature
		try {
			project.hasNature(CProjectNature.C_NATURE_ID);
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectCreation failed getting nature: " + e.getLocalizedMessage());
		}
		// Make sure it has a MakeNature
		try {
			project.hasNature(MakeProjectNature.NATURE_ID);
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectCreation failed getting nature: " + e.getLocalizedMessage());
		}
		// Check the default settings
		checkDefaultProjectSettings(project);
	}
	
	public void testProjectSettings() throws Exception {
		// Get the project
		IProject project = null;
		try {
			project = createProject(PROJECT_NAME);
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectSettings failed opening project: " + e.getLocalizedMessage());
		}
		assertNotNull(project);
				
		// Use the build info for the rest of the settings
		IMakeBuilderInfo builderInfo = MakeCorePlugin.createBuildInfo(project, MakeBuilder.BUILDER_ID);
		builderInfo.setStopOnError(ON);
		builderInfo.setUseDefaultBuildCmd(OFF);
		builderInfo.setBuildCommand(new Path(OVR_BUILD_COMMAND));
		builderInfo.setBuildArguments(OVR_BUILD_ARGS);
		builderInfo.setBuildLocation(new Path(OVR_BUILD_LOCATION));
		try {
			project.close(new NullProgressMonitor());
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectSettings failed to close project " + e.getLocalizedMessage());
		}
		try {
			project.open(new NullProgressMonitor());
		} catch (CoreException e) {
			fail ("StandardBuildTest testProjectSettings failed to open project " + e.getLocalizedMessage());
		}
		
		// Retest
		checkOverriddenProjectSettings(project);
	}
}
