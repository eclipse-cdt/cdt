/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CProjectNature implements IProjectNature {

	public static final String C_NATURE_ID = CCorePlugin.PLUGIN_ID + ".cnature"; //$NON-NLS-1$

	private IProject fProject;

	public CProjectNature() {
	}

	public CProjectNature(IProject project) {
		setProject(project);
	}

	public static void addCNature(IProject project, IProgressMonitor mon) throws CoreException {
		addNature(project, C_NATURE_ID, mon);
	}

	public static void removeCNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, C_NATURE_ID, mon);
	}

	/**
	 * Utility method for adding a nature to a project.
	 *
	 * @param project
	 *            the project to add the nature
	 * @param natureId
	 *            the id of the nature to assign to the project
	 * @param monitor
	 *            a progress monitor to indicate the duration of the operation,
	 *            or <code>null</code> if progress reporting is not required.
	 *
	 */
	public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			for (String prevNature : prevNatures) {
				if (natureId.equals(prevNature))
					return;
			}
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = natureId;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		}

		catch (CoreException e) {
			CCorePlugin.log(e);
		}

		finally {
			monitor.done();
		}
	}

	/**
	 * Utility method for removing a project nature from a project.
	 *
	 * @param project
	 *            the project to remove the nature from
	 * @param natureId
	 *            the nature id to remove
	 * @param monitor
	 *            a progress monitor to indicate the duration of the operation,
	 *            or <code>null</code> if progress reporting is not required.
	 */
	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		List<String> newNatures = new ArrayList<>(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

	/**
	 * @see IProjectNature#configure
	 */
	@Override
	public void configure() throws CoreException {
	}

	/**
	 * @see IProjectNature#deconfigure
	 */
	@Override
	public void deconfigure() throws CoreException {
	}

	/**
	 * @see IProjectNature#getProject
	 */
	@Override
	public IProject getProject() {
		return fProject;
	}

	/**
	 * @see IProjectNature#setProject
	 */
	@Override
	public void setProject(IProject project) {
		fProject = project;
	}
}
