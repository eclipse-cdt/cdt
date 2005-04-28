/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * This is the Environment Variable Supplier used to supply variables
 * defined by the MBS
 * 
 * @since 3.0
 */
public class MbsEnvironmentSupplier implements IEnvironmentVariableSupplier {
	private static final String fVariableNames[] = new String[]{
		"CWD",	//$NON-NLS-1$
		"PWD"	//$NON-NLS-1$
	};
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariable()
	 */
	public IBuildEnvironmentVariable getVariable(String name, Object context) {
		if(context instanceof IConfiguration)
			return getConfigurationVariable(name,(IConfiguration)context);
		return null;
	}
	
	public IBuildEnvironmentVariable getConfigurationVariable(String name, IConfiguration configuration) {
		IBuildEnvironmentVariable variable = null;
		if("CWD".equals(name) || "PWD".equals(name)){	//$NON-NLS-1$	//$NON-NLS-2$
			IResource owner = configuration.getOwner();
			if(owner != null){
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(owner);
				if(info != null && configuration.equals(info.getDefaultConfiguration())){
					IManagedBuilderMakefileGenerator generator = ManagedBuildManager.getBuildfileGenerator(configuration);
					generator.initialize((IProject)owner,info,null);
					
					IPath topBuildDir = generator.getBuildWorkingDir();
					if(topBuildDir == null)
						topBuildDir = new Path(info.getConfigurationName());

					IPath projectLocation = owner.getLocation();
					IPath workingDirectory = projectLocation.append(topBuildDir);
					String value = workingDirectory.toOSString(); 
					variable = new BuildEnvVar(name,value,IBuildEnvironmentVariable.ENVVAR_REPLACE,null);
				}
			}
		}
		return variable;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier#getVariables()
	 */
	public IBuildEnvironmentVariable[] getVariables(Object context) {
		if(context instanceof IConfiguration){
			List variables = new ArrayList(fVariableNames.length);
			for(int i = 0; i < fVariableNames.length; i++){
				IBuildEnvironmentVariable var = getConfigurationVariable(fVariableNames[i],(IConfiguration)context);
				if(var != null)
					variables.add(var);
			}
			if(variables.size() == 0)
				return null;
			return (IBuildEnvironmentVariable[])variables.toArray(new IBuildEnvironmentVariable[variables.size()]);
		}
		return null;
	}

}
