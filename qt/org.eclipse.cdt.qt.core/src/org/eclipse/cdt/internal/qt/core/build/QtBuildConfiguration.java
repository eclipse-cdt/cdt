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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.build.core.CBuildConfiguration;
import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.build.core.IToolChainManager;
import org.eclipse.cdt.core.build.ICBuildConfigEnvVarSupplier;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class QtBuildConfiguration extends CBuildConfiguration implements ICBuildConfiguration, ICBuildConfigEnvVarSupplier {

	private static IQtInstallManager qtInstallManager = Activator.getService(IQtInstallManager.class);
	private static IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);

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

	public static QtBuildConfiguration createConfiguration(IProject project, ILaunchTarget target, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		for (IQtInstall qtInstall : qtInstallManager.getInstalls()) {
			if (qtInstallManager.supports(qtInstall, target)) {
				// Create the build config
				Set<String> configNames = new HashSet<>();
				for (IBuildConfiguration config : project.getBuildConfigs()) {
					configNames.add(config.getName());
				}
				String baseName = qtInstall.getSpec() + "." + launchMode; //$NON-NLS-1$
				String newName = baseName;
				int n = 0;
				while (configNames.contains(newName)) {
					newName = baseName + (++n);
				}
				configNames.add(newName);
				IProjectDescription projectDesc = project.getDescription();
				projectDesc.setBuildConfigs(configNames.toArray(new String[configNames.size()]));
				project.setDescription(projectDesc, monitor);

				// Find the toolchain
				for (IToolChain toolChain : toolChainManager.getToolChainsSupporting(target)) {
					if (qtInstallManager.supports(qtInstall, toolChain)) {
						QtBuildConfiguration qtConfig = new QtBuildConfiguration(project.getBuildConfig(newName),
								toolChain, qtInstall, launchMode);
						return qtConfig;
						// TODO what if there's more than toolChain supported?
					}
				}
			}
		}
		return null;
	}

	private QtBuildConfiguration(IBuildConfiguration config, IToolChain toolChain, IQtInstall qtInstall,
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (ICBuildConfigEnvVarSupplier.class.equals(adapter)) {
			return (T) this;
		} else {
			return super.getAdapter(adapter);
		}
	}
	
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

	public Path getProgramPath() throws CoreException {
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
		case Platform.OS_WIN32:
			Path releaseFolder = getBuildDirectory().resolve("release");
			return releaseFolder.resolve(projectName + ".exe");
		default:
			throw new CoreException(
					new Status(IStatus.ERROR, Activator.ID, "platform not supported: " + Platform.getOS()));
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
				getToolChain().setEnvironment(procBuilder.environment());
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
	
}
