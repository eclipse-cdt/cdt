/*******************************************************************************
 * Copyright (c) 2004, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;

public class ManagedCommandLineGenerator implements
		IManagedCommandLineGenerator {

	public final String AT = "@";	//$NON-NLS-1$
	public final String COLON = ":";	//$NON-NLS-1$
	public final String DOT = ".";	//$NON-NLS-1$
	public final String ECHO = "echo";	//$NON-NLS-1$
	public final String IN_MACRO = "$<";	//$NON-NLS-1$
	public final String LINEBREAK = "\\\n";	//$NON-NLS-1$
	public final String NEWLINE = System.getProperty("line.separator");	//$NON-NLS-1$
	public final String OUT_MACRO = "$@";	//$NON-NLS-1$
	public final String SEPARATOR = "/";	//$NON-NLS-1$
	public final String SINGLE_QUOTE = "'";	//$NON-NLS-1$
	public final String DOUBLE_QUOTE = "\""; //$NON-NLS-1$
	public final String TAB = "\t";	//$NON-NLS-1$
	public final String WHITESPACE = " ";	//$NON-NLS-1$
	public final String WILDCARD = "%";	//$NON-NLS-1$
	public final String UNDERLINE = "_"; //$NON-NLS-1$
	public final String EMPTY = ""; //$NON-NLS-1$

	public final String VAR_FIRST_CHAR = "$"; //$NON-NLS-1$
	public final char VAR_SECOND_CHAR = '{';
	public final String VAR_FINAL_CHAR = "}"; //$NON-NLS-1$
	public final String CLASS_PROPERTY_PREFIX = "get"; //$NON-NLS-1$

	public final String CMD_LINE_PRM_NAME = "COMMAND"; //$NON-NLS-1$
	public final String FLAGS_PRM_NAME = "FLAGS"; //$NON-NLS-1$
	public final String OUTPUT_FLAG_PRM_NAME = "OUTPUT_FLAG"; //$NON-NLS-1$
	public final String OUTPUT_PREFIX_PRM_NAME = "OUTPUT_PREFIX"; //$NON-NLS-1$
	public final String OUTPUT_PRM_NAME = "OUTPUT"; //$NON-NLS-1$
	public final String INPUTS_PRM_NAME = "INPUTS"; //$NON-NLS-1$

	private static ManagedCommandLineGenerator cmdLineGen;

	protected ManagedCommandLineGenerator() {
		cmdLineGen = null;
	}

	public static ManagedCommandLineGenerator getCommandLineGenerator() {
		if( cmdLineGen == null ) cmdLineGen = new ManagedCommandLineGenerator();
		return cmdLineGen;
	}

	private String makeVariable(String variableName) {
		return "${"+variableName+"}"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator#getCommandLineInfo(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool,
			String commandName, String[] flags, String outputFlag,
			String outputPrefix, String outputName,
			String[] inputResources, String commandLinePattern)
	{
		if( commandLinePattern == null || commandLinePattern.length() <= 0 )
			commandLinePattern = Tool.DEFAULT_PATTERN;

		// if the output name isn't a variable then quote it
		if(outputName.length()>0 && outputName.indexOf("$(") != 0) //$NON-NLS-1$
			outputName = DOUBLE_QUOTE + outputName + DOUBLE_QUOTE;

		String inputsStr=""; //$NON-NLS-1$
		if (inputResources!=null) {
			for (String inp : inputResources) {
				if(inp!=null && inp.length()>0) {
					// if the input resource isn't a variable then quote it
					if(inp.indexOf("$(") != 0) { //$NON-NLS-1$
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

		return new ManagedCommandLineInfo(command.trim(), commandLinePattern, commandName, stringArrayToString(flags),
				outputFlag, outputPrefix, outputName, stringArrayToString(inputResources));
	}

	private String stringArrayToString( String[] array ) {
		if( array == null || array.length <= 0 ) return new String();
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < array.length; i++ )
			sb.append( array[i] + WHITESPACE );
		return sb.toString().trim();
	}

}
