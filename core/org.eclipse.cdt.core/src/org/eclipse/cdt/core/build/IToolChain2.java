/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial version
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 6.5
 * @author jjohnstn
 *
 */
public interface IToolChain2 {

	public Process startBuildProcess(ICBuildConfiguration config, List<String> command, String buildDirectory,
			IEnvironmentVariable[] envVars, IConsole console, IProgressMonitor monitor)
			throws CoreException, IOException;

}
