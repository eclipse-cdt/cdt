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

import java.io.IOException;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
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
		if (target != null) {
			ArduinoRemoteConnection arduinoTarget = target.getService(ArduinoRemoteConnection.class);
			ArduinoBoard targetBoard = arduinoTarget.getBoard();

			// 1. make sure proper build config is set active
			IProject project = configuration.getMappedResources()[0].getProject();
			ArduinoBuildConfiguration arduinoConfig = ArduinoBuildConfiguration.getConfig(project, targetBoard,
					monitor);
			arduinoConfig.setActive(monitor);
		}

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
					ArduinoRemoteConnection arduinoTarget = target.getService(ArduinoRemoteConnection.class);

					// The project
					IProject project = (IProject) configuration.getMappedResources()[0];

					// The build config
					ArduinoBuildConfiguration arduinoConfig = ArduinoBuildConfiguration.getConfig(project,
							arduinoTarget.getBoard(), monitor);
					String[] uploadCmd = arduinoConfig.getUploadCommand(arduinoTarget.getPortName());

					// If opened, temporarily close the connection so we can use
					// it to download the firmware.
					boolean wasOpened = target.isOpen();
					if (wasOpened) {
						arduinoTarget.pause();
					}

					// Run the process and capture the results in the console
					ProcessBuilder processBuilder = new ProcessBuilder(uploadCmd)
							.directory(arduinoConfig.getBuildDirectory());
					arduinoConfig.setEnvironment(processBuilder.environment());
					Process process = processBuilder.start();

					consoleService.monitor(process, null, null);
					try {
						process.waitFor();
					} catch (InterruptedException e) {
					}

					consoleService.writeOutput("Upload complete\n");

					// Reopen the connection
					if (wasOpened) {
						arduinoTarget.resume();
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
