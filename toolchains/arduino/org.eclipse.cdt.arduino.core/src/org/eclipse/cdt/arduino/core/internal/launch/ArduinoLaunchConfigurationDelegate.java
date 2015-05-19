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

import org.eclipse.cdt.arduino.core.ArduinoLaunchConsoleService;
import org.eclipse.cdt.arduino.core.ArduinoProjectGenerator;
import org.eclipse.cdt.arduino.core.IArduinoRemoteConnection;
import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.launchbar.core.PerTargetLaunchConfigProvider;
import org.eclipse.remote.core.IRemoteConnection;

public class ArduinoLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	public static final String TYPE_ID = "org.eclipse.cdt.arduino.core.launchConfigurationType"; //$NON-NLS-1$

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		IRemoteConnection target = PerTargetLaunchConfigProvider.getTarget(configuration);
		
		// 1. make sure proper build config is set active
		IProject project = configuration.getMappedResources()[0].getProject();
		ICProjectDescription projDesc = CCorePlugin.getDefault().getProjectDescription(project);
		ICConfigurationDescription configDesc = getBuildConfiguration(projDesc, target);
		boolean newConfig = false;
		if (configDesc == null) {
			IArduinoRemoteConnection arduinoRemote = target.getService(IArduinoRemoteConnection.class);
			configDesc = ArduinoProjectGenerator.createBuildConfiguration(projDesc, arduinoRemote.getBoard());
			newConfig = true;
		}
		if (newConfig || !projDesc.getActiveConfiguration().equals(configDesc)) {
			projDesc.setActiveConfiguration(configDesc);
			CCorePlugin.getDefault().setProjectDescription(project, projDesc);
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
	public void launch(final ILaunchConfiguration configuration, String mode, final ILaunch launch, IProgressMonitor monitor) throws CoreException {
		new Job(Messages.ArduinoLaunchConfigurationDelegate_0) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ArduinoLaunchConsoleService consoleService = getConsoleService();
					IRemoteConnection target = PerTargetLaunchConfigProvider.getTarget(configuration);
					if (target == null) {
						return new Status(IStatus.ERROR, Activator.getId(), Messages.ArduinoLaunchConfigurationDelegate_2);
					}

					// The project
					IProject project = (IProject) configuration.getMappedResources()[0];

					// The build environment
					ICProjectDescription projDesc = CCorePlugin.getDefault().getProjectDescription(project);
					ICConfigurationDescription configDesc = getBuildConfiguration(projDesc, target);
					IEnvironmentVariable[] envVars = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(configDesc, true);
					List<String> envVarList = new ArrayList<String>(envVars.length + 1);
					for (IEnvironmentVariable var : envVars) {
						envVarList.add(var.getName() + '=' + var.getValue());
					}
					// Add in the serial port based on launch config
					IArduinoRemoteConnection arduinoRemote = target.getService(IArduinoRemoteConnection.class);
					envVarList.add("SERIAL_PORT=" + arduinoRemote.getPortName()); //$NON-NLS-1$
					String[] envp = envVarList.toArray(new String[envVarList.size()]);

					// The project directory to launch from
					File projectDir = new File(project.getLocationURI());

					// The build command
					IConfiguration buildConfig = ManagedBuildManager.getConfigurationForDescription(configDesc);
					String command = buildConfig.getBuilder().getCommand();

					// If opened, temporarily close the connection so we can use it to download the firmware.
					boolean wasOpened = target.isOpen();
					if (wasOpened) {
						arduinoRemote.pause();
					}

					// Run the process and capture the results in the console
					Process process = Runtime.getRuntime().exec(command + " load", envp, projectDir); //$NON-NLS-1$
					consoleService.monitor(process);
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

	private ArduinoLaunchConsoleService getConsoleService() throws CoreException {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.getId(), "consoleService"); //$NON-NLS-1$
		IExtension extension = point.getExtensions()[0]; // should only be one
		return (ArduinoLaunchConsoleService) extension.getConfigurationElements()[0].createExecutableExtension("class"); //$NON-NLS-1$
	}

	/**
	 * Returns the build configuration for the active target and the launch configuration.
	 * 
	 * @param launchConfig
	 * @return
	 */
	private ICConfigurationDescription getBuildConfiguration(ICProjectDescription projDesc, IRemoteConnection target) throws CoreException {
		String boardId;
		if (target != null) {
			IArduinoRemoteConnection arduinoRemote = target.getService(IArduinoRemoteConnection.class);
			boardId = arduinoRemote.getBoard().getId();
		} else {
			boardId = "uno"; //$NON-NLS-1$
		}

		for (ICConfigurationDescription configDesc : projDesc.getConfigurations()) {
			IConfiguration config = ManagedBuildManager.getConfigurationForDescription(configDesc);
			if (ArduinoProjectGenerator.getBoard(config).getId().equals(boardId))
				return configDesc;
		}

		return null;
	}

}
