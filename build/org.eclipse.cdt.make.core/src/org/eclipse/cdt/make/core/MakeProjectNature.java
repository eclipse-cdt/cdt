package org.eclipse.cdt.make.core;
/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class MakeProjectNature implements IProjectNature {
	
	private IMakeBuilderInfo fBuildInfo;
	public final static String NATURE_ID = MakeCorePlugin.getUniqueIdentifier() + ".makeNature";
	private IProject fProject;

	public static void addNature(IProject project, SubProgressMonitor monitor) throws CoreException {
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

	public static ICommand getBuildSpec(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(MakeBuilder.BUILDER_ID)) {
				return commands[i];
			}
		}
		return null;
	}

	public void addToBuildSpec(String builderID, IProgressMonitor mon) throws CoreException {
		addToBuildSpec(getProject(), builderID, mon);
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

	public void removeBuildSpec(IProgressMonitor mon) throws CoreException {
		removeFromBuildSpec(MakeBuilder.BUILDER_ID, mon);
	}

	/**
		* Removes the given builder from the build spec for the given project.
		*/
	public void removeFromBuildSpec(String builderID, IProgressMonitor mon) throws CoreException {
		IProjectDescription description = getProject().getDescription();
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
		getProject().setDescription(description, mon);
	}

	/**
		* @see IProjectNature#configure
		*/
	public void configure() throws CoreException {
		addToBuildSpec(MakeBuilder.BUILDER_ID, null);
		IMakeBuilderInfo info = BuildInfoFactory.create(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID);
		fBuildInfo.setBuildLocation(info.getBuildLocation());


		fBuildInfo.setUseDefaultBuildCmd(info.isDefaultBuildCmd());
		fBuildInfo.setStopOnError(info.isStopOnError());
		fBuildInfo.setBuildCommand(info.getBuildCommand());

		fBuildInfo.setAutoBuildEnable(info.isAutoBuildEnable());
		fBuildInfo.setAutoBuildTarget(info.getAutoBuildTarget());

		fBuildInfo.setIncrementalBuildEnable(info.isIncrementalBuildEnabled());
		fBuildInfo.setIncrementalBuildTarget(info.getIncrementalBuildTarget());

		fBuildInfo.setFullBuildEnable(info.isFullBuildEnabled());
		fBuildInfo.setFullBuildTarget(info.getFullBuildTarget());
	}

	/**
		* @see IProjectNature#deconfigure
		*/
	public void deconfigure() throws CoreException {
		removeFromBuildSpec(MakeBuilder.BUILDER_ID, null);
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
		try {
			fProject = project;
			fBuildInfo = MakeBuildManager.getBuildInfo(fProject, true);
		} catch (CoreException e) {
		}
	}

}
