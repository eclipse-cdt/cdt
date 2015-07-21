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
package org.eclipse.cdt.arduino.core.board;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.gson.Gson;

// Closeable isn't API yet but it's recommended.
@SuppressWarnings("restriction")
public class ArduinoBoardManager {

	public static final ArduinoBoardManager instance = new ArduinoBoardManager();

	// Build tool ids
	public static final String BOARD_OPTION_ID = "org.eclipse.cdt.arduino.option.board"; //$NON-NLS-1$
	public static final String PLATFORM_OPTION_ID = "org.eclipse.cdt.arduino.option.platform"; //$NON-NLS-1$
	public static final String PACKAGE_OPTION_ID = "org.eclipse.cdt.arduino.option.package"; //$NON-NLS-1$
	public static final String AVR_TOOLCHAIN_ID = "org.eclipse.cdt.arduino.toolChain.avr"; //$NON-NLS-1$

	// TODO make this a preference
	private Path arduinoHome = Paths.get(System.getProperty("user.home"), ".arduinocdt"); //$NON-NLS-1$ //$NON-NLS-2$
	private Path packageIndexPath;
	private PackageIndex packageIndex;

	public ArduinoBoardManager() {
		new Job(Messages.ArduinoBoardManager_0) {
			protected IStatus run(IProgressMonitor monitor) {
				try (CloseableHttpClient client = HttpClients.createDefault()) {
					HttpGet get = new HttpGet("http://downloads.arduino.cc/packages/package_index.json"); //$NON-NLS-1$
					try (CloseableHttpResponse response = client.execute(get)) {
						if (response.getStatusLine().getStatusCode() >= 400) {
							return new Status(IStatus.ERROR, Activator.getId(),
									response.getStatusLine().getReasonPhrase());
						} else {
							HttpEntity entity = response.getEntity();
							if (entity == null) {
								return new Status(IStatus.ERROR, Activator.getId(), Messages.ArduinoBoardManager_1);
							}
							Files.createDirectories(arduinoHome);
							packageIndexPath = arduinoHome.resolve("package_index.json"); //$NON-NLS-1$
							Files.copy(entity.getContent(), packageIndexPath, StandardCopyOption.REPLACE_EXISTING);
						}
					}
				} catch (IOException e) {
					return new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	Path getArduinoHome() {
		return arduinoHome;
	}

	public PackageIndex getPackageIndex() throws IOException {
		if (packageIndex == null) {
			try (FileReader reader = new FileReader(packageIndexPath.toFile())) {
				packageIndex = new Gson().fromJson(reader, PackageIndex.class);
			}
		}
		return packageIndex;
	}

	public ICConfigurationDescription createBuildConfiguration(ICProjectDescription projDesc, String boardId,
			String platformId, String packageId) throws CoreException {
		Board board = packageIndex.getPackage(packageId).getPlatform(platformId).getBoard(boardId);
		ManagedProject managedProject = new ManagedProject(projDesc);
		// TODO find toolchain based on package (os), platform (arch).
		String configId = ManagedBuildManager.calculateChildId(ArduinoBoardManager.AVR_TOOLCHAIN_ID, null);
		IToolChain avrToolChain = ManagedBuildManager.getExtensionToolChain(ArduinoBoardManager.AVR_TOOLCHAIN_ID);

		org.eclipse.cdt.managedbuilder.internal.core.Configuration newConfig = new org.eclipse.cdt.managedbuilder.internal.core.Configuration(
				managedProject, (ToolChain) avrToolChain, configId, board.getName());

		IToolChain newToolChain = newConfig.getToolChain();
		IOption boardOption = newToolChain.getOptionBySuperClassId(BOARD_OPTION_ID);
		ManagedBuildManager.setOption(newConfig, newToolChain, boardOption, boardId);
		IOption platformOption = newToolChain.getOptionBySuperClassId(PLATFORM_OPTION_ID);
		ManagedBuildManager.setOption(newConfig, newToolChain, platformOption, platformId);
		IOption packageOption = newToolChain.getOptionBySuperClassId(PACKAGE_OPTION_ID);
		ManagedBuildManager.setOption(newConfig, newToolChain, packageOption, packageId);

		CConfigurationData data = newConfig.getConfigurationData();
		return projDesc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
	}

	public Board getBoard(String boardId, String platformId, String packageId) {
		return packageIndex.getPackage(packageId).getPlatform(platformId).getBoard(boardId);
	}

	public Board getBoard(IConfiguration configuration) throws CoreException {
		try {
			IToolChain toolChain = configuration.getToolChain();
			IOption boardOption = toolChain.getOptionBySuperClassId(BOARD_OPTION_ID);
			String boardId = boardOption.getStringValue();
			IOption platformOption = toolChain.getOptionBySuperClassId(PLATFORM_OPTION_ID);
			String platformId = platformOption.getStringValue();
			IOption packageOption = toolChain.getOptionBySuperClassId(PACKAGE_OPTION_ID);
			String packageId = packageOption.getStringValue();

			return getBoard(boardId, platformId, packageId);
		} catch (BuildException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e));
		}

	}

	public List<Board> getBoards() {
		List<Board> boards = new ArrayList<>();
		for (Package pkg : packageIndex.getPackages()) {
			for (Platform platform : pkg.getPlatforms()) {
				boards.addAll(platform.getBoards());
			}
		}
		return boards;
	}

}
