/*******************************************************************************
 * Copyright (c) 2002, 2004 Rational Software Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

public class ManagedCProjectNature implements IProjectNature {
	public static final String BUILDER_NAME = "genmakebuilder"; //$NON-NLS-1$
	public static final String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + "." + BUILDER_NAME; //$NON-NLS-1$
	public static final String MNG_NATURE_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".managedBuildNature";  //$NON-NLS-1$
	private IProject project;

	/**
	 * Utility method for adding a managed nature to a project.
	 * 
	 * @param proj the project to add the managed nature to.
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void addManagedNature(IProject project, IProgressMonitor monitor) throws CoreException {
		addNature(project, MNG_NATURE_ID, monitor);
	}

	public static void addManagedBuilder(IProject project, IProgressMonitor monitor) throws CoreException {
		// Add the builder to the project
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();

		// TODO Remove this when the new StandardBuild nature adds the cbuilder
		for (int i = 0; i < commands.length; i++) {
			ICommand command = commands[i];
			if (command.getBuilderName().equals("org.eclipse.cdt.core.cbuilder")) { //$NON-NLS-1$
				// Remove the command
				Vector vec = new Vector(Arrays.asList(commands));
				vec.removeElementAt(i);
				vec.trimToSize();
				ICommand[] tempCommands = (ICommand[]) vec.toArray(new ICommand[commands.length-1]); 
				description.setBuildSpec(tempCommands);
				break;
			}
		}
		
		commands = description.getBuildSpec();
		boolean found = false;
		// See if the builder is already there
		for (int i = 0; i < commands.length; ++i) {
		   if (commands[i].getBuilderName().equals(getBuilderID())) {
			  found = true;
			  break;
		   }
		}
		if (!found) { 
		   //add builder to project
		   ICommand command = description.newCommand();
		   command.setBuilderName(getBuilderID());
		   ICommand[] newCommands = new ICommand[commands.length + 1];
		   // Add it before other builders.
		   System.arraycopy(commands, 0, newCommands, 1, commands.length);
		   newCommands[0] = command;
		   description.setBuildSpec(newCommands);
		   project.setDescription(description, null);
		}		
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
	 * Get the correct builderID
	 */
	public static String getBuilderID() {
		Plugin plugin = (Plugin)ManagedBuilderCorePlugin.getDefault();
		if (Platform.getExtensionRegistry().getExtension(BUILDER_NAME) != null) {
			return ManagedBuilderCorePlugin.getUniqueIdentifier() + "." + BUILDER_NAME; //$NON-NLS-1$
		}
		return BUILDER_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		// Just return the project associated with the nature
		return project;
	}


	/**
	 * Utility method to remove the managed nature from a project.
	 * 
	 * @param project to remove the managed nature from
	 * @param mon progress monitor to indicate the duration of the operation, or 
	 * <code>null</code> if progress reporting is not required. 
	 * @throws CoreException
	 */
	public static void removeManagedNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, MNG_NATURE_ID, mon);
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
		List newNatures = new ArrayList(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds((String[])newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		// Set the project for the nature
		this.project = project;
	}

}
