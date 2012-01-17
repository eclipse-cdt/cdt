/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Nokia - Added support for AbsoluteSourceContainer(159833) 
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
 
/**
 * Computes the default source lookup path for a launch configuration.
 */
public class CSourcePathComputerDelegate implements ISourcePathComputerDelegate {

	/** 
	 * Constructor for CSourcePathComputerDelegate. 
	 */
	public CSourcePathComputerDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		ISourceContainer[] common = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().getSourceContainers();
		ISourceContainer[] containers = new ISourceContainer[common.length];
		
		for (int i = 0; i < common.length; i++) {
			ISourceContainer container = common[i];
			ISourceContainerType type = container.getType();
			// Clone the container to make sure that the original can be safely disposed.
			container = type.createSourceContainer(type.getMemento(container));
			containers[i] = container;
		}
		return containers;
	}
}
