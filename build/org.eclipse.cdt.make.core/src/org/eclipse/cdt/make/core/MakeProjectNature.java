/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class MakeProjectNature implements IProjectNature {
	
	public final static String NATURE_ID = MakeCorePlugin.getUniqueIdentifier() + ".makeNature"; //$NON-NLS-1$
	private IProject fProject;

	public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures= description.getNatureIds();
		for (int i= 0; i < prevNatures.length; i++) {
			if (NATURE_ID.equals(prevNatures[i]))
				return;
		}
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	public static ICommand getBuildSpec(IProjectDescription description, String builderID) {
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				return commands[i];
			}
		}
		return null;
	}
	
	/**
	 * Update the Java command in the build spec (replace existing one if present,
	 * add one first if none).
	 */
	public static IProjectDescription setBuildSpec(IProjectDescription description, ICommand newCommand) {

		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldCommand = getBuildSpec(description, newCommand.getBuilderName());
		ICommand[] newCommands;

		if (oldCommand == null) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
			newCommands[0] = newCommand;
		} else {
			for (int i = 0, max = oldCommands.length; i < max; i++) {
				if (oldCommands[i].getBuilderName().equals(oldCommand.getBuilderName())) {
					oldCommands[i] = newCommand;
					break;
				}
			}
			newCommands = oldCommands;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		return description;
	}	

	/**
		* Adds a builder to the build spec for the given project.
		*/
	public static void addToBuildSpec(IProject project, String builderID, IProgressMonitor mon) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		boolean found = false;
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				found = true;
				break;
			}
		}
		if (!found) {
			ICommand command = description.newCommand();
			command.setBuilderName(builderID);
			ICommand[] newCommands = new ICommand[commands.length + 1];
			// Add it before other builders. See 1FWJK7I: ITPJCORE:WIN2000
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			description.setBuildSpec(newCommands);
			project.setDescription(description, mon);
		}
	}

	/**
		* Removes the given builder from the build spec for the given project.
		*/
	public static void removeFromBuildSpec(IProject project, String builderID, IProgressMonitor mon) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				break;
			}
		}
		project.setDescription(description, mon);
	}

	public void addBuildSpec() throws CoreException {
		addToBuildSpec(getProject(), MakeBuilder.BUILDER_ID, null);
	}

	/**
		* @see IProjectNature#configure
		*/
	public void configure() throws CoreException {
		addBuildSpec();
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID, false);
		IMakeBuilderInfo projectInfo = MakeCorePlugin.createBuildInfo(getProject(), MakeBuilder.BUILDER_ID);
		projectInfo.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "")); //$NON-NLS-1$
		projectInfo.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$

		projectInfo.setUseDefaultBuildCmd(info.isDefaultBuildCmd());
		projectInfo.setStopOnError(info.isStopOnError());

		projectInfo.setAutoBuildEnable(info.isAutoBuildEnable());
		projectInfo.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, info.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, "")); //$NON-NLS-1$
		
		projectInfo.setIncrementalBuildEnable(info.isIncrementalBuildEnabled());
		projectInfo.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREAMENTAL, info.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREAMENTAL, "")); //$NON-NLS-1$

		projectInfo.setFullBuildEnable(info.isFullBuildEnabled());
		projectInfo.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_FULL, info.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_FULL, "")); //$NON-NLS-1$

		projectInfo.setCleanBuildEnable(info.isCleanBuildEnabled());
		projectInfo.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, info.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, "")); //$NON-NLS-1$

		projectInfo.setErrorParsers(info.getErrorParsers());
		projectInfo.setAppendEnvironment(info.appendEnvironment());
		projectInfo.setEnvironment(info.getEnvironment());
	}

	public void removeBuildSpec() throws CoreException {
		removeFromBuildSpec(getProject(), MakeBuilder.BUILDER_ID, null);
	}

	/**
	 * @see IProjectNature#deconfigure
	 */
	public void deconfigure() throws CoreException {
		removeBuildSpec();
	}

	/**
		* @see IProjectNature#getProject
		*/
	public IProject getProject() {
		return fProject;
	}

	/**
		* @see IProjectNature#setProject
		*/
	public void setProject(IProject project) {
		fProject = project;
	}
}
