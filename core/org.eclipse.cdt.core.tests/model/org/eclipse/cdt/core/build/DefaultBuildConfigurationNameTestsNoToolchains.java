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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchBarTracker;
import org.eclipse.cdt.internal.core.build.CBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for IDE-82683-REQ-024 #1084 part of #1000 in CDT 12.0.0. Test Part 2.
 *
 * Tests that a ICBuildConfiguration is never created with the default name when
 * no toolchains are available.
 *
 * @see {@link DefaultBuildConfigurationNameTests}
 *
 */
public class DefaultBuildConfigurationNameTestsNoToolchains extends BaseTestCase5 {
	private IProject project;
	private ICBuildConfigurationManager configManager = CDebugCorePlugin.getService(ICBuildConfigurationManager.class);
	private IToolChainManager toolchainManager = CDebugCorePlugin.getService(IToolChainManager.class);
	private ILaunchBarManager launchBarManager = CDebugCorePlugin.getService(ILaunchBarManager.class);

	@BeforeEach
	public void setup() throws Exception {
		/*
		 * Remove any discovered toolchains that happen to be installed on the host. This is because
		 * some test hosts may have a gcc installed and others not. So if we remove that gcc and do not
		 * rely on it and instead setup our mocked toolchain instead, the test conditions are easier to control.
		 */
		removeDiscoveredToolchains();

		CBuildConfigurationManager cbm = (CBuildConfigurationManager) configManager;
		cbm.reset();

		/*
		 * Copied from LaunchBarManagerTest.startupTest()
		 * Make sure the manager starts up and defaults everything to null
		 */
		LaunchBarManager manager = new LaunchBarManager(false);
		manager.init();
		assertThat(manager.getActiveLaunchDescriptor(), is(nullValue()));
		assertThat(manager.getActiveLaunchMode(), is(nullValue()));
		assertThat(manager.getActiveLaunchTarget(), is(nullValue()));

		/*
		 * Key to repeatedly running tests with the same initial conditions is to reset the
		 * CoreBuildLaunchBarTracker, so the launchbar controls are as they would be in a new
		 * workspace.
		 */
		CDebugCorePlugin.getDefault().resetCoreBuildLaunchBarTracker();
	}

	/**
	 * Test that when calling {@link ICBuildConfigurationManager#getBuildConfiguration(IBuildConfiguration)}
	 * when there are no toolchains available, the active IBuildConfiguration becomes set to "buildError/!".
	 *
	 * Test to confirm assumptions about the project's IBuildConfiguration lifecycle when no toolchains are
	 * installed and a project is created.
	 *
	 * Behaviour before IDE-82683-REQ-024 fixed AND after fix.
	 * When IToolChainManager contains no valid toolchains:
	 *
	 * After project creation, the project's active IBuildConfiguration name defaults to
	 * IBuildConfiguration.DEFAULT_CONFIG_NAME.
	 *
	 * After project creation, the first interaction with the Core Build system is through the call:
	 *   config.getAdapter(ICBuildConfiguration.class), where config is a IBuildConfiguration.
	 * This happens when the .project file is added in CModelManager.getBinaryParser(IProject).
	 * getAdapter calls CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration) with
	 * IBuildConfiguration==DEFAULT_CONFIG_NAME.
	 *
	 * When CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration buildConfig) is called,
	 * the provider.getCBuildConfiguration(buildConfig, configName) returns null because it has no toolchain.
	 * The CBuildConfigurationManager then quarantines this default buildConfig in the noConfigs bin and returns null.
	 *
	 * Later, when CoreBuildLaunchBarTracker triggers, it detects there are no toolchains available and so creates
	 * a new IBuildConfiguration with name "buildError/!" and then a special error ICBuildConfiguration, ErrorBuildConfiguration,
	 * with the message "No Toolchain found for Target Local".
	 * This new IBuildConfiguration/ICBuildConfiguration combo is then added as the active config using
	 * ProjectDescription.setActiveBuildConfig(String).
	 *
	 * Then project.getActiveBuildConfig() returns the buildConfig with name "buildError/!".
	 *
	 * And buildConfig.getAdapter(ICBuildConfiguration.class) returns the active buildConfig's ICBuildConfiguration
	 * which is instanceof ErrorBuildConfiguration.
	 */
	@Test
	public void getBuildConfigurationNoToolchainsErrorBuildConfig() throws Exception {
		// Create a CMake project, without any toolchains installed.
		project = createCMakeProject();

		waitForLaunchBarTracker();

		IBuildConfiguration buildConfig = project.getActiveBuildConfig();
		assertThat(buildConfig.getName(), is(not(IBuildConfiguration.DEFAULT_CONFIG_NAME)));
		assertThat(buildConfig.getName(), is("buildError/!"));
		System.out.println(
				"getBuildConfigurationNoToolchainsErrorBuildConfig::buildConfig.getName()=" + buildConfig.getName());

		// This calls CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration)
		ICBuildConfiguration iCBuildConfig = buildConfig.getAdapter(ICBuildConfiguration.class);
		System.out.println();
		// expected: config should be an instanceof ErrorBuildConfiguration, but no way to get the name to check.
		assertThat(iCBuildConfig, is(instanceOf(ErrorBuildConfiguration.class)));
		// expected: the buildConfig should be the buildError one
		assertThat(iCBuildConfig.getBuildConfiguration().getName(), is("buildError/!"));
	}

	/**
	 * Performs the same test as {@link #getBuildConfigurationNoToolchainsErrorBuildConfig()}.
	 * The test exists so we can prove multiple test runs function correctly and that
	 * initial conditions are correctly controlled and reset.
	 */
	@Test
	public void getBuildConfigurationNoToolchainsErrorBuildConfig2() throws Exception {
		getBuildConfigurationNoToolchainsErrorBuildConfig();
	}

	// Test infrastructure...

	private void waitForLaunchBarTracker() throws OperationCanceledException, InterruptedException {
		Job.getJobManager().join(CoreBuildLaunchBarTracker.JOB_FAMILY_CORE_BUILD_LAUNCH_BAR_TRACKER, null);
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

	private void removeDiscoveredToolchains() throws CoreException {
		Collection<IToolChain> allToolChains = toolchainManager.getAllToolChains();
		for (IToolChain toolchain : new ArrayList<>(allToolChains)) {
			// System.out.println(String.format("Removing toolchain '%s' for test '%s'", toolchain.getName(), getName()));
			toolchainManager.removeToolChain(toolchain);
		}
		// Expected: there are no toolchains
		assertThat(toolchainManager.getAllToolChains().size(), is(0));
	}
}
