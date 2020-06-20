/*******************************************************************************
 * Copyright (c) 2004, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOption.ITreeOption;
import org.eclipse.cdt.managedbuilder.core.IOption.ITreeRoot;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.core.runtime.IConfigurationElement;
import org.junit.Assert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ManagedBuildCoreTests extends TestCase {
	private static IProjectType exeType;
	private static IProjectType libType;
	private static IProjectType dllType;

	public ManagedBuildCoreTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildCoreTests.class.getName());
		suite.addTest(new ManagedBuildCoreTests("testLoadManifest"));
		suite.addTest(new ManagedBuildCoreTests("testTreeOptions"));
		suite.addTest(new ManagedBuildCoreTests("testOptionsAttributeUseByScannerDiscovery"));
		return suite;
	}

	/**
	 * Navigates through a CDT 2.1 manifest file and verifies that the
	 * definitions are loaded correctly.
	 */
	public void testLoadManifest() throws Exception {
		exeType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.exe");
		checkExeProjectType(exeType);
		dllType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.so");
		checkSoProjectType(dllType);
		libType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu21.lib");
		checkLibProjectType(libType);
	}

	/*
	 * Do a sanity check on the testgnu exe project type.
	 */
	private void checkExeProjectType(IProjectType ptype) throws BuildException {
		int i;
		int expecectedNumConfigs = 2;
		String[] expectedConfigName = { "Dbg", "Rel" };
		String expectedCleanCmd = "rm -rf";
		String expectedParserId = "org.eclipse.cdt.core.CWDLocator;org.eclipse.cdt.core.GCCErrorParser;org.eclipse.cdt.core.GLDErrorParser;org.eclipse.cdt.core.GASErrorParser;org.eclipse.cdt.core.GmakeErrorParser";
		String expectedOSList = "solaris,linux,hpux,aix,qnx";
		int expectedSizeOSList = 5;
		String[] expectedArchList = { "all" };
		String expectedBinaryParser = "org.eclipse.cdt.core.ELF";
		String expectedBinaryParser2 = "org.eclipse.cdt.core.PE64";
		String[] expectedPlatformName = { "Dbg Platform", "Rel Platform" };
		String expectedCommand = "make";
		String expectedArguments = "-k";
		String[] expectedBuilderName = { "Dbg Builder", "Rel Builder" };
		String expectedBuilderInfo = "org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator";
		String[] expectedToolId1 = { "cdt.managedbuild.tool.testgnu.c.compiler.exe.debug",
				"cdt.managedbuild.tool.testgnu.c.compiler.exe.release" };
		String expectedSuperToolId1 = "cdt.managedbuild.tool.testgnu.c.compiler";
		String expectedSuperOutputFlag1 = "-o";
		String expectedSuperGetToolCommand1 = "gcc";
		String[] expectedSuperInputExt1 = { "c" };
		String[] expectedSuperToolInterfaceExt1 = { "h" };
		String[] expectedSuperToolOutputExt1 = { "o" };
		String expectedOptionCategory1 = "testgnu.c.compiler.category.preprocessor";
		String[] OptionId1 = { "testgnu.c.compiler.exe.debug.option.optimization.level",
				"testgnu.c.compiler.exe.release.option.optimization.level" };
		String[] expectedOptionIdValue1 = { "testgnu.c.optimization.level.none", "testgnu.c.optimization.level.most" };
		String expectedEnumList1 = "Posix.Optimize.None, Posix.Optimize.Optimize, Posix.Optimize.More, Posix.Optimize.Most";
		int expectedSizeEnumList1 = 4;
		String[] expectedOptionEnumCmd1arr = { "-O0", "-O3" };
		String OptionId2 = "testgnu.c.compiler.option.debugging.other";
		String expectedOptionIdName2 = "Posix.Debug.Other";
		String OptionId3 = "testgnu.c.compiler.option.debugging.gprof";
		String expectedOptionIdName3 = "Posix.Debug.gprof";
		String expectedOptionIdCmd3 = "-pg";
		boolean expectedOptionIdValue3 = false;
		int expecectedNumTools = 5;
		int numOrderCCompilerTool = 0;
		int expecectedCNature = ITool.FILTER_C;

		// Check project attributes
		//
		assertNotNull(ptype);
		assertTrue(ptype.isTestProjectType());
		assertFalse(ptype.isAbstract());

		// Check project configurations
		//
		IConfiguration[] configs = ptype.getConfigurations();
		assertNotNull(configs);
		assertEquals(expecectedNumConfigs, configs.length);

		// Loop over configurations
		//
		for (int iconfig = 0; iconfig < configs.length; iconfig++) {

			// Verify configuration attributes
			//
			assertEquals(configs[iconfig].getName(), (expectedConfigName[iconfig]));
			assertEquals(expectedCleanCmd, configs[iconfig].getCleanCommand());
			assertEquals(expectedParserId, configs[iconfig].getErrorParserIds());

			// Fetch toolchain
			//
			IToolChain toolChain = configs[iconfig].getToolChain();

			// Fetch and check platform
			//
			ITargetPlatform platform = toolChain.getTargetPlatform();

			List<String> expectedOSListarr = new ArrayList<>();
			String[] expectedOSListTokens = expectedOSList.split(","); //$NON-NLS-1$
			for (i = 0; i < expectedOSListTokens.length; ++i) {
				expectedOSListarr.add(expectedOSListTokens[i].trim());
			}
			assertTrue(Arrays.equals(platform.getOSList(), expectedOSListarr.toArray(new String[expectedSizeOSList])));
			assertTrue(Arrays.equals(platform.getArchList(), expectedArchList));
			String[] binaryParsers = platform.getBinaryParserList();
			assertEquals(binaryParsers.length, 2);
			assertEquals(binaryParsers[0], expectedBinaryParser);
			assertEquals(binaryParsers[1], expectedBinaryParser2);
			assertEquals(platform.getName(), expectedPlatformName[iconfig]);

			// Fetch and check builder
			//
			IBuilder builder = toolChain.getBuilder();
			assertEquals(builder.getCommand(), expectedCommand);
			assertEquals(builder.getArguments(), expectedArguments);
			assertEquals(builder.getName(), expectedBuilderName[iconfig]);
			IConfigurationElement element = ((Builder) builder).getBuildFileGeneratorElement();
			if (element != null) {
				assertEquals(element.getAttribute(IBuilder.BUILDFILEGEN_ID), expectedBuilderInfo);
			}

			// Fetch and check tools list
			//
			ITool[] tools = toolChain.getTools();
			assertEquals(tools.length, expecectedNumTools);

			// Fetch and check the gnu C compiler tool
			//
			ITool tool;
			ITool superTool;

			tool = tools[numOrderCCompilerTool];
			superTool = tool.getSuperClass();
			assertEquals(tool.getId(), expectedToolId1[iconfig]);
			assertEquals(superTool.getId(), expectedSuperToolId1);
			assertEquals(tool.getNatureFilter(), expecectedCNature);
			assertTrue(Arrays.equals(superTool.getAllInputExtensions(), expectedSuperInputExt1));
			assertEquals(superTool.getOutputFlag(), expectedSuperOutputFlag1);
			assertEquals(superTool.getToolCommand(), expectedSuperGetToolCommand1);
			assertTrue(Arrays.equals(superTool.getAllDependencyExtensions(), expectedSuperToolInterfaceExt1));
			assertTrue(Arrays.equals(superTool.getOutputsAttribute(), expectedSuperToolOutputExt1));

			assertTrue(superTool.isAbstract());

			// Fetch and check an option category
			//
			IOptionCategory[] optionCats = superTool.getChildCategories();
			assertEquals(optionCats[0].getId(), (expectedOptionCategory1));

			// Fetch and check options customized for this tool
			//
			IOption option;

			// Fetch the optimization level option and verify that it has the proper
			// default value, which should overwrite the value set in the abstract
			// project that its containing project is derived from
			//
			option = tool.getOptionById(OptionId1[iconfig]);
			assertTrue(option.isExtensionElement());
			String optionDefaultValue = (String) option.getDefaultValue();
			assertEquals(option.getValueType(), IOption.ENUMERATED);
			assertEquals(optionDefaultValue, expectedOptionIdValue1[iconfig]);
			String optionEnumCmd1 = option.getEnumCommand(optionDefaultValue);
			assertEquals(optionEnumCmd1, expectedOptionEnumCmd1arr[iconfig]);
			List<String> expectedEnumList1arr = new ArrayList<>();
			String[] expectedEnumList1Tokens = expectedEnumList1.split(","); //$NON-NLS-1$
			for (i = 0; i < expectedEnumList1Tokens.length; ++i) {
				expectedEnumList1arr.add(expectedEnumList1Tokens[i].trim());
			}
			assertTrue(Arrays.equals(option.getApplicableValues(),
					expectedEnumList1arr.toArray(new String[expectedSizeEnumList1])));

			// Fetch the debug other option and verify
			//
			option = tool.getOptionById(OptionId2);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), IOption.STRING);
			assertEquals(option.getName(), expectedOptionIdName2);

			// Fetch the debug gprof option and verify
			//
			option = tool.getOptionById(OptionId3);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), IOption.BOOLEAN);
			boolean optionDefaultValueb = option.getBooleanValue();
			assertEquals(optionDefaultValueb, expectedOptionIdValue3);
			assertEquals(option.getName(), expectedOptionIdName3);
			assertEquals(option.getCommand(), expectedOptionIdCmd3);

		} // end for
	} // end routine

	/*
	 * Do a sanity check on the testgnu so project type.
	 */
	private void checkSoProjectType(IProjectType ptype) throws BuildException {
		int i;
		int expecectedNumConfigs = 2;
		String[] expectedConfigName = { "Debug", "Release" };
		String expectedCleanCmd = "rm -rf";
		String expectedParserId = "org.eclipse.cdt.core.CWDLocator;org.eclipse.cdt.core.GCCErrorParser;org.eclipse.cdt.core.GLDErrorParser;org.eclipse.cdt.core.GASErrorParser;org.eclipse.cdt.core.GmakeErrorParser";
		String expectedArtifactExtension = "so";
		String expectedOSList = "solaris,linux,hpux,aix,qnx";
		int expectedSizeOSList = 5;
		String[] expectedArchList = { "all" };
		String expectedBinaryParser = "org.eclipse.cdt.core.ELF";
		String[] expectedPlatformName = { "so Debug Platform", "so Release Platform" };
		String expectedCommand = "make";
		String expectedArguments = "-k";
		String[] expectedBuilderName = { "so Debug Builder", "so Release Builder" };
		String expectedScannerConfigDiscoveryProfileId = "org.eclipse.cdt.managedbuilder.core.GCCManagedMakePerProjectProfile";
		String[] expectedToolChainName = { "so Debug ToolChain", "so Release ToolChain" };
		String[] expectedToolId1 = { "cdt.managedbuild.tool.testgnu.c.linker.so.debug",
				"cdt.managedbuild.tool.testgnu.c.linker.so.release" };
		String expectedSuperToolId1 = "cdt.managedbuild.tool.testgnu.c.linker";
		String expectedToolOutputPrefix = "lib";
		String[] expectedToolOutput = { "" };
		String expectedSuperOutputFlag1 = "-o";
		String expectedSuperGetToolCommand1 = "gcc";
		String[] expectedSuperInputExt1 = { "o" };
		String[] expectedSuperToolOutputExt1 = { "" };
		String expectedOptionCategory1 = "testgnu.c.link.category.general";
		String OptionId1A = "testgnu.c.link.option.libs";
		String OptionId1B = "testgnu.c.link.option.paths";
		String OptionId1C = "testgnu.c.link.option.userobjs";
		String expectedOptionCmd1Aarr = "-l";
		String expectedOptionCmd1Barr = "-L";
		String OptionId2 = "testgnu.c.link.option.defname";
		String expectedOptionIdName2 = "Posix.Linker.Defname";
		String expectedOptionIdCmd2 = "-Wl,--output-def=";
		String OptionId3 = "testgnu.c.link.option.nostart";
		String expectedOptionIdName3 = "Posix.Linker.NoStartFiles";
		String expectedOptionIdCmd3 = "-nostartfiles";
		boolean expectedOptionIdValue3 = false;
		String OptionId4 = "testgnu.c.link.option.shared";
		String expectedOptionIdName4 = "Posix.Linker.Shared";
		String expectedOptionIdCmd4 = "-shared";
		boolean expectedOptionIdValue4 = false;
		int expecectedNumTools = 5;
		int numOrderCLinkerTool = 2;
		int expecectedCNature = ITool.FILTER_C;

		// Check project attributes
		//
		assertNotNull(ptype);
		assertTrue(ptype.isTestProjectType());
		assertFalse(ptype.isAbstract());

		// Check project configurations
		//
		IConfiguration[] configs = ptype.getConfigurations();
		assertNotNull(configs);
		assertEquals(expecectedNumConfigs, configs.length);

		// Loop over configurations
		//
		for (int iconfig = 0; iconfig < configs.length; iconfig++) {

			// Verify configuration attributes
			//
			assertEquals(configs[iconfig].getName(), (expectedConfigName[iconfig]));
			assertEquals(expectedCleanCmd, configs[iconfig].getCleanCommand());
			assertEquals(expectedParserId, configs[iconfig].getErrorParserIds());
			assertEquals(configs[iconfig].getArtifactExtension(), (expectedArtifactExtension));

			// Fetch toolchain and verify
			//
			IToolChain toolChain = configs[iconfig].getToolChain();
			assertEquals(toolChain.getName(), (expectedToolChainName[iconfig]));

			List<String> expectedOSListarr = new ArrayList<>();
			String[] expectedOSListTokens = expectedOSList.split(","); //$NON-NLS-1$
			for (i = 0; i < expectedOSListTokens.length; ++i) {
				expectedOSListarr.add(expectedOSListTokens[i].trim());
			}
			assertEquals(expectedParserId, configs[iconfig].getErrorParserIds());
			assertTrue(Arrays.equals(toolChain.getOSList(), expectedOSListarr.toArray(new String[expectedSizeOSList])));
			assertTrue(Arrays.equals(toolChain.getArchList(), expectedArchList));
			assertEquals(expectedScannerConfigDiscoveryProfileId, toolChain.getScannerConfigDiscoveryProfileId());

			// Fetch and check platform
			//
			ITargetPlatform platform = toolChain.getTargetPlatform();
			assertTrue(Arrays.equals(platform.getOSList(), expectedOSListarr.toArray(new String[expectedSizeOSList])));
			assertTrue(Arrays.equals(platform.getArchList(), expectedArchList));
			String[] binaryParsers = platform.getBinaryParserList();
			assertEquals(binaryParsers.length, 1);
			assertEquals(binaryParsers[0], expectedBinaryParser);
			assertEquals(platform.getName(), expectedPlatformName[iconfig]);

			// Fetch and check builder
			//
			IBuilder builder = toolChain.getBuilder();
			assertEquals(builder.getCommand(), expectedCommand);
			assertEquals(builder.getArguments(), expectedArguments);
			assertEquals(builder.getName(), expectedBuilderName[iconfig]);

			// Fetch and check tools list
			//
			ITool[] tools = toolChain.getTools();
			assertEquals(tools.length, expecectedNumTools);

			// Fetch and check the gnu C linker tool
			//
			ITool tool;
			ITool superTool;

			tool = tools[numOrderCLinkerTool];
			superTool = tool.getSuperClass();
			assertEquals(tool.getId(), expectedToolId1[iconfig]);
			assertEquals(superTool.getId(), expectedSuperToolId1);
			assertEquals(tool.getNatureFilter(), expecectedCNature);
			assertEquals(tool.getOutputPrefix(), expectedToolOutputPrefix);
			assertTrue(Arrays.equals(superTool.getOutputsAttribute(), expectedToolOutput));
			assertTrue(Arrays.equals(superTool.getAllInputExtensions(), expectedSuperInputExt1));
			assertEquals(superTool.getOutputFlag(), expectedSuperOutputFlag1);
			assertEquals(superTool.getToolCommand(), expectedSuperGetToolCommand1);
			assertTrue(Arrays.equals(superTool.getOutputsAttribute(), expectedSuperToolOutputExt1));

			// Fetch and check an option category
			//
			IOptionCategory[] optionCats = superTool.getChildCategories();
			assertEquals(optionCats[0].getId(), (expectedOptionCategory1));

			// Fetch and check options customized for this tool
			//
			IOption option;

			// Fetch the libs option and verify
			//
			option = tool.getOptionById(OptionId1A);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), (IOption.LIBRARIES));
			assertEquals(option.getCommand(), (expectedOptionCmd1Aarr));
			assertEquals(option.getBrowseType(), (IOption.BROWSE_FILE));

			// Fetch the libsearch option and verify
			//
			option = tool.getOptionById(OptionId1B);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), (IOption.STRING_LIST));
			assertEquals(option.getCommand(), (expectedOptionCmd1Barr));
			assertEquals(option.getBrowseType(), (IOption.BROWSE_DIR));

			// Fetch the user objs option and verify
			//
			option = tool.getOptionById(OptionId1C);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), (IOption.OBJECTS));
			assertEquals(option.getBrowseType(), (IOption.BROWSE_FILE));

			// Fetch the defname option and verify
			//
			option = tool.getOptionById(OptionId2);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), (IOption.STRING));
			assertEquals(option.getName(), (expectedOptionIdName2));
			assertEquals(option.getCommand(), (expectedOptionIdCmd2));

			// Fetch the nostartfiles option and verify
			//
			option = tool.getOptionById(OptionId3);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), (IOption.BOOLEAN));
			boolean optionDefaultValueb1 = option.getBooleanValue();
			assertEquals(optionDefaultValueb1, (expectedOptionIdValue3));
			assertEquals(option.getName(), (expectedOptionIdName3));
			assertEquals(option.getCommand(), (expectedOptionIdCmd3));

			// Fetch the shared option and verify that it has the proper
			// default value, which should overwrite the value set in the abstract
			// project that its containing project is derived from
			//
			option = tool.getOptionById(OptionId4);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), (IOption.BOOLEAN));
			boolean optionDefaultValueb2 = option.getBooleanValue();
			assertEquals(optionDefaultValueb2, (expectedOptionIdValue4));
			assertEquals(option.getName(), (expectedOptionIdName4));
			assertEquals(option.getCommand(), (expectedOptionIdCmd4));

		} // end for
	} //end routine

	/*
	 * Do a sanity check on the testgnu lib project type.
	 */
	private void checkLibProjectType(IProjectType ptype) throws BuildException {
		int i;
		int expecectedNumConfigs = 2;
		String[] expectedConfigName = { "Dbg", "Rel" };
		String expectedCleanCmd = "rm -rf";
		String expectedParserId = "org.eclipse.cdt.core.CWDLocator;org.eclipse.cdt.core.GCCErrorParser;org.eclipse.cdt.core.GLDErrorParser;org.eclipse.cdt.core.GASErrorParser;org.eclipse.cdt.core.GmakeErrorParser";
		String expectedArtifactExtension = "a";
		String expectedOSList = "solaris,linux,hpux,aix,qnx";
		int expectedSizeOSList = 5;
		String[] expectedArchList = { "all" };
		String expectedBinaryParser = "org.eclipse.cdt.core.ELF";
		String[] expectedPlatformName = { "Dbg P", "Rel P" };
		String expectedCommand = "make";
		String expectedArguments = "-k";
		String[] expectedBuilderName = { "Dbg B", "Rel B" };
		String expectedScannerConfigDiscoveryProfileId = "org.eclipse.cdt.managedbuilder.core.GCCManagedMakePerProjectProfile";
		String[] expectedToolId1 = { "cdt.managedbuild.tool.testgnu.cpp.compiler.lib.debug",
				"cdt.managedbuild.tool.testgnu.cpp.compiler.lib.release" };
		String expectedSuperToolId1 = "cdt.managedbuild.tool.testgnu.cpp.compiler";
		String expectedSuperOutputFlag1 = "-o";
		String expectedSuperGetToolCommand1 = "g++";
		String[] expectedSuperInputExt1 = { "c", "C", "cc", "cxx", "cpp" };
		String[] expectedSuperToolInterfaceExt1 = { "h", "H", "hpp" };
		String[] expectedSuperToolOutputExt1 = { "o" };
		String expectedOptionCategory1 = "testgnu.cpp.compiler.category.preprocessor";
		String[] OptionId1 = { "testgnu.cpp.compiler.lib.debug.option.optimization.level",
				"testgnu.cpp.compiler.lib.release.option.optimization.level" };
		String[] expectedOptionIdValue1 = { "testgnu.cpp.compiler.optimization.level.none",
				"testgnu.cpp.compiler.optimization.level.most" };
		String expectedEnumList1 = "Posix.Optimize.None, Posix.Optimize.Optimize, Posix.Optimize.More, Posix.Optimize.Most";
		int expectedSizeEnumList1 = 4;
		String[] expectedOptionEnumCmd1arr = { "-O0", "-O3" };

		String OptionId2 = "testgnu.cpp.compiler.option.other.other";
		String expectedOptionIdName2 = "OtherFlags";

		String OptionId3 = "testgnu.cpp.compiler.option.other.verbose";
		String expectedOptionIdName3 = "Posix.Verbose";
		String expectedOptionIdCmd3 = "-v";
		boolean expectedOptionIdValue3 = false;
		int expecectedNumTools = 4;
		int numOrderCppCompilerTool = 1;
		int expecectedCCNature = ITool.FILTER_CC;

		// Check project attributes
		//
		assertNotNull(ptype);
		assertTrue(ptype.isTestProjectType());
		assertFalse(ptype.isAbstract());

		// Check project configurations
		//
		IConfiguration[] configs = ptype.getConfigurations();
		assertNotNull(configs);
		assertEquals(expecectedNumConfigs, configs.length);

		// Loop over configurations
		//
		for (int iconfig = 0; iconfig < configs.length; iconfig++) {

			// Verify configuration attributes
			//
			assertEquals(configs[iconfig].getName(), (expectedConfigName[iconfig]));
			assertEquals(expectedCleanCmd, configs[iconfig].getCleanCommand());
			assertEquals(expectedParserId, configs[iconfig].getErrorParserIds());
			assertEquals(configs[iconfig].getArtifactExtension(), (expectedArtifactExtension));

			// Fetch toolchain and verify
			//
			IToolChain toolChain = configs[iconfig].getToolChain();

			List<String> expectedOSListarr = new ArrayList<>();
			String[] expectedOSListTokens = expectedOSList.split(","); //$NON-NLS-1$
			for (i = 0; i < expectedOSListTokens.length; ++i) {
				expectedOSListarr.add(expectedOSListTokens[i].trim());
			}
			assertEquals(expectedParserId, configs[iconfig].getErrorParserIds());
			assertTrue(Arrays.equals(toolChain.getOSList(), expectedOSListarr.toArray(new String[expectedSizeOSList])));
			assertTrue(Arrays.equals(toolChain.getArchList(), expectedArchList));
			assertEquals(expectedScannerConfigDiscoveryProfileId, toolChain.getScannerConfigDiscoveryProfileId());

			// Fetch and check platform
			//
			ITargetPlatform platform = toolChain.getTargetPlatform();
			assertTrue(Arrays.equals(platform.getOSList(), expectedOSListarr.toArray(new String[expectedSizeOSList])));
			assertTrue(Arrays.equals(platform.getArchList(), expectedArchList));
			String[] binaryParsers = platform.getBinaryParserList();
			assertEquals(binaryParsers.length, 1);
			assertEquals(binaryParsers[0], expectedBinaryParser);
			assertEquals(platform.getName(), expectedPlatformName[iconfig]);

			// Fetch and check builder
			//
			IBuilder builder = toolChain.getBuilder();
			assertEquals(builder.getCommand(), expectedCommand);
			assertEquals(builder.getArguments(), expectedArguments);
			assertEquals(builder.getName(), expectedBuilderName[iconfig]);

			// Fetch and check tools list
			//
			ITool[] tools = toolChain.getTools();
			assertEquals(tools.length, expecectedNumTools);

			// Fetch and check the gnu Cpp compiler tool
			//
			ITool tool;
			ITool superTool;

			tool = tools[numOrderCppCompilerTool];
			superTool = tool.getSuperClass();
			assertEquals(tool.getId(), expectedToolId1[iconfig]);
			assertEquals(superTool.getId(), expectedSuperToolId1);
			assertEquals(tool.getNatureFilter(), expecectedCCNature);
			assertTrue(Arrays.equals(superTool.getAllInputExtensions(), expectedSuperInputExt1));
			assertEquals(superTool.getOutputFlag(), expectedSuperOutputFlag1);
			assertEquals(superTool.getToolCommand(), expectedSuperGetToolCommand1);
			assertTrue(Arrays.equals(superTool.getAllDependencyExtensions(), expectedSuperToolInterfaceExt1));
			assertTrue(Arrays.equals(superTool.getOutputsAttribute(), expectedSuperToolOutputExt1));

			// Fetch and check an option category
			//
			IOptionCategory[] optionCats = superTool.getChildCategories();
			assertEquals(optionCats[0].getId(), (expectedOptionCategory1));

			// Fetch and check options customized for this tool
			//
			IOption option;

			// Fetch the optimization level option and verify that it has the proper
			// default value, which should overwrite the value set in the abstract
			// project that its containing project is derived from
			//
			option = tool.getOptionById(OptionId1[iconfig]);
			assertTrue(option.isExtensionElement());
			String optionDefaultValue = (String) option.getDefaultValue();
			assertEquals(option.getValueType(), (IOption.ENUMERATED));
			assertEquals(optionDefaultValue, (expectedOptionIdValue1[iconfig]));
			String optionEnumCmd1 = option.getEnumCommand(optionDefaultValue);
			assertEquals(optionEnumCmd1, (expectedOptionEnumCmd1arr[iconfig]));

			List<String> expectedEnumList1arr = new ArrayList<>();
			String[] expectedEnumList1Tokens = expectedEnumList1.split(","); //$NON-NLS-1$
			for (i = 0; i < expectedEnumList1Tokens.length; ++i) {
				expectedEnumList1arr.add(expectedEnumList1Tokens[i].trim());
			}
			assertTrue(Arrays.equals(option.getApplicableValues(),
					expectedEnumList1arr.toArray(new String[expectedSizeEnumList1])));

			// Fetch the other flags option and verify
			//
			option = tool.getOptionById(OptionId2);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), (IOption.STRING));
			assertEquals(option.getName(), (expectedOptionIdName2));

			// Fetch the verbose option and verify
			//
			option = tool.getOptionById(OptionId3);
			assertTrue(option.isExtensionElement());
			assertEquals(option.getValueType(), (IOption.BOOLEAN));
			boolean optionDefaultValueb = option.getBooleanValue();
			assertEquals(optionDefaultValueb, (expectedOptionIdValue3));
			assertEquals(option.getName(), (expectedOptionIdName3));
			assertEquals(option.getCommand(), (expectedOptionIdCmd3));

		} // end for
	} // end routine

	/**
	 * Tests Options of type tree as implemented in bug 365718
	 * @throws Exception
	 */
	public void testTreeOptions() throws Exception {
		IOption treeOption = ManagedBuildManager
				.getExtensionOption("cdt.managedbuild.tool.gnu.c.linker.test.tree.option");
		assertNotNull(treeOption);

		// standard options
		assertEquals(IOption.TREE, treeOption.getValueType());
		assertEquals("grandChild_1_1_1", treeOption.getValue());
		assertEquals("grandChild_1_1_1", treeOption.getDefaultValue());
		assertEquals("cdt.managedbuild.tool.gnu.c.linker.test.tree.option", treeOption.getId());
		assertEquals("-dummy", treeOption.getCommand());
		assertEquals("-dummy122", treeOption.getCommand("grandChild_1_2_2"));

		String[] applicableValues = treeOption.getApplicableValues();
		String[] expected = new String[18];
		int index = 0;
		for (int i = 1; i < 4; i++) {
			for (int j = 1; j < 3; j++) {
				for (int k = 1; k < 4; k++) {
					expected[index++] = "Grand Child " + i + ' ' + j + ' ' + k;
				}
			}
		}
		Assert.assertArrayEquals(expected, applicableValues);

		ITreeRoot treeRoot = treeOption.getTreeRoot();
		assertNotNull(treeRoot);

		// test some tree option attributes
		ITreeOption[] children = treeRoot.getChildren();
		assertNotNull(children);
		assertEquals(0, children[2].getOrder());
		assertEquals("Parent 2", children[1].getName());
		assertTrue(children[0].isContainer());

		ITreeOption findNode = treeRoot.findNode("grandChild_2_1_3");
		assertNotNull(findNode);

		int size = children.length;
		treeRoot.addChild("newID", "New Name");
		assertEquals(size + 1, treeRoot.getChildren().length);
		assertEquals("newID", treeRoot.getChild("New Name").getID());

		// check tree only methods
		IOption nonTreeOption = ManagedBuildManager
				.getExtensionOption("testgnu.c.compiler.exe.debug.option.debugging.level");
		assertFalse(IOption.TREE == nonTreeOption.getValueType());
		boolean exception = false;
		try {
			nonTreeOption.getTreeRoot();
		} catch (Exception e) {
			exception = true;
		}
		assertTrue(exception);

	}

	/**
	 * Tests attribute useByScannerDiscovery.
	 * @throws Exception
	 */
	public void testOptionsAttributeUseByScannerDiscovery() throws Exception {
		IOption optionNotSD = ManagedBuildManager.getExtensionOption("cdt.managedbuilder.lsp.tests.option.not-sd");
		assertNotNull(optionNotSD);
		assertEquals(false, optionNotSD.isForScannerDiscovery());

		IOption option = ManagedBuildManager.getExtensionOption("cdt.managedbuilder.lsp.tests.option.string");
		assertNotNull(option);
		assertEquals(true, option.isForScannerDiscovery());
	}
} // end class
