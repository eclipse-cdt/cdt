/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat Inc.. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial implementation
 *     IBM Rational Software - add and remove nature static methods
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

	public static final String AUTOTOOLS_NATURE_ID = "org.eclipse.cdt.autotools.core.autotoolsNatureV2";  //$NON-NLS-1$
	public static final String OLD_AUTOTOOLS_NATURE_ID = "org.eclipse.linuxtools.cdt.autotools.core.autotoolsNatureV2"; //$NON-NLS-1$
	public final static String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".genmakebuilder"; //$NON-NLS-1$
	public final static String OLD_AUTOTOOLS_BUILDER_ID = "org.eclipse.linuxtools.cdt.autotools.genmakebuilder"; //$NON-NLS-1$

	private IProject project;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		addAutotoolsBuilder(project, new NullProgressMonitor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		// TODO remove builder from here
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
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
		ArrayList<ICommand> commandList = new ArrayList<ICommand>();
	
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
				if (command.getBuilderName().equals(BUILDER_ID)) {
					// add Autotools Configuration builder just before builder
					ICommand newCommand = description.newCommand();
					newCommand.setBuilderName(AutotoolsConfigurationBuilder.BUILDER_ID);
					commandList.add(newCommand);
				}
				commandList.add(command);
			}
		}
		final ICommand[] newCommands = commandList.toArray(new ICommand[commandList.size()]);
		if (newCommands.length == commands.length) {
			boolean hasCorrectBuilderCommands = true;
			for (int j = 0; j < commands.length; ++j) {
				if (!commands[j].getBuilderName().equals(newCommands[j].getBuilderName())) {
					hasCorrectBuilderCommands = false;
					break;
				}
			}
			if (hasCorrectBuilderCommands)
				return;
		}
		final ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
		final IProject proj = project;

		Job backgroundJob = new Job("Autotools Set Project Description") {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
						protected boolean savedAutoBuildingValue;

						public void run(IProgressMonitor monitor) throws CoreException {
							IWorkspace workspace = ResourcesPlugin.getWorkspace();
							turnOffAutoBuild(workspace);
							IProjectDescription description = proj.getDescription();
							description.setBuildSpec(newCommands);
							proj.setDescription(description, new NullProgressMonitor());
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
		List<String> newNatures = new ArrayList<String>(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds((String[])newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

}
