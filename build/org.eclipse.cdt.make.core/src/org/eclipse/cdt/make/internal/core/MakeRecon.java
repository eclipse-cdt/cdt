/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class MakeRecon extends OutputStream {

	IPath make;
	String[] args;
	String[] environ;
	IPath directory;
	IProgressMonitor monitor;
	OutputStream console;
	BufferedReader log;
	StringBuffer currentLine;
	String expectation;

	public MakeRecon(
		IPath buildCommand,
		String[] buildArguments,
		String[] env,
		IPath workingDirectory,
		IProgressMonitor mon,
		OutputStream cos) {
		make = buildCommand;
		if (buildArguments != null) {
			args = new String[buildArguments.length + 1];
			args[0] = "-n";
			System.arraycopy(buildArguments, 0, args, 1, buildArguments.length);
		} else {
			args = new String[] { "-n" };
		}
		environ = env;
		directory = workingDirectory;
		monitor = mon;
		console = cos;
		currentLine = new StringBuffer();
	}

	public void invokeMakeRecon() {
		int i = 0;
		String[] array = new String[args.length + 1];
		array[0] = make.toOSString();
		System.arraycopy(args, 0, array, 1, args.length);
		Process p;
		try {
			p = ProcessFactory.getFactory().exec(array, environ, directory.toFile());
			InputStream in = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			// Swallow the output
			String line;
			StringBuffer sb = new StringBuffer();
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line).append('\n');
					i++;
				}
			} catch (IOException e) {
			}
			try {
				in.close();
			} catch (IOException e) {
			}
			p.destroy();
			log = new BufferedReader(new StringReader(sb.toString()));
		} catch (IOException e1) {
			i = IProgressMonitor.UNKNOWN;
		}
		if (monitor != null) {
			monitor.beginTask("", i);
		}
	}

	/**
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		if (console != null) {
			console.close();
		}
	}

	/**
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		if (console != null) {
			console.flush();
		}
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	public synchronized void write(int b) throws IOException {
		currentLine.append((char) b);
		checkProgress(false);
		if (console != null) {
			console.write(b);
		}
	}

	/**
	 * @see java.io.OutputStream#write(...)
	 */
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off != 0 || (len < 0) || (len > b.length)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		currentLine.append(new String(b, 0, len));
		checkProgress(false);
		if (console != null) {
			console.write(b, off, len);
		}
	}

	private void checkProgress(boolean flush) {
		String buffer = currentLine.toString();
		int i = 0;
		while ((i = buffer.indexOf("\n")) != -1) {
			String line = buffer.substring(0, i).trim(); // get rid of any trailing \r
			processLine(line);
			buffer = buffer.substring(i + 1); // skip the \n and advance
		}
		currentLine.setLength(0);
		if (flush) {
			if (buffer.length() > 0) {
				processLine(buffer);
			}
		} else {
			currentLine.append(buffer);
		}
	}

	private void processLine(String line) {
		if (expectation == null) {
			try {
				expectation = log.readLine();
								if (expectation != null) {
									String show;
									if (expectation.length() > 150) {
										show = expectation.substring(0, 150);
									} else {
										show = expectation;
									}
									monitor.subTask(show);
								}
			} catch (IOException e) {
			}
			if (expectation == null) {
				expectation = "";
			}
		}
		if (expectation.startsWith(line)) {
			expectation = null;
			if (monitor != null) {
				monitor.worked(1);
			}
		}
	}
}
