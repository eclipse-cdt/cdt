/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
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
 * @since 6.3
 */
public class CommandLauncherFactoryManager {
	
	private static CommandLauncherFactoryManager instance;
	
	private List<ICommandLauncherFactory> factories = new ArrayList<>();
	
	private CommandLauncherFactoryManager() {
		loadCommandLauncherFactoryExtensions();
	}
	
	public static synchronized CommandLauncherFactoryManager getInstance() {
		if (instance == null) {
			instance = new CommandLauncherFactoryManager();
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
		private CommandLauncherFactoryManager manager;
		
		public CommandLauncherWrapper(CommandLauncherFactoryManager manager) {
			this.manager = manager;
		}

		@Override
		public void setProject(IProject project) {
			if (launcher != null) {
				launcher.setProject(project);
			} else {
				fProject = project;
			}
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
			} else {
				fShowCommand = show;
			}
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
			} else {
				fErrorMessage = error;
			}
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
			return null;
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
	 * @param project - optional input to determine launcher.
	 * @return an ICommandLauncher for running commands
	 */
	public ICommandLauncher getCommandLauncher(IProject project) {
		// loop through list of factories and return first launcher
		// returned
		for (ICommandLauncherFactory factory : factories) {
			ICommandLauncher launcher = factory.getCommandLauncher(project);
			if (launcher != null) {
				return launcher;
			}
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
		IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.COMMAND_LAUNCHER_FACTORY_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				try {
					IConfigurationElement element[] = extension.getConfigurationElements();
					for (IConfigurationElement element2 : element) {
						if (element2.getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
							ICommandLauncherFactory factory = (ICommandLauncherFactory) element2.createExecutableExtension("run"); //$NON-NLS-1$
							factories.add(factory);
						}
					}
				} catch (Exception e) {
					CCorePlugin.log("Cannot load CommandLauncherFactory extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
				}
			}
		}
	}

	public void setLanguageSettingEntries(IProject project, List<? extends ICLanguageSettingEntry> entries) {
		for (ICommandLauncherFactory factory : factories) {
			ICommandLauncher launcher = factory.getCommandLauncher(project);
			if (launcher != null) {
				factory.registerLanguageSettingEntries(project, entries);
			}
		}

	}
	
	public List<ICLanguageSettingEntry> getLanguageSettingEntries(IProject project, List<ICLanguageSettingEntry> entries) {
		List<ICLanguageSettingEntry> verifiedEntries = entries;
		for (ICommandLauncherFactory factory : factories) {
			ICommandLauncher launcher = factory.getCommandLauncher(project);
			if (launcher != null) {
				verifiedEntries = factory.verifyLanguageSettingEntries(project, entries);
			}
		}
		return verifiedEntries;
	}


}
