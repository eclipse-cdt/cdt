/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests org.eclipse.cdt.core.build.ICBuildConfigurationProvider.
 *
 * Relies on manifest declarations in /org.eclipse.cdt.core.tests/plugin.xml.
 * See "org.eclipse.cdt.core.build.ICBuildConfigurationProviderTests.providerId" and
 * "extendedCmakeNature2"
 */
public class ICBuildConfigurationProviderTests extends BaseTestCase5 {
	private IProject project;
	private IToolChain mockToolchain;
	private ICBuildConfigurationManager configManager = CDebugCorePlugin.getService(ICBuildConfigurationManager.class);
	private ILaunchTargetManager launchTargetManager = CDebugCorePlugin.getService(ILaunchTargetManager.class);

	@BeforeEach
	public void setup() throws Exception {
		// Create a customized CMake project
		project = createCustomizedCMakeProject();
		// Setup a toolchain ready to use for creating the ICBuildConfiguration
		mockToolchain = mock(IToolChain.class);
		when(mockToolchain.getProperty(IToolChain.ATTR_OS)).thenReturn("osDummy");
		when(mockToolchain.getProperty(IToolChain.ATTR_ARCH)).thenReturn("archDummy");
		when(mockToolchain.getTypeId()).thenReturn("tc_typeId");
		when(mockToolchain.getId()).thenReturn("tcId");
		when(mockToolchain.getBuildConfigNameFragment()).thenReturn("buildConfigName");
	}

	/**
	 * Tests org.eclipse.cdt.core.build.ICBuildConfigurationProvider.getCBuildConfigName(IProject, String, IToolChain, String, ILaunchTarget)
	 * can be extended to provide a customized ICBuildConfiguration name and thereby a customized build output directory name.
	 *
	 * @see {@link ICBuildConfigurationProviderTestsProvider#getCBuildConfigName(IProject, String, IToolChain, String, org.eclipse.launchbar.core.target.ILaunchTarget)}
	 */
	@Test
	public void getCBuildConfigName() throws Exception {
		/*
		 * Test summary:
		 * 1) A ICBuildConfiguration is created using the provided toolchain, launch mode and launch target.
		 * Note, the key principle to the ICBuildConfigurationProvider being overriden successfully is the project nature.
		 * It uses a customized nature, declared in plugin.xml.
		 * When the project provider is accessed, the declaration of extension point="org.eclipse.cdt.core.buildConfigProvider" relates
		 * the customized nature, so the correct (ICBuildConfigurationProviderTestsProvider) provider is used.
		 *
		 * 2) ICBuildConfiguration2.getBuildDirectoryURI() queries the build output directory name. The correct value is specified
		 * by ICBuildConfigurationProviderTestsProvider.getCBuildConfigName.
		 */

		/*
		 * cmake because we extend CMakeBuildConfigurationProvider.
		 * run because ILaunchManager.RUN_MODE.
		 * buildConfigName because mockToolchain returns this from getBuildConfigNameFragment.
		 * Local because using Local launch target.
		 * customizedTest because ICBuildConfigurationProviderTestsProvider.getCBuildConfigName specifies this.
		 */
		final String expectedName = "cmake.run.buildConfigName.Local.customizedTest";

		/*
		 * (1) Create ICBuildConfiguration, using our customized ICBuildConfigurationProviderTestsProvider
		 */
		ICBuildConfiguration cBuildConfiguration = configManager.getBuildConfiguration(project, mockToolchain,
				ILaunchManager.RUN_MODE, launchTargetManager.getLocalLaunchTarget(), new NullProgressMonitor());
		CBuildConfiguration cBuildConfig = (CBuildConfiguration) cBuildConfiguration;
		assertThat(cBuildConfig.getName(), is(expectedName));
		/*
		 * (2) Check last segment of build output directory name is expected.
		 */
		Path buildDirectory = cBuildConfig.getBuildDirectory();
		String lastSegment = buildDirectory.getFileName().toString();
		assertThat(lastSegment, is(expectedName));
	}

	private IProject createCustomizedCMakeProject() throws Exception {
		// Create a  plain Eclipse project
		IProject project = ResourceHelper.createProject(this.getName());
		// Add our customized CMake nature to the project
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID,
				ICBuildConfigurationProviderTestsCMakeNature.ID });
		project.setDescription(description, null);
		return project;
	}
}
