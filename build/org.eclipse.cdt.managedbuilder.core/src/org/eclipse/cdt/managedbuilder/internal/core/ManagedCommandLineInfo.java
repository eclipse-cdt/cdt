/*******************************************************************************
 * Copyright (c) 2004, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;

public class ManagedCommandLineInfo implements
		IManagedCommandLineInfo {

	private String commandLine;
	private String commandLinePattern;
	private String commandName;
	private String flags;
	private String outputFlag;
	private String outputPrefix;
	private String outputName;
	private String inputResources;
	
	public ManagedCommandLineInfo( String commandLine, String commandLinePattern, String commandName, String flags, String outputFlag, 
			String outputPrefix, String outputName, String inputResources) {
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
	public String getCommandLine() {
		return this.commandLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandLinePattern()
	 */
	public String getCommandLinePattern() {
		return this.commandLinePattern;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandName()
	 */
	public String getCommandName() {
		return this.commandName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getFlags()
	 */
	public String getFlags() {
		return this.flags;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputFlag()
	 */
	public String getOutputFlag() {
		return this.outputFlag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputPrefix()
	 */
	public String getOutputPrefix() {
		return this.outputPrefix;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputName()
	 */
	public String getOutput() {
		return this.outputName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getInputResources()
	 */
	public String getInputs() {
		return this.inputResources;
	}
}
