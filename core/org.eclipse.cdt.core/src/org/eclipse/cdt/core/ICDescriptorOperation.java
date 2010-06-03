/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated
 */
@Deprecated
public interface ICDescriptorOperation {
	
	/**
	 * Call-back method executed atomically on a ICDescriptor in a runnable.
	 * 
	 * @see ICDescriptorManager#runDescriptorOperation(IProject, ICDescriptorOperation, IProgressMonitor)
	 * @see ICDescriptorManager#runDescriptorOperation(IProject, ICProjectDescription, ICDescriptorOperation, IProgressMonitor)
	 * @param descriptor
	 * @param monitor
	 * @throws CoreException
	 */
	void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException;

}
