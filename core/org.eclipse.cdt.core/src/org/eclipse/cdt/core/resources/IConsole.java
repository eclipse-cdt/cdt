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

	ConsoleOutputStream getOutputStream() throws CoreException;

	ConsoleOutputStream getInfoStream() throws CoreException;

	ConsoleOutputStream getErrorStream() throws CoreException;
}
