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
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;

/**
 *  Test command line generator
 */
public class ManagedBuildCommandLineGenerator implements IManagedCommandLineGenerator {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator#generateCommandLineInfo(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool, String commandName, String[] flags,
			String outputFlag, String outputPrefix, String outputName, String[] inputResources,
			String commandLinePattern) {
		ManagedBuildCommandLineInfo info = new ManagedBuildCommandLineInfo();
		//  Concatenate the tool name and the passed in command name
		info.commandName = tool.getName() + commandName;
		//  Put out the flags backwards
		String myflags = ""; //$NON-NLS-1$
		for (int i = flags.length - 1; i >= 0; i--) {
			if (i < flags.length - 1)
				myflags += " ";
			myflags += flags[i];
		}
		info.commandFlags = myflags;
		//  Alphabetize the inputs and add foo.cpp
		String[] inputs = new String[inputResources.length + 1];
		String myinputs = ""; //$NON-NLS-1$
		for (int i = 0; i < inputResources.length; i++) {
			inputs[i] = inputResources[i];
		}
		inputs[inputResources.length] = "foo.cpp";
		//  Sort
		for (int i = 0; i < inputs.length; i++) {
			for (int j = 1; j < inputs.length; j++) {
				if (inputs[j].compareTo(inputs[j - 1]) < 0) {
					String temp = inputs[j - 1];
					inputs[j - 1] = inputs[j];
					inputs[j] = temp;
				}
			}
		}
		for (int i = 0; i < inputs.length; i++) {
			if (i > 0)
				myinputs += " ";
			myinputs += inputs[i];
		}
		info.commandInputs = myinputs;
		// Don't change the command line pattern
		info.commandLinePattern = commandLinePattern;
		// Config artifact name
		info.commandOutput = ((IToolChain) tool.getParent()).getParent().getArtifactName();
		// -Oh
		info.commandOutputFlag = "-0h";
		// ""
		info.commandOutputPrefix = "";
		// "This is a test command line"
		info.commandLine = "This is a test command line";
		return info;
	}

}
