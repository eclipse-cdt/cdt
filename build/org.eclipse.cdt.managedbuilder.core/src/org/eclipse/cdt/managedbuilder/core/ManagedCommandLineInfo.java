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
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * @since 9.2
 */
public class ManagedCommandLineInfo implements IManagedCommandLineInfo {

	private String commandLine;
	private String commandLinePattern;
	private String commandName;
	private String flags;
	private String outputFlag;
	private String outputPrefix;
	private String outputName;
	private String inputResources;

	public ManagedCommandLineInfo(String commandLine, String commandLinePattern, String commandName, String flags,
			String outputFlag, String outputPrefix, String outputName, String inputResources) {
		this.commandLine = commandLine;
		this.commandLinePattern = commandLinePattern;
		this.commandName = commandName;
		this.flags = flags;
		this.outputFlag = outputFlag;
		this.outputPrefix = outputPrefix;
		this.outputName = outputName;
		this.inputResources = inputResources;
	}

	@Override
	public String getCommandLine() {
		return this.commandLine;
	}

	@Override
	public String getCommandLinePattern() {
		return this.commandLinePattern;
	}

	@Override
	public String getCommandName() {
		return this.commandName;
	}

	@Override
	public String getFlags() {
		return this.flags;
	}

	@Override
	public String getOutputFlag() {
		return this.outputFlag;
	}

	@Override
	public String getOutputPrefix() {
		return this.outputPrefix;
	}

	@Override
	public String getOutput() {
		return this.outputName;
	}

	@Override
	public String getInputs() {
		return this.inputResources;
	}
}
