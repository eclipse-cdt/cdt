/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility to process the output of a build command, feeding it to an error
 * parser monitor and then off to the build console.
 * 
 * @since 6.0
 */
public class BuildCommandRunner {

	private final IProject project;
	private final IConsole console;
	private final ErrorParserManager epm;

	public BuildCommandRunner(IProject project, IConsole console, ErrorParserManager epm) {
		this.project = project;
		this.console = console;
		this.epm = epm;
	}

	public int monitor(Process process) throws CoreException {
		console.start(project);
		epm.setOutputStream(console.getOutputStream());
		new ReaderThread(process.getInputStream()).start();
		new ReaderThread(process.getErrorStream()).start();

		try {
			return process.waitFor();
		} catch (InterruptedException e) {
			return -1;
		}
	}

	private class ReaderThread extends Thread {
		private final BufferedReader in;

		public ReaderThread(InputStream in) {
			this.in = new BufferedReader(new InputStreamReader(in));
		}

		@Override
		public void run() {
			try {
				for (String line = in.readLine(); line != null; line = in.readLine()) {
					// Synchronize to avoid interleaving of lines
					synchronized (BuildCommandRunner.this) {
						epm.processLine(line);
					}
				}
			} catch (IOException e) {
				CCorePlugin.log(e);
			}
		}
	}

}
