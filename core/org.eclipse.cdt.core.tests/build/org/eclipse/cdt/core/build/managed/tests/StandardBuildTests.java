package org.eclipse.cdt.core.build.managed.tests;

import java.util.Arrays;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.standard.StandardBuildManager;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.resources.IStandardBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.NullProgressMonitor;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

public class StandardBuildTests extends TestCase {
	private static final String DEFAULT_MAKE_CMD = "make";
	private static final String EMPTY_STRING = "";
	private static final boolean OFF = false;
	private static final boolean ON = true;
	private static final String OVR_BUILD_ARGS = "all";
	private static final String OVR_BUILD_LOCATION = "/home/tester/bin/nmake";
	private static final String[] OVR_INC_PATHS = {"/test", "C:\\windows", "//dev/home/include"};
	private static final String[] OVR_PREPROC_SYMS = {"_RELEASE", "NO ", " YES=1"};
	private static final String PROJECT_NAME = "StandardBuildTest";

	private class ScannerListener implements IScannerInfoChangeListener {
		private final String[] expectedPaths = {"/usr/include", "/home/tester/include", "/opt/gnome/include"};
		private final String[] expectedSymbols = {"_DEBUG", "TRUE=1", "FALSE ", ""};

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
		}
		/**
		 * @return
		 */
		public String[] getExpectedPaths() {
			return expectedPaths;
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
	
	private void checkDefaultProjectSettings(IProject project) {
		assertNotNull(project);

		// There should not be any include path or defined symbols for the project
		IStandardBuildInfo info = StandardBuildManager.getBuildInfo(project);
		assertNotNull(info);
		String[] includePaths = info.getIncludePaths();
		assertNotNull(includePaths);
		assertEquals(0, includePaths.length);
		String[] definedSymbols = info.getPreprocessorSymbols();
		assertNotNull(definedSymbols);
		assertEquals(0, definedSymbols.length);
		
		// Check the rest of the project information
		assertEquals(ON, info.isDefaultBuildCmd());
		assertEquals(OFF,info.isStopOnError());
		assertEquals(DEFAULT_MAKE_CMD, info.getBuildLocation());
		assertEquals(EMPTY_STRING, info.getFullBuildArguments());
		assertEquals(EMPTY_STRING, info.getIncrementalBuildArguments()); 
	}
	
	private void checkOverriddenProjectSettings(IProject project) {
		assertNotNull(project);

		// Check that the new stuff is there
		IStandardBuildInfo info = StandardBuildManager.getBuildInfo(project);
		assertNotNull(info);
		String[] includePaths = info.getIncludePaths();
		assertNotNull(includePaths);
		assertEquals(3, includePaths.length);
		assertTrue(Arrays.equals(includePaths, OVR_INC_PATHS));
		String[] definedSymbols = info.getPreprocessorSymbols();
		assertNotNull(definedSymbols);
		assertEquals(3, definedSymbols.length);
		assertTrue(Arrays.equals(definedSymbols, OVR_PREPROC_SYMS));
		
		// Check the rest of the project information
		assertEquals(OFF, info.isDefaultBuildCmd());
		assertEquals(ON, info.isStopOnError());
		assertEquals(OVR_BUILD_LOCATION, info.getBuildLocation());
		assertEquals(OVR_BUILD_ARGS, info.getFullBuildArguments());
		assertEquals(EMPTY_STRING, info.getIncrementalBuildArguments()); 
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

		return project;	
	}

	private IScannerInfoProvider findInfoProvider(IProject project) { 
		// Use the plugin mechanism to discover the supplier of the path information
		IExtensionPoint extensionPoint = CCorePlugin.getDefault().getDescriptor().getExtensionPoint("ScannerInfoProvider");
		if (extensionPoint == null) {
			fail("StandardBuildTest testScannerListernerInterface failed to retrieve the extension point ScannerInfoProvider.");
		}
		IExtension[] extensions = extensionPoint.getExtensions();
		IScannerInfoProvider provider = null;

		// Find the first IScannerInfoProvider that supplies build info for the project
		for (int i = 0; i < extensions.length && provider == null; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (int j = 0; j < elements.length; ++j) {
				IConfigurationElement element = elements[j];
				if (element.getName().equals("provider")) { 
					// Check if it handles the info for the project
					try {
						IScannerInfoProvider temp = (IScannerInfoProvider)element.createExecutableExtension("class");
						if (temp.managesResource(project)) {
							provider = temp;
							break;
						}
					} catch (CoreException e) {
						fail("Failed retrieving scanner info provider from plugin: " + e.getLocalizedMessage());
					}
				}
			}
		}
		return provider;
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

	public void testProjectConversion() {
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

		// Save, Close, and Reopen the project
		StandardBuildManager.saveBuildInfo(project);
		try {
			project.close(new NullProgressMonitor());
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectConversion failed to close project " + e.getLocalizedMessage());
		}
		StandardBuildManager.removeBuildInfo(project);
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
	public void testProjectCreation () {
		// Create a new project
		IProject project = null;
		try {
			project = createProject(PROJECT_NAME); 
			// Convert the new project to a standard make project
			CCorePlugin.getDefault().convertProjectToCC(project, new NullProgressMonitor(), CCorePlugin.PLUGIN_ID + ".make");
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
		
		// Check the default settings
		checkDefaultProjectSettings(project);
	}
	
	public void testProjectSettings() {
		// Get the project
		IProject project = null;
		try {
			project = createProject(PROJECT_NAME);
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectSettings failed opening project: " + e.getLocalizedMessage());
		}
		assertNotNull(project);
		
		// Change the settings
		StandardBuildManager.setIncludePaths(project, OVR_INC_PATHS);
		StandardBuildManager.setPreprocessorSymbols(project, OVR_PREPROC_SYMS);
		
		// Use the build info for the rest of the settings
		IStandardBuildInfo info = StandardBuildManager.getBuildInfo(project);
		info.setStopOnError(ON);
		info.setUseDefaultBuildCmd(OFF);
		info.setBuildLocation(OVR_BUILD_LOCATION);
		info.setFullBuildArguments(OVR_BUILD_ARGS);
		
		// Save, Close, and Reopen the project
		StandardBuildManager.saveBuildInfo(project);
		try {
			project.close(new NullProgressMonitor());
		} catch (CoreException e) {
			fail("StandardBuildTest testProjectSettings failed to close project " + e.getLocalizedMessage());
		}
		StandardBuildManager.removeBuildInfo(project);
		try {
			project.open(new NullProgressMonitor());
		} catch (CoreException e) {
			fail ("StandardBuildTest testProjectSettings failed to open project " + e.getLocalizedMessage());
		}
		
		// Retest
		checkOverriddenProjectSettings(project);
	}

	public void testScannerListenerInterface() {
		// Get the project
		IProject project = null;
		try {
			project = createProject(PROJECT_NAME);
		} catch (CoreException e) {
			fail("StandardBuildTest testScannerListernerInterface failed opening project: " + e.getLocalizedMessage());
		}
		assertNotNull(project);

		// Find the scanner info provider for this project
		IScannerInfoProvider provider = findInfoProvider(project);
		assertNotNull(provider);
		
		// Remove what's there
		StandardBuildManager.setIncludePaths(project, new String[0]);
		StandardBuildManager.setPreprocessorSymbols(project, new String[0]);

		// Subscribe
		ScannerListener listener = new ScannerListener();
		provider.subscribe(project, listener);
		
		// Change the settings
		StandardBuildManager.setIncludePaths(project, listener.getExpectedPaths());
		StandardBuildManager.setPreprocessorSymbols(project, listener.getExpectedSymbols());
		
		// Unsubscribe
		provider.unsubscribe(project, listener);
	}
}
