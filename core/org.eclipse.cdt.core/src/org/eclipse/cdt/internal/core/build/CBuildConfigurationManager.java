/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager2;
import org.eclipse.cdt.core.build.ICBuildConfigurationProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.CoreModel;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class CBuildConfigurationManager
		implements ICBuildConfigurationManager, ICBuildConfigurationManager2, IResourceChangeListener {

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

	/**
	 * Resets configs. Used for testing only.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void reset() {
		configs = new HashMap<>();
		noConfigs = new HashSet<>();
	}

	public CBuildConfigurationManager() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	private synchronized void initProviders() {
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

	public ICBuildConfigurationProvider getProvider(IProject project) {
		initProviders();
		for (Provider provider : providers.values()) {
			if (provider.supports(project)) {
				return provider.getProvider();
			}
		}
		return null;
	}

	@Override
	public boolean hasConfiguration(ICBuildConfigurationProvider provider, IProject project, String configName)
			throws CoreException {
		String name = provider.getId() + '/' + configName;
		return project.hasBuildConfig(name);
	}

	@Override
	public IBuildConfiguration createBuildConfiguration(ICBuildConfigurationProvider provider, IProject project,
			String configName, IProgressMonitor monitor) throws CoreException {
		String name = provider.getId() + '/' + configName;

		CoreModel m = CoreModel.getDefault();
		synchronized (m) {
			Set<String> names = new HashSet<>();
			for (IBuildConfiguration config : project.getBuildConfigs()) {
				names.add(config.getName());
			}
			// need to add default config name because it can be active by
			// default without being in the build config list used above
			names.add(IBuildConfiguration.DEFAULT_CONFIG_NAME);

			IProjectDescription desc = project.getDescription();
			names.add(name);
			desc.setBuildConfigs(names.toArray(new String[names.size()]));
			project.setDescription(desc, monitor);
		}

		return project.getBuildConfig(name);
	}

	@Override
	public void addBuildConfiguration(IBuildConfiguration buildConfig, ICBuildConfiguration cConfig) {
		synchronized (configs) {
			configs.put(buildConfig, cConfig);
		}

		// reset the binary parsers
		CModelManager.getDefault().resetBinaryParser(buildConfig.getProject());
	}

	@Override
	public void recheckConfigs() {
		initProviders();
		ICBuildConfiguration config = null;
		Set<IProject> projects = new HashSet<>();
		synchronized (configs) {
			Iterator<IBuildConfiguration> iterator = noConfigs.iterator();
			while (iterator.hasNext()) {
				IBuildConfiguration buildConfig = iterator.next();
				String configName = null;
				ICBuildConfigurationProvider provider = null;
				String[] segments = buildConfig.getName().split("/"); //$NON-NLS-1$
				if (segments.length == 2) {
					String providerId = segments[0];
					configName = segments[1];
					Provider delegate = getProviderDelegate(providerId);
					if (delegate != null && delegate.supports(buildConfig.getProject())) {
						provider = delegate.getProvider();
					}
				}

				if (provider != null) {
					try {
						config = provider.getCBuildConfiguration(buildConfig, configName);
					} catch (CoreException e) {
						// do nothing
					}
					if (config != null) {
						iterator.remove();
						projects.add(buildConfig.getProject());
						configs.put(buildConfig, config);
					}
				}

			}
		}

		for (IProject project : projects) {
			// Do this outside of the synchronized block to avoid deadlock with
			// BinaryRunner
			CModelManager.getDefault().resetBinaryParser(project);
		}
	}

	@Override
	public ICBuildConfiguration getBuildConfiguration(IBuildConfiguration buildConfig) throws CoreException {
		initProviders();
		ICBuildConfiguration config = null;
		boolean resetBinaryParser = false;
		synchronized (configs) {
			if (!noConfigs.contains(buildConfig)) {
				config = configs.get(buildConfig);
				if (config == null) {
					String configName = null;
					ICBuildConfigurationProvider provider = null;
					String[] segments = buildConfig.getName().split("/"); //$NON-NLS-1$
					if (segments.length == 2) {
						String providerId = segments[0];
						configName = segments[1];
						Provider delegate = getProviderDelegate(providerId);
						if (delegate != null && delegate.supports(buildConfig.getProject())) {
							provider = delegate.getProvider();
						}
					}

					if (provider != null) {
						try {
							config = provider.getCBuildConfiguration(buildConfig, configName);
						} catch (CoreException e) {
							IStatus status = e.getStatus();
							if (!status.getPlugin().equals(CCorePlugin.PLUGIN_ID)
									|| status.getCode() != CCorePlugin.STATUS_BUILD_CONFIG_NOT_VALID) {
								throw e;
							}
						}
						if (config != null) {
							configs.put(buildConfig, config);
							// Also make sure we reset the binary parser cache
							// for the new config
							resetBinaryParser = true;
						}
					}

					if (config == null) {
						noConfigs.add(buildConfig);
					}
				}
			}
		}

		if (resetBinaryParser) {
			// Do this outside of the synchronized block to avoid deadlock with
			// BinaryRunner
			CModelManager.getDefault().resetBinaryParser(buildConfig.getProject());
		}

		return config;
	}

	@Override
	public ICBuildConfiguration getBuildConfiguration(IProject project, IToolChain toolChain, String launchMode,
			ILaunchTarget launchTarget, IProgressMonitor monitor) throws CoreException {

		// First check if a matching ICBuildConfiguration exists already
		ICBuildConfiguration retVal = findCBuildConfiguration(project, toolChain, launchTarget, launchMode, monitor);

		if (retVal == null) {
			// No existing ICBuildConfiguration, so get ICBuildConfigurationProvider to create one
			retVal = createCBuildConfig(project, toolChain, launchTarget, launchMode, monitor);
		}
		return retVal;
	}

	private ICBuildConfiguration findCBuildConfiguration(IProject project, IToolChain toolChain,
			ILaunchTarget launchTarget, String launchMode, IProgressMonitor monitor) throws CoreException {
		for (IBuildConfiguration buildConfig : project.getBuildConfigs()) {
			ICBuildConfiguration cBuildConfig = getBuildConfiguration(buildConfig);
			if (cBuildConfig != null) {
				IToolChain tc = cBuildConfig.getToolChain();
				ILaunchTarget lt = cBuildConfig.getLaunchTarget();
				String lm = cBuildConfig.getLaunchMode();
				if (tc != null && tc.equals(toolChain) && lt != null && lt.equals(launchTarget) && lm != null
						&& lm.equals(launchMode)) {
					return cBuildConfig;
				}
			}
		}
		return null;
	}

	private ICBuildConfiguration createCBuildConfig(IProject project, IToolChain toolChain, ILaunchTarget launchTarget,
			String launchMode, IProgressMonitor monitor) throws CoreException {
		ICBuildConfiguration retVal = null;
		ICBuildConfigurationProvider provider = getProvider(project);
		if (provider != null) {
			// The provider will call us back to add in the new one
			retVal = provider.createCBuildConfiguration(project, toolChain, launchMode, launchTarget, monitor);
			if (retVal != null) {
				/*
				 * The IScannerInfoProvider may be cached with an incorrect value if the ICBuildConfiguration is not
				 * available at the time it is checked. Now that one has been created, the previous value should be
				 * forgotten so the new cconfig can be used.
				 */
				CCorePlugin.getDefault().resetCachedScannerInfoProvider(project);
				return retVal;
			}
			throw new CoreException(
					CCorePlugin.createStatus(String.format(Messages.CBuildConfigurationManager_CBuildConfigCreateFail,
							project.getName(), toolChain.getName(), launchTarget.getId(), launchMode), null));
		}
		throw new CoreException(
				CCorePlugin.createStatus(String.format(Messages.CBuildConfigurationManager_CBuildConfigProviderNotFound,
						project.getName(), toolChain.getName(), launchTarget.getId(), launchMode), null));
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
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
							if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
								// We need to keep the settings when the project is closed. They are used by
								// CBuildConfiguration.CBuildConfiguration(IBuildConfiguration config, String name)
								// to restore Debug core build configurations when the project is reopened.
								projectNode.removeNode();
							}
							parentNode.flush();
						} catch (BackingStoreException e) {
							CCorePlugin.log(e);
						}
					}
				}

				// Clean up the scanner info data
				IPath scannerInfoPath = CCorePlugin.getDefault().getStateLocation().append("infoCache") //$NON-NLS-1$
						.append(project.getName());
				Path directory = scannerInfoPath.toFile().toPath();
				if (!Files.exists(directory)) {
					return;
				}

				try {
					Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
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

	@Override
	public boolean supports(IProject project) throws CoreException {
		// Is this a CDT project?
		if (!CoreModel.hasCNature(project)) {
			return false;
		}

		initProviders();

		// First see if we have a build config registered
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			if (configs.containsKey(config)) {
				return true;
			}
		}

		// See if one of the providers supports this project
		for (Provider provider : providers.values()) {
			if (provider.supports(project)) {
				return true;
			}
		}

		return false;
	}

}
