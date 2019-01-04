/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Alex Collins (Broadcom Corp.) - choose build config automatically
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.launch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

import com.ibm.icu.text.MessageFormat;

/**
 * Utility methods.
 */
public class LaunchUtils {

	/**
	 * A specialised WrapperProgressMonitor which doesn't let cancellation of the
	 * child build task cause cancellation of our top-level launch task.
	 */
	static class BuildProgressMonitor extends SubProgressMonitor {
		private boolean cancelled;

		public BuildProgressMonitor(IProgressMonitor monitor, int ticks, int style) {
			super(monitor, ticks, style);
		}

		public BuildProgressMonitor(IProgressMonitor monitor, int ticks) {
			this(monitor, ticks, 0);
		}

		@Override
		public void setCanceled(boolean b) {
			// Only cancel this operation, not the top-level launch.
			cancelled = b;
		}

		@Override
		public boolean isCanceled() {
			// Canceled if this monitor has been explicitly canceled
			//  || parent has been canceled.
			return cancelled || super.isCanceled();
		}
	}

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
	 * Return the program path
	 *
	 * @param configuration Launch configuration to obtain paths from
	 * @return the program path
	 * @throws CoreException if program path can not be resolved.
	 */
	public static String getProgramPath(ILaunchConfiguration configuration) throws CoreException {
		return resolveProgramPath(configuration, null);
	}

	/**
	 * Return the program path, resolved as an absolute OS string.
	 *
	 * @param configuration Launch configuration to obtain paths from
	 * @param programName Optional (can be null) starting point for program name
	 * @return the program path
	 * @throws CoreException if program path can not be resolved.
	 */
	public static String resolveProgramPath(ILaunchConfiguration configuration, String programName)
			throws CoreException {
		if (programName == null) {
			programName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String) null);
		}
		if (programName == null) {
			throwException(Messages.LaunchUtils_program_file_not_specified, null,
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROGRAM);
		}
		programName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(programName);
		IPath programPath = new Path(programName);
		if (programPath.isEmpty()) {
			throwException(Messages.LaunchUtils_program_file_does_not_exist, null,
					ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}

		if (!programPath.isAbsolute()) {
			IProject project = getProject(configuration);
			ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);
			if (cproject != null) {
				// Find the specified program within the specified project
				IFile wsProgramPath = cproject.getProject().getFile(programPath);
				programPath = wsProgramPath.getLocation();
			}
		}
		if (!programPath.toFile().exists()) {
			throwException(Messages.LaunchUtils_program_file_does_not_exist,
					new FileNotFoundException(
							MessageFormat.format(Messages.LaunchUtils__0_not_found, programPath.toOSString())),
					ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}

		return programPath.toOSString();
	}

	/**
	 * Return project or <code>null</code> if project is not accessible or not specified.
	 * @param configuration Launch configuration to obtain project from
	 * @return the project
	 * @throws CoreException
	 */
	public static IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				(String) null);
		IProject project = null;
		if (projectName == null) {
			IResource[] resources = configuration.getMappedResources();
			if (resources != null && resources.length > 0 && resources[0] instanceof IProject) {
				project = (IProject) resources[0];
			}
		} else {
			projectName = projectName.trim();
			if (projectName.length() == 0) {
				return null;
			}
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		}

		if (project == null || !project.isAccessible()) {
			// No project
			return null;
		}
		return project;
	}

	/**
	 * @since 6.0
	 */
	public static IBinaryObject getBinary(IProject project, IPath exePath) throws CoreException {
		ICConfigExtensionReference[] parserRef = CCorePlugin.getDefault().getDefaultBinaryParserExtensions(project);
		for (int i = 0; i < parserRef.length; i++) {
			try {
				IBinaryParser parser = CoreModelUtil.getBinaryParser(parserRef[i]);
				IBinaryObject exe = (IBinaryObject) parser.getBinary(exePath);
				if (exe != null) {
					return exe;
				}
			} catch (ClassCastException e) {
			} catch (IOException e) {
			}
		}
		IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
		try {
			IBinaryObject exe = (IBinaryObject) parser.getBinary(exePath);
			return exe;
		} catch (ClassCastException e) {
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * @since 6.0
	 */
	public static IBinaryObject getBinary(String programName, String projectName) throws CoreException {
		if (programName != null) {
			IPath exePath = new Path(programName);
			IProject project = null;
			if (projectName != null && !projectName.isEmpty()) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (project == null || project.getLocation() == null) {
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
	public static void enableActivity(final String activityID, final boolean enableit) {
		if (PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
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
	}

	/**
	 * Get the build configuration that most likely builds the given program path.
	 * The build configuration is chosen as the one that outputs to a directory that contains
	 * the given program.
	 *
	 * @param projectDesc The description for the project in which to search for the configuration.
	 * @param programPath The path to the program to search the build configurations for
	 * @return The build configuration that builds programName; or null if none or more than one were found.
	 * @since 7.0
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

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 *
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	private static void throwException(String message, Throwable exception, int code) throws CoreException {
		MultiStatus status = new MultiStatus(LaunchUIPlugin.PLUGIN_ID, code, message, exception);
		status.add(new Status(IStatus.ERROR, LaunchUIPlugin.PLUGIN_ID, code,
				exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
				exception));
		throw new CoreException(status);
	}
}
