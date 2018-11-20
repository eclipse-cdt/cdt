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
package org.eclipse.cdt.core;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * @since 6.4
 */
public class CommandLauncherManager {

	private static CommandLauncherManager instance;

	private List<ICommandLauncherFactory> factories = new ArrayList<>();
	private Map<ICommandLauncherFactory, Integer> priorityMapping = new HashMap<>();

	private CommandLauncherManager() {
		loadCommandLauncherFactoryExtensions();
	}

	public static synchronized CommandLauncherManager getInstance() {
		if (instance == null) {
			instance = new CommandLauncherManager();
		}
		return instance;
	}

	public ICommandLauncher getCommandLauncher() {
		return new CommandLauncherWrapper(this);
	}

	private class CommandLauncherWrapper implements ICommandLauncher {

		private ICommandLauncher launcher;
		private IProject fProject;
		private boolean fShowCommand;
		private String fErrorMessage;
		private CommandLauncherManager manager;

		public CommandLauncherWrapper(CommandLauncherManager manager) {
			this.manager = manager;
		}

		@Override
		public void setProject(IProject project) {
			fProject = project;
			launcher = null;
		}

		@Override
		public IProject getProject() {
			if (launcher != null) {
				return launcher.getProject();
			}
			return fProject;
		}

		@Override
		public void showCommand(boolean show) {
			if (launcher != null) {
				launcher.showCommand(show);
			}
			fShowCommand = show;
		}

		@Override
		public String getErrorMessage() {
			if (launcher != null) {
				return launcher.getErrorMessage();
			}
			return fErrorMessage;
		}

		@Override
		public void setErrorMessage(String error) {
			if (launcher != null) {
				launcher.setErrorMessage(error);
			}
			fErrorMessage = error;
		}

		@Override
		public String[] getCommandArgs() {
			if (launcher != null) {
				return launcher.getCommandArgs();
			}
			return new String[0];
		}

		@Override
		public Properties getEnvironment() {
			if (launcher != null) {
				return launcher.getEnvironment();
			}
			// for backwards compatibility to ensure path is set up
			return EnvironmentReader.getEnvVars();
		}

		@Override
		public String getCommandLine() {
			if (launcher != null) {
				return launcher.getCommandLine();
			}
			return null;
		}

		@Override
		public Process execute(IPath commandPath, String[] args, String[] env, IPath workingDirectory,
				IProgressMonitor monitor) throws CoreException {
			if (launcher == null) {
				launcher = manager.getCommandLauncher(fProject);
				launcher.setProject(fProject);
				launcher.showCommand(fShowCommand);
				launcher.setErrorMessage(fErrorMessage);
			}
			return launcher.execute(commandPath, args, env, workingDirectory, monitor);
		}

		@SuppressWarnings("deprecation")
		@Override
		public int waitAndRead(OutputStream out, OutputStream err) {
			if (launcher != null) {
				return launcher.waitAndRead(out, err);
			}
			return 0;
		}

