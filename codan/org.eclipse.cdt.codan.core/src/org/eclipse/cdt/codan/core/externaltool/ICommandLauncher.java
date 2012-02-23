/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.externaltool;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Builds and launches the command necessary to invoke an external tool.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public interface ICommandLauncher {
	/**
	 * Builds and launches the command necessary to invoke an external tool.
	 * @param project the current project.
	 * @param externalToolName the name of the external tool.
	 * @param executablePath the path and name of the external tool executable.
	 * @param args the arguments to pass to the external tool executable.
	 * @param workingDirectory the directory where the external tool should be executed.
	 * @param shouldDisplayOutput indicates whether the output of the external tools should be
	 *        displayed in an Eclipse console.
	 * @param parsers parse the output of the external tool.
	 * @throws InvocationFailure if the external tool reports that it cannot be executed.
	 * @throws Throwable if the external tool cannot be launched.
	 */
	void buildAndLaunchCommand(IProject project, String externalToolName, IPath executablePath,
			String[] args, IPath workingDirectory, boolean shouldDisplayOutput,
			List<AbstractOutputParser> parsers) throws InvocationFailure, Throwable;
}