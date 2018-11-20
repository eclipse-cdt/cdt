/*******************************************************************************
 * Copyright (c) 2004, 2011 Intel Corporation and others.
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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;
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
			"${command} ${flags} ${output_flag}${output_prefix}${output} ${WRONG_VAR_NAME}" };
	private static String COMMAND_VAL = "[command]";
	private static String FLAGS_VAL = "[flags]";
	private static String[] FLAGS_ARRAY_VAL = FLAGS_VAL.split("\\s");
	private static String OUTPUT_FLAG_VAL = "[outputFlag]";
	private static String OUTPUT_PREFIX_VAL = "[outputPrefix]";
	private static String OUTPUT_VAL = "[output]";
	private static String INPUTS_VAL = "[inputs]";
	private static String[] INPUTS_ARRAY_VAL = INPUTS_VAL.split("\\s");
	private static String[] commandLineEtalonesForPatterns = {
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\""
					+ " " + "\"" + INPUTS_VAL + "\"",
			COMMAND_VAL, COMMAND_VAL + " " + FLAGS_VAL, COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL,
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL,
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"",
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"",
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " "
					+ "\"" + INPUTS_VAL + "\"",
			COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " "
					+ "${WRONG_VAR_NAME}" };

	public ManagedCommandLineGeneratorTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ManagedCommandLineGeneratorTest.class);
	}

	public final void testGetCommandLineGenerator() {
		IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
		assertNotNull(gen);
	}

	public final void testGenerateCommandLineInfoPatterns() {
		IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
		IManagedCommandLineInfo info = null;
		for (int i = 0; i < testCommandLinePatterns.length; i++) {
			info = gen.generateCommandLineInfo(null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL,
					OUTPUT_VAL, INPUTS_ARRAY_VAL, testCommandLinePatterns[i]);
			assertNotNull(info);
			if (i < commandLineEtalonesForPatterns.length) {
				assertEquals("i=" + i, commandLineEtalonesForPatterns[i], info.getCommandLine());
			}
		}
	}

	public final void testGenerateCommandLineInfoDoublePattern() {
		IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();

		IManagedCommandLineInfo info = gen.generateCommandLineInfo(null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL,
				OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, "${OUTPUT_FLAG} ${OUTPUT_FLAG}");
		assertNotNull(info);
		assertEquals(OUTPUT_FLAG_VAL + " " + OUTPUT_FLAG_VAL, info.getCommandLine());
	}

	public final void testGenerateCommandLineInfoParameters() {
		IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();

		IManagedCommandLineInfo info = gen.generateCommandLineInfo(null, "", FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL,
				OUTPUT_PREFIX_VAL, OUTPUT_VAL, INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " " + "\""
				+ INPUTS_VAL + "\"", info.getCommandLine());

		info = gen.generateCommandLineInfo(null, COMMAND_VAL, new String[0], OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL,
				OUTPUT_VAL, INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + "  " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " "
				+ "\"" + INPUTS_VAL + "\"", info.getCommandLine());

		info = gen.generateCommandLineInfo(null, COMMAND_VAL, FLAGS_ARRAY_VAL, "", OUTPUT_PREFIX_VAL, OUTPUT_VAL,
				INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + "  " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL + "\"" + " " + "\""
				+ INPUTS_VAL + "\"", info.getCommandLine());

		info = gen.generateCommandLineInfo(null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, "", OUTPUT_VAL,
				INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + "\"" + OUTPUT_VAL + "\"" + " " + "\""
				+ INPUTS_VAL + "\"", info.getCommandLine());

		info = gen.generateCommandLineInfo(null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL, "",
				INPUTS_ARRAY_VAL, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + " " + "\""
				+ INPUTS_VAL + "\"", info.getCommandLine());

		info = gen.generateCommandLineInfo(null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL,
				OUTPUT_VAL, new String[0], null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL
				+ "\"", info.getCommandLine());

		info = gen.generateCommandLineInfo(null, COMMAND_VAL, FLAGS_ARRAY_VAL, OUTPUT_FLAG_VAL, OUTPUT_PREFIX_VAL,
				OUTPUT_VAL, null, null);
		assertNotNull(info);
		assertEquals(COMMAND_VAL + " " + FLAGS_VAL + " " + OUTPUT_FLAG_VAL + " " + OUTPUT_PREFIX_VAL + "\"" + OUTPUT_VAL
				+ "\"", info.getCommandLine());
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
			ITool[] tools = config.getToolsBySuperClassId("cdt.test.customOptionCommand.Tool");
			assertEquals(tools.length, 1);

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

}
