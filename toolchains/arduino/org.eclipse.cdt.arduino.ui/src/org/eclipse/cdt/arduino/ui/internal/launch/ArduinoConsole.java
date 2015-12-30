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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.eclipse.cdt.arduino.core.internal.console.ArduinoConsoleParser;
import org.eclipse.cdt.arduino.core.internal.console.ArduinoConsoleService;
import org.eclipse.cdt.arduino.core.internal.console.ArduinoErrorParser;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ArduinoConsole implements ArduinoConsoleService, IResourceChangeListener {

	private static MessageConsole console;
	private static MessageConsoleStream out;
	private static MessageConsoleStream err;

	private IFolder buildDirectory;
	List<ArduinoPatternMatchListener> listeners = new ArrayList<>();

	public ArduinoConsole() {
		if (console == null) {
			console = new MessageConsole(Messages.ArduinoLaunchConsole_0, null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
			out = console.newMessageStream();
			err = console.newMessageStream();

			// set the colors
			final Display display = Display.getDefault();
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					out.setColor(display.getSystemColor(SWT.COLOR_BLACK));
					err.setColor(display.getSystemColor(SWT.COLOR_RED));
				}
			});
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_BUILD);
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		switch (event.getType()) {
		case IResourceChangeEvent.PRE_BUILD:
			if (event.getBuildKind() != IncrementalProjectBuilder.AUTO_BUILD) {
				// TODO this really should be done from the core and only when
				// our projects are being built
				console.clearConsole();
			}
			break;
		}
	}

	public IFolder getBuildDirectory() {
		return buildDirectory;
	}

	@Override
	public void monitor(final Process process, ArduinoConsoleParser[] consoleParsers, IFolder buildDirectory)
			throws IOException {
		this.buildDirectory = buildDirectory;

		// Clear the old listeners
		for (ArduinoPatternMatchListener listener : listeners) {
			console.removePatternMatchListener(listener);
		}
		listeners.clear();

		// Add in the new ones if any
		if (consoleParsers != null) {
			for (ArduinoConsoleParser parser : consoleParsers) {
				ArduinoPatternMatchListener listener;
				if (parser instanceof ArduinoErrorParser) {
					listener = new ArduinoErrorMatchListener(this, (ArduinoErrorParser) parser);
				} else {
					continue;
				}
				listeners.add(listener);
				console.addPatternMatchListener(listener);
			}
		}

		console.activate();

		final Semaphore sema = new Semaphore(-1);

		// Output stream reader
		new Thread(Messages.ArduinoLaunchConsole_2) {
			public void run() {
				try (BufferedReader processOut = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					for (String line = processOut.readLine(); line != null; line = processOut.readLine()) {
						out.write(line);
						out.write('\n');
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					sema.release();
				}
			}
		}.start();

		// Error stream reader
		new Thread(Messages.ArduinoLaunchConsole_2) {
			public void run() {
				try (BufferedReader processErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
					for (String line = processErr.readLine(); line != null; line = processErr.readLine()) {
						err.write(line);
						out.write('\n');
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					sema.release();
				}
			}
		}.start();

		try {
			sema.acquire();
			process.waitFor();
		} catch (InterruptedException e) {
			Activator.log(e);
		}
	}

	@Override
	public void writeOutput(String msg) throws IOException {
		out.write(msg);
	}

	@Override
	public void writeError(String msg) throws IOException {
		err.write(msg);
	}

}
