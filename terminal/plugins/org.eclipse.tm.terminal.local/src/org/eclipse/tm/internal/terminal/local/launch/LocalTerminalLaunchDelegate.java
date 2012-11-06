/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - [196337] Adapted from org.eclipse.ui.externaltools/ProgramLaunchDelegate
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.launch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.internal.terminal.local.LocalTerminalActivator;
import org.eclipse.tm.internal.terminal.local.LocalTerminalMessages;
import org.eclipse.tm.internal.terminal.local.LocalTerminalUtilities;
import org.eclipse.tm.internal.terminal.local.process.LocalTerminalProcess;
import org.eclipse.tm.internal.terminal.local.process.LocalTerminalProcessFactory;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.ui.PlatformUI;

/**
 * The class {@link LocalTerminalLaunchDelegate} provides a launch configuration delegate for local
 * terminal launches. It is based on the <code>ProgramLaunchDelegate</code> class in the
 * <code>org.eclipse.ui.externaltools</code> plug-in. In contrast to the original class,
 * {@link LocalTerminalLaunchDelegate} creates its low-level {@link Process} object using the CDT
 * {@link ProcessFactory}, which allows the process to run with a pseudo-terminal ({@link PTY}).
 *
 * @author Mirko Raner and others
 * @version $Revision: 1.4 $
 */
public class LocalTerminalLaunchDelegate extends LaunchConfigurationDelegate {

	private final static String EMPTY = ""; //$NON-NLS-1$
	private final static String PLUGIN_ID = LocalTerminalActivator.PLUGIN_ID;

	/**
	 * The launch configuration type ID for terminal launches.
	 */
	public final static String LAUNCH_CONFIGURATION_TYPE_ID = PLUGIN_ID + ".launch"; //$NON-NLS-1$

	private static LocalTerminalStillRunningListener workbenchCloseListener;

	/**
	 * Creates a new {@link LocalTerminalLaunchDelegate}.
	 */
	public LocalTerminalLaunchDelegate() {

		super();
	}

