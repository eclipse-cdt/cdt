/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
* Test for IDE_82683_REQ_004_005
*/
@TestMethodOrder(MethodOrderer.MethodName.class)
public class CMakeBuildEnvironmentSupplierTest extends BaseTestCase5 {

	protected ICBuildConfigurationManager configManager = CDebugCorePlugin
			.getService(ICBuildConfigurationManager.class);

	private static final String pathVariableName = "PATH"; //$NON-NLS-1$
	private static final String isvPath = "isvPath"; //$NON-NLS-1$

	private static String cmakeLocation;
	private static String[] generatorLocation;

	private ICMakeToolChainFile toolChainFile;
	private IBuildConfiguration buildConfig;
	private IToolChain mockToolchain;

	@BeforeAll
	public static void beforeAll() throws Exception {
		String genLoc = String.join(File.separator, "generator", "location", "no", "0");
		cmakeLocation = String.join(File.separator, "cmake", "location", "path");
		generatorLocation = new String[] { genLoc };
		// Setup Preferences
		try {
			getPreferences().clear();
			getPreferences().node(CMakeBuildEnvironmentSupplier.CMAKE_GENERATOR_LOCATION).clear();
			getPreferences().putBoolean(CMakeBuildEnvironmentSupplier.ENABLE_USE_CMAKE_LOCATION, true);
			getPreferences().put(CMakeBuildEnvironmentSupplier.CMAKE_LOCATION, cmakeLocation);
			int index;
			for (index = 0; index < generatorLocation.length; index++) {
				getPreferences().node(CMakeBuildEnvironmentSupplier.CMAKE_GENERATOR_LOCATION)
						.put(String.format("location.%d", index), generatorLocation[index]);//$NON-NLS-1$

			}
			getPreferences().flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	@AfterAll
	public static void cleanUpPreference() {
		try {
			getPreferences().removeNode();
			getPreferences().flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	@BeforeEach
	public void beforeEach() throws Exception {
		// Create a CMake project
		IProject project = createCMakeProject();
		// Setup a toolchain ready to use for creating the valid ICBuildConfiguration
		mockToolchain = mock(IToolChain.class);
		when(mockToolchain.getProperty(IToolChain.ATTR_OS)).thenReturn("osDummy");
		when(mockToolchain.getProperty(IToolChain.ATTR_ARCH)).thenReturn("archDummy");
		when(mockToolchain.getTypeId()).thenReturn("tc_typeId");
		when(mockToolchain.getId()).thenReturn("tcId");
		when(mockToolchain.getBuildConfigNameFragment()).thenReturn("buildConfigName");

		ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
		toolChainFile = manager.getToolChainFileFor(mockToolchain);

		ICBuildConfiguration cBuildConfiguration = configManager.getBuildConfiguration(project, mockToolchain,
				ILaunchManager.DEBUG_MODE, ILaunchTarget.NULL_TARGET, new NullProgressMonitor());
		buildConfig = cBuildConfiguration.getBuildConfiguration();

	}

	@Test
	public void test_1_CMakeEnvironmentContextInfo_Aquisition() {
		ExtendedCMakeBuildConfiguration cmBuildConfig = new ExtendedCMakeBuildConfiguration(buildConfig,
				"cmBuildConfigName", mockToolchain, toolChainFile);
		IEnvironmentContextInfo contextInfo = EnvironmentVariableManager.getDefault().getContextInfo(cmBuildConfig);
		ICoreEnvironmentVariableSupplier[] sups = contextInfo.getSuppliers();

		assertInstanceOf(CMakeEnvironmentContextInfo.class, contextInfo);
		assertTrue("Expect to contain CMakeBuildEnvironmentSupplier",
				Arrays.asList(sups).contains(EnvironmentVariableManager.fCmakeSupplier));
	}

	@Test
	public void test_2_CMakeBuildEnvironmentSupplier_getVariable() {
		IEnvironmentVariable var;
		ExtendedCMakeBuildConfiguration cmBuildConfig = new ExtendedCMakeBuildConfiguration(buildConfig,
				"cmBuildConfigName", mockToolchain, toolChainFile);
		ICoreEnvironmentVariableSupplier sup = EnvironmentVariableManager.fCmakeSupplier;
		// context is null and name not "PATH"
		var = sup.getVariable(pathVariableName, null);
		assertNull(var);
		var = sup.getVariable("notPATH", null);
		assertNull(var);
		// context is CMakeBuildConfiguration
		var = sup.getVariable(pathVariableName, cmBuildConfig);
		assertEquals(EnvironmentVariableManager.getDefault().getDefaultDelimiter(), var.getDelimiter());
		assertEquals(pathVariableName, var.getName());
		assertEquals(IEnvironmentVariable.ENVVAR_PREPEND, var.getOperation());
		assertTrue(var.getValue().contains(cmakeLocation));
		assertTrue(var.getValue().contains(generatorLocation[0]));

	}

	@Test
	public void test_3_CMakeBuildEnvironmentSupplier_getVariables() {
		IEnvironmentVariable[] vars;
		ExtendedCMakeBuildConfiguration cmBuildConfig = new ExtendedCMakeBuildConfiguration(buildConfig,
				"cmBuildConfigName", mockToolchain, toolChainFile);
		ICoreEnvironmentVariableSupplier sup = EnvironmentVariableManager.fCmakeSupplier;
		// context is null
		vars = sup.getVariables(null);
		assertArrayEquals(vars, new IEnvironmentVariable[0]);
		assertThat(Arrays.asList(vars), hasSize(0));
		// context is CMakeBuildConfiguration
		vars = sup.getVariables(cmBuildConfig);
		assertThat(Arrays.asList(vars), hasSize(1));
		assertEquals(EnvironmentVariableManager.getDefault().getDefaultDelimiter(), vars[0].getDelimiter());
		assertEquals("PATH", vars[0].getName());
		assertEquals(IEnvironmentVariable.ENVVAR_PREPEND, vars[0].getOperation());
		assertTrue(vars[0].getValue().contains(cmakeLocation));
		assertTrue(vars[0].getValue().contains(generatorLocation[0]));
	}

	@Test
	public void test_4_CmakePathAddedInOrder() {
		ExtendedCMakeBuildConfiguration cmBuildConfig = new ExtendedCMakeBuildConfiguration(buildConfig,
				"cmBuildConfigName", mockToolchain, toolChainFile);

		Properties environmentVariables = EnvironmentReader.getEnvVars();
		Map<String, String> env = new HashMap<>();
		for (String key : environmentVariables.stringPropertyNames()) {
			String value = environmentVariables.getProperty(key);
			env.put(key, value);
		}
		cmBuildConfig.setBuildEnvironment(env);
		// CMake tool locations must be added in order: ISV > CMake > System's path
		String pathStr = env.get(pathVariableName);
		assertNotNull(pathStr);
		String regex = String.join(EnvironmentVariableManager.getDefault().getDefaultDelimiter(), isvPath,
				cmakeLocation, generatorLocation[0], ".*").replace("\\", "\\\\"); //$NON-NLS-1$
		Pattern pattern = Pattern.compile(regex);
		assertTrue("CMake tools' location not found between ISV variable's path and System variable's path",
				pattern.matcher(pathStr).find());
	}

	private static Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node(CMakeBuildEnvironmentSupplier.NODENAME);
	}

	private class ExtendedCMakeBuildConfiguration extends CMakeBuildConfiguration {
		public ExtendedCMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
				ICMakeToolChainFile toolChainFile) {
			super(config, name, toolChain, toolChainFile, "run", ILaunchTarget.NULL_TARGET);
		}

		/**
		* Test for IDE_82683_REQ_004_005
		*
		* "The precedence of the location specified in "CMake location" is important and should be added to the PATH environment
		* variable, so it is in front of other system environment locations, but behind any locations added by an ISV."
		*/
		@Override
		public void setBuildEnvironment(Map<String, String> env) {
			super.setBuildEnvironment(env);
			List<String> pathsToAdd = new ArrayList<>(List.of(isvPath));
			String inheritedPath = env.get(pathVariableName);
			if (inheritedPath != null) {
				Collections.addAll(pathsToAdd, inheritedPath.split(File.pathSeparator));
			}
			String pathsToAddStr = pathsToAdd.stream().collect(Collectors.joining(File.pathSeparator));
			env.put(pathVariableName, pathsToAddStr);
		}
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
