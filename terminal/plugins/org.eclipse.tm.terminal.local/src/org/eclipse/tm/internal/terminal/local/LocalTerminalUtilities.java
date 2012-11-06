/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.internal.terminal.local.launch.LocalTerminalLaunchDelegate;
import org.eclipse.tm.internal.terminal.local.launch.LocalTerminalLaunchUtilities;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * The class {@link LocalTerminalUtilities} is a collection of commonly used constants and utility
 * methods.
 *
 * @author Mirko Raner
 * @version $Revision: 1.2 $
 */
public class LocalTerminalUtilities {

	private static String NULL = null;
	private static String LOCAL_TERMINAL = LocalTerminalLaunchDelegate.LAUNCH_CONFIGURATION_TYPE_ID;

	/** The name of the line separator system property (i.e., "<code>line.separator</code>"). */
	public final static String LINE_SEPARATOR_PROPERTY = "line.separator"; //$NON-NLS-1$

	/** The line separator CRLF (i.e., "<code>\r\n</code>"). */
	public final static String CRLF = "\r\n"; //$NON-NLS-1$

	/** The line separator CR (i.e., "<code>\r</code>"). */
	public final static String CR = "\r"; //$NON-NLS-1$

	/** The line separator LF (i.e., "<code>\n</code>"). */
	public final static String LF = "\n"; //$NON-NLS-1$

	private LocalTerminalUtilities() {

		super();
	}

	/** The {@link DebugPlugin}'s {@link ILaunchManager} instance. */
	public final static ILaunchManager LAUNCH_MANAGER = DebugPlugin.getDefault().getLaunchManager();

	/**
	 * The {@link ILaunchConfigurationType} for "Terminal" launches (in the "External Tools"
	 * category).
	 */
	public final static ILaunchConfigurationType TERMINAL_LAUNCH_TYPE =
		LAUNCH_MANAGER.getLaunchConfigurationType(LOCAL_TERMINAL);

	/**
	 * Finds a launch configuration by its name.
	 *
	 * @param name the name of the launch configuration
	 * @return the corresponding {@link ILaunchConfiguration} object or <code>null</code> if the
	 * configuration could not be found
	 * @throws CoreException if there was a general problem accessing launch configurations
	 */
	public static ILaunchConfiguration findLaunchConfiguration(String name) throws CoreException {

		ILaunchConfiguration[] configuration;
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		configuration = manager.getLaunchConfigurations(LocalTerminalUtilities.TERMINAL_LAUNCH_TYPE);
		int numberOfConfigurations = configuration.length;
		for (int index = 0; index < numberOfConfigurations; index++) {

			if (configuration[index].getName().equals(name)) {

				return configuration[index];
			}
		}
		String error = NLS.bind(LocalTerminalMessages.noSuchLaunchConfiguration, name);
		throw new CoreException(new Status(IStatus.ERROR, LocalTerminalActivator.PLUGIN_ID, error));
	}

	/**
	 * Gets the local echo setting that is stored in the launch configuration for the given
	 * {@link ILocalTerminalSettings}.
	 *
	 * @param settings the {@link ILocalTerminalSettings}
	 * @return <code>true</code> for local echo enabled, <code>false</code> otherwise
	 */
	public static boolean getLocalEcho(ILocalTerminalSettings settings) {

		return getBooleanSetting(settings, LocalTerminalLaunchUtilities.ATTR_LOCAL_ECHO);
	}

	/**
	 * Gets the Ctrl-C/SIGINT setting that is stored in the launch configuration for the given
	 * {@link ILocalTerminalSettings}.
	 *
	 * @param settings the {@link ILocalTerminalSettings}
	 * @return <code>true</code> if sending SIGINT for Ctrl-C is enabled,
	 * <code>false</code> otherwise
	 */
	public static boolean getCtrlC(ILocalTerminalSettings settings) {

		return getBooleanSetting(settings, LocalTerminalLaunchUtilities.ATTR_CTRL_C);
	}

	/**
	 * Gets the line separator setting that is stored in the launch configuration for the given
	 * {@link ILocalTerminalSettings}.
	 *
	 * @param settings the {@link ILocalTerminalSettings}
	 * @return {@link ILocalTerminalSettings#LINE_SEPARATOR_LF},
	 * {@link ILocalTerminalSettings#LINE_SEPARATOR_CRLF},
	 * {@link ILocalTerminalSettings#LINE_SEPARATOR_CR}, or <code>null</code> for the platform's
	 * default line separator
	 */
	public static String getLineSeparator(ILocalTerminalSettings settings) {

		String configurationName = settings.getLaunchConfigurationName();
		try {

			String ls;
			ILaunchConfiguration configuration = findLaunchConfiguration(configurationName);
			ls = configuration.getAttribute(LocalTerminalLaunchUtilities.ATTR_LINE_SEPARATOR, NULL);
			return ls;
		}
		catch (CoreException exception) {

			Logger.logException(exception);
			return null;
		}
	}

	//------------------------------------- PRIVATE SECTION --------------------------------------//

	private static boolean getBooleanSetting(ILocalTerminalSettings settings, String attribute) {

		String configurationName = settings.getLaunchConfigurationName();
		try {

			ILaunchConfiguration configuration = findLaunchConfiguration(configurationName);
			return configuration.getAttribute(attribute, false);
		}
		catch (CoreException exception) {

			Logger.logException(exception);
			return false;
		}
	}
}
