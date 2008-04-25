/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ICDescriptorManager {

	public void configure(IProject project, String id) throws CoreException;

	public void convert(IProject project, String id) throws CoreException;
		
	public ICDescriptor getDescriptor(IProject project) throws CoreException;
	
	public ICDescriptor getDescriptor(IProject project, boolean create) throws CoreException;

	public void runDescriptorOperation(IProject project, ICDescriptorOperation op, IProgressMonitor monitor) throws CoreException;
	
	public void runDescriptorOperation(IProject project,
			ICProjectDescription des,
			ICDescriptorOperation op,
			IProgressMonitor monitor)
				throws CoreException;
	
	public void addDescriptorListener(ICDescriptorListener listener);

	public void removeDescriptorListener(ICDescriptorListener listener);
}
