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
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToObject;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
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
import java.util.stream.Collectors;

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;
import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
* Test for IDE_82683_REQ_004_005 part of #1000<br>
* Verify behaviors of CMakeEnvironmentContextInfo.class and CMakeBuildEnvironmentSupplier.class
* <br>
* Test components:<br>
*   {@link CMakeEnvironmentContextInfo}<br>
*   {@link CMakeBuildEnvironmentSupplier}<br>
*/
public class CMakeBuildEnvironmentSupplierTest extends BaseTestCase5 {

	protected ICBuildConfigurationManager configManager = CDebugCorePlugin
			.getService(ICBuildConfigurationManager.class);

	private static final String LAUNCH_MODE = "run"; //$NON-NLS-1$
	private static final String pathVariableName = "PATH"; //$NON-NLS-1$
	private static final String isvPath = "isvPath"; //$NON-NLS-1$

	private static String cmakeLocation;
	private static String[] generatorLocation;

	private IBuildConfiguration buildConfig;
	private IToolChain mockToolchain;

	@BeforeEach
	public void setupPreference() throws Exception {
		String genLoc1 = String.join(File.separator, "generator", "location", "no", "1");
		String genLoc2 = String.join(File.separator, "generator", "location", "no", "2");
		cmakeLocation = String.join(File.separator, "cmake", "location", "path");
		generatorLocation = new String[] { genLoc1, genLoc2 };
		// Setup Preferences
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
		// Create a CMake project
		IProject project = createCMakeProject();
		// Setup a toolchain ready to use for creating the valid ICBuildConfiguration
		mockToolchain = mock(IToolChain.class);
		when(mockToolchain.getProperty(IToolChain.ATTR_OS)).thenReturn("osDummy");
		when(mockToolchain.getProperty(IToolChain.ATTR_ARCH)).thenReturn("archDummy");
		when(mockToolchain.getTypeId()).thenReturn("tc_typeId");
		when(mockToolchain.getId()).thenReturn("tcId");
		when(mockToolchain.getBuildConfigNameFragment()).thenReturn("buildConfigName");

		ICBuildConfiguration cBuildConfiguration = configManager.getBuildConfiguration(project, mockToolchain,
				ILaunchManager.DEBUG_MODE, ILaunchTarget.NULL_TARGET, new NullProgressMonitor());
		buildConfig = cBuildConfiguration.getBuildConfiguration();
	}

	@AfterEach
	public void cleanupPreference() throws BackingStoreException {
		getPreferences().removeNode();
		getPreferences().flush();
	}

	/**
	 * Testing for {@link CMakeEnvironmentContextInfo#getContextInfo(Object)}
	 * <br>
	 * This test verify CMakeEnvironmentContextInfo can be acquired via <code>EnvironmentVariableManager<code>
	 */
	@Test
	public void cMakeEnvironmentContextInfo_Aquisition() {
		ICBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, ILaunchTarget.NULL_TARGET);
		IEnvironmentContextInfo contextInfo = EnvironmentVariableManager.getDefault().getContextInfo(cmBuildConfig);
		ICoreEnvironmentVariableSupplier[] sups = contextInfo.getSuppliers();

