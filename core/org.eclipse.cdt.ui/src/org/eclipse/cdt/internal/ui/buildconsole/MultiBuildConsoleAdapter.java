/*******************************************************************************
 * Copyright (c) 2010, 2011 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alex Collins (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.io.IOException;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.IErrorMarkeredOutputStream;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Adapter that wraps a project console and the global console to allow builders
 * to send their build output to a single IConsole object
 */
class MultiBuildConsoleAdapter implements IConsole {

	private final IConsole fProjectConsole;
	private final IConsole fGlobalConsole;

	private static class BuildOutputStreamAdapter extends ConsoleOutputStream implements IErrorMarkeredOutputStream {
		private final BuildOutputStream one;
		private final BuildOutputStream two;

		public BuildOutputStreamAdapter(BuildOutputStream one, BuildOutputStream two) {
			this.one = one;
			this.two = two;
		}

		@Override
		public synchronized String readBuffer() {
			return one.readBuffer();
		}

		@Override
		public synchronized void write(int c) throws IOException {
			one.write(c);
			two.write(c);
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			one.write(b, off, len);
			two.write(b, off, len);
		}

		@Override
		public synchronized void write(String msg) throws IOException {
			one.write(msg);
			two.write(msg);
		}

		@Override
		public void write(String s, ProblemMarkerInfo marker) throws IOException {
			one.write(s, marker);
			two.write(s, marker);
		}

		@Override
		public void flush() throws IOException {
			one.flush();
			two.flush();
		}

		@Override
		public void close() throws IOException {
			one.flush();
			two.flush();
			one.close();
			two.close();
		}

	}

	public MultiBuildConsoleAdapter(IConsole projectConsole, IConsole globalConsole) {
		fProjectConsole = projectConsole;
		fGlobalConsole = globalConsole;
	}

	@Override
	public void start(IProject project) {
		fProjectConsole.start(project);
	}

	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return new BuildOutputStreamAdapter((BuildOutputStream) fProjectConsole.getOutputStream(),
				(BuildOutputStream) fGlobalConsole.getOutputStream());
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return new BuildOutputStreamAdapter((BuildOutputStream) fProjectConsole.getInfoStream(),
				(BuildOutputStream) fGlobalConsole.getInfoStream());
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return new BuildOutputStreamAdapter((BuildOutputStream) fProjectConsole.getErrorStream(),
				(BuildOutputStream) fGlobalConsole.getErrorStream());
	}

}
