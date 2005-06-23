/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * @see IProjectNature
 */
public class ScannerConfigNature implements IProjectNature {
	
	public final static String NATURE_ID = MakeCorePlugin.getUniqueIdentifier() + ".ScannerConfigNature"; //$NON-NLS-1$
	private IProject fProject;

	/**
	 * @see IProjectNature#configure
	 */
	public void configure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(ScannerConfigBuilder.BUILDER_ID)) {
				return;
			}
		}
		ICommand command = description.newCommand();
		command.setBuilderName(ScannerConfigBuilder.BUILDER_ID);
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		newCommands[commands.length] = command;
		description.setBuildSpec(newCommands);
		getProject().setDescription(description, null);
		
		// set default project scanner config settings
	}

	/**
	 * @see IProjectNature#deconfigure
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(ScannerConfigBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				break;
			}
		}
		getProject().setDescription(description, null);
	}

	/**
	 * @see IProjectNature#getProject
	 */
	public IProject getProject()  {
		return fProject;
	}

	/**
	 * @see IProjectNature#setProject
	 */
	public void setProject(IProject project)  {
		fProject = project;
	}
	
	public static void addScannerConfigNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		if (description.hasNature(NATURE_ID))
			return;
		String[] ids = description.getNatureIds();
		String[] newIds = new String[ids.length + 1];
		System.arraycopy(ids, 0, newIds, 0, ids.length);
		newIds[ids.length] = NATURE_ID;
		description.setNatureIds(newIds);
		project.setDescription(description, null);
		
	}
	
	public static void removeScannerConfigNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		if (!description.hasNature(NATURE_ID))
			return;
		String[] ids = description.getNatureIds();
		for (int i = 0; i < ids.length; ++i) {
			if (ids[i].equals(NATURE_ID)) {
				String[] newIds = new String[ids.length - 1];
				System.arraycopy(ids, 0, newIds, 0, i);
				System.arraycopy(ids, i + 1, newIds, i, ids.length - i - 1);
				description.setNatureIds(newIds);
				project.setDescription(description, null);
			}
		}

	}

	/**
	 * Returns build command as stored in .project file
	 * 
	 * @param description
	 * @param builderID
	 * @return ICommand
	 * @throws CoreException
	 */
	public static ICommand getBuildSpec(IProjectDescription description, String builderID) throws CoreException {
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				return commands[i];
			}
		}
		return null;
	}

	/**
	 * Stores a build command in .project file
	 * 
	 * @param description
	 * @param newCommand
	 * @return IProjecDescription
	 * @throws CoreException
	 */
	public static IProjectDescription setBuildSpec(IProjectDescription description, ICommand newCommand) throws CoreException {
		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldCommand = getBuildSpec(description, newCommand.getBuilderName());
		ICommand[] newCommands;

		if (oldCommand == null) {
			// Add the build spec at the end
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
			newCommands[oldCommands.length] = newCommand;
		} 
		else {
			for (int i = 0; i < oldCommands.length; i++) {
				if (oldCommands[i] == oldCommand) {
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
}
