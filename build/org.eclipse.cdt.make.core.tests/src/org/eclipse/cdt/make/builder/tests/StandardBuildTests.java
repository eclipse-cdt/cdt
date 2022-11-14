/*******************************************************************************
 * Copyright (c) 2002, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.builder.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StandardBuildTests extends BaseTestCase5 {
	private static final boolean OFF = false;
	private static final boolean ON = true;
	private static final String OVR_BUILD_ARGS = "-f";
	private static final String OVR_BUILD_COMMAND = "/home/tester/bin/nmake";
	private static final String OVR_BUILD_LOCATION = "src";
	private static final String PROJECT_NAME = "StandardBuildTest";
	private IProject project;

	@BeforeEach
	public void projectCreation() throws Exception {
		project = createProject(PROJECT_NAME);
		// Convert the new project to a standard make project
		MakeProjectNature.addNature(project, null);
	}

	@AfterEach
	public void projectCleanup() throws CoreException, IOException {
		project.delete(true, true, null);
	}

	@Test
	public void test() throws Exception {
		doProjectSettings();
		doProjectConversion();
	}

	private void checkDefaultProjectSettings(IProject project) throws Exception {
		assertNotNull(project);

		IMakeBuilderInfo defaultInfo = MakeCorePlugin
				.createBuildInfo(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID, true);

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

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IProject project = root.getProject(name);
				assertFalse(project.exists());
				project.create(null);

				if (!project.isOpen()) {
					project.open(null);
				}
				CCorePlugin.getDefault().convertProjectToC(project, new NullProgressMonitor(),
						MakeCorePlugin.MAKE_PROJECT_ID);
				result[0] = project;
			}
		}, null);
		return (IProject) result[0];
	}

	public void doProjectConversion() throws Exception {
		// Check the settings (they should be the override values)
		checkOverriddenProjectSettings(project);

		// Now convert the project
		CCorePlugin.getDefault().convertProjectFromCtoCC(project, new NullProgressMonitor());

		// Close, and Reopen the project
		project.close(new NullProgressMonitor());
		project.open(new NullProgressMonitor());

		// Make sure it has a CCNature
		assertTrue(project.hasNature(CCProjectNature.CC_NATURE_ID));

		// Nothing should have changed in the settings
		checkOverriddenProjectSettings(project);
	}

	private void doCheckInitialSettings() throws CoreException, Exception {
		// Make sure it has a CNature
		assertTrue(project.hasNature(CProjectNature.C_NATURE_ID));

		// Make sure it has a MakeNature
		assertTrue(project.hasNature(MakeProjectNature.NATURE_ID));

		// Check the default settings
		checkDefaultProjectSettings(project);
	}

	public void doProjectSettings() throws Exception {
		assertNotNull(project);

		// Use the build info for the rest of the settings
		IMakeBuilderInfo builderInfo = MakeCorePlugin.createBuildInfo(project, MakeBuilder.BUILDER_ID);
		builderInfo.setStopOnError(ON);
		builderInfo.setUseDefaultBuildCmd(OFF);
		builderInfo.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, OVR_BUILD_COMMAND);
		builderInfo.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, OVR_BUILD_ARGS);
		builderInfo.setBuildAttribute(IMakeCommonBuildInfo.BUILD_LOCATION, OVR_BUILD_LOCATION);

		project.close(new NullProgressMonitor());
		project.open(new NullProgressMonitor());

		// Retest
		checkOverriddenProjectSettings(project);
	}
}
