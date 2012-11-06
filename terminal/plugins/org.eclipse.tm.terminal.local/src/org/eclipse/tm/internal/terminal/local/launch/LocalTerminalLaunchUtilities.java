/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - [196337] Adapted from org.eclipse.ui.externaltools/ExternalToolsUtil
 * Mirko Raner - [314195] vi editor unusable in tcsh local terminal on Linux RHEL4
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.launch;

import java.io.File;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.internal.terminal.local.LocalTerminalActivator;
import org.eclipse.tm.internal.terminal.local.LocalTerminalMessages;
import org.eclipse.tm.internal.terminal.local.LocalTerminalUtilities;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * The class {@link LocalTerminalLaunchUtilities} provides some utility methods that are used by the
 * {@link LocalTerminalLaunchDelegate}. The class is based on the <code>ExternalToolsUtil</code>
 * class in the <code>org.eclipse.ui.externaltools</code> plug-in. This code had to be duplicated
 * because the original class is not part of the public API of its plug-in.
 *
 * @author Mirko Raner and others
 * @version $Revision: 1.4 $
 */
public class LocalTerminalLaunchUtilities {

	/** The launch configuration attribute for the local echo setting. */
	public final static String ATTR_LOCAL_ECHO = LocalTerminalActivator.PLUGIN_ID +
		".echo"; //$NON-NLS-1$

	/** The launch configuration attribute for the Ctrl-C/SIGINT setting. */
	public final static String ATTR_CTRL_C = LocalTerminalActivator.PLUGIN_ID +
		".sigint"; //$NON-NLS-1$

	/** The launch configuration attribute for the line terminator setting. */
	public final static String ATTR_LINE_SEPARATOR = LocalTerminalActivator.PLUGIN_ID +
		".lineseparator"; //$NON-NLS-1$

	private final static String[] EMPTY = {};
	private final static String STRING = null;
	private final static String TERM = "TERM"; //$NON-NLS-1$
	private final static String ANSI = "ansi"; //$NON-NLS-1$
	private final static Map TERM_ANSI = Collections.singletonMap(TERM, ANSI);

	// These constants were copied from IExternalToolConstants to avoid references to internal API:
	//
	private final static String XT = "org.eclipse.ui.externaltools"; //$NON-NLS-1$;
	private final static String ATTR_LOCATION = XT+".ATTR_LOCATION"; //$NON-NLS-1$
	private final static String ATTR_TOOL_ARGUMENTS = XT+".ATTR_TOOL_ARGUMENTS"; //$NON-NLS-1$
	private final static String ATTR_WORKING_DIRECTORY = XT+".ATTR_WORKING_DIRECTORY"; //$NON-NLS-1$

	private LocalTerminalLaunchUtilities() {

		super();
	}

	/**
	 * Gets the image that should be used for representing the given launch configuration.
	 *
	 * @param configuration the {@link ILaunchConfiguration}
	 * @return an SWT {@link Image} or <code>null</code> if no suitable image was found
	 */
	public static Image getImage(ILaunchConfiguration configuration) {

		String identifier;
		try {

			identifier = configuration.getType().getIdentifier();
		}
		catch (CoreException couldNotDetermineConfigurationType) {

			identifier = null;
			Logger.logException(couldNotDetermineConfigurationType);
		}
		if (identifier != null) {

			return DebugUITools.getImage(identifier);
		}
		return null;
	}

	/**
	 * Expands and returns the location attribute of the given launch configuration. The location is
	 * verified to point to an existing file in the local file system.
	 *
	 * @param configuration the {@link ILaunchConfiguration}
	 * @return an absolute path to a file in the local file system  
	 * @throws CoreException if unable to retrieve the associated launch configuration attribute, or
	 * if unable to resolve any variables, or if the resolved location does not point to an existing
	 * file in the local file system
	 */
	public static IPath getLocation(ILaunchConfiguration configuration) throws CoreException {

		Object[] configurationName = {configuration.getName()};
		String location = configuration.getAttribute(ATTR_LOCATION, STRING);
		if (location == null) {

			abort(NLS.bind(LocalTerminalMessages.locationNotSpecified, configurationName), null, 0);
		}
		String expandedLocation = getStringVariableManager().performStringSubstitution(location);
		if (expandedLocation == null || expandedLocation.length() == 0) {

			abort(NLS.bind(LocalTerminalMessages.invalidLocation, configurationName), null, 0);
		}
		File file = new File(expandedLocation);
		if (!file.isFile()) {

			abort(NLS.bind(LocalTerminalMessages.invalidLocation, configurationName), null, 0);
		} 
		return new Path(expandedLocation);
	}

	/**
	 * Expands and returns the working directory attribute of the given launch configuration.
	 * Returns <code>null</code> if a working directory is not specified. If specified, the working
	 * directory is guaranteed to point to an existing directory in the local file system.
	 *
	 * @param configuration the {@link ILaunchConfiguration}
	 * @return an absolute path to a directory in the local file system, or <code>null</code> if
	 * no working directory was specified
	 * @throws CoreException if unable to retrieve the associated launch  configuration attribute,
	 * or if unable to resolve any variables, or if the resolved location does not point to an
	 * existing directory in the local file system
	 */
	public static IPath getWorkingDirectory(ILaunchConfiguration configuration)
	throws CoreException {

		String location = configuration.getAttribute(ATTR_WORKING_DIRECTORY, STRING);
		if (location != null) {

			String expandedLocation;
			expandedLocation = getStringVariableManager().performStringSubstitution(location);
			if (expandedLocation.length() > 0) {

				File path = new File(expandedLocation);
				if (!path.isDirectory()) {

					Object[] detail = {expandedLocation, configuration.getName()};
					abort(NLS.bind(LocalTerminalMessages.invalidWorkingDirectory, detail), null, 0);
				} 
			}
			return new Path(expandedLocation);
		}
		return null;
	}

