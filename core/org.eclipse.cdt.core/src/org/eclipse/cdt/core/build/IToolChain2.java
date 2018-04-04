/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	
	public Process startBuildProcess(ICBuildConfiguration config, List<String> command, String buildDirectory, IEnvironmentVariable[] envVars,
			IConsole console, IProgressMonitor monitor) throws CoreException, IOException;

}
