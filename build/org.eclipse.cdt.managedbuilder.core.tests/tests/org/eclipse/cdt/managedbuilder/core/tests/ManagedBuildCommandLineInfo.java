/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;

/**
 *  Command line info for use with ManagedBuildCommandLineGenerator
 */
public class ManagedBuildCommandLineInfo implements IManagedCommandLineInfo {
	public String commandLine;
	public String commandLinePattern;
	public String commandName;
	public String commandFlags;
	public String commandInputs;
	public String commandOutput;
	public String commandOutputFlag;
	public String commandOutputPrefix;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandLine()
	 */
	public String getCommandLine() {
		return commandLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandLinePattern()
	 */
	public String getCommandLinePattern() {
		return commandLinePattern;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandName()
	 */
	public String getCommandName() {
		return commandName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getFlags()
	 */
	public String getFlags() {
		return commandFlags;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getInputs()
	 */
	public String getInputs() {
		return commandInputs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutput()
	 */
	public String getOutput() {
		return commandOutput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputFlag()
	 */
	public String getOutputFlag() {
		return commandOutputFlag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputPrefix()
	 */
	public String getOutputPrefix() {
		return commandOutputPrefix;
	}

}
