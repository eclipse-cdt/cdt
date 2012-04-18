/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.externaltool;

import org.eclipse.cdt.codan.core.cxx.externaltool.ArgsSeparator;
import org.eclipse.cdt.codan.core.cxx.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.cxx.externaltool.InvocationFailure;
import org.eclipse.cdt.codan.core.cxx.externaltool.InvocationParameters;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Invokes an external tool to perform checks on a single file.
 */
public class ExternalToolInvoker {
	private static final String[] ENV = {};
	private static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();

	private final CommandBuilder commandBuilder = new CommandBuilder();

	/**
	 * Invokes an external tool.
	 * @param parameters the parameters to pass to the external tool executable.
	 * @param settings user-configurable settings.
	 * @param argsSeparator separates the arguments to pass to the external tool executable. These
	 *        arguments are stored in a single {@code String}.
	 * @param parsers parse the output of the external tool.
	 * @throws InvocationFailure if the external tool could not be invoked or if the external tool
	 *         itself reports that it cannot be executed (e.g. due to a configuration error).
	 * @throws Throwable if something else goes wrong.
	 */
	public void invoke(InvocationParameters parameters, ConfigurationSettings settings,
			ArgsSeparator argsSeparator, IConsoleParser[] parsers)
			throws InvocationFailure, Throwable {
		Command command = commandBuilder.buildCommand(parameters, settings, argsSeparator);
		try {
			launchCommand(command, parsers, parameters);
		} finally {
			shutDown(parsers);
		}
	}

	private void launchCommand(Command command, IConsoleParser[] parsers,
			InvocationParameters parameters) throws InvocationFailure, CoreException {
		IProject project = parameters.getActualFile().getProject();
		IConsole c = startConsole(project);
		ConsoleOutputSniffer sniffer =
				new ConsoleOutputSniffer(c.getOutputStream(), c.getErrorStream(), parsers);
		ICommandLauncher launcher = commandLauncher(project);
		Process p = launcher.execute(command.getPath(), command.getArgs(), ENV,
				parameters.getWorkingDirectory(), NULL_PROGRESS_MONITOR);
		if (p == null) {
			throw new InvocationFailure("Unable to launch external tool. Cause unknown."); //$NON-NLS-1$
		}
		try {
			p.getOutputStream().close();
		} catch (Throwable ignored) {}
		try {
			launcher.waitAndRead(sniffer.getOutputStream(), sniffer.getErrorStream(), NULL_PROGRESS_MONITOR);
		} finally {
			p.destroy();
		}
	}

	private IConsole startConsole(IProject project) {
		IConsole console = CCorePlugin.getDefault().getConsole();
		console.start(project);
		return console;
	}

	private ICommandLauncher commandLauncher(IProject project) {
		ICommandLauncher launcher = new CommandLauncher();
		launcher.showCommand(true);
		launcher.setProject(project);
		return launcher;
	}

	private void shutDown(IConsoleParser[] parsers) {
		for (IConsoleParser parser : parsers) {
			parser.shutdown();
		}
	}
}