	/**
	 * Launches a new Local Terminal configuration in the specified mode. The launch object has
	 * already been registered with the launch manager.
	 *
	 * @param configuration the {@link ILaunchConfiguration} to launch
	 * @param mode the mode in which to launch; currently, Local Terminal launches only support the
	 * mode {@link org.eclipse.debug.core.ILaunchManager#RUN_MODE}
	 * @param progressMonitor an {@link IProgressMonitor}, or <code>null</code> for no progress
	 * monitoring
	 * @param launch the {@link ILaunch} object
	 * @exception CoreException if launching fails
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
	IProgressMonitor progressMonitor) throws CoreException {

		String processFactoryID;
		processFactoryID = configuration.getAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, EMPTY);
		if (!LocalTerminalProcessFactory.ID.equals(processFactoryID)) {

			// This launch was not launched via the terminal connector UI but via the launch dialog;
			// the launch needs to be explicitly connected to a terminal (otherwise it will appear
			// in the regular console), so launching from the launch dialog or from the launch
			// history is not supported right now.
			//
			String message = LocalTerminalMessages.errorDirectLaunch;
			IStatus status = new Status(IStatus.ERROR, LocalTerminalActivator.PLUGIN_ID, message);
			throw new CoreException(status);
		}

		// Extract all relevant information from the ILaunchConfiguration; the original
		// ProgramLaunchDelegate class checks for cancellation again and again after each step,
		// which is a somewhat suspect pattern; however, for now, LocalTerminalLaunchDelegate
		// handles cancellation in the same way:
		//
		if (progressMonitor.isCanceled()) {

			return;
		}
		IPath location = LocalTerminalLaunchUtilities.getLocation(configuration);
		if (progressMonitor.isCanceled()) {

			return;
		}	   
		IPath workingDirectory = LocalTerminalLaunchUtilities.getWorkingDirectory(configuration);
		if (progressMonitor.isCanceled()) {

			return;
		}
		String[] arguments = LocalTerminalLaunchUtilities.getArguments(configuration);
		if (progressMonitor.isCanceled()) {

			return;
		}
		String[] commandLine = new String[arguments != null? arguments.length+1:1];
		commandLine[0] = location.toOSString();
		if (arguments != null) {

			System.arraycopy(arguments, 0, commandLine, 1, arguments.length);
		}
		File workingDirectoryAsFile = null;
		if (workingDirectory != null) {

			workingDirectoryAsFile = workingDirectory.toFile();
		}
		if (progressMonitor.isCanceled()) {

			return;
		}
		String[] environment = LocalTerminalUtilities.LAUNCH_MANAGER.getEnvironment(configuration);
		if (progressMonitor.isCanceled()) {

			return;
		}
		//
		// TODO: check if there is a better way of handling cancellation of terminal launches!

		// Install an IWindowListener that checks for left-over terminal processes when the
		// workbench is closed:
		//
		if (workbenchCloseListener == null) {

			workbenchCloseListener = new LocalTerminalStillRunningListener();
			PlatformUI.getWorkbench().addWorkbenchListener(workbenchCloseListener);
		}

		// Create the low-level Process object:
		//
		Process spawner;
		PTY pty = null;
		try {

			ProcessFactory factory = ProcessFactory.getFactory();
			if (PTY.isSupported()) {

				pty = new PTY(false);
				spawner = factory.exec(commandLine, environment, workingDirectoryAsFile, pty);
			}
			else {

				spawner = factory.exec(commandLine, environment, workingDirectoryAsFile);
			}
		}
		catch (IOException exception) {

			Status error;
			String message = exception.getMessage();
			error = new Status(IStatus.ERROR, LocalTerminalActivator.PLUGIN_ID, message, exception);
			throw new CoreException(error);
		}

		// Use program name as "process type" attribute:
		//
		Map processAttributes = new HashMap();
		String programName = location.lastSegment();
		String extension = location.getFileExtension();
		if (extension != null) {

			programName = programName.substring(0, programName.length()-extension.length()-1);
		}
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName.toLowerCase());

		// Create the IProcess:
		//
		IProcess process = null;
		if (spawner != null) {

			String[] configurationName = {configuration.getName()};
			String task = NLS.bind(LocalTerminalMessages.launchingConfiguration, configurationName);
			progressMonitor.beginTask(task, IProgressMonitor.UNKNOWN);
			process = DebugPlugin.newProcess(launch, spawner, commandLine[0], processAttributes);
		}
		if (spawner == null || process == null) {

			if (spawner != null) {

				spawner.destroy();
			}
			String pluginID = LocalTerminalActivator.PLUGIN_ID;
			String errorMessage = LocalTerminalMessages.couldNotCreateIProcess;
			Status error = new Status(IStatus.ERROR, pluginID, IStatus.ERROR, errorMessage, null);
			throw new CoreException(error);
		}
		if (process instanceof LocalTerminalProcess) {

			((LocalTerminalProcess)process).setPTY(pty);
		}
		process.setAttribute(IProcess.ATTR_CMDLINE, generateCommandLine(commandLine));

		// Wait for process termination if necessary (though probably highly unusual for terminal
		// launches); again, the busy waiting pattern was copied from ProgramLaunchDelegate and is
		// somewhat suspect:
		//
		if (!CommonTab.isLaunchInBackground(configuration)) {

			while (!process.isTerminated()) {

				try {

					if (progressMonitor.isCanceled()) {

						process.terminate();
						break;
					}
					Thread.sleep(50);
				}
				catch (InterruptedException interrupt) {

					Logger.logException(interrupt);
				}
			}
		}
		//
		// TODO: find a better replacement for the busy waiting loop
	}

	//------------------------------------- PRIVATE SECTION --------------------------------------//

	private String generateCommandLine(String[] commandLine) {

		if (commandLine.length < 1) {

			return EMPTY;
		}
		StringBuffer buffer = new StringBuffer();
		for (int element = 0; element < commandLine.length; element++) {

			if (element > 0) {

				buffer.append(' ');
			}
			StringBuffer argument = new StringBuffer();
			char[] characters = commandLine[element].toCharArray();
			boolean argumentContainsSpace = false;
			for (int index = 0; index < characters.length; index++) {

				char character = characters[index];
				if (character == '"') {

					argument.append('\\');
				}
				else if (character == ' ') {

					argumentContainsSpace = true;
				}
				argument.append(character);
			}
			if (argumentContainsSpace) {

				buffer.append('"').append(argument).append('"');
			}
			else {

				buffer.append(argument);
			}
		}   
		return buffer.toString();
	}   
}
