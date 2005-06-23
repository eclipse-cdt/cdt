/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

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
	MyList log;
	StringBuffer currentLine;

	class MyList extends ArrayList {
		public void removeInterval (int start, int len) {
			removeRange(start, len);
		}
	}

	public MakeRecon(IPath buildCommand, String[] buildArguments,
		String[] env, IPath workingDirectory, IProgressMonitor mon, OutputStream cos) {
		this(buildCommand, new String[]{"-n"}, buildArguments, env, workingDirectory, mon, cos); //$NON-NLS-1$
	}

	public MakeRecon(IPath buildCommand, String[] options, String[] buildArguments,
		String[] env, IPath workingDirectory, IProgressMonitor mon, OutputStream cos) {

		make = buildCommand;

		args = new String[0];
		if (options != null) {
			String[]array = new String[args.length + options.length];
			System.arraycopy(args, 0, array, 0, args.length);
			System.arraycopy(options, 0, array, args.length, options.length);
			args = array;
		}
		if (buildArguments != null) {
			String[] array = new String[args.length + buildArguments.length];
			System.arraycopy(args, 0, array, 0, args.length);
			System.arraycopy(buildArguments, 0, array, args.length, buildArguments.length);
			args = array;
		}

		environ = env;
		directory = workingDirectory;
		monitor = mon;
		console = cos;
		currentLine = new StringBuffer();
		log = new MyList();

		// Get the buffer log.
		invokeMakeRecon();

	}

	private void invokeMakeRecon() {
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
			try {
				while ((line = reader.readLine()) != null) {
					log.add(line);
					i++;
				}
			} catch (IOException e) {
			}
			try {
				in.close();
			} catch (IOException e) {
			}
			p.destroy();
			log.trimToSize();
		} catch (IOException e1) {
			i = IProgressMonitor.UNKNOWN;
		}
		monitor.beginTask("", i); //$NON-NLS-1$
	}

	/**
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		if (console != null) {
			console.close();
		}
		monitor.done();
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
		while ((i = buffer.indexOf("\n")) != -1) { //$NON-NLS-1$
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
		int found = -1;
		for (int i = 0; i < log.size(); i++) {
			String s = (String)log.get(i);
			if (s.startsWith(line)) {
				found = i;
				break;
			}
		}

		if (found != -1) {
			String show = (String)log.get(found);
			if (show.length() > 50) {
				show = show.substring(0, 50);
			}
			monitor.subTask(show);
			monitor.worked(found + 1);
			log.removeInterval(0, found + 1);
		} 
	}
}
