/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Alex Collins (Broadcom Corp.) - choose build config automatically
 *******************************************************************************/
package org.eclipse.cdt.launch;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IFile;
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
	public static void enableActivity(final String activityID, final boolean enableit)
	{
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
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
		});

	}

	/**
	 * Get the build configuration that most likely builds the given program path.
	 * The build configuration is chosen as the one that outputs to a directory that contains
	 * the given program.
	 * 
	 * @param projectDesc The description for the project in which to search for the configuration.
	 * @param programPath The path to the program to search the build configurations for
	 * @return The build configuration that builds programName; or null if none or more than one were found.
	 * @since 6.2
	 */
	public static ICConfigurationDescription getBuildConfigByProgramPath(IProject project, String programPath) {
		if (project == null || programPath == null)
			return null;
		ICProjectDescription projectDesc = CoreModel.getDefault().getProjectDescription(project, false);
		if (projectDesc == null)
			return null;

		// If the program path is relative, it must be relative to the projects root
		IPath path = new Path(programPath);
		if (!path.isAbsolute()) {
			IPath projLocation = project.getLocation();
			if (projLocation == null)
				return null;
			path = projLocation.append(path);
		}

		// Get all possible files that the program path could refer to
		IFile[] files = ResourceLookup.findFilesForLocation(path);

		// Find the build config whose output directory matches one of the possible files
		ICConfigurationDescription buildConfig = null;
		findCfg: for (ICConfigurationDescription cfgDes : projectDesc.getConfigurations()) {
			CConfigurationData cfgData = cfgDes.getConfigurationData();
			if (cfgData == null)
				continue;
			CBuildData buildData = cfgData.getBuildData();
			if (buildData == null)
				continue;
			for (ICOutputEntry dir : buildData.getOutputDirectories()) {
				ICOutputEntry absoluteDir = CDataUtil.makeAbsolute(project, dir);
				if (absoluteDir == null)
					continue;
				IPath dirLocation = absoluteDir.getLocation();
				if (dirLocation == null)
					continue;
				for (IFile file : files) {
					if (dirLocation.isPrefixOf(file.getLocation())) {
						if (buildConfig != null && buildConfig != cfgDes) {
							// Matched more than one, so use the active configuration
							buildConfig = null;
							break findCfg;
						}
						buildConfig = cfgDes;
					}
				}
			}
		}
		return buildConfig;
	}
}
