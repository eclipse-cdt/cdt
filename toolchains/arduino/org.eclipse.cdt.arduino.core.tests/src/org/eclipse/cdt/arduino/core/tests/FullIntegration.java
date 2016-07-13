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
import java.util.Map.Entry;
import java.util.Set;

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
		URL[] urls = new URL[] {
				new URL("http://downloads.arduino.cc/packages/package_index.json"),
				new URL("https://adafruit.github.io/arduino-board-index/package_adafruit_index.json"),
				new URL("http://drazzy.com/package_drazzy.com_index.json"),
				new URL("https://github.com/chipKIT32/chipKIT-core/raw/master/package_chipkit_index.json"),
				// esp8266com and sparkfun overlap with the esp8266 package.
				new URL("http://arduino.esp8266.com/stable/package_esp8266com_index.json"),
				//new URL("https://raw.githubusercontent.com/sparkfun/Arduino_Boards/master/IDE_Board_Manager/package_sparkfun_index.json"),
		};
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
		skipBuild.add(arduinoManager.getBoard("chipKIT", "pic32", "cerebot32mx7"));
		skipBuild.add(arduinoManager.getBoard("chipKIT", "pic32", "OpenScope"));

		// What is Microsoft doing?
		skipBuild.add(arduinoManager.getBoard("Microsoft", "win10", "w10iotcore"));
	}

	private static void setupSkipUpload() throws Exception {
		skipUpload = new HashSet<>();

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
		ArduinoBuildConfiguration config = provider.getConfiguration(project, arduinoTarget, "run", monitor);

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
		String arch = board.getPlatform().getArchitecture();
		String pkg = board.getPlatform().getPackage().getName();
		String targetName = pkg + '-' + arch + '-' + board.getName().replace('/', '_');
		targetName = targetName.trim();

		IRemoteConnection connection = type.getConnection(targetName);
		if (connection != null) {
			type.removeConnection(connection);
		}

		IRemoteConnectionWorkingCopy workingCopy = type.newConnection(targetName);
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

		HierarchicalProperties programmers = board.getPlatform().getProgrammers();
		if (programmers != null && programmers.getChildren() != null) {
			for (String programmer : programmers.getChildren().keySet()) {
				ArduinoRemoteConnection.setProgrammer(workingCopy, programmer);
				break;
			}
		}

		connection = workingCopy.save();

		return connection.getService(ArduinoRemoteConnection.class);
	}

}
