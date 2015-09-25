/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.qt.core.QtPlugin;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;

public class QtBuildConfiguration extends CBuildConfiguration {

	private QtInstall qtInstall;
	private String launchMode;
	private Map<String, String> properties;

	public QtBuildConfiguration(IBuildConfiguration config) {
		super(config);
	}

	private static Map<IBuildConfiguration, QtBuildConfiguration> cache = new HashMap<>();

	public static class Factory implements IAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			if (adapterType.equals(QtBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
				synchronized (cache) {
					IBuildConfiguration config = (IBuildConfiguration) adaptableObject;
					QtBuildConfiguration qtConfig = cache.get(config);
					if (qtConfig == null) {
						qtConfig = new QtBuildConfiguration(config);
						cache.put(config, qtConfig);
					}
					return (T) qtConfig;
				}
			}
			return null;
		}

		@Override
		public Class<?>[] getAdapterList() {
			return new Class<?>[] { QtBuildConfiguration.class };
		}
	}

	public static QtBuildConfiguration getConfig(IProject project, String os, String arch, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		// return it if it exists already
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			QtBuildConfiguration qtConfig = config.getAdapter(QtBuildConfiguration.class);
			QtInstall qtInstall = qtConfig.getQtInstall();
			if (qtInstall != null && qtInstall.supports(os, arch) && launchMode.equals(qtConfig.getLaunchMode())) {
				return qtConfig;
			}
		}

		// Nope, create it
		for (QtInstall qtInstall : QtInstallManager.instance.getInstalls()) {
			if (qtInstall.supports(os, arch)) {
				Set<String> configNames = new HashSet<>();
				for (IBuildConfiguration config : project.getBuildConfigs()) {
					configNames.add(config.getName());
				}
				String baseName = qtInstall.getSpec() + ":" + launchMode; //$NON-NLS-1$
				String newName = baseName;
				int n = 0;
				while (configNames.contains(newName)) {
					newName = baseName + (++n);
				}
				configNames.add(newName);
				IProjectDescription projectDesc = project.getDescription();
				projectDesc.setBuildConfigs(configNames.toArray(new String[configNames.size()]));
				project.setDescription(projectDesc, monitor);

				QtBuildConfiguration qtConfig = project.getBuildConfig(newName).getAdapter(QtBuildConfiguration.class);
				qtConfig.setup(qtInstall, launchMode);
				return qtConfig;
			}
		}
		return null;
	}

	public QtInstall getQtInstall() {
		if (qtInstall == null) {
			// TODO set based on settings
		}
		return qtInstall;
	}

	private String getLaunchMode() {
		if (launchMode != null) {
			// TODO set based on settings
		}
		return launchMode;
	}

	private void setup(QtInstall qtInstall, String launchMode) {
		this.qtInstall = qtInstall;
		this.launchMode = launchMode;
		// TODO save settings
	}

	public String getQmakeCommand() {
		return qtInstall.getQmakePath().toString();
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

	public IFolder getBuildFolder() {
		return getProject().getFolder("build").getFolder(getBuildConfiguration().getName()); //$NON-NLS-1$
	}

	public Path getBuildDirectory() {
		return getBuildFolder().getLocation().toFile().toPath();
	}

	public String getProperty(String key) {
		if (properties == null) {
			List<String> cmd = new ArrayList<>();
			cmd.add(getQmakeCommand());
			cmd.add("-E"); //$NON-NLS-1$

			String config = getQmakeConfig();
			if (config != null) {
				cmd.add(config);
			}

			cmd.add(getProjectFile().toString());

			try {
				Process proc = new ProcessBuilder(cmd).directory(getBuildDirectory().toFile()).start();
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
					properties = new HashMap<>();
					for (String line = reader.readLine(); line != null; line = reader.readLine()) {
						if (line.contains("=")) { //$NON-NLS-1$
							String[] parts = line.split("="); //$NON-NLS-1$
							if (parts.length == 2) {
								properties.put(parts[0].trim(), parts[1].trim());
							}
						}
					}
				}
			} catch (IOException e) {
				QtPlugin.log(e);
			}
		}

		return properties != null ? properties.get(key) : null;
	}

	@Override
	public IScannerInfo getScannerInfo(IResource resource) throws CoreException {
		IScannerInfo info = super.getScannerInfo(resource);
		if (info == null) {
			List<String> cmd = new ArrayList<>();
			cmd.add(getProperty("QMAKE_CXX")); //$NON-NLS-1$
			cmd.addAll(Arrays.asList(getProperty("QMAKE_CXXFLAGS").split(" "))); //$NON-NLS-1$ //$NON-NLS-2$

			for (String include : getProperty("INCLUDEPATH").split(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
				cmd.add("-I"); //$NON-NLS-1$
				cmd.add(include);
			}

			cmd.add("-o"); //$NON-NLS-1$
			cmd.add("-"); //$NON-NLS-1$

			// TODO need to make sure this path is valid
			// The gcc toolchain uses IFile to make sure it exists
			cmd.add(resource.getFullPath().toPortableString());

			ILanguage language = LanguageManager.getInstance()
					.getLanguage(CCorePlugin.getContentType(getProject(), resource.getName()), getProject()); // $NON-NLS-1$
			putScannerInfo(language, getToolChain().getScannerInfo(getBuildFolder(), cmd));
		}
		return info;
	}

}
