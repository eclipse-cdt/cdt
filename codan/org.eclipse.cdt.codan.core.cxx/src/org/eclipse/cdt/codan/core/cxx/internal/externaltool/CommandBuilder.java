/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.externaltool;

import java.io.File;

import org.eclipse.cdt.codan.core.cxx.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.cxx.externaltool.IArgsSeparator;
import org.eclipse.cdt.codan.core.cxx.externaltool.InvocationParameters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Creates the command to use to invoke an external tool.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
class CommandBuilder {
	Command buildCommand(InvocationParameters parameters, ConfigurationSettings settings,
			IArgsSeparator argsSeparator) {
		IPath executablePath = executablePath(settings);
		String[] args = argsToPass(parameters, settings, argsSeparator);
		return new Command(executablePath, args);
	}

	private IPath executablePath(ConfigurationSettings configurationSettings) {
		File executablePath = configurationSettings.getPath().getValue();
		return new Path(executablePath.toString());
	}

	private String[] argsToPass(InvocationParameters parameters,
			ConfigurationSettings configurationSettings, IArgsSeparator argsSeparator) {
		String actualFilePath = parameters.getActualFilePath();
		String[] args = configuredArgs(configurationSettings, argsSeparator);
		return addFilePathToArgs(actualFilePath, args);
	}

	private String[] configuredArgs(ConfigurationSettings settings, IArgsSeparator argsSeparator) {
		String args = settings.getArgs().getValue();
		return argsSeparator.separateArgs(args);
	}

	private String[] addFilePathToArgs(String actualFilePath, String[] configuredArgs) {
		int argCount = configuredArgs.length;
		String[] allArgs = new String[argCount + 1];
		// alruiz: Arrays.copyOf leaves empty cells at the end. We need an empty cell at the
		// beginning of the array.
		System.arraycopy(configuredArgs, 0, allArgs, 1, argCount);
		// add file to process as the first argument
		allArgs[0] = actualFilePath;
		return allArgs;
	}
}