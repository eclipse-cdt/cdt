/*******************************************************************************
 * Copyright (C) 2006, 2011 Siemens AG.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.core.model.IncludeEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOptionPathConverter;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This is a test for the pathConverter attribute
 * which may be specified for a tool or toolchain
 * The manifest has an extra buildDefinitions section
 * with a dedicated project type "pathconvertertest.projecttype"
 * to support these tests.
 * @author pn3484
 *
 */
public class PathConverterTest extends TestCase {

	public PathConverterTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(PathConverterTest.class.getName());
		suite.addTest(new PathConverterTest("testPathConversionInProject"));
		suite.addTest(new PathConverterTest("testPathConverterConfigurations"));
		return suite;
	}

	/**
	 * The expected converter can be determined from the configuration's id string.
	 * The id string has a toolchain part "tc&lt;d&gt;&lt;i&gt;" and a tool part "to&lt;d&gt;&lt;i&gt;". <br>
	 * <br>
	 * Where &lt;d&gt; stands for whether a converter is directly specified: <br>
	 *   'n' : No converter specified<br>
	 *   'y' : Converter of type TestPathConverter2 specified<br>
	 * <br>
	 * and &lt;i&gt; stands for whether a converter is inherited from the superclass.<br>
	 *   'n' : Converter is not inherited<br>
	 *   'y' : Converter is directly specified<br>
	 * <br>
	 * Inherited converters are always TestPathConverter1 type.<br>
	 * <br>
	 * The test setup in the manifest file tests the follwing precedence order: <br>
	 * - A converter set directly on the tool overrides an inherited tool converter <br>
	 * - An inherited converter overrides any toolchain converters <br>
	 * - A converter set directly on the toolchain overrides an inherited toolchain converter <br>
	 */
	protected Class<? extends IOptionPathConverter> getExpectedToolConverterClass(String configId) {
		// Conservative defaults
		boolean hasToolConverter = false;
		boolean hasToolInheritedConverter = false;
		// Analyze tool information
		int toolInfoAt = configId.indexOf("to");
		String toolinfo = configId.substring(toolInfoAt + 2, toolInfoAt + 4);
		hasToolConverter = (toolinfo.charAt(0) == 'y');
		hasToolInheritedConverter = (toolinfo.charAt(1) == 'y');
		// Assume no converter
		Class<? extends IOptionPathConverter> toolConverterClass = getExpectedToolchainConverterClass(configId);
		// Modify converter as appropriate
		if (hasToolInheritedConverter)
			toolConverterClass = TestPathConverter2.class;
		if (hasToolConverter)
			toolConverterClass = TestPathConverter4.class;

		return toolConverterClass;
	}

	/**
	 * @see #getExpectedToolConverterClass(String)
	 */
	protected Class<? extends IOptionPathConverter> getExpectedToolchainConverterClass(String configId) {
		// Conservative defaults
		boolean hasToolchainConverter = false;
		boolean hasToolchainInheritedConverter = false;
		// Analyze toolchain information
		int toolchainInfoAt = configId.indexOf("tc");
		String toolchaininfo = configId.substring(toolchainInfoAt + 2, toolchainInfoAt + 4);
		hasToolchainConverter = (toolchaininfo.charAt(0) == 'y');
		hasToolchainInheritedConverter = (toolchaininfo.charAt(1) == 'y');
		// Assume no converter
		Class<? extends IOptionPathConverter> toolConverterClass = null;
		// Modify converter as appropriate
		if (hasToolchainInheritedConverter)
			toolConverterClass = TestPathConverter1.class;
		if (hasToolchainConverter)
			toolConverterClass = TestPathConverter3.class;

		return toolConverterClass;
	}

	/**
	 * Check the converter settings for some key configurations
	 */
	public void testPathConverterConfigurations() {
		IProjectType[] projTypes = ManagedBuildManager.getDefinedProjectTypes();
		assertNotNull("Project types were not loaded!", projTypes);
		IProjectType projType = ManagedBuildManager.getProjectType("pathconvertertest.projecttype");
		assertNotNull("Projecttype should have been loaded!", projType);

		IConfiguration[] configurations = projType.getConfigurations();
		assertTrue("There should be some configurations!", configurations.length > 0);

		// Check all configurations
		for (int i = 0; i < configurations.length; i++) {
			IConfiguration configuration = configurations[i];
			IToolChain toolchain = configuration.getToolChain();

			Class<? extends IOptionPathConverter> expectedToolchainConverterClass = getExpectedToolchainConverterClass(
					configuration.getId());
			IOptionPathConverter toolchainPathConverter = toolchain.getOptionPathConverter();
			if (null == expectedToolchainConverterClass) {
				assertNull("null pathConverter expected for toolchain!", toolchainPathConverter);
			} else {
				assertEquals("Unexpected pathConverter type for toolchain", expectedToolchainConverterClass,
						toolchainPathConverter.getClass());
			}

			ITool tool = toolchain.getTools()[0]; // We have only one tool in the test setup
			Class<? extends IOptionPathConverter> expectedToolConverterClass = getExpectedToolConverterClass(
					configuration.getId());
			IOptionPathConverter toolPathConverter = tool.getOptionPathConverter();
			if (null == expectedToolConverterClass) {
				assertNull("null pathConverter expected for tool!", toolPathConverter);
			} else {
				assertEquals("Unexpected pathConverter type for tool", expectedToolConverterClass,
						toolPathConverter.getClass());
			}
		}
	}

	/**
	 * Check the path conversion in live project for a specific tool.
	 */
	public void testPathConversionInProject() throws Exception {
		IProjectType type = ManagedBuildManager.getProjectType("pathconvertertest.projecttype");
		IProject project = ManagedBuildTestHelper.createProject("pathconverter01", type.getId());
		IManagedBuildInfo iinfo = ManagedBuildManager.getBuildInfo(project);
		assertNotNull("build info could not be obtained", iinfo);
		ManagedBuildInfo info = (ManagedBuildInfo) iinfo;
		boolean isConfigurationSet = info.setDefaultConfiguration("config toolchain-yy, tool-yy");
		assertTrue("Configuration could not be set", isConfigurationSet);

		IPathEntry[] pathEntries = info.getManagedBuildValues();
		assertEquals("Unexpected number of path entries", 1, pathEntries.length);
		IncludeEntry entry = (IncludeEntry) pathEntries[0];
		IPath path = entry.getIncludePath();
		String pathText = path.toString();
		assertEquals("Unexpected value for include path", "/usr/local/include", pathText);
		ManagedBuildTestHelper.removeProject("pathconverter01");
	}
}
