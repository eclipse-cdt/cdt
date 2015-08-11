/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.IArduinoRemoteConnection;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.arduino.core.internal.console.ArduinoConsoleService;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

public class ArduinoLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	public static final String TYPE_ID = "org.eclipse.cdt.arduino.core.launchConfigurationType"; //$NON-NLS-1$
	public static final String CONNECTION_NAME = Activator.getId() + ".connectionName"; //$NON-NLS-1$

	private static IRemoteConnection getTarget(ILaunchConfiguration configuration) throws CoreException {
		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connectionType = remoteManager.getConnectionType(ArduinoRemoteConnection.TYPE_ID);
		String connectionName = configuration.getAttribute(CONNECTION_NAME, ""); //$NON-NLS-1$
		return connectionType.getConnection(connectionName);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		IRemoteConnection target = getTarget(configuration);

		// 1. make sure proper build config is set active
		IProject project = configuration.getMappedResources()[0].getProject();
		// TODO set active build config for the selected target

		// 2. Run the build
		return super.buildForLaunch(configuration, mode, monitor);
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		IProject project = configuration.getMappedResources()[0].getProject();
		return new IProject[] { project };
	}

	@Override
	public void launch(final ILaunchConfiguration configuration, String mode, final ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		new Job(Messages.ArduinoLaunchConfigurationDelegate_0) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ArduinoConsoleService consoleService = Activator.getConsoleService();
					IRemoteConnection target = getTarget(configuration);
					if (target == null) {
						return new Status(IStatus.ERROR, Activator.getId(),
								Messages.ArduinoLaunchConfigurationDelegate_2);
					}

					// The project
					IProject project = (IProject) configuration.getMappedResources()[0];

					// The build environment
					List<String> envVarList = new ArrayList<>();

					// Add in the serial port based on launch config
					IArduinoRemoteConnection arduinoRemote = target.getService(IArduinoRemoteConnection.class);
					envVarList.add("SERIAL_PORT=" + arduinoRemote.getPortName()); //$NON-NLS-1$
					String[] envp = envVarList.toArray(new String[envVarList.size()]);

					// The project directory to launch from
					File projectDir = new File(project.getLocationURI());

					// The build command
					String command = "make";

					// If opened, temporarily close the connection so we can use
					// it to download the firmware.
					boolean wasOpened = target.isOpen();
					if (wasOpened) {
						arduinoRemote.pause();
					}

					// Run the process and capture the results in the console
					Process process = Runtime.getRuntime().exec(command + " load", envp, projectDir); //$NON-NLS-1$
					consoleService.monitor(process, null);
					try {
						process.waitFor();
					} catch (InterruptedException e) {
					}

					// Reopen the connection
					if (wasOpened) {
						arduinoRemote.resume();
					}
				} catch (CoreException e) {
					return e.getStatus();
				} catch (IOException e) {
					return new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e);
				} finally {
					DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
				}

				return Status.OK_STATUS;
			};
		}.schedule();
	}

}
