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
package org.eclipse.cdt.cmake.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests a new API added to the CMake Build Configuration which allows default CMake properties to be set.
 * See the new interface {@link ICMakeBuildConfiguration}.
 */
public class CMakeBuildConfigurationTests extends BaseTestCase5 {
	private static final String LAUNCH_MODE = "run";
	private static final ILaunchTarget LOCAL_LAUNCH_TARGET = Activator.getService(ILaunchTargetManager.class)
			.getLocalLaunchTarget();

	private IBuildConfiguration buildConfig;
	private IToolChain mockToolchain;

	@BeforeEach
	public void setup() throws Exception {
		// Create a CMake project
		IProject project = createCMakeProject();
		// Get the default build config from the project (it always has one)
		buildConfig = project.getBuildConfig(IBuildConfiguration.DEFAULT_CONFIG_NAME);
		// Setup a toolchain ready to use for creating the valid ICBuildConfiguration
		mockToolchain = mock(IToolChain.class);
		when(mockToolchain.getProperty(IToolChain.ATTR_OS)).thenReturn("osDummy");
		when(mockToolchain.getProperty(IToolChain.ATTR_ARCH)).thenReturn("archDummy");
		when(mockToolchain.getTypeId()).thenReturn("tc_typeId");
		when(mockToolchain.getId()).thenReturn("tcId");
		when(mockToolchain.getBuildConfigNameFragment()).thenReturn("buildConfigName");
	}

	/**
	 * Test for {@link ICMakeProperties#setGenerator()}.
	 *
	 * This test also verifies that what the ISV overrides in getCMakeProperties is what takes effect.
	 */
	@Test
	public void getCMakePropertiesTestSetGenerator() throws Exception {
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET) {

			@Override
			public ICMakeProperties getCMakeProperties() {
				ICMakeProperties properties = super.getCMakeProperties();
				properties.setGenerator(CMakeGenerator.WatcomWMake);
				return properties;
			}
		};

		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();

