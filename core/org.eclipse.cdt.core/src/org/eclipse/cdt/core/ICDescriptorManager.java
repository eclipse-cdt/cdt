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
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ICDescriptorManager {

	/**
	 * @param project
	 * @param id
	 * @throws CoreException
	 */
	public void configure(IProject project, String id) throws CoreException;
	/**
	 * @param project
	 * @param id
	 * @throws CoreException
	 */
	public void convert(IProject project, String id) throws CoreException;
		
	/**
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	public ICDescriptor getDescriptor(IProject project) throws CoreException;
	
	/**
	 * @param project
	 * @param forceCreation
	 * @return
	 * @throws CoreException
	 */
	public ICDescriptor getDescriptor(IProject project, boolean create) throws CoreException;

	/**
	 * @param project
	 * @param op
	 * @param monitor
	 * @throws CoreException
	 */
	public void runDescriptorOperation(IProject project, ICDescriptorOperation op, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * @param listener
	 */
	public void addDescriptorListener(ICDescriptorListener listener);

	/**
	 * @param listener
	 */
	public void removeDescriptorListener(ICDescriptorListener listener);
}
