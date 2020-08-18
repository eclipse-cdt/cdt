/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * This is the Environment Variable Supplier used to supply variables
 * defined by the MBS
 *
 * @since 3.0
 */
public class MbsEnvironmentSupplier implements IEnvironmentVariableSupplier {
	//	private static final String fVariableNames[] = new String[]{
	//		"CWD",	//$NON-NLS-1$
	//		"PWD"	//$NON-NLS-1$
	//	};

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		if (context instanceof IConfiguration)
			return getConfigurationVariable(name, (IConfiguration) context);
		return null;
	}

	public IEnvironmentVariable getConfigurationVariable(String name, IConfiguration configuration) {
		IEnvironmentVariable variable = null;
		if ("CWD".equals(name) || "PWD".equals(name)) { //$NON-NLS-1$	//$NON-NLS-2$
			IResource owner = configuration.getOwner();
			if (owner != null) {
				//				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(owner);
				//				if(info != null && configuration.equals(info.getDefaultConfiguration())){
				//					IManagedBuilderMakefileGenerator generator = ManagedBuildManager.getBuildfileGenerator(configuration);
				//					generator.initialize((IProject)owner,info,null);
				IBuilder builder = configuration.getEditableBuilder();
				IPath topBuildDir = ManagedBuildManager.getBuildLocation(configuration, builder);

				////					IPath topBuildDir = generator.getBuildWorkingDir();
				//					if(topBuildDir == null)
				//						topBuildDir = new Path(configuration.getName());
				//
				//					IPath projectLocation = owner.getLocation();
				//					IPath workingDirectory = projectLocation.append(topBuildDir);
				//					String value = workingDirectory.toOSString();
				if (topBuildDir != null) {
					variable = new EnvironmentVariable(name, topBuildDir.toOSString(),
							IEnvironmentVariable.ENVVAR_REPLACE, null);
				}

				//				}
			}
		}
		return variable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	@Override
	public IEnvironmentVariable[] getVariables(Object context) {
		if (context instanceof IConfiguration) {
			List<IEnvironmentVariable> variables = new ArrayList<>(2);
			IEnvironmentVariable var = getConfigurationVariable("CWD", (IConfiguration) context); //$NON-NLS-1$
			if (var != null) {
				variables.add(var);
				variables
						.add(new EnvironmentVariable("PWD", var.getValue(), IEnvironmentVariable.ENVVAR_REPLACE, null)); //$NON-NLS-1$
			} else {
				return null;
			}
			return variables.toArray(new IEnvironmentVariable[variables.size()]);
		}
		return null;
	}

}
