/*******************************************************************************
 * Copyright (c) 2017, 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *     Mathema      - Refactor
 *******************************************************************************/
package org.eclipse.cdt.docker.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.ICommandLauncherFactory;
import org.eclipse.cdt.core.ICommandLauncherFactory2;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.docker.launcher.Messages;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildConfigurationData;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;

@SuppressWarnings("restriction")
public class ContainerCommandLauncherFactory implements ICommandLauncherFactory, ICommandLauncherFactory2 {

	private IProject m_project;

	/**
	 * Helper-Struct
	 */
	private class ImageConnection {
		final String connectionName;
		final String imageName;

		public ImageConnection(String connectionName, String imageName) {
			this.connectionName = connectionName;
			this.imageName = imageName;
		}

	}

	/**
	 * Filter out paths that should not be copied from the container
	 * @param dirs A List of paths that should be filtered
	 * @return The filtered list
	 */
	private List<String> filterOutLocalPaths(List<String> dirs) {
		String prj = m_project.getLocation().toString();
		return dirs.stream().filter(x -> !(x.startsWith(prj) || x.startsWith("/${ProjName}")) //$NON-NLS-1$
		).collect(Collectors.toList());

	}

	/**
	 * Transform a string into something that can be used as path name
	 * @param name The name that should be used as part of a path
	 * @return A string that should be usable as path name
	 */
	private String normalizeToPathName(String name) {
		String cleanName = name.replace("unix:///", "unix_"); //$NON-NLS-1$ //$NON-NLS-2$
		cleanName = cleanName.replace("tcp://", "tcp_"); //$NON-NLS-1$ //$NON-NLS-2$
		cleanName = cleanName.replaceAll("[:/.]", "_"); //$NON-NLS-1$ //$NON-NLS-2$#
		assert Path.ROOT.isValidSegment(cleanName) : "Invalid Path - please file a bug"; //$NON-NLS-1$
		return cleanName;
	}

	/**
	 * Convert the ImageConnection to the local path where the data is mirrored to
	 * @param imgCon The connection
	 * @return A host path
	 */
	private IPath getHostMirrorPath(ImageConnection imgCon) {
		IPath pluginPath = Platform.getStateLocation(Platform.getBundle(DockerLaunchUIPlugin.PLUGIN_ID))
				.append("HEADERS"); //$NON-NLS-1$
		pluginPath = pluginPath.append(normalizeToPathName(imgCon.connectionName));
		pluginPath = pluginPath.append(normalizeToPathName(imgCon.imageName));
		return pluginPath;
	}

	/**
	 * Mirror a list of paths from the imgCon to the host
	 * @param imgCon The image and connection to copy from
	 * @param paths The paths to copy
	 * @return Whether the operation was successful
	 */
	private boolean getPaths(ImageConnection imgCon, List<String> paths) {
		IPath targetPath = getHostMirrorPath(imgCon);

		ContainerLauncher launcher = new ContainerLauncher();
		int rv = launcher.fetchContainerDirs(imgCon.connectionName, imgCon.imageName, paths, null, targetPath);
		return (rv == 0);

	}

	/**
	 * Get the Image and Connection from the current project configuration
	 * @return An ImageConnection or null if connection and image are configured
	 */
	private ImageConnection getImgCnn() {
		ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(m_project)
				.getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
		IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
		if (props == null) {
			return null;
		}

		String enablementProperty = props.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
		if (enablementProperty == null) {
			return null;
		}

		if (!Boolean.parseBoolean(enablementProperty)) {
			return null;
		}

		String connectionName = props.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		String imageName = props.getProperty(ContainerCommandLauncher.IMAGE_ID);
		if (connectionName == null || connectionName.isEmpty() || imageName == null || imageName.isEmpty()) {
			DockerLaunchUIPlugin.logErrorMessage(Messages.ContainerCommandLauncher_invalid_values);
			return null;
		}

		return new ImageConnection(connectionName, imageName);

	}

