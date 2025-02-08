/*******************************************************************************
 * Copyright (c) 2024 Renesas Electronics Europe.
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
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.cdt.cmake.core.CMakeBuildConfigurationProvider;
import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsScannerInfoProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CBuildConfigurationManagerTests extends BaseTestCase5 {
	private IProject project;
	private IToolChain mockToolchain;
	private ICBuildConfigurationManager configManager = CDebugCorePlugin.getService(ICBuildConfigurationManager.class);
	private ILaunchBarManager launchBarManager = CDebugCorePlugin.getService(ILaunchBarManager.class);
	private ILaunchTargetManager launchTargetManager = CDebugCorePlugin.getService(ILaunchTargetManager.class);

	@BeforeEach
	public void setup() throws Exception {
		// Create a CMake project
		project = createCMakeProject();
		// Setup a toolchain ready to use for creating the valid ICBuildConfiguration
		mockToolchain = mock(IToolChain.class);
		when(mockToolchain.getProperty(IToolChain.ATTR_OS)).thenReturn("osDummy");
		when(mockToolchain.getProperty(IToolChain.ATTR_ARCH)).thenReturn("archDummy");
		when(mockToolchain.getTypeId()).thenReturn("tc_typeId");
		when(mockToolchain.getId()).thenReturn("tcId");
		when(mockToolchain.getBuildConfigNameFragment()).thenReturn("buildConfigName");
	}

	/**
	 * Tests that CBuildConfigurationManager.getBuildConfiguration(IProject, IToolChain, String, IProgressMonitor)
	 * calls CCorePlugin.resetCachedScannerInfoProvider(IProject) after creating a new ICBuildConfiguration.
	 *
	 * Tests the following failure mode.
	 * When the project's active IBuildConfiguration has the default name and the chosen ICBuildConfigurationProvider.
	 * getCBuildConfiguration does not support the IBuildConfiguration.DEFAULT_CONFIG_NAME and returns null, this can
	 * cause the project's ScannerInfoProvider to become "stuck"
	 * (https://bugs.eclipse.org/bugs/show_bug.cgi?id=413357) on the wrong setting (eg
	 * LanguageSettingsScannerInfoProvider instead of ICBuildConfiguration) until Eclipse is restarted or the project
	 * is closed and reopened. When this happens, the indexer does not function.
	 *
	 * This problem may arise if an ISV contributes a ICBuildConfigurationProvider which has very specific naming
	 * conventions for it's build configurations.
	 *
	 * Test approach:
	 * The test requires that a faulty build configuration is setup for the project to trick the getScannerInfoProvider
	 * into thinking it's not a core build project. Typically ContainerGCCToolChainProvider may find a valid gcc toolchain
	 * and then the provider's getCBuildConfiguration will return a valid ICBuildConfiguration. So need to set the active
	 * build configuration to one which does not use the default name.
	 *
	 * (1)
	 * In a CMake project, add a new build configuration which doesn't use the default name and add this to the project
	 * using an invalid ICBuildConfiguration and set this as the active build configuration.
	 *
	 * (2)
	 * Later, when the indexer gets the project's active build configuration it is null and so the project's
	 * IScannerInfoProvider is set to the wrong type and is cached.
	 *
	 * (3)
	 * Later still, a new build configuration is created, this time with a valid ICBuildConfiguration, which is set as the
	 * project's active build configuration.
	 *
	 * (4)
	 * With the fix in place (resetCachedScannerInfoProvider), the next time the indexer gets the project's
	 * IScannerInfoProvider it will be recomputed and return the project's expected IScannerInfoProvider
	 * (ie: ICBuildConfiguration).
	 */
	//	@Disabled("temp for testing")
	@Test
	public void testResetCachedScannerInfoProvider() throws Exception {
		// (1) create a CMake project
		//		IProject project = createCMakeProject();

		CMakeBuildConfigurationProvider provider = new CMakeBuildConfigurationProvider();
		String buildConfigBaseName = "notDefaultName";
		// Create a new IBuildConfiguration with a name that is not the default name.
		IBuildConfiguration buildConfiguration = configManager.createBuildConfiguration(provider, project,
				buildConfigBaseName, new NullProgressMonitor());
		// Add the IBuildConfiguration/ICBuildConfiguration combo using an invalid ICBuildConfiguration
		configManager.addBuildConfiguration(buildConfiguration, null);
		// Set the IBuildConfiguration, with this name, as the active build config
		IProjectDescription description = project.getDescription();
		String buildConfigName = provider.getId() + "/" + buildConfigBaseName;
		description.setActiveBuildConfig(buildConfigName);
		project.setDescription(description, new NullProgressMonitor());

		// (2) The project's scannerInfoProvider is expected to be the wrong type here
		IScannerInfoProvider scannerInfoProvider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		assertThat("scannerInfoProvider expected to be LanguageSettingsScannerInfoProvider",
				scannerInfoProvider instanceof LanguageSettingsScannerInfoProvider);

		// (3) Setup a toolchain ready to use for creating the valid ICBuildConfiguration
		//		IToolChain mockToolchain = mock(IToolChain.class);
		//		when(mockToolchain.getProperty(IToolChain.ATTR_OS)).thenReturn("osDummy");
		//		when(mockToolchain.getProperty(IToolChain.ATTR_ARCH)).thenReturn("archDummy");
		//		when(mockToolchain.getTypeId()).thenReturn("tc_typeId");
		//		when(mockToolchain.getId()).thenReturn("tcId");
		//		when(mockToolchain.getBuildConfigNameFragment()).thenReturn("buildConfigName");

		ILaunchTarget launchTarget = launchTargetManager.getLaunchTarget(ILaunchTargetManager.localLaunchTargetTypeId,
				"Local");
		ICBuildConfiguration cBuildConfiguration = configManager.getBuildConfiguration(project, mockToolchain,
				ILaunchManager.RUN_MODE, launchTarget, new NullProgressMonitor());
		assertThat("The cBuildConfiguration should be of type CBuildConfiguration",
				cBuildConfiguration instanceof CBuildConfiguration);
		CBuildConfiguration cbc = (CBuildConfiguration) cBuildConfiguration;
		// Set this ICBuildConfiguration as the active build configuration
		cbc.setActive(new NullProgressMonitor());

		// (4) The project's scannerInfoProvider is expected to be the correct type here
		scannerInfoProvider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		assertThat("scannerInfoProvider expected to be ICBuildConfiguration",
				scannerInfoProvider instanceof ICBuildConfiguration);
	}

	/**
	 * Test org.eclipse.cdt.core.build.ICBuildConfigurationManager.getBuildConfiguration(IProject, IToolChain, String, ILaunchTarget, IProgressMonitor)
	 *
	 * The new parameter, ILaunchTarget, was added in 9.0
	 *
	 * @throws Exception
	 */
	//	@Disabled("temp for testing")
	@Test
	public void getBuildConfiguration0() throws Exception {
		//		IProject project = createCMakeProject();
		//		IToolChain toolchain = null; // todo: mock this
		String launchMode = "debug";
		ILaunchTarget launchTarget = ILaunchTarget.NULL_TARGET;
		ICBuildConfiguration cBuildConfiguration = configManager.getBuildConfiguration(project, mockToolchain,
				launchMode, launchTarget, new NullProgressMonitor());

		// Add asserts for expected values
		assertThat(cBuildConfiguration.getToolChain().getTypeId(), is("tc_typeId"));
		assertThat(cBuildConfiguration.getToolChain().getId(), is("tcId"));
		assertThat(cBuildConfiguration.getLaunchMode(), is("debug"));
		assertThat(cBuildConfiguration.getLaunchTarget(), is(ILaunchTarget.NULL_TARGET));
	}

	/**
	 * idea for a test.
	 * create a new cmake project.
	 * set active launch mode and set active launch target to specific value
	 * wait a bit or sync with CoreBuildLaunchBarTracker job.
	 * check if the CoreBuildLaunchBarTracker.setActiveBuildConfig has set the correct active config.
	 * TODO: replace sleeps with proper synchronisation
	 */
	@Test
	public void getBuildConfiguration1() throws Exception {
		// create project
		// by default, active launchtarget is set to Local
		// set active launch mode, set active launch target to specific value
		// See if this causes CoreBuildLaunchBarTracker to be called again

		Job job = getJobByName("Change Build Configurations");
		//wait for the CoreBuildLaunchBarTracker to finish processing
		//		job.join();
		sleep();

		//		ILaunchMode activeLaunchMode = launchBarManager.getActiveLaunchMode();
		//		ILaunchTarget launchTarget = ILaunchTarget.NULL_TARGET;
		//		launchBarManager.setActiveLaunchTarget(launchTarget);
		//
		//		//wait for the CoreBuildLaunchBarTracker to finish processing
		//		//		job.join();
		//		sleep();

		ILaunchMode debugLaunchMode = getDebugLaunchMode();
		assertThat(debugLaunchMode.getIdentifier(), is("debug"));
		launchBarManager.setActiveLaunchMode(debugLaunchMode);
		sleep();
		assertThat(launchBarManager.getActiveLaunchMode(), is(notNullValue()));
		assertThat(launchBarManager.getActiveLaunchMode().getIdentifier(), is(debugLaunchMode.getIdentifier()));

		// Adding LT also sets it active
		ILaunchTarget launchTarget = launchTargetManager.addLaunchTarget(ILaunchTargetManager.localLaunchTargetTypeId,
				"id0");
		assertThat(launchTarget.getId(), is("id0"));
		//		launchBarManager.setActiveLaunchTarget(launchTarget2);
		//wait for the CoreBuildLaunchBarTracker to finish processing
		//		job.join();
		sleep();
		assertThat(launchBarManager.getActiveLaunchTarget(), is(notNullValue()));
		assertThat(launchBarManager.getActiveLaunchTarget().getId(), is("id0"));

		System.out.println("Active launch target id=" + launchBarManager.getActiveLaunchTarget().getId());
		System.out.println("Active launch mode id=" + launchBarManager.getActiveLaunchMode().getIdentifier());
		System.out.println();

		IBuildConfiguration buildConfig = project.getActiveBuildConfig();
		ICBuildConfiguration cBuildConfig = buildConfig.getAdapter(ICBuildConfiguration.class);
		assertThat(cBuildConfig, is(notNullValue()));
		// Check the active ICBuildConfiguration has the same launch target we set active previously
		ILaunchTarget launchTargetCBuildConfig = cBuildConfig.getLaunchTarget();
		assertThat(launchTargetCBuildConfig, is(notNullValue()));
		assertThat(launchTargetCBuildConfig.getId(), is(launchTarget.getId()));
		assertThat(launchTargetCBuildConfig.getTypeId(), is(launchTarget.getTypeId()));
	}

	private ILaunchMode getDebugLaunchMode() throws CoreException {
		ILaunchMode[] launchModes = launchBarManager.getLaunchModes();
		for (int i = 0; i < launchModes.length; i++) {
			ILaunchMode iLaunchMode = launchModes[i];
			if (iLaunchMode.getIdentifier().equals("debug")) {
				return iLaunchMode;
			}
		}
		return null;
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

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

		}
	}

	private Job getJobByName(String jobName) {
		Job[] jobs = Job.getJobManager().find(null); // Find all jobs
		for (Job job : jobs) {
			if (jobName.equals(job.getName())) {
				return job;
			}
		}
		return null; // Job not found
	}
}
