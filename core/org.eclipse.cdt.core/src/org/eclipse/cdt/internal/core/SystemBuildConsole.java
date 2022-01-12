/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * CDT console which redirects output to system console ({@code System.out}, {@code System.err}).
 * Used by headless builder.
 *
 */
public class SystemBuildConsole implements IConsole {
	final ConsoleOutputStream out;
	final ConsoleOutputStream err;

	public SystemBuildConsole() {
		out = new ConsoleOutputStream() {
			@Override
			public synchronized void write(byte[] b, int off, int len) throws java.io.IOException {
				System.out.write(b, off, len);
			}

			@Override
			public synchronized void write(int c) throws java.io.IOException {
				System.out.write(c);
			}

			@Override
			public synchronized void write(String msg) throws java.io.IOException {
				System.out.print(msg);
			}
		};
		err = new ConsoleOutputStream() {
			@Override
			public synchronized void write(byte[] b, int off, int len) throws java.io.IOException {
				System.err.write(b, off, len);
			}

			@Override
			public synchronized void write(int c) throws java.io.IOException {
				System.err.write(c);
			}

			@Override
			public synchronized void write(String msg) throws java.io.IOException {
				System.err.print(msg);
			}
		};
	}

	@Override
	public void start(IProject project) {
	}

	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return out;
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return out;
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return err;
	}
}
