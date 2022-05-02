/*******************************************************************************
 * Copyright (c) 2004, 2022 Intel Corporation and others.
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
 *     Baltasar Belyavsky (Texas Instruments) - [279633] Custom command-generator support
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ManagedCommandLineGeneratorTest extends TestCase {

	private static String[] testCommandLinePatterns = { null, "${COMMAND}", "${COMMAND} ${FLAGS}",
			"${COMMAND} ${FLAGS} ${OUTPUT_FLAG}", "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}",
			"${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT}",
			"${COMMAND} ${FLAGS} ${OUTPUT_FLAG} ${OUTPUT_PREFIX}${OUTPUT}",
			"${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT} ${INPUTS}",
			"${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT} ${INPUTS} ${EXTRA_FLAGS}",
			"${command} ${flags} ${output_flag}${output_prefix}${output} ${WRONG_VAR_NAME}" };
	private static String TEST_TOOL_ID = "test.four.dot.zero.cdt.managedbuild.tool.gnu.c.linker";
	private static String COMMAND_VAL = "[command]";
	private static String FLAGS_VAL = "[flags]";
	private static String[] FLAGS_ARRAY_VAL = FLAGS_VAL.split("\\s");
	private static String OUTPUT_FLAG_VAL = "[outputFlag]";
	private static String OUTPUT_PREFIX_VAL = "[outputPrefix]";
	private static String OUTPUT_VAL = "[output]";
	private static String INPUTS_VAL = "[inputs]";
	private static String[] INPUTS_ARRAY_VAL = INPUTS_VAL.split("\\s");
	private static String[] OBJECTS_ARRAY_VAL = new String[] { "obj0", "obj1" };
	private static String[] LIBRARIES_ARRAY_VAL = new String[] { "lib0", "lib1" };
	private static String EXTRA_FLAGS = "obj0 obj1 -llib0 -llib1";
	private static String[] commandLineEtalonesForPatterns = {
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\""
					+ " " + "\"" + INPUTS_VAL + "\" " + EXTRA_FLAGS,
			COMMAND_VAL, COMMAND_VAL + " " + FLAGS_VAL, COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL,
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL,
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"",
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"",
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " "
					+ "\"" + INPUTS_VAL + "\"",
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " "
					+ "\"" + INPUTS_VAL + "\" " + EXTRA_FLAGS,
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " "
					+ "${WRONG_VAR_NAME}" };

	public ManagedCommandLineGeneratorTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ManagedCommandLineGeneratorTest.class);
	}

	public final void testGenerateCommandLineInfoPatterns() throws BuildException {
		ITool tool = ManagedBuildManager.getExtensionTool(TEST_TOOL_ID);
		setToolOptionByType(tool, IOption.OBJECTS, OBJECTS_ARRAY_VAL);
		setToolOptionByType(tool, IOption.LIBRARIES, LIBRARIES_ARRAY_VAL);
		IManagedCommandLineGenerator gen = new ManagedCommandLineGenerator();
		IManagedCommandLineInfo info = null;
		for (int i = 0; i < testCommandLinePatterns.length; i++) {
			info = gen.generateCommandLineInfo(tool, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL,
					OUTPUT_VAL, INPUTS_ARRAY_VAL, testCommandLinePatterns[i]);
			assertNotNull(info);
			if (i < commandLineEtalonesForPatterns.length) {
				assertEquals("i=" + i, commandLineEtalonesForPatterns[i], info.getCommandLine());
			}
		}
	}

	public final void testGenerateCommandLineInfoDoublePattern() throws BuildException {
		ITool tool = ManagedBuildManager.getExtensionTool(TEST_TOOL_ID);
		IManagedCommandLineGenerator gen = new ManagedCommandLineGenerator();

		IManagedCommandLineInfo info = gen.generateCommandLineInfo(tool, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL,
				OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, "${OUTPUT_FLAG} ${OUTPUT_FLAG}");
		assertNotNull(info);
		assertEquals(OUTPUT_FLAG_VAL + " " + OUTPUT_FLAG_VAL, info.getCommandLine());
	}

	public final void testGenerateCommandLineInfoParameters() throws BuildException {
		ITool tool = ManagedBuildManager.getExtensionTool(TEST_TOOL_ID);
		setToolOptionByType(tool, IOption.OBJECTS, OBJECTS_ARRAY_VAL);
		setToolOptionByType(tool, IOption.LIBRARIES, LIBRARIES_ARRAY_VAL);
		IManagedCommandLineGenerator gen = new ManagedCommandLineGenerator();

		IManagedCommandLineInfo info = gen.generateCommandLineInfo(tool, "", FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL,
				OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " " + "\""
				+ INPUTS_VAL + "\" " + EXTRA_FLAGS, info.getCommandLine());

		info = gen.generateCommandLineInfo(tool, COMMAND_VAL, new String[0], OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL,
				OUTPUT_VAL, INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + "  " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " "
				+ "\"" + INPUTS_VAL + "\" " + EXTRA_FLAGS, info.getCommandLine());

		info = gen.generateCommandLineInfo(tool, COMMAND_VAL, FLAGS_ARRAY_VAL, "", OUTPUT_PREFIX_VAL, OUTPUT_VAL,
				INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + "  " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " " + "\""
				+ INPUTS_VAL + "\" " + EXTRA_FLAGS, info.getCommandLine());

		info = gen.generateCommandLineInfo(tool, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, "", OUTPUT_VAL,
				INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + "\"" + OUTPUT_VAL + "\"" + " " + "\""
				+ INPUTS_VAL + "\" " + EXTRA_FLAGS, info.getCommandLine());

		info = gen.generateCommandLineInfo(tool, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, "",
				INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + " " + "\""
				+ INPUTS_VAL + "\" " + EXTRA_FLAGS, info.getCommandLine());

		info = gen.generateCommandLineInfo(tool, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL,
				OUTPUT_VAL, new String[0], null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL
				+ "\"  " + EXTRA_FLAGS, info.getCommandLine());

		info = gen.generateCommandLineInfo(tool, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL,
				OUTPUT_VAL, null, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL
				+ "\"  " + EXTRA_FLAGS, info.getCommandLine());
	}

	public final void testCustomGenerator() {

		//  First, verify the elements in the project type
		IProjectType proj = ManagedBuildManager.getProjectType("cdt.managedbuild.test.java.attrs");
		assertNotNull(proj);
		IConfiguration[] configs = proj.getConfigurations();
		assertEquals(1, configs.length);
		IConfiguration config = proj.getConfiguration("cdt.managedbuild.test.java.attrs.config");
		assertNotNull(config);
		ITool[] tools = config.getTools();
		assertEquals(1, tools.length);
		ITool tool = config.getTool("cdt.managedbuild.test.java.attrs.tool");
		assertNotNull(tool);
		IOption[] options = tool.getOptions();
		assertEquals(20, options.length);
		IOption option = tool.getOption("testgnu.c.compiler.option.preprocessor.def.symbols.test");
		assertNotNull(option);
		Object val = option.getValue();
		assertTrue(val instanceof ArrayList);
		@SuppressWarnings("unchecked")
		ArrayList<String> list = (ArrayList<String>) val;
		assertEquals("foo", list.get(0));
		assertEquals("bar", list.get(1));

		//  Next, invoke the commandLineGenerator for this tool
		IManagedCommandLineGenerator gen = tool.getCommandLineGenerator();
		String[] flags = { "-a", "-b", "-c" };
		String[] inputs = { "xy.cpp", "ab.cpp", "lt.cpp", "c.cpp" };
		IManagedCommandLineInfo info = gen.generateCommandLineInfo(tool, "MyName", flags, "-of", "opre",
				"TheOutput.exe", inputs, "[COMMAND] [FLAGS]");
		assertEquals("compiler.gnu.cMyName", info.getCommandName());
		assertEquals("-c -b -a", info.getFlags());
		assertEquals("ab.cpp c.cpp foo.cpp lt.cpp xy.cpp", info.getInputs());
		assertEquals("-0h", info.getOutputFlag());
		assertEquals("", info.getOutputPrefix());
		assertEquals("Testme", info.getOutput());
		assertEquals("[COMMAND] [FLAGS]", info.getCommandLinePattern());
		assertEquals("This is a test command line", info.getCommandLine());

		//  Next, invoke the build file generator for the tool chain
		IManagedBuilderMakefileGenerator makeGen = ManagedBuildManager.getBuildfileGenerator(config);
		String name = makeGen.getMakefileName();
		assertEquals("TestBuildFile.mak", name);
	}

	public final void testCustomOptionCommandGenerator() {
		try {
			IProject project = ManagedBuildTestHelper.createProject("COCG", null, (IPath) null,
					"cdt.test.customOptionCommand.ProjectType");
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration config = info.getDefaultConfiguration();
			ITool[] tools = config.getToolsBySuperClassId("cdt.test.customOptionCommand.Tool1");
			assertEquals(1, tools.length);

			ITool tool = tools[0];

			IOption option1 = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.option1");
			IOption option2 = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.option2");
			IOption option3 = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.option3");
			IOption option4 = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.option4");

			assertTrue(option1.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(option2.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(option3.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertNull(option4.getCommandGenerator());

			option1 = config.setOption(tool, option1, new String[] { "val1", "val2", "${ProjName}" });
			option2 = config.setOption(tool, option2, "${ProjName}");
			option3 = config.setOption(tool, option3, "${ProjName}");
			option4 = config.setOption(tool, option4, "${ProjName}");

			/* Expected results
			 * 	option1: custom command-generator concatenates list-entries into quoted semicolon-separated list.
			 * 	option2/3: custom command-generator returns 'null' causing CDT to fall-back to default behaviour.
			 * 	option4: no custom command-generator contributed - CDT falls back to default behaviour.
			 */

			String command = tool.getToolCommandFlagsString(null, null);
			assertEquals("-opt1=\"val1;val2;COCG;\" -opt2=COCG -opt3 COCG -opt4=COCG", command);

			ManagedBuildTestHelper.removeProject("COCG");
		} catch (Exception e) {
			fail("Test failed on project creation: " + e.getLocalizedMessage());
		}
	}

	public final void testCustomOptionCommandGenerator2() {
		try {
			IProject project = ManagedBuildTestHelper.createProject("COCG2", null, (IPath) null,
					"cdt.test.customOptionCommand.ProjectType");
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration config = info.getDefaultConfiguration();

			ITool[] tools = config.getToolsBySuperClassId("cdt.test.customOptionCommand.Tool2");
			assertEquals(1, tools.length);

			ITool tool = tools[0];

			IOption optionString = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionString");
			IOption optionStringList = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionStringList");
			IOption optionBoolean = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionBoolean");
			IOption optionEnumerated = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionEnumerated");
			IOption optionIncludePath = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionIncludePath");
			IOption optionDefinedSymbols = tool
					.getOptionBySuperClassId("cdt.test.customOptionCommand.optionDefinedSymbols");
			IOption optionLibs = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionLibs");
			IOption optionUserObjs = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionUserObjs");
			IOption optionSymbolFiles = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionSymbolFiles");
			IOption optionIncludeFiles = tool
					.getOptionBySuperClassId("cdt.test.customOptionCommand.optionIncludeFiles");
			IOption optionLibPaths = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionLibPaths");
			IOption optionLibFiles = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionLibFiles");
			IOption optionUndefIncludePath = tool
					.getOptionBySuperClassId("cdt.test.customOptionCommand.optionUndefIncludePath");
			IOption optionUndefDefinedSymbols = tool
					.getOptionBySuperClassId("cdt.test.customOptionCommand.optionUndefDefinedSymbols");
			IOption optionUndefLibPaths = tool
					.getOptionBySuperClassId("cdt.test.customOptionCommand.optionUndefLibPaths");
			IOption optionUndefLibFiles = tool
					.getOptionBySuperClassId("cdt.test.customOptionCommand.optionUndefLibFiles");
			IOption optionUndefIncludeFiles = tool
					.getOptionBySuperClassId("cdt.test.customOptionCommand.optionUndefIncludeFiles");
			IOption optionUndefSymbolFiles = tool
					.getOptionBySuperClassId("cdt.test.customOptionCommand.optionUndefSymbolFiles");
			IOption optionTree = tool.getOptionBySuperClassId("cdt.test.customOptionCommand.optionTree");

			assertTrue(optionString.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionStringList.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionBoolean.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionEnumerated.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionIncludePath.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionDefinedSymbols.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionLibs.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionUserObjs.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionSymbolFiles.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionIncludeFiles.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionLibPaths.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionLibFiles.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionUndefIncludePath.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionUndefDefinedSymbols.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionUndefLibPaths.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionUndefLibFiles.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionUndefIncludeFiles.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionUndefSymbolFiles.getCommandGenerator() instanceof CustomOptionCommandGenerator);
			assertTrue(optionTree.getCommandGenerator() instanceof CustomOptionCommandGenerator);

			optionString = config.setOption(tool, optionString, "${ProjName}");
			optionStringList = config.setOption(tool, optionStringList, new String[] { "val1", "${ProjName}" });
			optionBoolean = config.setOption(tool, optionBoolean, true);
			optionEnumerated = config.setOption(tool, optionEnumerated,
					"org.eclipse.cdt.managedbuilder.core.tests.enumeratedOptionValue2");
			optionIncludePath = config.setOption(tool, optionIncludePath, new String[] { "val2", "${ProjName}" });
			optionDefinedSymbols = config.setOption(tool, optionDefinedSymbols, new String[] { "val3", "${ProjName}" });
			optionLibs = config.setOption(tool, optionLibs, new String[] { "val4", "${ProjName}" });
			optionUserObjs = config.setOption(tool, optionUserObjs, new String[] { "val5", "${ProjName}" });
			optionSymbolFiles = config.setOption(tool, optionSymbolFiles, new String[] { "val6", "${ProjName}" });
			optionIncludeFiles = config.setOption(tool, optionIncludeFiles, new String[] { "val7", "${ProjName}" });
			optionLibPaths = config.setOption(tool, optionLibPaths, new String[] { "val8", "${ProjName}" });
			optionLibFiles = config.setOption(tool, optionLibFiles, new String[] { "val9", "${ProjName}" });
			optionUndefIncludePath = config.setOption(tool, optionUndefIncludePath,
					new String[] { "val10", "${ProjName}" });
			optionUndefDefinedSymbols = config.setOption(tool, optionUndefDefinedSymbols,
					new String[] { "val11", "${ProjName}" });
			optionUndefLibPaths = config.setOption(tool, optionUndefLibPaths, new String[] { "val12", "${ProjName}" });
			optionUndefLibFiles = config.setOption(tool, optionUndefLibFiles, new String[] { "val13", "${ProjName}" });
			optionUndefIncludeFiles = config.setOption(tool, optionUndefIncludeFiles,
					new String[] { "val14", "${ProjName}" });
			optionUndefSymbolFiles = config.setOption(tool, optionUndefSymbolFiles,
					new String[] { "val15", "${ProjName}" });
			optionTree = config.setOption(tool, optionTree, "org.eclipse.cdt.managedbuilder.core.tests.treeOption2");

			String command = tool.getToolCommandFlagsString(null, null);
			assertEquals(String.join(" ", "-optString=COCG2", //
					"-optStringList=\"val1;COCG2;\"", //
					"-optBoolean=true", //
					"-optEnumerated=value2", //
					"-optIncludePath=\"val2;COCG2;\"", //
					"-optDefinedSymbols=\"val3;COCG2;\"", //
					"-optSymbolFiles=\"val6;COCG2;\"", //
					"-optIncludeFiles=\"val7;COCG2;\"", //
					"-optLibPaths=\"val8;COCG2;\"", //
					"-optLibFiles=\"val9;COCG2;\"", //
					"-optUndefIncludePath=\"val10;COCG2;\"", //
					"-optUndefDefinedSymbols=\"val11;COCG2;\"", //
					"-optUndefLibPaths=\"val12;COCG2;\"", //
					"-optUndefLibFiles=\"val13;COCG2;\"", //
					"-optUndefIncludeFiles=\"val14;COCG2;\"", //
					"-optUndefSymbolFiles=\"val15;COCG2;\"", //
					"-optTree=value2"), //
					command);

			String[] libs = config.getLibs(config.getArtifactExtension());
			assertEquals(Arrays.asList("-optLibs=\"val4;COCG2;\"").toString(), Arrays.asList(libs).toString());
			assertEquals(Arrays.asList(libs).toString(),
					Arrays.asList(tool.getExtraFlags(IOption.LIBRARIES)).toString());

			String[] userObjs = config.getUserObjects(config.getArtifactExtension());
			assertEquals(Arrays.asList("-optUserObjs=\"val5;COCG2;\"").toString(), Arrays.asList(userObjs).toString());
			assertEquals(Arrays.asList(userObjs).toString(),
					Arrays.asList(tool.getExtraFlags(IOption.OBJECTS)).toString());

			ManagedBuildTestHelper.removeProject("COCG2");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed on project creation: " + e.getLocalizedMessage());
		}
	}

	public final void testDollarValue() {
		try {
			IProject project = ManagedBuildTestHelper.createProject("CDV", null, (IPath) null,
					"cdt.test.dollarValue.ProjectType");
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration config = info.getDefaultConfiguration();
			//  Set values for the options
			ITool[] tools = config.getToolsBySuperClassId("cdt.test.dollarValue.Tool");
			assertEquals(tools.length, 1);
			ITool tool = tools[0];
			IOption option1 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option1");
			IOption option2 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option2");
			IOption option3 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option3");
			IOption option4 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option4");
			IOption option5 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option5");
			IOption option6 = tool.getOptionBySuperClassId("cdt.test.dollarValue.option6");

			String command;

			option1 = config.setOption(tool, option1, "OPT1VALUE");
			option2 = config.setOption(tool, option2, "");
			option3 = config.setOption(tool, option3, "X");
			option4 = config.setOption(tool, option4, "opt4");
			command = tool.getToolCommandFlagsString(null, null);
			assertEquals("-opt1 OPT1VALUE X ${opt4}", command);

			option1 = config.setOption(tool, option1, "");
			option2 = config.setOption(tool, option2, "Opt2");
			option3 = config.setOption(tool, option3, "All work and no play...");
			option4 = config.setOption(tool, option4, "123456789");
			option5 = config.setOption(tool, option5, "DollarValue");
			command = tool.getToolCommandFlagsString(null, null);
			assertEquals("-opt2Opt2suffix All work and no play... ${123456789} DollarValueDollarValue", command);

			option1 = config.setOption(tool, option1, "0");
			option2 = config.setOption(tool, option2, "LongValue");
			option3 = config.setOption(tool, option3, "");
			option4 = config.setOption(tool, option4, "");
			option5 = config.setOption(tool, option5, "$");
			option6 = config.setOption(tool, option6, "%%");
			command = tool.getToolCommandFlagsString(null, null);
			assertEquals("-opt1 0 -opt2LongValuesuffix $$ x%%yy%%z", command);

			option1 = config.setOption(tool, option1, "1");
			option2 = config.setOption(tool, option2, "2");
			option3 = config.setOption(tool, option3, "3");
			option4 = config.setOption(tool, option4, "4");
			option5 = config.setOption(tool, option5, "");
			option6 = config.setOption(tool, option6, "");
			command = tool.getToolCommandFlagsString(null, null);
			assertEquals("-opt1 1 -opt22suffix 3 ${4}", command);

			ManagedBuildTestHelper.removeProject("CDV");
		} catch (Exception e) {
			fail("Test failed on project creation: " + e.getLocalizedMessage());
		}
	}

	private void setToolOptionByType(ITool tool, int valueType, String[] value) throws BuildException {
		for (IOption option : tool.getOptions()) {
			if (valueType == option.getValueType()) {
				option.setValue(value);
			}
		}
	}

}
