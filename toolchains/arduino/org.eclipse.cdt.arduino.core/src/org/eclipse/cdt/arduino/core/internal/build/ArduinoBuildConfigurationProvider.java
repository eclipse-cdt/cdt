/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.build;

import java.util.List;

import org.eclipse.cdt.arduino.core.internal.Activator;
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
		return new ArduinoBuildConfiguration(config, name);
	}

	@Override
	public ICBuildConfiguration getDefaultCBuildConfiguration(IProject project) throws CoreException {
		ArduinoBoard board = arduinoManager.getBoard("Arduino/Genuino Uno", "Arduino AVR Boards", "arduino"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (board == null) {
			List<ArduinoBoard> boards = arduinoManager.getInstalledBoards();
			if (!boards.isEmpty()) {
				board = boards.get(0);
			}
		}
		if (board != null) {
			String launchMode = "run"; //$NON-NLS-1$
			for (IBuildConfiguration config : project.getBuildConfigs()) {
				ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
				if (cconfig != null) {
					ArduinoBuildConfiguration arduinoConfig = cconfig.getAdapter(ArduinoBuildConfiguration.class);
					if (arduinoConfig != null && arduinoConfig.getLaunchMode().equals(launchMode)
							&& arduinoConfig.getBoard().equals(board)) {
						return arduinoConfig;
					}
				}
			}

			// not found, create one
			String configName = ArduinoBuildConfiguration.generateName(board, launchMode);
			IBuildConfiguration config = configManager.createBuildConfiguration(this, project, configName,
					null);

			// Create the toolChain
			IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
			IToolChainProvider provider = toolChainManager.getProvider(ArduinoToolChainProvider.ID);
			IToolChain toolChain = new ArduinoToolChain(provider, config);
			toolChainManager.addToolChain(toolChain);

			ArduinoBuildConfiguration arduinoConfig = new ArduinoBuildConfiguration(config, configName, board,
					launchMode, toolChain);
			arduinoConfig.setActive(null);
			configManager.addBuildConfiguration(config, arduinoConfig);
			return arduinoConfig;
		}
		return null;
	}

	public ArduinoBuildConfiguration getConfiguration(IProject project, ArduinoRemoteConnection target,
			String launchMode,
			IProgressMonitor monitor) throws CoreException {
		ArduinoBoard board = target.getBoard();
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
			if (cconfig != null) {
				ArduinoBuildConfiguration arduinoConfig = cconfig.getAdapter(ArduinoBuildConfiguration.class);
				if (arduinoConfig != null && arduinoConfig.getLaunchMode().equals(launchMode)
						&& arduinoConfig.getBoard().equals(board) && arduinoConfig.matches(target)) {
					return arduinoConfig;
				}
			}
		}
		return null;
	}

	public ArduinoBuildConfiguration createConfiguration(IProject project, ArduinoRemoteConnection target,
			String launchMode,
			IProgressMonitor monitor) throws CoreException {
		ArduinoBoard board = target.getBoard();
		String configName = ArduinoBuildConfiguration.generateName(board, launchMode);
		IBuildConfiguration config = configManager.createBuildConfiguration(this, project, configName,
				monitor);
		IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
		IToolChainProvider provider = toolChainManager.getProvider(ArduinoToolChainProvider.ID);
		IToolChain toolChain = new ArduinoToolChain(provider, config);
		toolChainManager.addToolChain(toolChain);
		ArduinoBuildConfiguration arduinoConfig = new ArduinoBuildConfiguration(config, configName, target, launchMode, toolChain);
		configManager.addBuildConfiguration(config, arduinoConfig);
		return arduinoConfig;
	}

}
