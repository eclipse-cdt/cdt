/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.build;

import java.util.Collection;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

public class ArduinoBuildConfigurationProvider implements ICBuildConfigurationProvider {

	public static final String ID = "org.eclipse.cdt.arduino.core.provider"; //$NON-NLS-1$

	private static final ICBuildConfigurationManager configManager = Activator
			.getService(ICBuildConfigurationManager.class);
	private static final ArduinoManager arduinoManager = Activator.getService(ArduinoManager.class);

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ICBuildConfiguration getCBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		if (config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
			// Use the good ol' Uno as the default
			ArduinoBoard board = arduinoManager.getBoard("arduino", "avr", "uno"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (board == null) {
				Collection<ArduinoBoard> boards = arduinoManager.getInstalledBoards();
				if (!boards.isEmpty()) {
					board = boards.iterator().next();
				}
			}
			if (board != null) {
				IToolChain toolChain = createToolChain("default"); //$NON-NLS-1$
				return new ArduinoBuildConfiguration(config, name, "run", board, toolChain); //$NON-NLS-1$
			}
		} else {
			IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
			IRemoteConnectionType connectionType = remoteManager.getConnectionType(ArduinoRemoteConnection.TYPE_ID);
			IRemoteConnection connection = connectionType.getConnection(name);
			if (connection == null) {
				throw Activator.coreException(
						String.format(Messages.ArduinoBuildConfigurationProvider_UnknownConnection, name), null);
			}

			ArduinoRemoteConnection target = connection.getService(ArduinoRemoteConnection.class);
			if (target != null) {
				IToolChain toolChain = createToolChain(connection.getName());
				return new ArduinoBuildConfiguration(config, name, "run", target, toolChain); //$NON-NLS-1$
			}
		}
		return null;
	}

	public ArduinoBuildConfiguration getConfiguration(IProject project, ArduinoRemoteConnection target,
			String launchMode, IProgressMonitor monitor) throws CoreException {
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
			if (cconfig != null) {
				ArduinoBuildConfiguration arduinoConfig = cconfig.getAdapter(ArduinoBuildConfiguration.class);
				if (arduinoConfig != null && target.equals(arduinoConfig.getTarget())
						&& arduinoConfig.getLaunchMode().equals(launchMode)) {
					return arduinoConfig;
				}
			}
		}

		// Make a new one
		String configName = target.getRemoteConnection().getName();
		IBuildConfiguration config = configManager.createBuildConfiguration(this, project, configName, monitor);
		IToolChain toolChain = createToolChain(configName);
		ArduinoBuildConfiguration arduinoConfig = new ArduinoBuildConfiguration(config, configName, "run", target, //$NON-NLS-1$
				toolChain);
		configManager.addBuildConfiguration(config, arduinoConfig);
		return arduinoConfig;
	}

	private IToolChain createToolChain(String id) throws CoreException {
		IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
		IToolChain toolChain = toolChainManager.getToolChain(ArduinoToolChain.TYPE_ID, id);
		if (toolChain != null) {
			return toolChain;
		}

		IToolChainProvider provider = toolChainManager.getProvider(ArduinoToolChainProvider.ID);
		toolChain = new ArduinoToolChain(provider, id);
		toolChainManager.addToolChain(toolChain);
		return toolChain;
	}
}
