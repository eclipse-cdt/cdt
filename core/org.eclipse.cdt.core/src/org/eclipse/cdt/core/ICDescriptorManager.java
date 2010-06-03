/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated replace with {@link ICProjectDescriptionManager} & {@link ICProjectDescription}
 */
@Deprecated
public interface ICDescriptorManager {

	public void configure(IProject project, String id) throws CoreException;

	public void convert(IProject project, String id) throws CoreException;

	/**
 	 * Return the ICDescriptor for the project.  If project doesn't contain
 	 * an ICDescriptor then one is created.
 	 * Equivalent to: {@code ICDescriptorManager#getDescriptor(project, true)}
 	 * 
 	 * Users should consider batching changes in an ICDescriptorOperation
 	 * 
 	 * @see ICDescriptorManager#getDescriptor(IProject, boolean)
	 * @param project
	 * @return ICDescriptor
	 * @throws CoreException
	 */
	public ICDescriptor getDescriptor(IProject project) throws CoreException;

	/**
	 * Return the ICDescriptor for the project.  If project doesn't contain
	 * an ICDescriptor and create == true, then one is created
	 * 
 	 * Users should consider batching changes in an ICDescriptorOperation
	 * 
	 * @param project
	 * @param create
	 * @return ICDescriptor
	 * @throws CoreException
	 */
	public ICDescriptor getDescriptor(IProject project, boolean create) throws CoreException;

	/**
	 * Atomically runs the descriptor operation on the current project's configuration
	 * 
	 * The descriptor is automatically 'applied' after the CDescriptorOperation has been run
	 * @param project
	 * @param op
	 * @param monitor
	 * @throws CoreException
	 */
	public void runDescriptorOperation(IProject project, ICDescriptorOperation op, IProgressMonitor monitor) throws CoreException;

	/**
	 * Runs the ICDescriptorOperation on the provided ICProjectDescription. The changes are reconciled into
	 * the provided ICProjectDescription.
	 * 
	 * Currently this project description may be different from the current project description
	 * @param project
	 * @param des
	 * @param op
	 * @param monitor
	 * @throws CoreException
	 */
	public void runDescriptorOperation(IProject project, ICProjectDescription des, ICDescriptorOperation op,
										IProgressMonitor monitor) throws CoreException;

	public void addDescriptorListener(ICDescriptorListener listener);

	public void removeDescriptorListener(ICDescriptorListener listener);
}
