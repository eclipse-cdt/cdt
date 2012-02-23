/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.externaltool;

import org.eclipse.cdt.codan.core.externaltool.InvocationFailure;
import org.eclipse.core.runtime.IPath;

import java.io.IOException;

/**
 * Executes a command in a separate process.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ProcessInvoker {
	private static final String[] ENVIRONMENT_VARIABLE_SETTINGS = {};
	private static final String ERROR_FORMAT = "Unable to invoke command '%s'"; //$NON-NLS-1$

	/**
	 * Invokes the given command in a separate process.
	 * @param command the command to execute.
	 * @param workingDirectory the working directory of the process, or {@code null} should inherit
	 *        the working directory of the current process.
	 * @return the process where the given command is being executed.
	 * @throws InvocationFailure if the command fails to be executed.
	 */
	public Process invoke(String command, IPath workingDirectory) throws InvocationFailure {
		try {
			Runtime runtime = Runtime.getRuntime();
			if (workingDirectory == null) {
				return runtime.exec(command);
			}
			return runtime.exec(command, ENVIRONMENT_VARIABLE_SETTINGS, workingDirectory.toFile());
		} catch (IOException e) {
			throw new InvocationFailure(String.format(ERROR_FORMAT, command), e);
		}
	}
}