	/**
	 * Expands and returns the arguments attribute of the given launch  configuration. Returns
	 * <code>null</code> if arguments were not specified.
	 *
	 * @param configuration the {@link ILaunchConfiguration}
	 * @return an array of resolved arguments, or <code>null</code> if no arguments were specified
	 * @throws CoreException if unable to retrieve the associated launch  configuration attribute,
	 * or if unable to resolve any variables
	 */
	public static String[] getArguments(ILaunchConfiguration configuration) throws CoreException {

		String arguments = configuration.getAttribute(ATTR_TOOL_ARGUMENTS, STRING);
		if (arguments != null) {

			String expanded = getStringVariableManager().performStringSubstitution(arguments);
			return parseStringIntoList(expanded);
		}
		return null;
	}

	/**
	 * Creates an initial default launch configuration for starting a shell if no terminal/program
	 * launch configurations are defined yet.
	 *
	 * @return new {@link ILaunchConfiguration}, or {@code null} if there were already some
	 * terminal/program launch configurations defined
	 */
	public static ILaunchConfiguration createDefaultLaunchConfiguration() {

		ILaunchConfiguration[] configs;
		ILaunchManager manager = LocalTerminalUtilities.LAUNCH_MANAGER;
		try {

			configs = manager.getLaunchConfigurations(LocalTerminalUtilities.TERMINAL_LAUNCH_TYPE);
			if (configs == null || configs.length == 0) {

				// Create a default launch configuration only if there aren't any terminal launch
				// configurations defined at all:
				//
				ILaunchConfigurationWorkingCopy workingCopy;
				workingCopy = createNewLaunchConfigurationWorkingCopy();
				return workingCopy.doSave();
			}
		}
		catch (CoreException exception)
		{
			exception.printStackTrace(); // TODO: implement proper exception handling
		}
		return null;
	}

	/**
	 * Creates an {@link ILaunchConfigurationWorkingCopy} that uses the default shell as its
	 * executable and the user's home directory as the working directory.
	 *
	 * @return an unsaved {@link ILaunchConfigurationWorkingCopy}
	 * @throws CoreException if the {@link ILaunchConfigurationWorkingCopy} could not be
	 * instantiated
	 * @see #getDefaultShell()
	 */
	public static ILaunchConfigurationWorkingCopy createNewLaunchConfigurationWorkingCopy()
	throws CoreException {

		ILaunchConfigurationWorkingCopy workingCopy;
		ILaunchManager manager = LocalTerminalUtilities.LAUNCH_MANAGER;
		String userHome = System.getProperty("user.home", "/"); //$NON-NLS-1$//$NON-NLS-2$
		String defaultShell = getDefaultShell().getAbsolutePath();
		String name = defaultShell.substring(defaultShell.lastIndexOf(File.separator) + 1);
		Format terminalLaunchName = new MessageFormat(LocalTerminalMessages.newTerminalLaunchName);
		name = terminalLaunchName.format(new Object[] {name});
		name = manager.generateLaunchConfigurationName(name);
		workingCopy = LocalTerminalUtilities.TERMINAL_LAUNCH_TYPE.newInstance(null, name);
		workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap(TERM_ANSI));
		workingCopy.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		workingCopy.setAttribute(ATTR_LOCATION, defaultShell);
		workingCopy.setAttribute(ATTR_WORKING_DIRECTORY, userHome);
		workingCopy.setAttribute(ATTR_LOCAL_ECHO, runningOnWindows());
		return workingCopy;
	}

	/**
	 * Returns the system's default shell. First, this method will read the value of the environment
	 * variable {@code SHELL}. If that variable is not set, it will default to {@code cmd.exe} on
	 * Windows systems, and to {@code /bin/sh} on all other systems.
	 *
	 * @return a {@link File} pointing to the default shell (the underlying file is not guaranteed
	 * to exist in the file system)
	 */
	public static File getDefaultShell() {

		String shell = System.getenv("SHELL"); //$NON-NLS-1$
		if (shell == null) {

			if (runningOnWindows()) {

				shell = "C:\\Windows\\System32\\cmd.exe"; //$NON-NLS-1$
			}
			else {

				shell = "/bin/sh"; //$NON-NLS-1$
			}
		}
		return new File(shell);
	}

	//------------------------------------- PRIVATE SECTION --------------------------------------//

	private static boolean runningOnWindows() {

		return Platform.OS_WIN32.equals(Platform.getOS());
	}

	private static IStringVariableManager getStringVariableManager() {

		return VariablesPlugin.getDefault().getStringVariableManager();
	}

	private static String[] parseStringIntoList(String arguments) {

		if (arguments == null || arguments.length() == 0) {

			return EMPTY;
		}
		return DebugPlugin.parseArguments(arguments);
	}   

	private static void abort(String text, Throwable exception, int code) throws CoreException {

		Status status;
		status = new Status(IStatus.ERROR, LocalTerminalActivator.PLUGIN_ID, code, text, exception);
		throw new CoreException(status);
	}
}
