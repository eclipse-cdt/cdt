/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
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

public class QtBuildConfiguration extends CBuildConfiguration implements ICBuildConfiguration, IQtBuildConfiguration {

	private static final String QTINSTALL_NAME = "cdt.qt.install.name"; //$NON-NLS-1$
	private static final String LAUNCH_MODE = "cdt.qt.launchMode"; //$NON-NLS-1$

	private final IQtInstall qtInstall;
	private final String launchMode;
	private Map<String, String> properties;

	public QtBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		Preferences settings = getSettings();
		String installName = settings.get(QTINSTALL_NAME, ""); //$NON-NLS-1$
		if (!installName.isEmpty()) {
			IQtInstallManager manager = Activator.getService(IQtInstallManager.class);
			qtInstall = manager.getInstall(Paths.get(installName));
		} else {
			qtInstall = null;
		}

		launchMode = settings.get(LAUNCH_MODE, null); // $NON-NLS-1$
	}

	QtBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain, IQtInstall qtInstall,
			String launchMode) throws CoreException {
		super(config, name, toolChain);
		this.qtInstall = qtInstall;
		this.launchMode = launchMode;

		Preferences settings = getSettings();
		settings.put(QTINSTALL_NAME, qtInstall.getQmakePath().toString());
		if (launchMode != null) {
			settings.put(LAUNCH_MODE, launchMode);
		}
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return super.getAdapter(adapter);
	}

	public IQtInstall getQtInstall() {
		return qtInstall;
	}

	@Override
	public String getLaunchMode() {
		return launchMode;
	}

	@Override
	public Path getQmakeCommand() {
		return qtInstall.getQmakePath();
	}

	@Override
	public String[] getQmakeConfig() {
		if (launchMode != null) {
			switch (launchMode) {
			case "run": //$NON-NLS-1$
				return new String[] { "CONFIG+=release" }; //$NON-NLS-1$
			case "debug": //$NON-NLS-1$
				return new String[] { "CONFIG+=debug" }; //$NON-NLS-1$
			default:
				return new String[] { "CONFIG+=launch_mode_" + launchMode }; //$NON-NLS-1$
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

	@Override
	public Path getProgramPath() throws CoreException {
		String projectName = getProject().getName();
		switch (Platform.getOS()) {
		case Platform.OS_MACOSX:
			// TODO this is mac local specific and really should be
			// in the config
			// TODO also need to pull the app name out of the pro
			// file name
			Path appFolder = getBuildDirectory().resolve(projectName + ".app"); //$NON-NLS-1$
			Path contentsFolder = appFolder.resolve("Contents"); //$NON-NLS-1$
			Path macosFolder = contentsFolder.resolve("MacOS"); //$NON-NLS-1$
			return macosFolder.resolve(projectName);
		case Platform.OS_WIN32: {
			String subdir = "run".equals(launchMode) ? "release" : "debug"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return getBuildDirectory().resolve(subdir).resolve(projectName + ".exe"); //$NON-NLS-1$
		}
		default:
			Path releaseFolder = getBuildDirectory().resolve("release"); //$NON-NLS-1$
			return releaseFolder.resolve(projectName);
		}
	}

	public String getProperty(String key) {
		if (properties == null) {
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
					properties = new HashMap<>();
					for (String line = reader.readLine(); line != null; line = reader.readLine()) {
						int i = line.indexOf('=');
						if (i >= 0) {
							String k = line.substring(0, i);
							String v = line.substring(i + 1);
							properties.put(k.trim(), v.trim());
						}
					}
				}
			} catch (IOException e) {
				Activator.log(e);
			}
		}

		return properties != null ? properties.get(key) : null;
	}

	@Override
	public IEnvironmentVariable getVariable(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables() {
		// TODO
		return new IEnvironmentVariable[0];
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		IQtInstall qtInstall = getQtInstall();

		String cxx = getProperty("QMAKE_CXX"); //$NON-NLS-1$
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
		args.addAll(Arrays.asList(getProperty("QMAKE_CXXFLAGS").split(" "))); //$NON-NLS-1$ //$NON-NLS-2$
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

		String[] includePaths = getProperty("INCLUDEPATH").split(" "); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < includePaths.length; ++i) {
			Path path = Paths.get(includePaths[i]);
			if (!path.isAbsolute()) {
				try {
					includePaths[i] = getBuildDirectory().resolve(path).toString();
				} catch (CoreException e) {
					Activator.log(e);
				}
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

			Path makeCommand = getMakeCommand();
			if (makeCommand == null) {
				errStream.write("'make' not found.\n");
				return null;
			}

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				Path buildDir = getBuildDirectory();
				if (!buildDir.resolve("Makefile").toFile().exists()) { //$NON-NLS-1$
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
					watchProcess(process, new IConsoleParser[0], console);
				}

				// run make
				ProcessBuilder processBuilder = new ProcessBuilder(makeCommand.toString(), "all").directory(buildDir.toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				outStream.write(makeCommand.toString() + '\n');
				watchProcess(process, new IConsoleParser[] { epm }, console);
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

			Path makeCommand = getMakeCommand();
			if (makeCommand == null) {
				errStream.write("'make' not found.\n");
				return;
			}

			Path buildDir = getBuildDirectory();

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				// run make
				ProcessBuilder processBuilder = new ProcessBuilder(makeCommand.toString(), "clean") //$NON-NLS-1$
						.directory(buildDir.toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				outStream.write(makeCommand.toString() + "clean\n"); //$NON-NLS-1$
				watchProcess(process, new IConsoleParser[] { epm }, console);
			}

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Cleaning " + project.getName(), e)); //$NON-NLS-1$
		}
	}

	public Path getMakeCommand() {
		Path makeCommand = findCommand("make"); //$NON-NLS-1$
		if (makeCommand == null) {
			makeCommand = findCommand("mingw32-make"); //$NON-NLS-1$
		}
		return makeCommand;
	}

}
