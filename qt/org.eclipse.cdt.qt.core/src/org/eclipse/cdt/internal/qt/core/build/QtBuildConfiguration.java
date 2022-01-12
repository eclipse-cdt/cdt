/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.Messages;
import org.eclipse.cdt.internal.qt.core.QtInstallManager;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallListener;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.cdt.qt.core.QtInstallEvent;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class QtBuildConfiguration extends CBuildConfiguration implements IQtBuildConfiguration, IQtInstallListener {

	public static final String QMAKE_COMMAND = "cdt.qt.qmake.command"; //$NON-NLS-1$
	public static final String QMAKE_ARGS = "cdt.qt.qmake.args"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "cdt.qt.buildCommand"; //$NON-NLS-1$

	private static final String QTINSTALL_NAME = "cdt.qt.install.name"; //$NON-NLS-1$
	private static final String QTINSTALL_SPEC = "cdt.qt.install.spec"; //$NON-NLS-1$
	private static final String LAUNCH_MODE = "cdt.qt.launchMode"; //$NON-NLS-1$

	private final String qtInstallSpec;
	private IQtInstall qtInstall;
	private Map<String, String> qtProperties;
	private boolean doFullBuild;

	private IEnvironmentVariable pathVar = new IEnvironmentVariable() {
		@Override
		public String getValue() {
			return getQmakeCommand().getParent().toString();
		}

		@Override
		public int getOperation() {
			return IEnvironmentVariable.ENVVAR_PREPEND;
		}

		@Override
		public String getName() {
			return "PATH"; //$NON-NLS-1$
		}

		@Override
		public String getDelimiter() {
			return File.pathSeparator;
		}
	};

	public QtBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		IQtInstallManager manager = Activator.getService(IQtInstallManager.class);
		manager.addListener(this);

		Preferences settings = getSettings();
		String installName = settings.get(QTINSTALL_NAME, ""); //$NON-NLS-1$
		qtInstallSpec = settings.get(QTINSTALL_SPEC, ""); //$NON-NLS-1$
		if (!installName.isEmpty()) {
			qtInstall = manager.getInstall(Paths.get(installName));
			if (qtInstallSpec.isEmpty()) {
				// save the spec if it wasn't set
				settings.put(QTINSTALL_SPEC, qtInstall.getSpec());
				try {
					settings.flush();
				} catch (BackingStoreException e) {
					Activator.log(e);
				}
			}
		}

		if (getQtInstall() == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, CCorePlugin.STATUS_BUILD_CONFIG_NOT_VALID,
					String.format(Messages.QtBuildConfiguration_ConfigNotFound, name), null));
		}

		String oldLaunchMode = settings.get(LAUNCH_MODE, null);
		if (oldLaunchMode != null) {
			setLaunchMode(oldLaunchMode);
			settings.remove(LAUNCH_MODE);
			try {
				settings.flush();
			} catch (BackingStoreException e) {
				Activator.log(e);
			}
		}
	}

	QtBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain, IQtInstall qtInstall,
			String launchMode) throws CoreException {
		super(config, name, toolChain, launchMode);
		this.qtInstall = qtInstall;
		this.qtInstallSpec = qtInstall.getSpec();

		IQtInstallManager manager = Activator.getService(IQtInstallManager.class);
		manager.addListener(this);

		Preferences settings = getSettings();
		settings.put(QTINSTALL_NAME, qtInstall.getQmakePath().toString());
		settings.put(QTINSTALL_SPEC, qtInstallSpec);
		if (launchMode != null) {
			settings.put(LAUNCH_MODE, launchMode);
		}

		try {
			settings.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IQtBuildConfiguration.class)) {
			return (T) this;
		} else {
			return super.getAdapter(adapter);
		}
	}

	@Override
	public IQtInstall getQtInstall() {
		if (qtInstall == null && !qtInstallSpec.isEmpty()) {
			// find one that matches the spec
			IQtInstallManager manager = Activator.getService(IQtInstallManager.class);
			Collection<IQtInstall> candidates = manager.getInstall(qtInstallSpec);
			if (!candidates.isEmpty()) {
				qtInstall = candidates.iterator().next();
			}
		}
		return qtInstall;
	}

	@Override
	public void installChanged(QtInstallEvent event) {
		if (event.getType() == QtInstallEvent.REMOVED && event.getInstall().equals(qtInstall)) {
			// clear the cache so we refetch later
			qtInstall = null;
		}
	}

	@Override
	public Path getQmakeCommand() {
		return getQtInstall().getQmakePath();
	}

	@Override
	public String[] getQmakeConfig() {
		String qmakeArgs = getProperty(QMAKE_ARGS);
		if (qmakeArgs != null) {
			return qmakeArgs.split(" "); //$NON-NLS-1$
		}

		String launchMode = getLaunchMode();
		if (launchMode != null) {
			switch (launchMode) {
			case "run": //$NON-NLS-1$
				return new String[] { "CONFIG-=debug_and_release", "CONFIG+=release" }; //$NON-NLS-1$ //$NON-NLS-2$
			case "debug": //$NON-NLS-1$
				return new String[] { "CONFIG-=debug_and_release", "CONFIG+=debug" }; //$NON-NLS-1$ //$NON-NLS-2$
			default:
				return new String[] { "CONFIG-=debug_and_release", "CONFIG+=launch_mode_" + launchMode }; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return new String[] { "CONFIG+=debug_and_release", "CONFIG+=launch_modeall" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public Path getProjectFile() {
		File projectDir = getProject().getLocation().toFile();
		File[] proFiles = projectDir.listFiles((dir, name) -> name.endsWith(".pro")); //$NON-NLS-1$
		if (proFiles.length > 0) {
			// TODO what if there are more than one.
			return proFiles[0].toPath();
		} else {
			return null;
		}
	}

	@Deprecated
	@Override
	public Path getProgramPath() throws CoreException {
		// TODO get the app name from the .pro file.
		String projectName = getProject().getName();
		switch (Platform.getOS()) {
		case Platform.OS_MACOSX:
			Path appFolder = getBuildDirectory().resolve(projectName + ".app"); //$NON-NLS-1$
			Path contentsFolder = appFolder.resolve("Contents"); //$NON-NLS-1$
			Path macosFolder = contentsFolder.resolve("MacOS"); //$NON-NLS-1$
			return macosFolder.resolve(projectName);
		case Platform.OS_WIN32:
			return getBuildDirectory().resolve(projectName + ".exe"); //$NON-NLS-1$
		case Platform.OS_LINUX:
			return getBuildDirectory().resolve(projectName);
		default:
			Path releaseFolder = getBuildDirectory().resolve("release"); //$NON-NLS-1$
			return releaseFolder.resolve(projectName);
		}
	}

	public String getQtProperty(String key) {
		if (qtProperties == null) {
			List<String> cmd = new ArrayList<>();
			cmd.add(getQmakeCommand().toString());
			cmd.add("-E"); //$NON-NLS-1$

			String[] config = getQmakeConfig();
			if (config != null) {
				for (String str : config) {
					cmd.add(str);
				}
			}

			cmd.add(getProjectFile().toString());

			try {
				ProcessBuilder processBuilder = new ProcessBuilder(cmd)
						.directory(getProjectFile().getParent().toFile());
				setBuildEnvironment(processBuilder.environment());
				Process proc = processBuilder.start();
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
					qtProperties = new HashMap<>();
					for (String line = reader.readLine(); line != null; line = reader.readLine()) {
						int i = line.indexOf('=');
						if (i >= 0) {
							String k = line.substring(0, i);
							String v = line.substring(i + 1);
							qtProperties.put(k.trim(), v.trim());
						}
					}
				}
			} catch (IOException e) {
				Activator.log(e);
			}
		}

		return qtProperties != null ? qtProperties.get(key) : null;
	}

	@Override
	public IEnvironmentVariable getVariable(String name) {
		if ("PATH".equals(name)) { //$NON-NLS-1$
			return pathVar;
		} else {
			return null;
		}
	}

	@Override
	public IEnvironmentVariable[] getVariables() {
		return new IEnvironmentVariable[] { pathVar };
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		IQtInstall qtInstall = getQtInstall();

		String cxx = getQtProperty("QMAKE_CXX"); //$NON-NLS-1$
		if (cxx == null) {
			Activator.log("No QMAKE_CXX for " + qtInstall.getSpec()); //$NON-NLS-1$
			return null;
		}
		String[] cxxSplit = cxx.split(" "); //$NON-NLS-1$
		Path command = Paths.get(cxxSplit[0]);

		List<String> args = new ArrayList<>();
		for (int i = 1; i < cxxSplit.length; ++i) {
			args.add(cxxSplit[i]);
		}
		args.addAll(Arrays.asList(getQtProperty("QMAKE_CXXFLAGS").split(" "))); //$NON-NLS-1$ //$NON-NLS-2$
		args.add("-o"); //$NON-NLS-1$
		args.add("-"); //$NON-NLS-1$

		String srcFile;
		if (resource instanceof IFile) {
			srcFile = resource.getLocation().toOSString();
			// Only add file if it's an IFile
			args.add(srcFile);
		} else {
			// Doesn't matter, the toolchain will create a tmp file for this
			srcFile = "scannerInfo.cpp"; //$NON-NLS-1$
		}

		String[] includePaths = getQtProperty("INCLUDEPATH").split(" "); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < includePaths.length; ++i) {
			Path path = Paths.get(includePaths[i]);
			if (!path.isAbsolute()) {
				Path projectDir = getProjectFile().getParent();
				includePaths[i] = projectDir.resolve(path).toString();
			}
		}

		IExtendedScannerInfo baseScannerInfo = new ExtendedScannerInfo(null, includePaths);
		try {
			return getToolChain().getScannerInfo(getBuildConfiguration(), command,
					args.toArray(new String[args.size()]), baseScannerInfo, resource,
					getBuildContainer().getLocationURI());
		} catch (CoreException e) {
			Activator.log(e);
			return null;
		}
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream errStream = console.getErrorStream();
			ConsoleOutputStream outStream = console.getOutputStream();

			String[] makeCommand = getMakeCommand();
			if (makeCommand == null) {
				errStream.write(Messages.QtBuildConfiguration_MakeNotFound);
				return null;
			}

			Path buildDir = getBuildDirectory();

			if (doFullBuild || !buildDir.resolve("Makefile").toFile().exists()) { //$NON-NLS-1$
				// Need to run qmake
				List<String> command = new ArrayList<>();
				command.add(getQmakeCommand().toString());

				String[] config = getQmakeConfig();
				if (config != null) {
					for (String str : config) {
						command.add(str);
					}
				}

				IFile projectFile = project.getFile(project.getName() + ".pro"); //$NON-NLS-1$
				command.add(projectFile.getLocation().toOSString());

				ProcessBuilder processBuilder = new ProcessBuilder(command).directory(getBuildDirectory().toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();

				StringBuffer msg = new StringBuffer();
				for (String arg : command) {
					msg.append(arg).append(' ');
				}
				msg.append('\n');
				outStream.write(msg.toString());

				// TODO qmake error parser
				watchProcess(process, console);
				doFullBuild = false;
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());
				// run make
				List<String> command = new ArrayList<>(Arrays.asList(makeCommand));
				command.add("all"); //$NON-NLS-1$
				ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
				watchProcess(process, new IConsoleParser[] { epm });
			}

			getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Building " + project.getName(), e)); //$NON-NLS-1$
		}
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			ConsoleOutputStream errStream = console.getErrorStream();
			ConsoleOutputStream outStream = console.getOutputStream();

			String[] makeCommand = getMakeCommand();
			if (makeCommand == null) {
				errStream.write(Messages.QtBuildConfiguration_MakeNotFound);
				return;
			}

			Path buildDir = getBuildDirectory();

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				epm.setOutputStream(console.getOutputStream());
				// run make
				List<String> command = new ArrayList<>(Arrays.asList(makeCommand));
				command.add("clean"); //$NON-NLS-1$
				ProcessBuilder processBuilder = new ProcessBuilder(command).directory(buildDir.toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				outStream.write(String.join(" ", command) + '\n'); //$NON-NLS-1$
				watchProcess(process, new IConsoleParser[] { epm });
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Cleaning " + project.getName(), e)); //$NON-NLS-1$
		}
	}

	public String[] getMakeCommand() {
		String buildCommandStr = getProperty(BUILD_COMMAND);
		if (buildCommandStr != null) {
			String[] buildCommand = buildCommandStr.split(" "); //$NON-NLS-1$
			Path command = findCommand(buildCommand[0]);
			if (command == null) {
				command = findCommand("make"); //$NON-NLS-1$
				if (command == null) {
					command = findCommand("mingw32-make"); //$NON-NLS-1$
				}
			}

			if (command != null) {
				buildCommand[0] = command.toString();
			}
			return buildCommand;
		} else {
			Path command = findCommand("make"); //$NON-NLS-1$
			if (command == null) {
				command = findCommand("mingw32-make"); //$NON-NLS-1$
			}

			if (command != null) {
				return new String[] { command.toString() };
			} else {
				return null;
			}
		}
	}

	@Override
	public boolean setProperties(Map<String, String> properties) {
		if (super.setProperties(properties)) {
			String qmakeCommand = properties.get(QMAKE_COMMAND);
			if (qmakeCommand != null && !qmakeCommand.equals(qtInstall.getQmakePath().toString())) {
				// TODO change the qtInstall
				QtInstallManager installManager = Activator.getService(QtInstallManager.class);
				IQtInstall newInstall = installManager.getInstall(Paths.get(qmakeCommand));
				if (newInstall != null) {
					qtInstall = newInstall;
				}
			}

			// Do a full build to take in new properties
			doFullBuild = true;
			return true;
		} else {
			return false;
		}
	}

}
