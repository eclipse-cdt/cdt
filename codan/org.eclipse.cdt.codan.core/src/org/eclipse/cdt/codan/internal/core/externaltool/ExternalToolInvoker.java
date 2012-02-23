/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.externaltool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.externaltool.IArgsSeparator;
import org.eclipse.cdt.codan.core.externaltool.IConsolePrinter;
import org.eclipse.cdt.codan.core.externaltool.IConsolePrinterProvider;
import org.eclipse.cdt.codan.core.externaltool.InvocationFailure;
import org.eclipse.cdt.codan.core.externaltool.InvocationParameters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Invokes an external tool to perform checks on a single file.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public class ExternalToolInvoker {
	private final IConsolePrinterProvider consolePrinterProvider;
	private final ProcessInvoker processInvoker;

	/**
	 * Constructor.
	 * @param consolePrinterProvider creates an Eclipse console that uses the name of an external
	 *        tool as its own.
	 */
	public ExternalToolInvoker(IConsolePrinterProvider consolePrinterProvider) {
		this(consolePrinterProvider, new ProcessInvoker());
	}

	/**
	 * Constructor, for testing purposes only.
	 * @param consolePrinterProvider creates an Eclipse console that uses the name of an external
	 *        tool as its own.
	 * @param processInvoker executes a command in a separate process.
	 */
	public ExternalToolInvoker(IConsolePrinterProvider consolePrinterProvider,
			ProcessInvoker processInvoker) {
		this.consolePrinterProvider = consolePrinterProvider;
		this.processInvoker = processInvoker;
	}

	/**
	 * Invokes an external tool.
	 *
	 * @param parameters the parameters to pass to the external tool executable.
	 * @param configurationSettings user-configurable settings.
	 * @param argsSeparator separates the arguments to pass to the external tool executable. These
	 *        arguments are stored in a single {@code String}.
	 * @param parsers parse the output of the external tool.
	 * @throws InvocationFailure if the external tool reports that it cannot be executed.
	 * @throws Throwable if the external tool cannot be launched.
	 */
	public void invoke(InvocationParameters parameters, ConfigurationSettings configurationSettings,
			IArgsSeparator argsSeparator, List<AbstractOutputParser> parsers)
					throws InvocationFailure, Throwable {
		IPath executablePath = executablePath(configurationSettings);
		String[] args = argsToPass(parameters, configurationSettings, argsSeparator);
		boolean shouldDisplayOutput = configurationSettings.getShouldDisplayOutput().getValue();
		try {
			buildAndLaunchCommand(configurationSettings.getExternalToolName(), executablePath, args,
					parameters.getWorkingDirectory(), shouldDisplayOutput, parsers);
		} finally {
			reset(parsers);
		}
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

	private String[] configuredArgs(ConfigurationSettings configurationSettings,
			IArgsSeparator argsSeparator) {
		String args = configurationSettings.getArgs().getValue();
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

	private void buildAndLaunchCommand(String externalToolName, IPath executablePath, String[] args,
			IPath workingDirectory, boolean shouldDisplayOutput, List<AbstractOutputParser> parsers)
					throws InvocationFailure, Throwable {
		String command = buildCommand(executablePath, args);
		Process process = null;
		IConsolePrinter console =
				consolePrinterProvider.createConsole(externalToolName, shouldDisplayOutput);
		try {
			console.clear();
			console.println(command);
			console.println();
			process = processInvoker.invoke(command, workingDirectory);
			processStream(process.getInputStream(), parsers, console);
			processStream(process.getErrorStream(), parsers, console);
		} finally {
			if (process != null) {
				process.destroy();
			}
			console.close();
		}
	}

	private String buildCommand(IPath executablePath, String[] args) {
		StringBuilder builder = new StringBuilder();
		builder.append(executablePath.toOSString());
		for (String arg : args) {
			builder.append(" ").append(arg); //$NON-NLS-1$
		}
		return builder.toString();
	}

	private void processStream(InputStream inputStream, List<AbstractOutputParser> parsers,
			IConsolePrinter console) throws IOException, InvocationFailure {
		Reader reader = null;
		try {
			reader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				console.println(line);
				for (AbstractOutputParser parser : parsers) {
					if (parser.parse(line)) {
						break;
					}
				}
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {}
			}
		}
	}

	private void reset(List<AbstractOutputParser> parsers) {
		for (AbstractOutputParser parser : parsers) {
			parser.reset();
		}
	}
}
