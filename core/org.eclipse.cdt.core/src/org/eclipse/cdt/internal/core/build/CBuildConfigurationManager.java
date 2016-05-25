/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.build;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class CBuildConfigurationManager implements ICBuildConfigurationManager, IResourceChangeListener {

	private static class Provider {
		private String id;
		private String natureId;
		private IConfigurationElement element;
		private ICBuildConfigurationProvider provider;

		public Provider(IConfigurationElement element) {
			this.id = element.getAttribute("id"); //$NON-NLS-1$
			this.natureId = element.getAttribute("natureId"); //$NON-NLS-1$
			this.element = element;
		}

		public String getId() {
			return id;
		}

		public ICBuildConfigurationProvider getProvider() {
			if (provider == null) {
				try {
					provider = (ICBuildConfigurationProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
				} catch (CoreException e) {
					CCorePlugin.log(e.getStatus());
				}
			}
			return provider;
		}

		public boolean supports(IProject project) {
			try {
				if (natureId != null) {
					return project.hasNature(natureId);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e.getStatus());
			}
			return false;
		}
	}

	private Map<String, Provider> providers;
	private Map<IBuildConfiguration, ICBuildConfiguration> configs = new HashMap<>();
	private Set<IBuildConfiguration> noConfigs = new HashSet<>();

	public CBuildConfigurationManager() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	private void initProviders() {
		if (providers == null) {
			providers = new HashMap<>();

			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
					"buildConfigProvider"); //$NON-NLS-1$
			for (IConfigurationElement element : point.getConfigurationElements()) {
				Provider provider = new Provider(element);
				providers.put(provider.getId(), provider);
			}
		}
	}

	private Provider getProviderDelegate(String id) {
		return providers.get(id);
	}

	@Override
	public ICBuildConfigurationProvider getProvider(String id) {
		initProviders();
		Provider provider = providers.get(id);
		return provider != null ? provider.getProvider() : null;
	}
	
	public ICBuildConfigurationProvider getProvider(String id, IProject project) {
		initProviders();
		Provider provider = getProviderDelegate(id);
		if (provider != null && provider.supports(project)) {
			return provider.getProvider();
		}
		return null;
	}

	public ICBuildConfigurationProvider getProvider(IProject project) throws CoreException {
		initProviders();
		for (Provider provider : providers.values()) {
			if (provider.supports(project)) {
				return provider.getProvider();
			}
		}
		return null;
	}

	@Override
	public IBuildConfiguration createBuildConfiguration(ICBuildConfigurationProvider provider,
			IProject project, String configName, IProgressMonitor monitor) throws CoreException {
		String name = provider.getId() + '/' + configName;

		Set<String> names = new HashSet<>();
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			names.add(config.getName());
		}

		IProjectDescription desc = project.getDescription();
		names.add(name);
		desc.setBuildConfigs(names.toArray(new String[names.size()]));
		project.setDescription(desc, monitor);

		return project.getBuildConfig(name);
	}

	@Override
	public void addBuildConfiguration(IBuildConfiguration buildConfig, ICBuildConfiguration cConfig) {
		synchronized (configs) {
			configs.put(buildConfig, cConfig);
		}
	}

	@Override
	public ICBuildConfiguration getBuildConfiguration(IBuildConfiguration buildConfig) throws CoreException {
		initProviders();
		synchronized (configs) {
			if (noConfigs.contains(buildConfig)) {
				return null;
			} else {
				ICBuildConfiguration config = configs.get(buildConfig);
				if (config != null) {
					return config;
				} else {
					String configName;
					ICBuildConfigurationProvider provider;
					if (IBuildConfiguration.DEFAULT_CONFIG_NAME.equals(buildConfig.getName())) {
						configName = ICBuildConfiguration.DEFAULT_NAME;
						provider = getProvider(buildConfig.getProject());
					} else {
						String[] segments = buildConfig.getName().split("/"); //$NON-NLS-1$
						if (segments.length == 2) {
							String providerId = segments[0];
							configName = segments[1];
							Provider delegate = getProviderDelegate(providerId);
							if (delegate != null && delegate.supports(buildConfig.getProject())) {
								provider = delegate.getProvider();
							} else {
								return null;
							}
						} else {
							// Not ours
							return null;
						}
					}

					if (provider != null) {
						config = provider.getCBuildConfiguration(buildConfig, configName);
						if (config != null) {
							configs.put(buildConfig, config);

							// Also make sure we reset the binary parser cache for the new config
							CModelManager.getDefault().resetBinaryParser(buildConfig.getProject());
							return config;
						}
					}

					noConfigs.add(buildConfig);
					return null;
				}
			}
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE
				|| event.getType() == IResourceChangeEvent.PRE_DELETE) {
			if (event.getResource().getType() == IResource.PROJECT) {
				IProject project = event.getResource().getProject();
				try {
					if (!project.isOpen() || !project.hasNature(CProjectNature.C_NATURE_ID))
						return;
				} catch (CoreException e) {
					CCorePlugin.log(e.getStatus());
					return;
				}

				// Clean up the configMap
				try {
					for (IBuildConfiguration buildConfig : project.getBuildConfigs()) {
						configs.remove(buildConfig);
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}

				// Clean up the config settings
				Preferences parentNode = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config"); //$NON-NLS-1$
				if (parentNode != null) {
					Preferences projectNode = parentNode.node(project.getName());
					if (projectNode != null) {
						try {
							projectNode.removeNode();
							parentNode.flush();
						} catch (BackingStoreException e) {
							CCorePlugin.log(e);
						}
					}
				}

				// Clean up the scanner info data
				IPath stateLoc = CCorePlugin.getDefault().getStateLocation();
				IPath scannerInfoPath = stateLoc.append(project.getName());
				Path directory = scannerInfoPath.toFile().toPath();
				if (!Files.exists(directory)) {
					return;
				}

				try {
					Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
								throws IOException {
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc)
								throws IOException {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

}
