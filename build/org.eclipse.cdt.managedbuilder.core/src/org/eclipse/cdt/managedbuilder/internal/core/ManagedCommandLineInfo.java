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
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;

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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandLine()
	 */
	@Override
	public String getCommandLine() {
		return this.commandLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandLinePattern()
	 */
	@Override
	public String getCommandLinePattern() {
		return this.commandLinePattern;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandName()
	 */
	@Override
	public String getCommandName() {
		return this.commandName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getFlags()
	 */
	@Override
	public String getFlags() {
		return this.flags;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputFlag()
	 */
	@Override
	public String getOutputFlag() {
		return this.outputFlag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputPrefix()
	 */
	@Override
	public String getOutputPrefix() {
		return this.outputPrefix;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputName()
	 */
	@Override
	public String getOutput() {
		return this.outputName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getInputResources()
	 */
	@Override
	public String getInputs() {
		return this.inputResources;
	}
}
