/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * Utility methods.
 */
public class LaunchUtils {

	/**
	 * For given launch configuration returns the program arguments as 
	 * an array of individual arguments.
	 */
	public static String[] getProgramArgumentsArray(ILaunchConfiguration config) throws CoreException {
		return parseArguments(getProgramArguments(config));
	}

	/**
	 * Returns the program arguments as a String.
	 */
	public static String getProgramArguments(ILaunchConfiguration config) throws CoreException {
		String args = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);
		if (args != null) {
			args = getStringVariableManager().performStringSubstitution(args);
		}
		return args;
	}
	
	/**
	 * @since 6.0
	 */
	public static IBinaryObject getBinary(IProject project, IPath exePath) throws CoreException {
		ICExtensionReference[] parserRef = CCorePlugin.getDefault().getBinaryParserExtensions(project);
		for (int i = 0; i < parserRef.length; i++) {
			try {
				IBinaryParser parser = (IBinaryParser)parserRef[i].createExtension();
				IBinaryObject exe = (IBinaryObject)parser.getBinary(exePath);				
				if (exe != null) {
					return exe;
				}
			} catch (ClassCastException e) {
			} catch (IOException e) {
			}
		}
		IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
		try {
			IBinaryObject exe = (IBinaryObject)parser.getBinary(exePath);
			return exe;
		} catch (ClassCastException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	/**
	 * @since 6.0
	 */
	public static IBinaryObject getBinary(String programName, String projectName)
		throws CoreException
	{
		if (programName != null ) {
			IPath exePath = new Path(programName);
			IProject project = null;
			if (projectName != null && !projectName.equals("")) { //$NON-NLS-1$
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (project == null || project.getLocation() == null)
				{
					return null;
				}
				if (!exePath.isAbsolute()) {
					exePath = project.getLocation().append(exePath);
				}
			}		
			return getBinary(project, exePath);
		}
		return null;
	}

	
	/**
	 * Convenience method.
	 */
	public static IStringVariableManager getStringVariableManager() {
		return VariablesPlugin.getDefault().getStringVariableManager();
	}

	private static String[] parseArguments(String args) {
		return CommandLineUtil.argumentsToArray(args);
	}
	
	/**
	 * @since 6.1
	 */
	@SuppressWarnings("unchecked")
	public static void enableActivity(String activityID, boolean enableit)
	{
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
		IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
		Set<String> enabledActivityIds = new HashSet<String>(activityManager.getEnabledActivityIds());
		boolean changed = false;
		if (enableit)
			changed = enabledActivityIds.add(activityID);		
		else
			changed = enabledActivityIds.remove(activityID);		
		if (changed)
			workbenchActivitySupport.setEnabledActivityIds(enabledActivityIds);
	}

}