	@Override
	public ICommandLauncher getCommandLauncher(IProject project) {
		m_project = project;
		// check if container build enablement has been checked
		ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(project, false)
				.getActiveConfiguration();

		return getCommandLauncher(cfgd);
	}

	@Override
	public ICommandLauncher getCommandLauncher(ICConfigurationDescription cfgd) {
		// check if container build enablement has been checked
		IConfiguration cfg = null;

		try {
			if (cfgd instanceof CConfigurationDescriptionCache) {
				CConfigurationData data = ((CConfigurationDescriptionCache) cfgd).getConfigurationData();
				if (data instanceof BuildConfigurationData) {
					cfg = ((BuildConfigurationData) data).getConfiguration();
				}
			}

			if (cfg == null) {
				cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
			}
		} catch (Exception e) {
			// ignore
		}

		if (cfg == null) {
			return null;
		}

		m_project = (IProject) cfg.getManagedProject().getOwner();
		IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
		if (props != null) {
			String enablementProperty = props.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
			if (enablementProperty != null) {
				boolean enableContainer = Boolean.parseBoolean(enablementProperty);
				// enablement has occurred, we can return a
				// ContainerCommandLauncher
				if (enableContainer) {
					return new ContainerCommandLauncher();
				}
			}
		}
		return null;
	}

	@Override
	public ICommandLauncher getCommandLauncher(ICBuildConfiguration cfgd) {
		try {
			m_project = cfgd.getBuildConfiguration().getProject();
		} catch (CoreException e1) {
			return null;
		}
		// check if container linux os is set
		IToolChain toolchain;
		try {
			toolchain = cfgd.getToolChain();
			if (toolchain != null) {
				if (ContainerTargetTypeProvider.CONTAINER_LINUX.equals(toolchain.getProperty(IToolChain.ATTR_OS))) {
					return new ContainerCommandLauncher();
				}
			}
		} catch (CoreException e) {
			DockerLaunchUIPlugin.log(e);
		}
		return null;
	}

	@Override
	public void registerLanguageSettingEntries(IProject project, List<? extends ICLanguageSettingEntry> langEntries) {
		if (langEntries == null) {
			// langEntries can be null when the last item is removed from a list,
			// see org.eclipse.cdt.internal.ui.language.settings.providers.LanguageSettingsEntriesTab.saveEntries(ILanguageSettingsProvider, List<ICLanguageSettingEntry>)
			// for an example that passes null to mean "use parent entries instead".
			return;
		}

		@SuppressWarnings("unchecked")
		List<ICLanguageSettingEntry> entries = (List<ICLanguageSettingEntry>) langEntries;

		List<String> paths = new ArrayList<>();
		for (ICLanguageSettingEntry entry : entries) {
			if (entry instanceof ICIncludePathEntry) {
				paths.add(entry.getValue());
			} else if (entry instanceof ICIncludeFileEntry) {
				paths.add(new org.eclipse.core.runtime.Path(entry.getValue()).removeLastSegments(1).toString());
			}
		}

		paths = filterOutLocalPaths(paths);
		if (paths.size() == 0) {
			return;
		}
		ImageConnection imgCnn = getImgCnn();
		getPaths(imgCnn, paths);

	}

