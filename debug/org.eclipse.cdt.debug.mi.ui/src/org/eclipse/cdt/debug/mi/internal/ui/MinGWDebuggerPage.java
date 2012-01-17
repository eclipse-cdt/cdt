/**********************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.debug.mi.internal.ui;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;



/**
 * @author Doug Schaefer
 *
 */
public class MinGWDebuggerPage extends StandardGDBDebuggerPage {

	@Override
	protected String defaultGdbCommand(ILaunchConfiguration configuration) {
		// Lets look it up in the project
		try {
			String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	        	ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project);
	        	ICConfigurationDescription configDesc = projDesc.getActiveConfiguration();
	        	IEnvironmentVariableManager envVarMgr = CCorePlugin.getDefault().getBuildEnvironmentManager();
	        	IEnvironmentVariable pathvar = envVarMgr.getVariable("PATH", configDesc, true); //$NON-NLS-1$
	        	if(pathvar != null)
	        	{
		        	String path = pathvar.getValue();
		        	String[] dirs = path.split(pathvar.getDelimiter());
		        	for (int i = 0; i < dirs.length; ++i) {
		        		IPath gdbPath = new Path(dirs[i]).append("gdb.exe"); //$NON-NLS-1$
	        			File gdbFile = gdbPath.toFile();
	        			if (gdbFile.exists())
	        				return gdbPath.toOSString();
		        	}	
	        	}
			}
		} catch (CoreException e) {
		}
		
		return super.defaultGdbCommand(configuration);
	}
	
}
