/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.launch;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.arduino.core.internal.ArduinoLaunchConsoleService;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.progress.UIJob;

public class ArduinoLaunchConsole implements ArduinoLaunchConsoleService {

	private static MessageConsole console;

	public ArduinoLaunchConsole() {
		if (console == null) {
			console = new MessageConsole(Messages.ArduinoLaunchConsole_0, null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		}
	}

	@Override
	public void monitor(final Process process) {
		console.clearConsole();
		console.activate();

		new UIJob(Messages.ArduinoLaunchConsole_1) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				final IOConsoleOutputStream out = console.newOutputStream();
				out.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				new Thread(Messages.ArduinoLaunchConsole_2) {
					public void run() {
						try (InputStream processOut = process.getInputStream()) {
							for (int c = processOut.read(); c >= 0; c = processOut.read()) {
								out.write(c);
							}
						} catch (IOException e) {
							// Nothing. Just exit
						}
					}
				}.start();

				final IOConsoleOutputStream err = console.newOutputStream();
				err.setColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				new Thread(Messages.ArduinoLaunchConsole_3) {
					public void run() {
						try (InputStream processErr = process.getErrorStream()) {
							for (int c = processErr.read(); c >= 0; c = processErr.read()) {
								err.write(c);
							}
						} catch (IOException e) {
							// Nothing. Just exit
						}
					}
				}.start();

				return Status.OK_STATUS;
			}
		}.schedule();
	}

}
