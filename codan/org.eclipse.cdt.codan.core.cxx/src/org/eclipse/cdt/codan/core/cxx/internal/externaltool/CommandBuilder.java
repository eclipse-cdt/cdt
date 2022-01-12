/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.externaltool;

import java.io.File;

import org.eclipse.cdt.codan.core.cxx.externaltool.ArgsSeparator;
import org.eclipse.cdt.codan.core.cxx.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.cxx.externaltool.InvocationParameters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Creates the command to use to invoke an external tool.
 */
class CommandBuilder {
	Command buildCommand(InvocationParameters parameters, ConfigurationSettings settings, ArgsSeparator argsSeparator) {
		IPath executablePath = executablePath(settings);
		String[] args = argsToPass(parameters, settings, argsSeparator);
		return new Command(executablePath, args);
	}

	private IPath executablePath(ConfigurationSettings configurationSettings) {
		File executablePath = configurationSettings.getPath().getValue();
		return new Path(executablePath.toString());
	}

	private String[] argsToPass(InvocationParameters parameters, ConfigurationSettings configurationSettings,
			ArgsSeparator argsSeparator) {
		String actualFilePath = parameters.getActualFilePath();
		String[] args = configuredArgs(configurationSettings, argsSeparator);
		return addFilePathToArgs(actualFilePath, args);
	}

	private String[] configuredArgs(ConfigurationSettings settings, ArgsSeparator argsSeparator) {
		String args = settings.getArgs().getValue();
		return argsSeparator.splitArguments(args);
	}

	private String[] addFilePathToArgs(String actualFilePath, String[] configuredArgs) {
		int argCount = configuredArgs.length;
		String[] allArgs = new String[argCount + 1];
		allArgs[0] = actualFilePath;
		// Copy arguments
		System.arraycopy(configuredArgs, 0, allArgs, 1, argCount);
		return allArgs;
	}
}