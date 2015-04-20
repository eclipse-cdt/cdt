/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

public class TerminalConsoleFactory implements IConsoleFactory {

	@Override
	public void openConsole() {
		final TerminalConsoleSettingsDialog settingsDialog = new TerminalConsoleSettingsDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		if (settingsDialog.open() == Window.OK) {
			openConsole(settingsDialog.getRemoteConnection(), settingsDialog.getEncoding());
		}
	}

	public static void openConsole(final IRemoteConnection connection, final String encoding) {
		new Job(ConsoleMessages.OPENNING_TERMINAL) {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				IRemoteCommandShellService commandShellService = connection.getService(IRemoteCommandShellService.class);
				if (commandShellService == null) {
					return Status.CANCEL_STATUS;
				}

				try {
					if (!connection.isOpen()) {
						connection.open(monitor);
					}

					// TODO, how to handle command shells that are singletons, like serial ports

					// Find the index;
					IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
					IConsole[] consoles = consoleManager.getConsoles();
					boolean[] indices = new boolean[consoles.length];
					for (IConsole console : consoles) {
						if (console instanceof TerminalConsole) {
							TerminalConsole terminalConsole = (TerminalConsole) console;
							if (terminalConsole.getConnection().equals(connection)) {
								indices[terminalConsole.getIndex()] = true;
							}
						}
					}
					int index = 0;
					while (index < indices.length && indices[index]) {
						index++;
					}

					TerminalConsole terminalConsole = new TerminalConsole(connection, index, encoding);
					consoleManager.addConsoles(new IConsole[] { terminalConsole });
					consoleManager.showConsoleView(terminalConsole);

					return Status.OK_STATUS;
				} catch (RemoteConnectionException e) {
					return e.getStatus();
				}
			}
		}.schedule();
	}

}
