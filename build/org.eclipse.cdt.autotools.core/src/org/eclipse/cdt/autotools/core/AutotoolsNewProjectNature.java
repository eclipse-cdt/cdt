/*******************************************************************************
 * Copyright (c) 2008, 2016 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Incorporated         - initial implementation
 * IBM Rational Software        - add and remove nature static methods
 * Anna Dushistova (MontaVista) - [402595]Autotools nature loses builders added by contributed wizard pages
 *******************************************************************************/
package org.eclipse.cdt.autotools.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.internal.autotools.core.AutotoolsConfigurationBuilder;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

public class AutotoolsNewProjectNature implements IProjectNature {

	public static final String AUTOTOOLS_NATURE_ID = "org.eclipse.cdt.autotools.core.autotoolsNatureV2"; //$NON-NLS-1$
	public static final String OLD_AUTOTOOLS_NATURE_ID = "org.eclipse.linuxtools.cdt.autotools.core.autotoolsNatureV2"; //$NON-NLS-1$
	public static final String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".genmakebuilder"; //$NON-NLS-1$
	/**
	 * @since 1.3
	 */
	public static final String REMOTE_BUILDER_ID = "org.eclipse.ptp.rdt.sync.cdt.core.SyncBuilder"; //$NON-NLS-1$
	public static final String OLD_AUTOTOOLS_BUILDER_ID = "org.eclipse.linuxtools.cdt.autotools.genmakebuilder"; //$NON-NLS-1$

	private IProject project;

	@Override
	public void configure() throws CoreException {
		addAutotoolsBuilder(project, new NullProgressMonitor());
	}

	@Override
	public void deconfigure() throws CoreException {
		// TODO remove builder from here
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * Add the Autotools builder to the project
	 * @param project
	 * @param monitor
	 * @throws CoreException
	 */
	public static void addAutotoolsBuilder(IProject project, IProgressMonitor monitor) throws CoreException {
		// Add the builder to the project
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		if (checkEquals(commands, getBuildCommandsList(description, commands))) {
			return;
		}
		final ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
		final IProject proj = project;

		Job backgroundJob = new Job("Autotools Set Project Description") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
						protected boolean savedAutoBuildingValue;

						@Override
						public void run(IProgressMonitor monitor) throws CoreException {
							IWorkspace workspace = ResourcesPlugin.getWorkspace();
							turnOffAutoBuild(workspace);
							IProjectDescription prDescription = proj.getDescription();
							//Other pieces of wizard might have contributed new builder commands;
							//need to make sure we are using the most recent ones
							ICommand[] currentCommands = prDescription.getBuildSpec();
							ICommand[] newCommands = getBuildCommandsList(prDescription, currentCommands);
							if (!checkEquals(currentCommands, newCommands)) {
								prDescription.setBuildSpec(newCommands);
								proj.setDescription(prDescription, new NullProgressMonitor());
							}
							restoreAutoBuild(workspace);
						}

						protected final void turnOffAutoBuild(IWorkspace workspace) throws CoreException {
							IWorkspaceDescription workspaceDesc = workspace.getDescription();
							savedAutoBuildingValue = workspaceDesc.isAutoBuilding();
							workspaceDesc.setAutoBuilding(false);
							workspace.setDescription(workspaceDesc);
						}

						protected final void restoreAutoBuild(IWorkspace workspace) throws CoreException {
							IWorkspaceDescription workspaceDesc = workspace.getDescription();
							workspaceDesc.setAutoBuilding(savedAutoBuildingValue);
							workspace.setDescription(workspaceDesc);
						}

					}, rule, IWorkspace.AVOID_UPDATE, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				IStatus returnStatus = Status.OK_STATUS;
				return returnStatus;
			}
		};
		backgroundJob.setRule(rule);
		backgroundJob.schedule();
	}

	static boolean checkEquals(ICommand[] commands, ICommand[] newCommands) {
		if (newCommands.length != commands.length) {
			return false;
		}
		for (int j = 0; j < commands.length; ++j) {
			if (!commands[j].getBuilderName().equals(newCommands[j].getBuilderName())) {
				return false;
			}
		}
		return true;
	}

	static ICommand[] getBuildCommandsList(IProjectDescription description, ICommand[] commands) {
		ArrayList<ICommand> commandList = new ArrayList<>();

		// Make sure the Autotools Configuration builder just precedes the Common Builder
		for (int i = 0; i < commands.length; i++) {
			ICommand command = commands[i];
			if (command.getBuilderName().equals(AutotoolsConfigurationBuilder.BUILDER_ID)) {
				// ignore it
			} else {
				if (command.getBuilderName().equals(OLD_AUTOTOOLS_BUILDER_ID)) {
					ICommand newCommand = description.newCommand();
					newCommand.setBuilderName(BUILDER_ID);
					command = newCommand;
				}
				// Make sure that the Autotools builder precedes the Managed builder
				// or the Remote Synchronized builder.
				if (command.getBuilderName().equals(BUILDER_ID) || command.getBuilderName().equals(REMOTE_BUILDER_ID)) {
					// add Autotools Configuration builder just before builder
					ICommand newCommand = description.newCommand();
					newCommand.setBuilderName(AutotoolsConfigurationBuilder.BUILDER_ID);
					commandList.add(newCommand);
				}
				commandList.add(command);
			}
		}
		return commandList.toArray(new ICommand[commandList.size()]);
	}

	/**
	 * Utility method for adding an autotools nature to a project.
	 *
	 * @param proj the project to add the autotools nature to.
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void addAutotoolsNature(IProject project, IProgressMonitor monitor) throws CoreException {
		addNature(project, AUTOTOOLS_NATURE_ID, monitor);
	}

	/**
	 * Utility method for adding a nature to a project.
	 *
	 * @param proj the project to add the nature to.
	 * @param natureId the id of the nature to assign to the project
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		for (int i = 0; i < prevNatures.length; i++) {
			if (natureId.equals(prevNatures[i]))
				return;
		}
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	/**
	 * Utility method to remove the autotools nature from a project.
	 *
	 * @param project to remove the autotools nature from
	 * @param mon progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @throws CoreException
	 */
	public static void removeAutotoolsNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, AUTOTOOLS_NATURE_ID, mon);
	}

	/**
	 * Utility method to remove the old autotools nature from a project.
	 *
	 * @param project to remove the old autotools nature from
	 * @param mon progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @throws CoreException
	 */
	public static void removeOldAutotoolsNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, OLD_AUTOTOOLS_NATURE_ID, mon);
	}

	/**
	 * Utility method for removing a project nature from a project.
	 *
	 * @param proj the project to remove the nature from
	 * @param natureId the nature id to remove
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		List<String> newNatures = new ArrayList<>(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

}
