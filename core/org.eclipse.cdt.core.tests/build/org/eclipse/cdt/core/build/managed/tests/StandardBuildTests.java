package org.eclipse.cdt.core.build.managed.tests;
/**********************************************************************
 * Copyright (c) 2002,2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import java.util.Arrays;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.MakeScannerInfo;
import org.eclipse.cdt.make.core.MakeScannerProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class StandardBuildTests extends TestCase {
	private static final String DEFAULT_BUILD_COMMAND = "make";
	private static final String EMPTY_STRING = "";
	private static final boolean OFF = false;
	private static final boolean ON = true;
	private static final String OVR_BUILD_ARGS = "-f";
	private static final String OVR_BUILD_COMMAND = "/home/tester/bin/nmake";
	private static final String OVR_BUILD_LOCATION = "src";
	private static final String[] OVR_INC_PATHS = {"/test", "C:\\windows", "//dev/home/include"};
	private static final String[] OVR_PREPROC_SYMS = {"_RELEASE", "NO ", " YES=1"};
	private static final String PROJECT_NAME = "StandardBuildTest";

	private class ScannerListener implements IScannerInfoChangeListener {
		private final String[] expectedPaths = {"/usr/include", "/home/tester/include", "/opt/gnome/include"};
		private final String[] expectedSymbols = {"_DEBUG", "TRUE=1", "FALSE ", ""};
		private boolean bNotified = false;
		
		public void changeNotification(IResource project, IScannerInfo info) {
			// Are there any symbols
			Map definedSymbols = info.getDefinedSymbols();
			if (!definedSymbols.isEmpty()) {
				assertTrue(definedSymbols.containsKey(expectedSymbols[0]));
				assertEquals(EMPTY_STRING, (String)definedSymbols.get(expectedSymbols[0]));
				assertTrue(definedSymbols.containsKey("TRUE"));
				assertEquals("1", (String)definedSymbols.get("TRUE"));
				assertFalse(definedSymbols.containsKey(expectedSymbols[2]));
				assertTrue(definedSymbols.containsKey(expectedSymbols[2].trim()));
				assertEquals(EMPTY_STRING, (String)definedSymbols.get(expectedSymbols[2].trim()));
				// We should have discarded the empty string
				assertFalse(definedSymbols.containsKey(""));
			}
			
			// What paths have been set
			String[] paths = info.getIncludePaths();
			if (paths.length > 0) {
				assertTrue(Arrays.equals(expectedPaths, paths));
			}
			bNotified = true;
		}
		/**
		 * @return
		 */
		public String[] getExpectedPaths() {
			return expectedPaths;
		}

		public boolean triggedNotification() {
			return bNotified;
		}
		/**
		 * @return
		 */
		public String[] getExpectedSymbols() {
			return expectedSymbols;
		}

	}
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
		suite.addTest(new StandardBuildTests("testScannerListenerInterface"));
		suite.addTest(new StandardBuildTests("testProjectCleanup"));
		
		return suite;
	}
	
	private void checkDefaultProjectSettings(IProject project) throws Exception {
		assertNotNull(project);

		// There should not be any include path or defined symbols for the project
		MakeScannerInfo scannerInfo = MakeScannerProvider.getDefault().getMakeScannerInfo(project, true);
		assertNotNull(scannerInfo);
		String[] includePaths = scannerInfo.getIncludePaths();
		assertNotNull(includePaths);
		assertEquals(0, includePaths.length);
		String[] definedSymbols = scannerInfo.getPreprocessorSymbols();
		assertNotNull(definedSymbols);
		assertEquals(0, definedSymbols.length);
		
		IMakeBuilderInfo builderInfo = MakeCorePlugin.createBuildInfo(project, MakeBuilder.BUILDER_ID);
		// Check the rest of the project information
		assertEquals(ON, builderInfo.isDefaultBuildCmd());
		assertEquals(OFF,builderInfo.isStopOnError());
		assertEquals(new Path(DEFAULT_BUILD_COMMAND), builderInfo.getBuildCommand());
		assertEquals(EMPTY_STRING, builderInfo.getBuildArguments());
		assertEquals(false, builderInfo.isAutoBuildEnable());	
		assertEquals("all", builderInfo.getAutoBuildTarget()); 
		assertEquals(true, builderInfo.isIncrementalBuildEnabled());	
		assertEquals("all", builderInfo.getIncrementalBuildTarget()); 
		assertEquals(true, builderInfo.isFullBuildEnabled());	
		assertEquals("clean all", builderInfo.getFullBuildTarget()); 
	}
	
	private void checkOverriddenProjectSettings(IProject project) throws Exception {
		assertNotNull(project);

		MakeScannerInfo scannerInfo = MakeScannerProvider.getDefault().getMakeScannerInfo(project, true);
		assertNotNull(scannerInfo);
		String[] includePaths = scannerInfo.getIncludePaths();
		assertNotNull(includePaths);
		assertEquals(3, includePaths.length);
		assertTrue(Arrays.equals(includePaths, OVR_INC_PATHS));
		String[] definedSymbols = scannerInfo.getPreprocessorSymbols();
		assertNotNull(definedSymbols);
		assertEquals(3, definedSymbols.length);
		assertTrue(Arrays.equals(definedSymbols, OVR_PREPROC_SYMS));
		
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
	private IProject createProject(String name) throws CoreException {
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
		
		// Change the settings
		MakeScannerInfo scannerInfo = MakeScannerProvider.getDefault().getMakeScannerInfo(project, false);
		scannerInfo.setIncludePaths(OVR_INC_PATHS);
		scannerInfo.setPreprocessorSymbols(OVR_PREPROC_SYMS);
		scannerInfo.update();
		
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

	public void testScannerListenerInterface() throws Exception  {
		// Get the project
		IProject project = null;
		try {
			project = createProject(PROJECT_NAME);
		} catch (CoreException e) {
			fail("StandardBuildTest testScannerListernerInterface failed opening project: " + e.getLocalizedMessage());
		}
		assertNotNull(project);

		// Find the scanner info provider for this project
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		assertNotNull(provider);
		
		// Check out the information we can get through the interface
		IScannerInfo currentSettings = provider.getScannerInformation(project);
		Map currentSymbols = currentSettings.getDefinedSymbols();
		assertTrue(currentSymbols.containsKey("_RELEASE"));
		assertEquals("", currentSymbols.get("_RELEASE"));
		assertTrue(currentSymbols.containsKey("YES"));
		assertEquals("1", currentSymbols.get("YES"));
		assertTrue(currentSymbols.containsKey("NO"));
		assertEquals("", currentSymbols.get("NO"));
		String[] currentPaths = currentSettings.getIncludePaths();
		assertTrue(Arrays.equals(OVR_INC_PATHS, currentPaths));
		
		// Remove what's there
		MakeScannerInfo info = MakeScannerProvider.getDefault().getMakeScannerInfo(project, false);
		info.setIncludePaths(new String[0]);
		info.setPreprocessorSymbols(new String[0]);
		info.update();
		// Subscribe
		ScannerListener listener = new ScannerListener();
		provider.subscribe(project, listener);
		
		// Change the settings
		info.setIncludePaths(listener.getExpectedPaths());
		info.setPreprocessorSymbols(listener.getExpectedSymbols());
		info.update();
		assertEquals(true, listener.triggedNotification());
		// Unsubscribe
		provider.unsubscribe(project, listener);
	}
}
