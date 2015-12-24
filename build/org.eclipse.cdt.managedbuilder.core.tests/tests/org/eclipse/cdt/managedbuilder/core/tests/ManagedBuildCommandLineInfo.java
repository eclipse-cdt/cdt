/*******************************************************************************
 * Copyright (c) 2004, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
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
	@Override
	public String getCommandLine() {
		return commandLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandLinePattern()
	 */
	@Override
	public String getCommandLinePattern() {
		return commandLinePattern;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getCommandName()
	 */
	@Override
	public String getCommandName() {
		return commandName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getFlags()
	 */
	@Override
	public String getFlags() {
		return commandFlags;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getInputs()
	 */
	@Override
	public String getInputs() {
		return commandInputs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutput()
	 */
	@Override
	public String getOutput() {
		return commandOutput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputFlag()
	 */
	@Override
	public String getOutputFlag() {
		return commandOutputFlag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo#getOutputPrefix()
	 */
	@Override
	public String getOutputPrefix() {
		return commandOutputPrefix;
	}

}