	/**
	 * @since 1.2
	 */
	@Override
	public List<String> verifyIncludePaths(ICBuildConfiguration cfgd, List<String> includePaths) {
		IToolChain toolchain = null;

		try {
			toolchain = cfgd.getToolChain();
		} catch (CoreException e) {
			DockerLaunchUIPlugin.log(e);
		}

		if (toolchain == null) {
			return includePaths;
		}

		if (!ContainerTargetTypeProvider.CONTAINER_LINUX.equals(toolchain.getProperty(IToolChain.ATTR_OS))) {
			DockerLaunchUIPlugin.logErrorMessage(Messages.ContainerCommandLauncher_invalid_container_type);
			return includePaths;
		}

		String connectionName = toolchain.getProperty(IContainerLaunchTarget.ATTR_CONNECTION_URI);
		String imageName = toolchain.getProperty(IContainerLaunchTarget.ATTR_IMAGE_ID);

		if (connectionName == null || connectionName.isEmpty() || imageName == null || imageName.isEmpty()) {
			DockerLaunchUIPlugin.logErrorMessage(Messages.ContainerCommandLauncher_invalid_values);
			return includePaths;
		}

		ImageConnection imgCnn = new ImageConnection(connectionName, imageName);

		if (includePaths.isEmpty()) {
			// Bug 536884 - if no include entries, check if the copied
			// header files have been erased by the end-user in which
			// case mark that scanner info needs refreshing (only way
			// the headers will be recopied)
			// TODO: fix this in a minor release to be an additional method
			// that can be registered by the removal of the header files
			IPath pluginPath = getHostMirrorPath(imgCnn);
			toolchain.setProperty("cdt.needScannerRefresh", //$NON-NLS-1$
					pluginPath.toFile().exists() ? "false" : "true"); //$NON-NLS-1$ //$NON-NLS-2$
			return includePaths;
		}

		List<String> fetchPaths = filterOutLocalPaths(includePaths);
		if (fetchPaths.size() == 0) {
			return includePaths;
		}

		if (!getPaths(imgCnn, includePaths)) {
			// There should be sufficient log messages by the root cause
			return includePaths;
		}

		// Do the actual work

		IPath tpath = getHostMirrorPath(imgCnn);
		Set<IPath> copiedVolumes = ContainerLauncher.getCopiedVolumes(tpath);
		List<String> newEntries = new ArrayList<>();

		for (String path : includePaths) {
			if (copiedVolumes.contains(new Path(path))) {
				IPath newPath = tpath.append(path);
				String newEntry = newPath.toString();
				newEntries.add(newEntry);
			} else {
				newEntries.add(path);
			}
		}
		return newEntries;

	}

	@Override
	public List<ICLanguageSettingEntry> verifyLanguageSettingEntries(IProject project,
			List<ICLanguageSettingEntry> entries) {
		if (entries == null) {
			return null;
		}

		ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(project)
				.getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
		IOptionalBuildProperties props = cfg.getOptionalBuildProperties();

		if (props == null)
			return entries;

		String enablementProperty = props.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
		if (enablementProperty == null)
			return entries;

		boolean enableContainer = Boolean.parseBoolean(enablementProperty);
		if (!enableContainer)
			return entries;

		String connectionName = props.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		String imageName = props.getProperty(ContainerCommandLauncher.IMAGE_ID);
		if (connectionName == null || connectionName.isEmpty() || imageName == null || imageName.isEmpty()) {
			DockerLaunchUIPlugin.logErrorMessage(Messages.ContainerCommandLauncher_invalid_values);
			return entries;
		}

		IPath tpath = getHostMirrorPath(new ImageConnection(connectionName, imageName));
		Set<IPath> copiedVolumes = ContainerLauncher.getCopiedVolumes(tpath);
		List<ICLanguageSettingEntry> newEntries = new ArrayList<>();

		for (ICLanguageSettingEntry entry : entries) {
			if (entry instanceof ICIncludePathEntry) {
				Path tp = new Path(((ICIncludePathEntry) entry).getName().toString());
				if (copiedVolumes.stream().anyMatch(p -> p.isPrefixOf(tp))) {
					IPath newPath = tpath.append(entry.getName());
					CIncludePathEntry newEntry = new CIncludePathEntry(newPath.toString(), entry.getFlags());
					newEntries.add(newEntry);
					continue;
				}
			}
			if (entry instanceof ICIncludeFileEntry) {
				IPath tp = new Path(((ICIncludeFileEntry) entry).getName()).removeLastSegments(1);
				if (copiedVolumes.stream().anyMatch(p -> p.isPrefixOf(tp))) {
					IPath newPath = tpath.append(entry.getName());
					CIncludeFileEntry newEntry = new CIncludeFileEntry(newPath.toString(), entry.getFlags());
					newEntries.add(newEntry);
					continue;
				}
			}
			newEntries.add(entry);
		}
		return newEntries;
	}

}
