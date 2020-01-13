/*******************************************************************************
 * Copyright (c) 2014, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ILaunchConfigurationTargetedDelegate;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.progress.UIJob;

/**
 * Build active project
 */
public class BuildActiveCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
			ILaunchConfiguration config = launchBarManager.getActiveLaunchConfiguration();
			ILaunchMode launchMode = launchBarManager.getActiveLaunchMode();
			ILaunchTarget target = launchBarManager.getActiveLaunchTarget();

			new UIJob(Display.getDefault(), Messages.BuildActiveCommandHandler_0) {
				@Override
				public boolean belongsTo(Object family) {
					return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
				}

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					final Collection<IProject> projects = getProjects(config);
					if (BuildAction.isSaveAllSet()) {
						saveEditors(projects);
					}

					new Job(Messages.BuildActiveCommandHandler_1) {
						@Override
						public boolean belongsTo(Object family) {
							return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
						}

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								if (config == null) {
									// Default, build the workspace
									ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
											monitor);
									return Status.OK_STATUS;
								}

								String mode = launchMode != null ? launchMode.getIdentifier() : "run"; //$NON-NLS-1$
								Set<String> modes = new HashSet<>();
								modes.add(mode);
								ILaunchDelegate delegate = config.getType().getPreferredDelegate(modes);
								if (delegate == null)
									delegate = config.getType().getDelegates(modes)[0];
								ILaunchConfigurationDelegate configDel = delegate.getDelegate();
								if (configDel instanceof ILaunchConfigurationTargetedDelegate) {
									ILaunchConfigurationTargetedDelegate configDel2 = (ILaunchConfigurationTargetedDelegate) configDel;
									boolean ret;
									ret = configDel2.preLaunchCheck(config, mode, target, monitor);
									if (!ret) {
										return Status.CANCEL_STATUS;
									}
									if (!configDel2.buildForLaunch(config, mode, target, monitor)) {
										return Status.OK_STATUS;
									}
								} else if (configDel instanceof ILaunchConfigurationDelegate2) {
									ILaunchConfigurationDelegate2 configDel2 = (ILaunchConfigurationDelegate2) configDel;
									boolean ret;
									ret = configDel2.preLaunchCheck(config, mode, monitor);
									if (!ret) {
										return Status.CANCEL_STATUS;
									}
									if (!configDel2.buildForLaunch(config, mode, monitor)) {
										return Status.OK_STATUS;
									}
								}

								// Fall through, do a normal build
								if (projects.isEmpty()) {
									ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
											monitor);
								} else {
									Collection<IBuildConfiguration> buildConfigs = getBuildConfigs(projects);
									ResourcesPlugin.getWorkspace().build(
											buildConfigs.toArray(new IBuildConfiguration[buildConfigs.size()]),
											IncrementalProjectBuilder.INCREMENTAL_BUILD, true, monitor);
									// TODO, may need to get the buildReferences
									// argument from the descriptor
								}
								return Status.OK_STATUS;
							} catch (CoreException e) {
								return e.getStatus();
							}
						}
					}.schedule();

					return Status.OK_STATUS;
				}
			}.schedule();

		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	protected Collection<IProject> getProjects(ILaunchConfiguration config) {
		Set<IProject> projects = new HashSet<>();

		if (config != null) {
			IResource[] mappedResources;
			try {
				mappedResources = config.getMappedResources();
			} catch (CoreException e) {
				return projects;
			}
			if (mappedResources != null) {
				for (IResource resource : mappedResources) {
					IProject project = resource.getProject();
					if (projects.contains(project))
						continue;
					projects.add(project);
					try {
						projects.addAll(Arrays.asList(project.getReferencedProjects()));
					} catch (CoreException e) {
						// skip
					}
				}
			}
		}

		return projects;
	}

	protected Collection<IBuildConfiguration> getBuildConfigs(Collection<IProject> projects) {
		Set<IBuildConfiguration> configs = new HashSet<>();

		for (IProject project : projects) {
			try {
				configs.add(project.getActiveBuildConfig());
			} catch (CoreException e) {
				// skip
			}
		}

		return configs;
	}

	protected void saveEditors(final Collection<IProject> projects) {
		Display.getDefault().syncExec(() -> {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : windows) {
				IWorkbenchPage[] pages = window.getPages();
				for (IWorkbenchPage page : pages) {
					if (projects.isEmpty()) {
						page.saveAllEditors(false);
					} else {
						IEditorPart[] editors = page.getDirtyEditors();
						for (IEditorPart editor : editors) {
							IFile inputFile = ResourceUtil.getFile(editor.getEditorInput());
							if (inputFile != null) {
								if (projects.contains(inputFile.getProject())) {
									page.saveEditor(editor, false);
								}
							}
						}
					}
				}
			}
		});
	}

}
