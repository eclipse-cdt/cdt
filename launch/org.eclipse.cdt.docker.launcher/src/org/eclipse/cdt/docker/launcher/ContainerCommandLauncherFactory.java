/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.docker.launcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.ICommandLauncherFactory;
import org.eclipse.cdt.core.ICommandLauncherFactory2;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;

@SuppressWarnings("restriction")
public class ContainerCommandLauncherFactory implements ICommandLauncherFactory, ICommandLauncherFactory2 {

	private IProject project;

	@Override
	public ICommandLauncher getCommandLauncher(IProject project) {
		this.project = project;
		// check if container build enablement has been checked
		ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(project, false)
				.getActiveConfiguration();

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
	public ICommandLauncher getCommandLauncher(ICConfigurationDescription cfgd) {
		// check if container build enablement has been checked
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
		// TODO: figure out why this occurs
		if (cfg == null) {
			return null;
		}
		this.project = (IProject) cfg.getManagedProject().getOwner();
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
			this.project = cfgd.getBuildConfiguration().getProject();
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
		@SuppressWarnings("unchecked")
		List<ICLanguageSettingEntry> entries = (List<ICLanguageSettingEntry>) langEntries;
		ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(project)
				.getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
		IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
		if (props != null) {
			String enablementProperty = props.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
			if (enablementProperty != null) {
				boolean enableContainer = Boolean.parseBoolean(enablementProperty);
				if (enableContainer) {
					String connectionName = props.getProperty(ContainerCommandLauncher.CONNECTION_ID);
					String imageName = props.getProperty(ContainerCommandLauncher.IMAGE_ID);
					if (connectionName == null || connectionName.isEmpty() || imageName == null
							|| imageName.isEmpty()) {
						DockerLaunchUIPlugin.logErrorMessage(Messages.ContainerCommandLauncher_invalid_values);
						return;
					}
					ContainerLauncher launcher = new ContainerLauncher();
					List<String> paths = new ArrayList<>();
					for (ICLanguageSettingEntry entry : entries) {
						if (entry instanceof ICIncludePathEntry) {
							paths.add(entry.getValue());
						}
					}
					if (paths.size() > 0) {
						// Create a directory to put the header files for
						// the image. Use the connection name to form
						// the directory name as the connection may be
						// connected to a different repo using the same
						// image name.
						IPath pluginPath = Platform.getStateLocation(Platform.getBundle(DockerLaunchUIPlugin.PLUGIN_ID))
								.append("HEADERS"); //$NON-NLS-1$
						pluginPath.toFile().mkdir();
						pluginPath = pluginPath.append(getCleanName(connectionName));
						pluginPath.toFile().mkdir();
						// To allow the user to later manage the headers, store
						// the
						// real connection name in a file.
						IPath connectionNamePath = pluginPath.append(".name"); //$NON-NLS-1$
						File f = connectionNamePath.toFile();
						try {
							f.createNewFile();
							try (FileWriter writer = new FileWriter(f);
									BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
								bufferedWriter.write(connectionName);
								bufferedWriter.newLine();
							} catch (IOException e) {
								DockerLaunchUIPlugin.log(e);
								return;
							}
							pluginPath = pluginPath.append(getCleanName(imageName));
							pluginPath.toFile().mkdir();
							// To allow the user to later manage the headers,
							// store the
							// real image name in a file.
							IPath imageNamePath = pluginPath.append(".name"); //$NON-NLS-1$
							f = imageNamePath.toFile();
							f.createNewFile();
							try (FileWriter writer = new FileWriter(f);
									BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
								bufferedWriter.write(imageName);
								bufferedWriter.newLine();
							} catch (IOException e) {
								DockerLaunchUIPlugin.log(e);
								return;
							}
						} catch (IOException e) {
							DockerLaunchUIPlugin.log(e);
							return;
						}
						IPath hostDir = pluginPath;
						List<String> excludeList = new ArrayList<>();
						excludeList.add(project.getLocation().toString());
						@SuppressWarnings("unused")
						int status = launcher.fetchContainerDirs(connectionName, imageName, paths, excludeList,
								hostDir);
					}
				}
			}
		}

	}

	/**
	 * @since 1.2
	 */
	@Override
	public List<String> verifyIncludePaths(ICBuildConfiguration cfgd, List<String> includePaths) {
		IToolChain toolchain = null;
		boolean isContainerEnabled = false;
		try {
			toolchain = cfgd.getToolChain();
			if (toolchain != null) {
				if (ContainerTargetTypeProvider.CONTAINER_LINUX.equals(toolchain.getProperty(IToolChain.ATTR_OS))) {
					isContainerEnabled = true;
				}
			}
		} catch (CoreException e) {
			DockerLaunchUIPlugin.log(e);
		}

		if (isContainerEnabled) {
			String connectionName = toolchain.getProperty(IContainerLaunchTarget.ATTR_CONNECTION_URI);
			String imageName = toolchain.getProperty(IContainerLaunchTarget.ATTR_IMAGE_ID);
			if (connectionName == null || connectionName.isEmpty() || imageName == null || imageName.isEmpty()) {
				DockerLaunchUIPlugin.logErrorMessage(Messages.ContainerCommandLauncher_invalid_values);
				return includePaths;
			}
			if (includePaths.size() > 0) {
				ContainerLauncher launcher = new ContainerLauncher();
				// Create a directory to put the header files for
				// the image. Use the connection name to form
				// the directory name as the connection may be
				// connected to a different repo using the same
				// image name.
				IPath pluginPath = Platform.getStateLocation(Platform.getBundle(DockerLaunchUIPlugin.PLUGIN_ID))
						.append("HEADERS"); //$NON-NLS-1$
				pluginPath.toFile().mkdir();
				pluginPath = pluginPath.append(getCleanName(connectionName));
				pluginPath.toFile().mkdir();
				// To allow the user to later manage the headers, store
				// the
				// real connection name in a file.
				IPath connectionNamePath = pluginPath.append(".name"); //$NON-NLS-1$
				File f = connectionNamePath.toFile();
				try {
					f.createNewFile();
					try (FileWriter writer = new FileWriter(f);
							BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
						bufferedWriter.write(connectionName);
						bufferedWriter.newLine();
					} catch (IOException e) {
						DockerLaunchUIPlugin.log(e);
						return includePaths;
					}
					pluginPath = pluginPath.append(getCleanName(imageName));
					pluginPath.toFile().mkdir();
					// To allow the user to later manage the headers,
					// store the
					// real image name in a file.
					IPath imageNamePath = pluginPath.append(".name"); //$NON-NLS-1$
					f = imageNamePath.toFile();
					f.createNewFile();
					try (FileWriter writer = new FileWriter(f);
							BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
						bufferedWriter.write(imageName);
						bufferedWriter.newLine();
					} catch (IOException e) {
						DockerLaunchUIPlugin.log(e);
						return includePaths;
					}
				} catch (IOException e) {
					DockerLaunchUIPlugin.log(e);
					return includePaths;
				}
				IPath hostDir = pluginPath;
				// exclude project directories from any copying operation
				List<String> excludeList = new ArrayList<>();
				excludeList.add(project.getLocation().toString());
				int status = launcher.fetchContainerDirsSync(connectionName, imageName, includePaths, excludeList,
						hostDir);
				if (status == 0) {
					Set<String> copiedVolumes = launcher.getCopiedVolumes(connectionName, imageName);
					List<String> newEntries = new ArrayList<>();

					for (String path : includePaths) {
						if (copiedVolumes.contains(path)) {
							IPath newPath = hostDir.append(path);
							String newEntry = newPath.toOSString();
							newEntries.add(newEntry);
						} else {
							newEntries.add(path);
						}
					}
					return newEntries;
				}

			} else {
				// Bug 536884 - if no include entries, check if the copied
				// header files have been erased by the end-user in which
				// case mark that scanner info needs refreshing (only way
				// the headers will be recopied)
				// TODO: fix this in a minor release to be an additional method
				// that can be registered by the removal of the header files
				IPath pluginPath = Platform.getStateLocation(Platform.getBundle(DockerLaunchUIPlugin.PLUGIN_ID))
						.append("HEADERS").append(getCleanName(connectionName)) //$NON-NLS-1$
						.append(getCleanName(imageName));
				toolchain.setProperty("cdt.needScannerRefresh", //$NON-NLS-1$
						pluginPath.toFile().exists() ? "false" : "true"); //$NON-NLS-2$
			}
		}
		return includePaths;
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
		if (props != null) {
			String enablementProperty = props.getProperty(ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
			if (enablementProperty != null) {
				boolean enableContainer = Boolean.parseBoolean(enablementProperty);
				if (enableContainer) {
					String connectionName = props.getProperty(ContainerCommandLauncher.CONNECTION_ID);
					String imageName = props.getProperty(ContainerCommandLauncher.IMAGE_ID);
					if (connectionName == null || connectionName.isEmpty() || imageName == null
							|| imageName.isEmpty()) {
						DockerLaunchUIPlugin.logErrorMessage(Messages.ContainerCommandLauncher_invalid_values);
						return entries;
					}

					ContainerLauncher launcher = new ContainerLauncher();
					Set<String> copiedVolumes = launcher.getCopiedVolumes(connectionName, imageName);
					List<ICLanguageSettingEntry> newEntries = new ArrayList<>();
					IPath pluginPath = Platform.getStateLocation(Platform.getBundle(DockerLaunchUIPlugin.PLUGIN_ID));
					IPath hostDir = pluginPath.append("HEADERS") //$NON-NLS-1$
							.append(getCleanName(connectionName)).append(getCleanName(imageName));

					for (ICLanguageSettingEntry entry : entries) {
						if (entry instanceof ICIncludePathEntry) {
							if (copiedVolumes.contains(((ICIncludePathEntry) entry).getName().toString())) {
								// //$NON-NLS-2$
								IPath newPath = hostDir.append(entry.getName());
								CIncludePathEntry newEntry = new CIncludePathEntry(newPath.toString(),
										entry.getFlags());
								newEntries.add(newEntry);
								continue;
							} else {
								newEntries.add(entry);
							}
						} else {
							newEntries.add(entry);
						}
					}
					return newEntries;
				}
			}
		}
		return entries;
	}

	private String getCleanName(String name) {
		String cleanName = name.replace("unix:///", "unix_"); //$NON-NLS-1$ //$NON-NLS-2$
		cleanName = cleanName.replace("tcp://", "tcp_"); //$NON-NLS-1$ //$NON-NLS-2$
		return cleanName.replaceAll("[:/.]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
