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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.cmake.core.CMakeBuildConfigurationProvider;
import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.autotools.core.AutotoolsBuildConfigurationProvider;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchBarTracker;
import org.eclipse.cdt.internal.core.build.CBuildConfigurationManager;
import org.eclipse.cdt.internal.meson.core.MesonBuildConfigurationProvider;
import org.eclipse.cdt.make.core.MakefileBuildConfigurationProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test for IDE-82683-REQ-024 #1084 part of #1000 in CDT 12.0.0. Test Part 1.
 *
 * Tests that a ICBuildConfiguration is never created with the default name.
 *
 * @see {@link DefaultBuildConfigurationNameTestsNoToolchains}
 *
 */
public class DefaultBuildConfigurationNameTests extends BaseTestCase5 {
	private static final String TC_BUILD_CONFIG_NAME_FRAGMENT = "MockToolchainBuildConfigName";
	private IProject project;
	private IToolChain mockToolchain;
	private ICBuildConfigurationManager configManager = CDebugCorePlugin.getService(ICBuildConfigurationManager.class);
	private ILaunchTargetManager launchTargetManager = CDebugCorePlugin.getService(ILaunchTargetManager.class);
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

		// Add a mocked toolchain, which acts like a gcc toolchain and has Platform related OS ans ARCH.
		addMockToolchain();
		// Create a CMake project
		project = createCMakeProject();
		waitForLaunchBarTracker();
	}

	/**
	 * This test describes the behaviour BEFORE IDE-82683-REQ-024 was fixed, hence why it is disabled.
	 *
	 * Project creation lifecycle:
	 *
	 * Project created:
	 * The project's active IBuildConfiguration defaults to DEFAULT_CONFIG_NAME.
	 * CModelManager.create(IFile, ICProject) where file==.project
	 *   CModelManager.getBinaryParser(IProject) calls
	 *     config.getAdapter(ICBuildConfiguration.class);
	 *       CBuildConfigAdapterFactory.getAdapter(Object, Class<T>)
	 *       	CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration) with IBuildConfiguration==DEFAULT_CONFIG_NAME
	 *
	 * When IToolChainManager contains a valid toolchain:
	 * When CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration buildConfig) is called,
	 * the IBuildConfiguration.DEFAULT_CONFIG_NAME buildConfig is used to get the project's ICBuildConfigurationProvider.
	 *
	 * The provider.getCBuildConfiguration(buildConfig, configName) with configName="default" is called which creates
	 * a new ICBuildConfiguration. This is how the "first" config is often named "default".
	 *
	 * This test is disabled and retained for historical and documentation purposes to show the previous behaviour prior to
	 * fixing IDE-82683-REQ-024.
	 */
	@Test
	@Disabled("This test is permanently disabled and retained for historical and documentation purposes to show"
			+ " the previous behaviour prior to fixing IDE-82683-REQ-024.")
	public void getBuildConfigurationDefaultName() throws Exception {
		IBuildConfiguration buildConfig = project.getActiveBuildConfig();
		assertThat(buildConfig.getName(), is(IBuildConfiguration.DEFAULT_CONFIG_NAME));

		// This calls CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration)
		ICBuildConfiguration iCBuildConfig = buildConfig.getAdapter(ICBuildConfiguration.class);
		// Expected: the ICBuildConfiguration is associated with the default IBuildConfiguration
		assertThat(iCBuildConfig.getBuildConfiguration(), is(buildConfig));
		// Expected: the CBuildConfiguration has name "default".
		CBuildConfiguration cBuildConfig = (CBuildConfiguration) iCBuildConfig;
		assertThat(cBuildConfig.getName(), is("default")); // ICBuildConfiguration.DEFAULT_NAME
	}

	/**
	 * This test describes the behaviour AFTER IDE-82683-REQ-024 was fixed.
	 *
	 * Project creation and CoreBuildLaunchBarTracker lifecycle:
	 *
	 * When the project is being created and the .project file is created, the getBinaryParser(), using
	 * the projects IBuildConfiguration which defaults to DEFAULT_CONFIG_NAME, makes the getAdapter call:
	 *
	 *   ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
	 *
	 * This eventually calls into:
	 *
	 * 	 CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration)
	 *
	 * The first time into getBuildConfiguration, with the default IBuildConfiguration, execution falls through without
	 * creating an ICBuildConfiguration. When this happens, the default IBuildConfiguration is "quarantined" into the
	 * noConfigs "bin" so the default IBuildConfiguration is not used on subsequent calls.
	 *
	 * 	Callstack:
	 * 	CModelManager.create(IFile, ICProject) line: 364
	 * 	  CModelManager.createBinaryFile(IFile) line: 679
	 * 	    CModelManager.getBinaryParser(IProject) line: 619
	 * 	      BuildConfiguration.getAdapter(Class<T>) line: 109
	 * 	        ...
	 * 	        CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration) line: 249
	 *
	 * Project creation continues in IProject.setDescription(...), eventually calling
	 * ILaunchBarManager.launchObjectChanged(Object) when the launchbar controls are updated
	 * with the new project contents; active launch descriptor, launch target and mode.This
	 * triggers CoreBuildLaunchBarTracker.setActiveBuildConfig(...) to fire.
	 *
	 * CoreBuildLaunchBarTracker is pivotal in creating new ICBuildConfiguration configs, according
	 * to the active launchbar controls.
	 *
	 * The CoreBuildLaunchBarTracker runs as a workspace Job and gets the project's IBuildConfiguration configs.
	 * At this point there is only the default IBuildConfiguration.
	 *
	 * The CoreBuildLaunchBarTracker calls into the CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration)
	 * to get the ICBuildConfiguration for this IBuildConfiguration. Because it's already been quarantined, it returns
	 * null.
	 *
	 * So the CoreBuildLaunchBarTracker requests the CBuildConfigurationManager to create a new ICBuildConfiguration
	 * by calling:
	 *   CBuildConfigurationManager.getBuildConfiguration(IProject, IToolChain, String, ILaunchTarget, IProgressMonitor)
	 * This calls the project's ICBuildConfigurationProvider to create a new IBuildConfiguration/ICBuildConfiguration
	 * combination using:
	 *
	 *   ICBuildConfigurationProvider.createCBuildConfiguration(IProject, IToolChain, String, ILaunchTarget, IProgressMonitor)
	 *
	 * The CoreBuildLaunchBarTracker finally sets the new ICBuildConfiguration as the active configuration.
	 *
	 * 	Callstack:
	 * 	Project.setDescription(IProjectDescription, IProgressMonitor) line: 1378
	 * 	  ...
	 * 	  LaunchBarManager.launchObjectChanged(Object) line: 398
	 * 	    ...
	 *        CoreBuildLaunchBarTracker.setActiveBuildConfig(ILaunchMode, ILaunchDescriptor, ILaunchTarget) line: 99
	 */
	@Test
	public void getBuildConfigurationOneToolchainsActiveBuildConfig() throws Exception {
		// Expected: the CoreBuildLaunchBarTracker has run and already set the correct active build config
		IBuildConfiguration activeBuildConfig = project.getActiveBuildConfig();
		assertThat(activeBuildConfig.getName(), is(not(IBuildConfiguration.DEFAULT_CONFIG_NAME)));

		// Expected: cBuildConfigName=cmake.run.MockToolchainBuildConfigName.Local
		final String expectedCBuildConfigName = "cmake." + ILaunchManager.RUN_MODE + //
				"." + TC_BUILD_CONFIG_NAME_FRAGMENT + //
				"." + ILaunchTargetManager.localLaunchTargetId;
		// Expected: buildConfig name=org.eclipse.cdt.cmake.core.provider/cmake.run.MockToolchainBuildConfigName.Local
		final String expectedBuildConfigName = CMakeBuildConfigurationProvider.ID + //
				"/" + expectedCBuildConfigName;
		assertThat(activeBuildConfig.getName(), is(expectedBuildConfigName));

		// This calls CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration)
		ICBuildConfiguration iCBuildConfig = activeBuildConfig.getAdapter(ICBuildConfiguration.class);
		// Expected: the ICBuildConfiguration is associated with the active IBuildConfiguration
		assertThat(iCBuildConfig.getBuildConfiguration(), is(activeBuildConfig));

		CBuildConfiguration cBuildConfig = (CBuildConfiguration) iCBuildConfig;
		assertThat(cBuildConfig.getName(), is(expectedCBuildConfigName));
	}

	/**
	 * Tests that ICBuildConfiguration configuration is never created with the default name when using
	 * CBuildConfigAdapterFactory.getAdapter(Object, Class<T>)
	 *
	 * Expect: returned ICBuildConfiguration is null.
	 */
	@Test
	public void getBuildConfigurationOneToolchainsNonDefaultBuildConfig() throws Exception {
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);

		// This calls CBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration)
		ICBuildConfiguration cBuildConfig = buildConfig.getAdapter(ICBuildConfiguration.class);
		assertThat(cBuildConfig, is(nullValue()));
	}

	/**
	 * Tests that ICBuildConfiguration configuration is never created with the default name when using
	 * ICBuildConfigurationManager.getBuildConfiguration(IBuildConfiguration)
	 *
	 * Expect: returned ICBuildConfiguration is null.
	 **/
	@Test
	public void getBuildConfiguration() throws Exception {
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);

		ICBuildConfiguration cBuildConfig = configManager.getBuildConfiguration(buildConfig);
		assertThat(cBuildConfig, is(nullValue()));
	}

	/**
	 * Tests that CMake build configurations are never created with the default name.
	 *
	 * <p>Test summary:
	 * Get the project's default IBuildConfiguration.
	 * Use this and the name "default" as params and call the provider directly,
	 * ICBuildConfigurationProvider.getCBuildConfiguration(IBuildConfiguration, String).
	 * Expect: returned ICBuildConfiguration is null.
	 */
	@Test
	public void cMakeBuildConfigurationProviderGetCBuildConfiguration() throws Exception {
		ICBuildConfigurationProvider provider = new CMakeBuildConfigurationProvider();
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = provider.getCBuildConfiguration(buildConfig, "default");
		assertThat(cBuildConfig, is(nullValue()));
	}

	/**
	 * Tests that Makefile build configurations are never created with the default name.
	 *
	 * @see "Test summary:" {@link #cMakeBuildConfigurationProviderGetCBuildConfiguration()}
	 */
	@Test
	public void makefileBuildConfigurationProviderGetCBuildConfiguration() throws Exception {
		ICBuildConfigurationProvider provider = new MakefileBuildConfigurationProvider();
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = provider.getCBuildConfiguration(buildConfig, "default");
		assertThat(cBuildConfig, is(nullValue()));
	}

	/**
	 * Tests that Meson build configurations are never created with the default name.
	 *
	 * @see "Test summary:" {@link #cMakeBuildConfigurationProviderGetCBuildConfiguration()}
	 */
	@Test
	public void mesonBuildConfigurationProviderGetCBuildConfiguration() throws Exception {
		ICBuildConfigurationProvider provider = new MesonBuildConfigurationProvider();
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = provider.getCBuildConfiguration(buildConfig, "default");
		assertThat(cBuildConfig, is(nullValue()));
	}

	/**
	 * Tests that Autotools build configurations are never created with the default name.
	 *
	 * @see "Test summary:" {@link #cMakeBuildConfigurationProviderGetCBuildConfiguration()}
	 */
	@Test
	public void autotoolsBuildConfigurationProviderGetCBuildConfiguration() throws Exception {
		ICBuildConfigurationProvider provider = new AutotoolsBuildConfigurationProvider();
		// Get the default build config from the project (it always has one)
		IBuildConfiguration buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		ICBuildConfiguration cBuildConfig = provider.getCBuildConfiguration(buildConfig, "default");
		assertThat(cBuildConfig, is(nullValue()));
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

	/**
	 * Add a mocked toolchain, which acts like a gcc toolchain and has Platform related OS ans ARCH.
	 */
	private void addMockToolchain() throws CoreException {
		// Setup a toolchain ready to use for creating the ICBuildConfiguration
		mockToolchain = mock(IToolChain.class);
		when(mockToolchain.getProperty(anyString())).thenAnswer(new Answer<>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String key = invocation.getArgument(0);
				return switch (key) {
				case IToolChain.ATTR_OS -> Platform.getOS();
				case IToolChain.ATTR_ARCH -> Platform.getOSArch();
				case "name" -> null; // name=Local
				default -> null;
				};
			}
		});
		when(mockToolchain.getTypeId()).thenReturn("tc_typeId");
		when(mockToolchain.getId()).thenReturn("tcId");
		when(mockToolchain.matches(anyMap())).thenCallRealMethod();
		when(mockToolchain.getBuildConfigNameFragment()).thenReturn(TC_BUILD_CONFIG_NAME_FRAGMENT);
		when(mockToolchain.getName()).thenReturn("MockToolchainName");

		// Add our mocked toolchain
		toolchainManager.addToolChain(mockToolchain);
		// Expected: there is a single toolchain now
		assertThat(toolchainManager.getAllToolChains().size(), is(1));
	}

	private void removeDiscoveredToolchains() throws CoreException {
		Collection<IToolChain> allToolChains = toolchainManager.getAllToolChains();
		for (IToolChain toolchain : new ArrayList<>(allToolChains)) {
			//			System.out.println(String.format("Removing toolchain '%s' for test '%s'", toolchain.getName(), getName()));
			toolchainManager.removeToolChain(toolchain);
		}
		// Expected: there are no toolchains
		assertThat(toolchainManager.getAllToolChains().size(), is(0));
	}
}
