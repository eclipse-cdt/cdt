package org.eclipse.cdt.arduino.core.tests;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.ArduinoProjectGenerator;
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
import org.junit.Test;

@SuppressWarnings("nls")
public class FullIntegration {

	private static final ArduinoManager arduinoManager = Activator.getService(ArduinoManager.class);
	private static final IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
	private static final ICBuildConfigurationManager buildConfigManager = Activator
			.getService(ICBuildConfigurationManager.class);

	private void setBoardUrls() throws Exception {
		URL[] urls = new URL[] { new URL("http://downloads.arduino.cc/packages/package_index.json"),
				new URL("https://adafruit.github.io/arduino-board-index/package_adafruit_index.json") };
		ArduinoPreferences.setBoardUrlList(urls);
	}

	private Set<ArduinoBoard> getSkipBoards() throws Exception {
		Set<ArduinoBoard> boards = new HashSet<ArduinoBoard>();
		
		// Fails in arduino too
		boards.add(arduinoManager.getBoard("arduino", "avr", "robotControl"));
		boards.add(arduinoManager.getBoard("arduino", "avr", "robotMotor"));
		boards.add(arduinoManager.getBoard("adafruit", "avr", "adafruit32u4"));
		
		// TODO Need to add support for menu specific build properties
		boards.add(arduinoManager.getBoard("arduino", "avr", "mini"));
		boards.add(arduinoManager.getBoard("arduino", "avr", "lilypad"));
		boards.add(arduinoManager.getBoard("arduino", "avr", "diecimila"));
		boards.add(arduinoManager.getBoard("arduino", "avr", "pro"));
		boards.add(arduinoManager.getBoard("arduino", "avr", "atmegang"));
		boards.add(arduinoManager.getBoard("arduino", "avr", "bt"));
		boards.add(arduinoManager.getBoard("arduino", "avr", "mega"));
		boards.add(arduinoManager.getBoard("arduino", "avr", "nano"));
		boards.add(arduinoManager.getBoard("TeeOnArdu", "avr", "CirPlayTeensyCore"));
		boards.add(arduinoManager.getBoard("TeeOnArdu", "avr", "FloraTeensyCore"));
		boards.add(arduinoManager.getBoard("TeeOnArdu", "avr", "TeeOnArdu"));
		
		// TODO build.system.path missing
		boards.add(arduinoManager.getBoard("arduino", "sam", "arduino_due_x"));
		boards.add(arduinoManager.getBoard("arduino", "sam", "arduino_due_x_dbg"));
		boards.add(arduinoManager.getBoard("Intel", "arc32", "arduino_101"));

		return boards;
	}

	@Test
	public void runTest() throws Exception {
		IProgressMonitor monitor = new SysoutProgressMonitor();

		setArduinoHome();
		setBoardUrls();
		loadPlatforms(monitor);

		IProject project = createProject(monitor);
		Set<ArduinoBoard> skip = getSkipBoards();
		for (ArduinoBoard board : arduinoManager.getInstalledBoards()) {
			if (!skip.contains(board)) {
				buildBoard(project, board, monitor);
			}
		}
	}

	private void setArduinoHome() throws Exception {
		Path workspace = Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
		ArduinoPreferences.setArduinoHome(workspace.resolve(".arduinocdt"));
	}

	private void loadPlatforms(IProgressMonitor monitor) throws Exception {
		Collection<ArduinoPlatform> plats = arduinoManager.getAvailablePlatforms(monitor);
		arduinoManager.installPlatforms(plats, monitor);
	}

	private IProject createProject(IProgressMonitor monitor) throws Exception {
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

		return generator.getProject();
	}

	private void buildBoard(IProject project, ArduinoBoard board, IProgressMonitor monitor) throws Exception {
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
		connection = workingCopy.save();

		return connection.getService(ArduinoRemoteConnection.class);
	}

}
