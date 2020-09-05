/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import java.util.Objects;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/** Intercepts output to a console and forwards its error stream to a stream that does error parsing for processing.

 * @author Martin Weber
 *
 */
class CMakeConsoleWrapper implements IConsole {
	private final IConsole delegate;
	private final ConsoleOutputStream err;

	/**
	 * @param delegate
	 * 			the console to wrap
	 * @param parsingConsoleOutputStream
	 *          the replacement of the error output stream of the wrapped console that parses for errors
	 */
	public CMakeConsoleWrapper(IConsole delegate, ConsoleOutputStream parsingConsoleErrOutputStream)
			throws CoreException {
		this.delegate = Objects.requireNonNull(delegate);
		// NOTE: we need one parser for each stream, since the output streams are not synchronized
		// when the process is started via o.e.c.core.CommandLauncher, causing loss of
		// the internal parser state
		err = Objects.requireNonNull(parsingConsoleErrOutputStream);
	}

	@Override
	public void start(IProject project) {
		delegate.start(project);
	}

	@Override
	public ConsoleOutputStream getInfoStream() throws CoreException {
		return delegate.getInfoStream();
	}

	@Override
	public ConsoleOutputStream getOutputStream() throws CoreException {
		return delegate.getOutputStream();
	}

	@Override
	public ConsoleOutputStream getErrorStream() throws CoreException {
		return err;
	}
}