		assertThat(cMakeProperties.getGenerator(), is(CMakeGenerator.WatcomWMake));
	}

	/**
	 * Test for IDE_82683_REQ_013 part of #1000
	 * <br>
	 * Testing {@link ICMakeProperties#getBuildType()} <br>
	 * <br>
	 * This test verify default build type is used in case:
	 * {@link ICMakeBuildConfiguration#CMAKE_USE_DEFAULT_CMAKE_SETTINGS} is <code>true<code>
	 */
	@Test
	public void getCMakePropertiesTestGetDefaultBuildType() {
		// CMAKE_USE_DEFAULT_CMAKE_SETTINGS = "true"
		CMakeBuildConfiguration cmBuildConfig;
		ICMakeProperties cMakeProperties;
		// Test for ILaunchManager.RUN_MODE
		cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName", mockToolchain, null,
				ILaunchManager.RUN_MODE, LOCAL_LAUNCH_TARGET);
		cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getBuildType(), is("Release"));

		// Test for ILaunchManager.DEBUG_MODE
		cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName", mockToolchain, null,
				ILaunchManager.DEBUG_MODE, LOCAL_LAUNCH_TARGET);
		cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getBuildType(), is("Debug"));

		// Test for ILaunchManager.PROFILE_MODE
		cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName", mockToolchain, null,
				ILaunchManager.PROFILE_MODE, LOCAL_LAUNCH_TARGET);
		cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getBuildType(), is("Release"));
	}

	/**
	 * Test for IDE_82683_REQ_013 part of #1000
	 * <br>
	 * This test verify default build type is used in case:
	 * {@link ICMakeBuildConfiguration#CMAKE_USE_DEFAULT_CMAKE_SETTINGS} is <code>true<code>
	 */
	@Test
	public void getCMakePropertiesLoadISVSelectBuildType_UseDefaultBuildType_1() {
		ICMakeProperties cMakeProperties;
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, ILaunchManager.RUN_MODE, LOCAL_LAUNCH_TARGET);
		// Setup ISV properties for CMakeBuildConfiguration
		// CMAKE_USE_DEFAULT_CMAKE_SETTINGS = "true"
		// CMAKE_BUILD_TYPE = "RelWithDebInfo"
		cmBuildConfig.removeProperty(CMakeBuildConfiguration.CMAKE_BUILD_TYPE);
		cmBuildConfig.setProperty(CMakeBuildConfiguration.CMAKE_USE_DEFAULT_CMAKE_SETTINGS, "true");
		cmBuildConfig.setProperty(CMakeBuildConfiguration.CMAKE_BUILD_TYPE, "RelWithDebInfo");
		// Expected: default build type is used (in this case: "Release" for ILaunchManager.RUN_MODE)
		cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getBuildType(), is("Release"));
	}

	/**
	 * Test for IDE_82683_REQ_013 part of #1000
	 * <br>
	 * This test verify default build type is used in case ISV build type is blank:
	 * {@link ICMakeBuildConfiguration#CMAKE_USE_DEFAULT_CMAKE_SETTINGS} is <code>false<code> and
	 * {@link ICMakeBuildConfiguration#CMAKE_BUILD_TYPE} is blank
	 */
	@Test
	public void getCMakePropertiesLoadISVSelectBuildType_ISVBuildTypeIsBlank() {
		ICMakeProperties cMakeProperties;
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, ILaunchManager.RUN_MODE, LOCAL_LAUNCH_TARGET);
		// Setup ISV properties for CMakeBuildConfiguration
		// CMAKE_USE_DEFAULT_CMAKE_SETTINGS = "false"
		// CMAKE_BUILD_TYPE = ""
		cmBuildConfig.removeProperty(CMakeBuildConfiguration.CMAKE_BUILD_TYPE);
		cmBuildConfig.setProperty(CMakeBuildConfiguration.CMAKE_USE_DEFAULT_CMAKE_SETTINGS, "false");
		cmBuildConfig.setProperty(CMakeBuildConfiguration.CMAKE_BUILD_TYPE, "");
		// Expected: "Release" build type is used (in this case: "Release" for ILaunchManager.RUN_MODE)
		cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getBuildType(), is("Release"));
	}

	/**
	 * Test for IDE_82683_REQ_013 part of #1000
	 * <br>
	 * This test verify ISV's selected build type is used in case:
	 * {@link ICMakeBuildConfiguration#CMAKE_USE_DEFAULT_CMAKE_SETTINGS} is <code>false<code> and
	 * {@link ICMakeBuildConfiguration#CMAKE_BUILD_TYPE} is NOT blank
	 */
	@Test
	public void getCMakePropertiesLoadISVSelectBuildType_UseISVBuildTypeNotBlank() {
		ICMakeProperties cMakeProperties;
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, ILaunchManager.RUN_MODE, LOCAL_LAUNCH_TARGET);
		// Setup ISV properties for CMakeBuildConfiguration
		// CMAKE_USE_DEFAULT_CMAKE_SETTINGS = "false"
		// CMAKE_BUILD_TYPE = "RelWithDebInfo"
		cmBuildConfig.removeProperty(CMakeBuildConfiguration.CMAKE_BUILD_TYPE);
		cmBuildConfig.setProperty(CMakeBuildConfiguration.CMAKE_USE_DEFAULT_CMAKE_SETTINGS, "false");
		cmBuildConfig.setProperty(CMakeBuildConfiguration.CMAKE_BUILD_TYPE, "RelWithDebInfo");
		// Expected: "RelWithDebInfo" build type is used
		cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getBuildType(), is("RelWithDebInfo"));
	}

	/**
	 * Test for {@link ICMakeProperties#setExtraArguments()}
	 *
	 * This test also verifies that what the ISV overrides in getCMakeProperties is what takes effect.
	 */
	@Test
	public void getCMakePropertiesTestSetExtraArguments() throws Exception {
		// Create a C Build Configuration using the default build config and an arbitrary name
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET) {

			@Override
			public ICMakeProperties getCMakeProperties() {
				ICMakeProperties properties = super.getCMakeProperties();
				properties.setExtraArguments(
						new ArrayList<>((List.of("-DplatformAgnosticArgsTest0=0", "-DplatformAgnosticArgsTest1=1"))));
				return properties;
			}
		};
		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();
		List<String> extraArguments = cMakeProperties.getExtraArguments();
		assertThat(extraArguments, contains("-DplatformAgnosticArgsTest0=0", "-DplatformAgnosticArgsTest1=1"));
	}

	/**
	 * Test for {@link CMakeBuildConfiguration#getDefaultProperties()}
	 */
	@Test
	public void getDefaultProperties() throws Exception {
		// Create a C Build Configuration using the default build config and an arbitrary name
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET) {

			@Override
			public Map<String, String> getDefaultProperties() {
				var defs = new HashMap<>(super.getDefaultProperties());
				defs.put(CMAKE_GENERATOR, CMakeGenerator.WatcomWMake.getCMakeName());
				return defs;
			}
		};
		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getGenerator(), is(CMakeGenerator.WatcomWMake));
	}

	@Test
	public void getDefaultPropertiesTestExtraArgs() throws Exception {
		// Create a C Build Configuration using the default build config and an arbitrary name
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET) {
			@Override
			public Map<String, String> getDefaultProperties() {
				var defs = new HashMap<>(super.getDefaultProperties());
				defs.put(CMAKE_ARGUMENTS, "-Dtest0=0 -Dtest1=1");
				return defs;
			}
		};
		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();
		List<String> extraArguments = cMakeProperties.getExtraArguments();
		assertThat(extraArguments, contains("-Dtest0=0", "-Dtest1=1"));
	}

	/**
	 * Test that a custom cmake generator can be entered and auto-created
	 */
	@Test
	public void customCMakeGeneratorEntryAuto() throws Exception {
		// Create a C Build Configuration using the default build config and an arbitrary name
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET) {
			@Override
			public Map<String, String> getDefaultProperties() {
				var defs = new HashMap<>(super.getDefaultProperties());
				// A custom generator for a custom cmake version
				defs.put(CMAKE_GENERATOR, "My Personal Generator");
				return defs;
			}
		};

		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getGenerator().getCMakeName(), is("My Personal Generator"));
		assertThat(cMakeProperties.getGenerator().getIgnoreErrOption(), is(nullValue()));
		assertThat(cMakeProperties.getGenerator().getMakefileName(), is(nullValue()));
	}

	/**
	 * Test that a custom cmake generator can be entered and manually-created
	 */
	@Test
	public void customCMakeGeneratorEntryManual() throws Exception {
		// Create a C Build Configuration using the default build config and an arbitrary name
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET) {
			@Override
			public Map<String, String> getDefaultProperties() {
				var defs = new HashMap<>(super.getDefaultProperties());
				// A custom generator for a custom cmake version
				defs.put(CMAKE_GENERATOR, "My Personal Generator");
				return defs;
			}

			@Override
			public ICMakeProperties getCMakeProperties() {
				ICMakeProperties properties = super.getCMakeProperties();
				if ("My Personal Generator".equals(properties.getGenerator().getCMakeName())) {
					var generator = new ICMakeGenerator() {
						@Override
						public String getMakefileName() {
							return "MyMak.mak";
						}

						@Override
						public String getIgnoreErrOption() {
							return "-mycustom";
						}

						@Override
						public String getCMakeName() {
							return "My Personal Generator";
						}
					};
					properties.setGenerator(generator);
				}
				return properties;
			}
		};

		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getGenerator().getCMakeName(), is("My Personal Generator"));
		assertThat(cMakeProperties.getGenerator().getIgnoreErrOption(), is("-mycustom"));
		assertThat(cMakeProperties.getGenerator().getMakefileName(), is("MyMak.mak"));
	}

	/**
	 * Test all and clean targets and cmake command have working defaults
	 */
	@Test
	public void targetsAndCommandDefaults() throws Exception {
		// Create a C Build Configuration using the default build config and an arbitrary name
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET);

		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getCommand(), is("cmake"));
		assertThat(cMakeProperties.getAllTarget(), is("all"));
		assertThat(cMakeProperties.getCleanTarget(), is("clean"));
	}

	/**
	 * Test all and clean targets and cmake command can be overridden
	 */
	@Test
	public void targetsAndCommand() throws Exception {
		// Create a C Build Configuration using the default build config and an arbitrary name
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET) {
			@Override
			public Map<String, String> getDefaultProperties() {
				var defs = new HashMap<>(super.getDefaultProperties());
				defs.put(CMAKE_BUILD_COMMAND, "mycmake");
				defs.put(CMAKE_ALL_TARGET, "myall");
				defs.put(CMAKE_CLEAN_TARGET, "myclean");
				return defs;
			}
		};

		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getCommand(), is("mycmake"));
		assertThat(cMakeProperties.getAllTarget(), is("myall"));
		assertThat(cMakeProperties.getCleanTarget(), is("myclean"));
	}

	/**
	 * Test that extra arguments parse correctly, e.g. handles ".
	 *
	 * Note that this test is minimal here as the real functionality is in {@link CommandLineUtil}
	 * and all the special cases are tested in CommandLineUtilTest.
	 */
	@Test
	public void extraArgumentsParseCorrectly() throws Exception {
		// Create a C Build Configuration using the default build config and an arbitrary name
		CMakeBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, LOCAL_LAUNCH_TARGET) {
			@Override
			public Map<String, String> getDefaultProperties() {
				var defs = new HashMap<>(super.getDefaultProperties());
				defs.put(CMAKE_ARGUMENTS, "-Da=\"something with space and quotes\" \"-Danother=quoted\"");
				return defs;
			}
		};

		// Call the new method on ICMakeBuildConfiguration to get the default CMake properties.
		ICMakeProperties cMakeProperties = cmBuildConfig.getCMakeProperties();
		assertThat(cMakeProperties.getExtraArguments(),
				is(List.of("-Da=something with space and quotes", "-Danother=quoted")));
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