		@Override
		public int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor) {
			if (launcher != null) {
				return launcher.waitAndRead(output, err, monitor);
			}
			return 0;
		}

	}

	/**
	 * Get a command launcher.
	 *
	 * @param project - IProject to determine launcher for.
	 * @return an ICommandLauncher for running commands
	 */
	public ICommandLauncher getCommandLauncher(IProject project) {
		// loop through list of factories and return launcher returned with
		// highest priority
		int highestPriority = -1;
		ICommandLauncher bestLauncher = null;
		for (ICommandLauncherFactory factory : factories) {
			ICommandLauncher launcher = factory.getCommandLauncher(project);
			if (launcher != null) {
				int factoryPriority = priorityMapping.get(factory);
				if (factoryPriority > highestPriority) {
					bestLauncher = launcher;
					highestPriority = factoryPriority;
				}
			}
		}
		if (bestLauncher != null) {
			return bestLauncher;
		}
		// default to local CommandLauncher
		return new CommandLauncher();
	}

	/**
	 * Get a command launcher.
	 *
	 * @param config - ICBuildConfiguration to determine launcher for.
	 * @return an ICommandLauncher for running commands
	 * @since 6.5
	 */
	public ICommandLauncher getCommandLauncher(ICBuildConfiguration config) {
		// loop through list of factories and return launcher returned with
		// highest priority
		int highestPriority = -1;
		ICommandLauncher bestLauncher = null;
		for (ICommandLauncherFactory factory : factories) {
			if (factory instanceof ICommandLauncherFactory2) {
				ICommandLauncher launcher = ((ICommandLauncherFactory2) factory).getCommandLauncher(config);
				if (launcher != null) {
					int factoryPriority = priorityMapping.get(factory);
					if (factoryPriority > highestPriority) {
						bestLauncher = launcher;
						highestPriority = factoryPriority;
					}
				}
			}
		}
		if (bestLauncher != null) {
			return bestLauncher;
		}
		// default to local CommandLauncher
		return new CommandLauncher();
	}

	/**
	 * Get a command launcher.
	 *
	 * @param cfgd - ICConfigurationDescription to get command launcher for.
	 * @return an ICommandLauncher for running commands
	 */
	public ICommandLauncher getCommandLauncher(ICConfigurationDescription cfgd) {
		// loop through list of factories and return launcher returned with
		// highest priority
		int highestPriority = -1;
		ICommandLauncher bestLauncher = null;
		for (ICommandLauncherFactory factory : factories) {
			ICommandLauncher launcher = factory.getCommandLauncher(cfgd);
			if (launcher != null) {
				int factoryPriority = priorityMapping.get(factory);
				if (factoryPriority > highestPriority) {
					bestLauncher = launcher;
					highestPriority = factoryPriority;
				}
			}
		}
		if (bestLauncher != null) {
			return bestLauncher;
		}
		// default to local CommandLauncher
		return new CommandLauncher();
	}

	/**
	 * Load command launcher factory contributed extensions from extension registry.
	 *
	 */
	private void loadCommandLauncherFactoryExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID,
				CCorePlugin.COMMAND_LAUNCHER_FACTORY_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				try {
					IConfigurationElement element[] = ext.getConfigurationElements();
					for (IConfigurationElement element2 : element) {
						if (element2.getName().equalsIgnoreCase("factory")) { //$NON-NLS-1$
							ICommandLauncherFactory factory = (ICommandLauncherFactory) element2
									.createExecutableExtension("class"); //$NON-NLS-1$
							String priorityAttr = element2.getAttribute("priority"); //$NON-NLS-1$
							int priority = Integer.valueOf(0);
							if (priorityAttr != null) {
								try {
									priority = Integer.valueOf(priorityAttr);
								} catch (NumberFormatException e) {
									CCorePlugin.log(e);
								}
							}
							factories.add(factory);
							priorityMapping.put(factory, priority);
						}
					}
				} catch (Exception e) {
					CCorePlugin.log("Cannot load CommandLauncherFactory extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
				}
			}
		}
	}

	private ICommandLauncherFactory getBestFactory(IProject project) {
		// loop through list of factories and return launcher returned with
		// highest priority
		int highestPriority = -1;
		ICommandLauncherFactory bestLauncherFactory = null;
		for (ICommandLauncherFactory factory : factories) {
			ICommandLauncher launcher = factory.getCommandLauncher(project);
			if (launcher != null) {
				if (priorityMapping.get(factory) > highestPriority) {
					bestLauncherFactory = factory;
				}
			}
		}
		return bestLauncherFactory;
	}

	private ICommandLauncherFactory getBestFactory(ICBuildConfiguration config) {
		// loop through list of factories and return launcher returned with
		// highest priority
		int highestPriority = -1;
		ICommandLauncherFactory bestLauncherFactory = null;
		for (ICommandLauncherFactory factory : factories) {
			if (factory instanceof ICommandLauncherFactory2) {
				ICommandLauncher launcher = ((ICommandLauncherFactory2) factory).getCommandLauncher(config);
				if (launcher != null) {
					if (priorityMapping.get(factory) > highestPriority) {
						bestLauncherFactory = factory;
					}
				}
			}
		}
		return bestLauncherFactory;
	}

	/**
	 * @since 6.5
	 */
	public List<String> processIncludePaths(ICBuildConfiguration config, List<String> includePaths) {
		ICommandLauncherFactory factory = getBestFactory(config);
		if (factory != null && factory instanceof ICommandLauncherFactory2) {
			return ((ICommandLauncherFactory2) factory).verifyIncludePaths(config, includePaths);
		}
		return includePaths;
	}

	public void setLanguageSettingEntries(IProject project, List<? extends ICLanguageSettingEntry> entries) {
		ICommandLauncherFactory factory = getBestFactory(project);
		if (factory != null) {
			factory.registerLanguageSettingEntries(project, entries);
		}
	}

	public List<ICLanguageSettingEntry> getLanguageSettingEntries(IProject project,
			List<ICLanguageSettingEntry> entries) {
		List<ICLanguageSettingEntry> verifiedEntries = entries;
		ICommandLauncherFactory factory = getBestFactory(project);
		if (factory != null) {
			verifiedEntries = factory.verifyLanguageSettingEntries(project, entries);
		}
		return verifiedEntries;
	}

}
