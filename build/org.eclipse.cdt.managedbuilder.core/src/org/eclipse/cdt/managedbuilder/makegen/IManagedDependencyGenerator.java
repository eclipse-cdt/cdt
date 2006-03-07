/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * @since 2.0
 * @deprecated 3.1
 * 
 * Use IManagedDependencyGenerator2 instead.
*/
public interface IManagedDependencyGenerator extends IManagedDependencyGeneratorType {
	
	public IResource[] findDependencies(IResource resource, IProject project);
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info);
}
