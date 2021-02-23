/*******************************************************************************
 * Copyright (c) 2004, 2016 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.cdt.managedbuilder.internal.core.Tool;

/**
 * @since 9.2
 */
public class ManagedCommandLineGenerator implements IManagedCommandLineGenerator {

	private static final String DOUBLE_QUOTE = "\""; //$NON-NLS-1$
	private static final String WHITESPACE = " "; //$NON-NLS-1$

	private static final String CMD_LINE_PRM_NAME = "COMMAND"; //$NON-NLS-1$
	private static final String FLAGS_PRM_NAME = "FLAGS"; //$NON-NLS-1$
	private static final String OUTPUT_FLAG_PRM_NAME = "OUTPUT_FLAG"; //$NON-NLS-1$
	private static final String OUTPUT_PREFIX_PRM_NAME = "OUTPUT_PREFIX"; //$NON-NLS-1$
	private static final String OUTPUT_PRM_NAME = "OUTPUT"; //$NON-NLS-1$
	private static final String INPUTS_PRM_NAME = "INPUTS"; //$NON-NLS-1$

	private static ManagedCommandLineGenerator cmdLineGen;

	protected ManagedCommandLineGenerator() {
	}

	public static ManagedCommandLineGenerator getCommandLineGenerator() {
		if (cmdLineGen == null) {
			cmdLineGen = new ManagedCommandLineGenerator();
		}
		return cmdLineGen;
	}

	private String makeVariable(String variableName) {
		return "${" + variableName + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool, String commandName, String[] flags,
			String outputFlag, String outputPrefix, String outputName, String[] inputResources,
			String commandLinePattern) {
		if (commandLinePattern == null || commandLinePattern.length() <= 0) {
			commandLinePattern = Tool.DEFAULT_PATTERN;
		}

		// if the output name isn't a variable then quote it
		if (outputName.length() > 0 && outputName.indexOf("$(") != 0) { //$NON-NLS-1$
			outputName = DOUBLE_QUOTE + outputName + DOUBLE_QUOTE;
		}

		String inputsStr = ""; //$NON-NLS-1$
		if (inputResources != null) {
			for (String inp : inputResources) {
				if (inp != null && !inp.isEmpty()) {
					// if the input resource isn't a variable then quote it
					if (inp.indexOf("$(") != 0) { //$NON-NLS-1$
						inp = DOUBLE_QUOTE + inp + DOUBLE_QUOTE;
					}
					inputsStr = inputsStr + inp + WHITESPACE;
				}
			}
			inputsStr = inputsStr.trim();
		}

		String flagsStr = stringArrayToString(flags);

		String command = commandLinePattern;

		command = command.replace(makeVariable(CMD_LINE_PRM_NAME), commandName);
		command = command.replace(makeVariable(FLAGS_PRM_NAME), flagsStr);
		command = command.replace(makeVariable(OUTPUT_FLAG_PRM_NAME), outputFlag);
		command = command.replace(makeVariable(OUTPUT_PREFIX_PRM_NAME), outputPrefix);
		command = command.replace(makeVariable(OUTPUT_PRM_NAME), outputName);
		command = command.replace(makeVariable(INPUTS_PRM_NAME), inputsStr);

		command = command.replace(makeVariable(CMD_LINE_PRM_NAME.toLowerCase()), commandName);
		command = command.replace(makeVariable(FLAGS_PRM_NAME.toLowerCase()), flagsStr);
		command = command.replace(makeVariable(OUTPUT_FLAG_PRM_NAME.toLowerCase()), outputFlag);
		command = command.replace(makeVariable(OUTPUT_PREFIX_PRM_NAME.toLowerCase()), outputPrefix);
		command = command.replace(makeVariable(OUTPUT_PRM_NAME.toLowerCase()), outputName);
		command = command.replace(makeVariable(INPUTS_PRM_NAME.toLowerCase()), inputsStr);

		return toManagedCommandLineInfo(command.trim(), commandLinePattern, commandName, flags, outputFlag,
				outputPrefix, outputName, inputResources);
	}

	protected IManagedCommandLineInfo toManagedCommandLineInfo(String commandLine, String commandLinePattern,
			String commandName, String[] flags, String outputFlag, String outputPrefix, String outputName,
			String[] inputResources) {
		return new IManagedCommandLineInfo() {
			@Override
			public String getCommandLine() {
				return commandLine;
			}

			@Override
			public String getCommandLinePattern() {
				return commandLinePattern;
			}

			@Override
			public String getCommandName() {
				return commandName;
			}

			@Override
			public String getFlags() {
				return stringArrayToString(flags);
			}

			@Override
			public String getOutputFlag() {
				return outputFlag;
			}

			@Override
			public String getOutputPrefix() {
				return outputPrefix;
			}

			@Override
			public String getOutput() {
				return outputName;
			}

			@Override
			public String getInputs() {
				return stringArrayToString(inputResources);
			}
		};
	}

	private String stringArrayToString(String[] array) {
		if (array == null || array.length <= 0) {
			return ""; //$NON-NLS-1$
		}
		return String.join(WHITESPACE, array);
	}
}
