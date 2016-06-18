/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.tests;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.ArduinoProjectGenerator;
import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfigurationProvider;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("nls")
@RunWith(Parameterized.class)
public class FullIntegration {

	private static final ArduinoManager arduinoManager = Activator.getService(ArduinoManager.class);
	private static final IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
	private static final ICBuildConfigurationManager buildConfigManager = Activator
			.getService(ICBuildConfigurationManager.class);
	private static final IProgressMonitor monitor = new SysoutProgressMonitor();

	private static Set<ArduinoBoard> skipBuild;
	private static Set<ArduinoBoard> skipUpload;
	private static IProject project;

	private ArduinoBoard board;

	private static void setPreferences() throws Exception {
		URL[] urls = new URL[] { new URL("http://downloads.arduino.cc/packages/package_index.json"),
				new URL("https://adafruit.github.io/arduino-board-index/package_adafruit_index.json"),
				new URL("http://drazzy.com/package_drazzy.com_index.json") };
		ArduinoPreferences.setBoardUrlList(urls);

		Path workspace = Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
		ArduinoPreferences.setArduinoHome(workspace.resolve(".arduinocdt"));
	}

	private static void setupSkipBuild() throws Exception {
		skipBuild = new HashSet<>();

		// Fails in arduino too
		skipBuild.add(arduinoManager.getBoard("arduino", "avr", "robotControl"));
		skipBuild.add(arduinoManager.getBoard("arduino", "avr", "robotMotor"));
		skipBuild.add(arduinoManager.getBoard("adafruit", "avr", "adafruit32u4"));

		// What is Microsoft doing?
		skipBuild.add(arduinoManager.getBoard("Microsoft", "win10", "w10iotcore"));

		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			// tool chain incorrect?
			skipBuild.add(arduinoManager.getBoard("Intel", "i586", "izmir_fd"));
			skipBuild.add(arduinoManager.getBoard("Intel", "i586", "izmir_fg"));
			skipBuild.add(arduinoManager.getBoard("Intel", "i686", "izmir_ec"));
		}

		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			// i586/pokysdk missing
			skipBuild.add(arduinoManager.getBoard("Intel", "i586", "izmir_fd"));
			skipBuild.add(arduinoManager.getBoard("Intel", "i586", "izmir_fg"));
			skipBuild.add(arduinoManager.getBoard("Intel", "i686", "izmir_ec"));
		}
	}

	private static void setupSkipUpload() throws Exception {
		skipUpload = new HashSet<>();

		// missing upload.protocol
		skipUpload.add(arduinoManager.getBoard("arduino", "avr", "gemma"));
		skipUpload.add(arduinoManager.getBoard("adafruit", "avr", "gemma"));
		skipUpload.add(arduinoManager.getBoard("adafruit", "avr", "trinket5"));
		skipUpload.add(arduinoManager.getBoard("adafruit", "avr", "trinket3"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attinyx7"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attinyx61"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attinyx8"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attiny1634"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attinyx313"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attinyx5"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attinyx4"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attinyx41"));
		skipUpload.add(arduinoManager.getBoard("ATTinyCore", "avr", "attiny828"));
		skipUpload.add(arduinoManager.getBoard("arduino-tiny-841", "avr", "attiny1634"));
		skipUpload.add(arduinoManager.getBoard("arduino-tiny-841", "avr", "attinyx41"));
		skipUpload.add(arduinoManager.getBoard("arduino-tiny-841", "avr", "attiny828"));
		skipUpload.add(arduinoManager.getBoard("arduino-tiny-841", "avr", "attiny828"));

		// usbtiny missing
		skipUpload.add(arduinoManager.getBoard("adafruit", "avr", "protrinket3"));
		skipUpload.add(arduinoManager.getBoard("adafruit", "avr", "protrinket5"));
	}

	private static void createProject() throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		String projectName = "ArduinoTest";
		ArduinoProjectGenerator generator = new ArduinoProjectGenerator("templates/cppsketch/manifest.xml"); //$NON-NLS-1$

		Job job = new Job("Create") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					IProject project = root.getProject(projectName);
					if (project.exists()) {
						project.delete(true, monitor);
					}

					generator.setProjectName(projectName);
					generator.generate(new HashMap<String, Object>(), monitor);
					return Status.OK_STATUS;
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
		};
		job.setRule(root);
		job.schedule();
		job.join();

		project = generator.getProject();
	}

	@BeforeClass
	public static void setup() throws Exception {
		setupSkipBuild();
		setupSkipUpload();
		createProject();
	}

	@Parameters(name = "{0}")
	public static Collection<ArduinoBoard> getBoards() throws Exception {
		setPreferences();
		Collection<ArduinoPlatform> plats = arduinoManager.getAvailablePlatforms(monitor);
		arduinoManager.installPlatforms(plats, monitor);
		return arduinoManager.getInstalledBoards();
	}

	public FullIntegration(ArduinoBoard board) {
		this.board = board;
	}

	@Test
	public void runTest() throws Exception {
		if (!skipBuild.contains(board)) {
			buildBoard(project, board, !skipUpload.contains(board));
		}
	}

	private void buildBoard(IProject project, ArduinoBoard board, boolean upload) throws Exception {
		ArduinoRemoteConnection arduinoTarget = createTarget(board);
		ArduinoBuildConfigurationProvider provider = (ArduinoBuildConfigurationProvider) buildConfigManager
				.getProvider(ArduinoBuildConfigurationProvider.ID);
		ArduinoBuildConfiguration config = provider.createConfiguration(project, arduinoTarget, "run", monitor);

		System.out.println(String.format("Building board: %s\n    %s - %s", board.getName(), board.getId(),
				board.getPlatform().getInstallPath()));

		config.generateMakeFile(monitor);
		ProcessBuilder processBuilder = new ProcessBuilder().command(config.getBuildCommand())
				.directory(config.getBuildDirectory().toFile()).inheritIO();
		config.setBuildEnvironment(processBuilder.environment());
		Process process = processBuilder.start();
		int rc = process.waitFor();
		if (rc != 0) {
			throw new Exception("Build failed");
		}

		// Test to make sure we can get the upload command cleanly
		if (upload) {
			System.out.println(String.join(" ", config.getUploadCommand("port1")));
		}
	}

	private ArduinoRemoteConnection createTarget(ArduinoBoard board) throws Exception {
		IRemoteConnectionType type = remoteManager.getConnectionType(ArduinoRemoteConnection.TYPE_ID);
		IRemoteConnection connection = type.getConnection(board.getName());
		if (connection != null) {
			type.removeConnection(connection);
		}

		IRemoteConnectionWorkingCopy workingCopy = type.newConnection(board.getName());
		ArduinoRemoteConnection.setBoardId(workingCopy, board);
		ArduinoRemoteConnection.setPortName(workingCopy, "port1");

		HierarchicalProperties menus = board.getMenus();
		if (menus != null) {
			for (Entry<String, HierarchicalProperties> menuEntry : menus.getChildren().entrySet()) {
				String key = menuEntry.getKey();

				for (Entry<String, HierarchicalProperties> valueEntry : menuEntry.getValue().getChildren().entrySet()) {
					String value = valueEntry.getKey();
					ArduinoRemoteConnection.setMenuValue(workingCopy, key, value);
					break;
				}
			}
		}

		connection = workingCopy.save();

		return connection.getService(ArduinoRemoteConnection.class);
	}

}
