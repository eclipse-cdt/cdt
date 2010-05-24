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

import org.eclipse.osgi.util.NLS;

/**
 * The class {@link LocalTerminalMessages} provides localization keys to internationalized display
 * messages used by the Local Terminal Connector.
 *
 * @author Mirko Raner
 * @version $Revision: 1.3 $
 */
public class LocalTerminalMessages extends NLS {

	static {

		NLS.initializeMessages(LocalTerminalMessages.class.getName(), LocalTerminalMessages.class);
	}

	private LocalTerminalMessages() {

		super();
	}

	/** The title for the launch configuration selection: "Launch configuration:". */
	public static String launchConfiguration;

	/** The label for the "New..." button. */
	public static String labelNew;

	/** The label for the "Edit..." button. */
	public static String labelEdit;

	/** The label for the "Delete" button. */
	public static String labelDelete;

	/** The label for the "Enable terminal echo" check box. */
	public static String enableLocalEcho;

	/** The label for the "Send SIGINT when Ctrl-C is pressed" check box. */
	public static String sendInterruptOnCtrlC;

	/** The line separator option "LF". */
	public static String lineSeparatorLF;

	/** The line separator option "CR". */
	public static String lineSeparatorCR;

	/** The line separator option "CRLF". */
	public static String lineSeparatorCRLF;

	/** The default line separator option. */
	public static String lineSeparatorDefault;

	/** The base name for new launch configurations. */
	public static String newTerminalLaunchName;

	/** The error message to be issued if a launch configuration could not be found. */
	public static String noSuchLaunchConfiguration;

	/** The message displayed while launching a configuration. */
	public static String launchingConfiguration;

	/** The error message displayed when process creation failed. */
	public static String couldNotCreateIProcess;

	/** The error message for a missing executable path. */
	public static String locationNotSpecified;

	/** The error message for a specified but invalid executable path. */
	public static String invalidLocation;

	/** The error message for an invalid working directory location. */
	public static String invalidWorkingDirectory;

	/** The question message for confirming deletion of a launch configuration. */
	public static String questionDeleteLaunchConfiguration;

	/** The question title for confirming deletion of a launch configuration. */
	public static String questionTitleDeleteLaunchConfiguration;

	/** The error message for attempting to directly launch a Terminal launch configuration. */
	public static String errorDirectLaunch;

	/** The error message for attempting to launch a no longer existing launch configuration. */
	public static String errorLaunchConfigurationNoLongerExists;

	/** The error dialog title for failed terminal connections. */
	public static String errorTitleCouldNotConnectToTerminal;

	/** The title string of the warning displayed when terminal launches are still running. */
	public static String warningTitleTerminalsStillRunning;

	/** The warning message displayed when terminal launches are still running. */
	public static String warningMessageTerminalsStillRunning;

	/** The label for the button that quits the workbench anyway. */
	public static String quitWorkbenchAnyway;

	/** The label for the button that vetoes a shutdown of the workbench. */
	public static String doNoQuitWorkbench;

	/** The label for a terminal process that was terminated during workbench shut-down. */
	public static String terminatedProcess;

	/** The name of the launch configuration tab for terminal settings. */
	public static String terminalTabName;

	/** The group label for the terminal settings on the launch configuration page. */
	public static String terminalSettings;
}
