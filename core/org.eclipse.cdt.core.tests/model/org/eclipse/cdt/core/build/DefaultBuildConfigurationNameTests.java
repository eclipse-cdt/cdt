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
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.cdt.cmake.core.CMakeBuildConfigurationProvider;
import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.autotools.core.AutotoolsBuildConfigurationProvider;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.internal.meson.core.MesonBuildConfigurationProvider;
import org.eclipse.cdt.make.core.MakefileBuildConfigurationProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that a ICBuildConfiguration is never created with the default name.
 *
 *
 * Tests org.eclipse.cdt.cmake.core.CMakeBuildConfigurationProvider and friends.
 * org.eclipse.cdt.core.build.ICBuildConfigurationProvider.getCBuildConfiguration(IBuildConfiguration, String)
 * TBC
 */
public class DefaultBuildConfigurationNameTests extends BaseTestCase5 {
	private IProject project;
	private IToolChain mockToolchain;
	private ICBuildConfigurationManager configManager = CDebugCorePlugin.getService(ICBuildConfigurationManager.class);
	private ILaunchTargetManager launchTargetManager = CDebugCorePlugin.getService(ILaunchTargetManager.class);

	@BeforeEach
	public void setup() throws Exception {
		// Create a CMake project
		project = createCMakeProject();
		// Setup a toolchain ready to use for creating the ICBuildConfiguration
		mockToolchain = mock(IToolChain.class);
		when(mockToolchain.getProperty(IToolChain.ATTR_OS)).thenReturn("osDummy");
		when(mockToolchain.getProperty(IToolChain.ATTR_ARCH)).thenReturn("archDummy");
		when(mockToolchain.getTypeId()).thenReturn("tc_typeId");
		when(mockToolchain.getId()).thenReturn("tcId");
		when(mockToolchain.getBuildConfigNameFragment()).thenReturn("buildConfigName");
	}

	/**
	 * Tests that a ICBuildConfiguration is never created with the default name.
	 *
	 * Tests that org.eclipse.cdt.core.build.ICBuildConfigurationProvider.getCBuildConfiguration(IBuildConfiguration, String)
	 * never returns a valid ICBuildConfiguration when a default IBuildConfiguration and name=ICBuildConfiguration.DEFAULT_NAME
	 * are passed.
	 *
	 * Test uses ICBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration) to attempt to get a C build config
	 * named ICBuildConfiguration.DEFAULT_NAME
	 * TBC
	 *
	 **/
	@Test
	public void getBuildConfiguration() throws Exception {
		/*
		 * Test summary:
		 */
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = configManager.getBuildConfiguration(buildConfig);
		assertThat(cBuildConfig, is(nullValue()));
	}

	/**
	 * Tests that a ICBuildConfiguration is never created with the default name.
	 *
	 * Test uses ICBuildConfigurationProvider.getCBuildConfiguration(IBuildConfiguration, String) directly by
	 * instantiating a ICBuildConfigurationProvider (CMakeBuildConfigurationProvider)
	 *
	 * @throws Exception
	 */
	@Test
	public void cMakeBuildConfigurationProviderGetCBuildConfiguration() throws Exception {
		/*
		 * Test summary:
		 */
		ICBuildConfigurationProvider provider = new CMakeBuildConfigurationProvider();
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = provider.getCBuildConfiguration(buildConfig, "default");
		assertThat(cBuildConfig, is(nullValue()));
	}

	@Test
	public void makefileBuildConfigurationProviderGetCBuildConfiguration() throws Exception {
		/*
		 * Test summary:
		 */
		ICBuildConfigurationProvider provider = new MakefileBuildConfigurationProvider();
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = provider.getCBuildConfiguration(buildConfig, "default");
		assertThat(cBuildConfig, is(nullValue()));
	}

	@Test
	public void mesonBuildConfigurationProviderGetCBuildConfiguration() throws Exception {
		/*
		 * Test summary:
		 */
		ICBuildConfigurationProvider provider = new MesonBuildConfigurationProvider();
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = provider.getCBuildConfiguration(buildConfig, "default");
		assertThat(cBuildConfig, is(nullValue()));
	}

	@Test
	public void autotoolsBuildConfigurationProviderGetCBuildConfiguration() throws Exception {
		/*
		 * Test summary:
		 */
		ICBuildConfigurationProvider provider = new AutotoolsBuildConfigurationProvider();
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = provider.getCBuildConfiguration(buildConfig, "default");
		assertThat(cBuildConfig, is(nullValue()));
	}

	private IProject createCMakeProject() throws Exception {
		// Create a  plain Eclipse project
		IProject project = ResourceHelper.createProject(this.getName());
		// Add C/C++ and CMake natures to make it a CMake project
		IProjectDescription description = project.getDescription();
		description.setNatureIds(
				new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, CMakeNature.ID });
		project.setDescription(description, null);
		return project;
	}
}
