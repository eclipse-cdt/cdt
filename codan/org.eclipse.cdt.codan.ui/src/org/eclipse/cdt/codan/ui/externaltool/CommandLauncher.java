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
package org.eclipse.cdt.codan.ui.externaltool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.eclipse.cdt.codan.core.externaltool.ICommandLauncher;
import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.InvocationFailure;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Invokes an external tool command.
 *
 * @author alruiz@google.com (Alex Ruiz)
 * 
 * @since 2.1
 */
public class CommandLauncher implements ICommandLauncher {
	private static final String[] ENVIRONMENT_VARIABLE_SETTINGS = {};
	
	private ConsolePrinterFactory consolePrinterFactory = new ConsolePrinterFactory();
	
	@Override
	public void buildAndLaunchCommand(IProject project, String externalToolName,
			IPath executablePath, String[] args, IPath workingDirectory, boolean shouldDisplayOutput,
			List<AbstractOutputParser> parsers) throws InvocationFailure, Throwable {
		ConsolePrinter consolePrinter = consolePrinter(externalToolName, shouldDisplayOutput);
		String command = buildCommand(executablePath, args);
		Process process = null;
		try {
			consolePrinter.clear();
			consolePrinter.println(command);
			consolePrinter.println();
			try {
				process = invoke(command, workingDirectory);
			} catch (IOException e) {
				throw new InvocationFailure("Unable to start " + externalToolName, e); //$NON-NLS-1$
			}
			processStream(process.getInputStream(), parsers, consolePrinter);
			processStream(process.getErrorStream(), parsers, consolePrinter);
		} finally {
			if (process != null) {
				process.destroy();
			}
			consolePrinter.close();
		}
	}

	private ConsolePrinter consolePrinter(String externalToolName, boolean shouldDisplayOutput) {
		return consolePrinterFactory.createConsolePrinter(externalToolName, shouldDisplayOutput);
	}
	
	private String buildCommand(IPath executablePath, String[] args) {
		StringBuilder builder = new StringBuilder();
		builder.append(executablePath.toOSString());
		for (String arg : args) {
			builder.append(" ").append(arg); //$NON-NLS-1$
		}
		return builder.toString();
	}

	private Process invoke(String command, IPath workingDirectory) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		if (workingDirectory == null) {
			return runtime.exec(command);
		}
		return runtime.exec(command, ENVIRONMENT_VARIABLE_SETTINGS,	workingDirectory.toFile());
	}
	
	private void processStream(InputStream inputStream, List<AbstractOutputParser> parsers, 
			ConsolePrinter consolePrinter) throws IOException, InvocationFailure {
		Reader reader = null;
		try {
			reader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				consolePrinter.println(line);
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
	
	// Visible for testing.
	void setConsolePrinterFactory(ConsolePrinterFactory newVal) {
		this.consolePrinterFactory = newVal;
	}
}
