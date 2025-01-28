/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * CDT console adaptor interface providing output streams.
 * The adaptor provides the means of access to UI plugin console streams.
 */
public interface IConsole {
	/**
	 * Start the console for a given project.
	 *
	 * @param project - the project to start the console.
	 */
	void start(IProject project);

	/**
	 * Get the stream that shows up as output in the console. This
	 * is typically connected to the output of the build process.
	 */
	ConsoleOutputStream getOutputStream() throws CoreException;

	/**
	 * Get the stream that shows up as information messages in
	 * the console. This is typically not connected to the output
	 * of the build process. Typically information messages, such
	 * as build started and build completed messages are written
	 * to the info stream.
	 *
	 * @apiNote Whether the command line used to launch the process
	 * is written to the info stream or to the output stream is
	 * very inconsistent in CDT's code base. Core Build mostly
	 * uses the info stream for this purpose, but MBS typically
	 * uses output stream.
	 */
	ConsoleOutputStream getInfoStream() throws CoreException;

	/**
	 * Get the stream that shows up as output in the console. This
	 * is typically connected to the error output of the build process
	 * and errors detected when launching the process can be output
	 * to here as well.
	 */
	ConsoleOutputStream getErrorStream() throws CoreException;
}
