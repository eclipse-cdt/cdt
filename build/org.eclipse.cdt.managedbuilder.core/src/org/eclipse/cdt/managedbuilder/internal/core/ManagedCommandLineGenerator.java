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
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;

/**
 * @deprecated Use {@link org.eclipse.cdt.managedbuilder.core.ManagedCommandLineGenerator}
 */
@Deprecated
public class ManagedCommandLineGenerator implements IManagedCommandLineGenerator {

	public final String AT = "@"; //$NON-NLS-1$
	public final String COLON = ":"; //$NON-NLS-1$
	public final String DOT = "."; //$NON-NLS-1$
	public final String ECHO = "echo"; //$NON-NLS-1$
	public final String IN_MACRO = "$<"; //$NON-NLS-1$
	public final String LINEBREAK = "\\\n"; //$NON-NLS-1$
	public final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$
	public final String OUT_MACRO = "$@"; //$NON-NLS-1$
	public final String SEPARATOR = "/"; //$NON-NLS-1$
	public final String SINGLE_QUOTE = "'"; //$NON-NLS-1$
	public final String DOUBLE_QUOTE = "\""; //$NON-NLS-1$
	public final String TAB = "\t"; //$NON-NLS-1$
	public final String WHITESPACE = " "; //$NON-NLS-1$
	public final String WILDCARD = "%"; //$NON-NLS-1$
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
	}

	public static ManagedCommandLineGenerator getCommandLineGenerator() {
		if (cmdLineGen == null)
			cmdLineGen = new ManagedCommandLineGenerator();
		return cmdLineGen;
	}

	@Override
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool, String commandName, String[] flags,
			String outputFlag, String outputPrefix, String outputName, String[] inputResources,
			String commandLinePattern) {
		// Forward the call to the API implementation
		return new org.eclipse.cdt.managedbuilder.core.ManagedCommandLineGenerator().generateCommandLineInfo(tool,
				commandName, flags, outputFlag, outputPrefix, outputName, inputResources, commandLinePattern);
	}
}
