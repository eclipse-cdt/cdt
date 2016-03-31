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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class QtBuildConfiguration extends CBuildConfiguration implements ICBuildConfiguration, IQtBuildConfiguration {

	private static IQtInstallManager qtInstallManager = Activator.getService(IQtInstallManager.class);

	private static final String QTINSTALL_NAME = "cdt.qt.install.name"; //$NON-NLS-1$
	private static final String LAUNCH_MODE = "cdt.qt.launchMode"; //$NON-NLS-1$

	private final IQtInstall qtInstall;
	private final String launchMode;
	private Map<String, String> properties;

	public QtBuildConfiguration(IBuildConfiguration config) {
		super(config);

		Preferences settings = getSettings();
		String installName = settings.get(QTINSTALL_NAME, ""); //$NON-NLS-1$
		if (!installName.isEmpty()) {
			IQtInstallManager manager = Activator.getService(IQtInstallManager.class);
			qtInstall = manager.getInstall(installName);
		} else {
			qtInstall = null;
		}

		launchMode = settings.get(LAUNCH_MODE, ""); //$NON-NLS-1$
	}

	QtBuildConfiguration(IBuildConfiguration config, IToolChain toolChain, IQtInstall qtInstall,
			String launchMode) throws CoreException {
		super(config, toolChain);
		this.qtInstall = qtInstall;
		this.launchMode = launchMode;

		Preferences settings = getSettings();
		settings.put(QTINSTALL_NAME, qtInstall.getName());
		settings.put(LAUNCH_MODE, launchMode);
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

	@Override
	public boolean supports(ILaunchTarget target, String launchMode) {
		return qtInstallManager.supports(qtInstall, target) && this.launchMode.equals(launchMode);
	}

	public IQtInstall getQtInstall() {
		return qtInstall;
	}

	public String getLaunchMode() {
		return launchMode;
	}

	public Path getQmakeCommand() {
		return qtInstall.getQmakePath();
	}

	public String getQmakeConfig() {
		switch (launchMode) {
		case "run": //$NON-NLS-1$
			return "CONFIG+=release"; //$NON-NLS-1$
		case "debug": //$NON-NLS-1$
			return "CONFIG+=debug"; //$NON-NLS-1$
		default:
			// TODO probably need an extension point for guidance
			return null;
		}
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
	public Path getProgramPath() {
		String projectName = getProject().getName();
		switch (Platform.getOS()) {
		case Platform.OS_MACOSX:
			// TODO this is mac local specific and really should be
			// in the config
			// TODO also need to pull the app name out of the pro
			// file name
			Path appFolder = getBuildDirectory().resolve(projectName + ".app");
			Path contentsFolder = appFolder.resolve("Contents");
			Path macosFolder = contentsFolder.resolve("MacOS");
			return macosFolder.resolve(projectName);
		case Platform.OS_WIN32: {
			Path releaseFolder = getBuildDirectory().resolve("release");
			return releaseFolder.resolve(projectName + ".exe");
		}
		default:
			Path releaseFolder = getBuildDirectory().resolve("release");
			return releaseFolder.resolve(projectName);
		}
	}

	public void setProgramEnvironment(Map<String, String> env) {
		Path libPath = getQtInstall().getLibPath();
		switch (Platform.getOS()) {
		case Platform.OS_MACOSX:
			String libPathEnv = env.get("DYLD_LIBRARY_PATH");
			if (libPathEnv == null) {
				libPathEnv = libPath.toString();
			} else {
				libPathEnv = libPath.toString() + File.pathSeparator + libPathEnv;
			}
			env.put("DYLD_LIBRARY_PATH", libPathEnv);
			break;
		case Platform.OS_WIN32:
			String path = env.get("PATH");
			// TODO really need a bin path
			// and resolve doesn't work properly on Windows
			path = "C:/Qt/5.5/mingw492_32/bin;" + path;
			env.put("PATH", path);
			break;
		}
	}

	public String getProperty(String key) {
		if (properties == null) {
			List<String> cmd = new ArrayList<>();
			cmd.add(getQmakeCommand().toString());
			cmd.add("-E"); //$NON-NLS-1$

			String config = getQmakeConfig();
			if (config != null) {
				cmd.add(config);
			}

			cmd.add(getProjectFile().toString());

			try {
				ProcessBuilder procBuilder = new ProcessBuilder(cmd).directory(getProjectFile().getParent().toFile());
				Process proc = procBuilder.start();
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
		try {
			IProject project = resource.getProject();
			IQtInstall qtInstall = getQtInstall();

			String cxx = getProperty("QMAKE_CXX"); //$NON-NLS-1$
			if (cxx == null) {
				Activator.log("No QMAKE_CXX for " + qtInstall.getSpec()); //$NON-NLS-1$
				return null;
			}
			String[] cxxSplit = cxx.split(" "); //$NON-NLS-1$
			String command = cxxSplit[0];

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
					includePaths[i] = getBuildDirectory().resolve(path).toString();
				}
			}

			Path dir = Paths.get(project.getLocationURI());
			IExtendedScannerInfo extendedInfo = getToolChain().getScannerInfo(command, args,
					Arrays.asList(includePaths), resource, dir);
			return extendedInfo;
		} catch (IOException e) {
			Activator.log(e);
		}
		return null;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

}