		//The return IEnvironmentContextInfo is instance of CMakeEnvironmentContextInfo.class
		assertThat(contextInfo, is(instanceOf(CMakeEnvironmentContextInfo.class)));
		//The return IEnvironmentContextInfo contains CMakeBuildEnvironmentSupplier
		assertThat(sups, hasItemInArray(EnvironmentVariableManager.fCmakeSupplier));
	}

	/**
	 * Testing for {@link CMakeBuildEnvironmentSupplier#getVariable(String, Object)}
	 * <br>
	 * This test verify that <code>getVariable(String, Object)<code> return the null value in case:<br>
	 * <li> Input variable name is not <code>"PATH"<code>, or: <br>
	 * <li> Input contextInfo cannot be adapted to {@link ICBuildConfiguration}</li>
	 */
	@Test
	public void cMakeBuildEnvironmentSupplier_getVariable_nullValueReturn() {
		CMakeBuildEnvironmentSupplier sdSupplier;
		IEnvironmentVariable var;
		ICBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, ILaunchTarget.NULL_TARGET);
		ICoreEnvironmentVariableSupplier sup = EnvironmentVariableManager.fCmakeSupplier;
		// Variable's name not "PATH"
		var = sup.getVariable("notPATH", cmBuildConfig);
		assertThat(var, is(nullValue()));
		// Input context cannot be adapted to ICBuildConfiguration
		var = sup.getVariable(pathVariableName, null);
		assertThat(var, is(nullValue()));
	}

	/**
	* Testing for {@link CMakeBuildEnvironmentSupplier#getVariable(String, Object)}
	* <br>
	* This test verify that <code>getVariable(String, Object)<code> can return the "PATH" variable with
	* CMake location and Generator location in case:<br>
	* <li> Input variable name is <code>"PATH"<code>
	* <li> Input contextInfo can be adapted to {@link ICBuildConfiguration}
	*/
	@Test
	public void cMakeBuildEnvironmentSupplier_getVariable_variableReturn() {
		CMakeBuildEnvironmentSupplier sdSupplier;
		IEnvironmentVariable var;
		ICBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, ILaunchTarget.NULL_TARGET);
		ICoreEnvironmentVariableSupplier sup = EnvironmentVariableManager.fCmakeSupplier;
		var = sup.getVariable(pathVariableName, cmBuildConfig);
		assertThat(var, not(nullValue()));
		// Return "PATH" variable contains CMake location and Generator location
		assertThat(var.getDelimiter(), equalToObject(EnvironmentVariableManager.getDefault().getDefaultDelimiter()));
		assertThat(var.getName(), equalToObject(pathVariableName));
		assertThat(var.getOperation(), equalToObject(IEnvironmentVariable.ENVVAR_PREPEND));
		assertThat(var.getValue(), containsString(cmakeLocation));
		assertThat(var.getValue(), containsString(generatorLocation[0]));
		assertThat(var.getValue(), containsString(generatorLocation[1]));
	}

	/**
	 * Testing for {@link CMakeBuildEnvironmentSupplier#getVariables(Object)}
	 * <br>
	 * This test verify that <code>getVariables(Object)<code> return empty variable array in case:<br>
	 * <li> Input contextInfo cannot be adapted to {@link ICBuildConfiguration}
	 */
	@Test
	public void cMakeBuildEnvironmentSupplier_getVariables_emptyArrReturn() {
		IEnvironmentVariable[] vars;
		ICoreEnvironmentVariableSupplier sup = EnvironmentVariableManager.fCmakeSupplier;
		// When context is null, empty array is returned
		vars = sup.getVariables(null);
		assertThat(vars, equalToObject(new IEnvironmentVariable[0]));
	}

	/**
	* Testing for {@link CMakeBuildEnvironmentSupplier#getVariables(Object)}
	* <br>
	* This test verify that <code>getVariables(Object)<code> can return the PATH variables contains
	* CMake location and Generator location in case:<br>
	* <li> Input contextInfo can be adapted to {@link ICBuildConfiguration}
	*/
	@Test
	public void cMakeBuildEnvironmentSupplier_getVariables_arrReturnConntainsPathVar() {
		IEnvironmentVariable[] vars;
		ICBuildConfiguration cmBuildConfig = new CMakeBuildConfiguration(buildConfig, "cmBuildConfigName",
				mockToolchain, null, LAUNCH_MODE, ILaunchTarget.NULL_TARGET);
		ICoreEnvironmentVariableSupplier sup = EnvironmentVariableManager.fCmakeSupplier;
		// When context is CMakeBuildConfiguration, return array contains PATH variable with CMake data
		vars = sup.getVariables(cmBuildConfig);
		assertThat(vars, arrayWithSize(1));
		assertThat(vars[0], not(nullValue()));
		assertThat(vars[0].getDelimiter(),
				equalToObject(EnvironmentVariableManager.getDefault().getDefaultDelimiter()));
		assertThat(vars[0].getName(), equalToObject(pathVariableName));
		assertThat(vars[0].getValue(), containsString(cmakeLocation));
		assertThat(vars[0].getValue(), containsString(generatorLocation[0]));
		assertThat(vars[0].getValue(), containsString(generatorLocation[1]));
	}

	/**
	 * This test verify that CMake tools' locations are added into "PATH" variable following order:
	 * ISV > CMake > System's path
	 */
	@Test
	public void cMakePathAddedInOrder() {
		ExtendedCMakeBuildConfiguration cmBuildConfig = new ExtendedCMakeBuildConfiguration(buildConfig,
				"cmBuildConfigName", mockToolchain, null);
		// Setup build environment
		Properties environmentVariables = EnvironmentReader.getEnvVars();
		Map<String, String> env = new HashMap<>();
		for (String key : environmentVariables.stringPropertyNames()) {
			String value = environmentVariables.getProperty(key);
			env.put(key, value);
		}
		cmBuildConfig.setBuildEnvironment(env);
		// Confirm CMake tool locations are added in order: ISV > CMake > System's path
		String pathStr = env.get(pathVariableName);
		assertNotNull(pathStr);
		List<String> list = Arrays.asList(pathStr.split(File.pathSeparator)).subList(0, 4);
		assertThat(list, is(List.of(isvPath, cmakeLocation, generatorLocation[0], generatorLocation[1])));
	}

	private static Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node(CMakeBuildEnvironmentSupplier.NODENAME);
	}

	private class ExtendedCMakeBuildConfiguration extends CMakeBuildConfiguration {
		public ExtendedCMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
				ICMakeToolChainFile toolChainFile) {
			super(config, name, toolChain, toolChainFile, LAUNCH_MODE, ILaunchTarget.NULL_TARGET);
		}

		/**
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
