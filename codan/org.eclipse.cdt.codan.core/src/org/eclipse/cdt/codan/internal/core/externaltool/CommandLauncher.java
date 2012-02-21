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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.IConsolePrinter;
import org.eclipse.cdt.codan.core.externaltool.IConsolePrinterProvider;
import org.eclipse.cdt.codan.core.externaltool.InvocationFailure;
import org.eclipse.core.runtime.IPath;

/**
 * Invokes an external tool command.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public class CommandLauncher {
	private final IConsolePrinterProvider consolePrinterProvider;
	private final ProcessInvoker processInvoker;

	/**
	 * Constructor.
	 * @param consolePrinterProvider creates an Eclipse console that uses the name of an external
	 *        tool as its own.
	 */
	public CommandLauncher(IConsolePrinterProvider consolePrinterProvider) {
		this(consolePrinterProvider, new ProcessInvoker());
	}

	/**
	 * Constructor.
	 * @param consolePrinterProvider creates an Eclipse console that uses the name of an external
	 *        tool as its own.
	 * @param processInvoker executes a command in a separate process.
	 */
	public CommandLauncher(IConsolePrinterProvider consolePrinterProvider,
			ProcessInvoker processInvoker) {
		this.consolePrinterProvider = consolePrinterProvider;
		this.processInvoker = processInvoker;
	}

	/**
	 * Builds and launches the command necessary to invoke an external tool.
	 * @param externalToolName the name of the external tool.
	 * @param executablePath the path and name of the external tool executable.
	 * @param args the arguments to pass to the external tool executable.
	 * @param workingDirectory the directory where the external tool should be executed.
	 * @param shouldDisplayOutput indicates whether the output of the external tools should be
	 *        displayed in an Eclipse console.
	 * @param parsers parse the output of the external tool.
	 * @throws InvocationFailure if the external tool cannot be executed.
	 * @throws Throwable if the external tool cannot be launched.
	 */
	public void buildAndLaunchCommand(String externalToolName, IPath executablePath, String[] args,
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
}
