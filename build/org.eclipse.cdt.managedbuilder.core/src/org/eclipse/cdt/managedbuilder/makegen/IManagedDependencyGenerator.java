/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.makegen;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * @since 2.0
 */
public interface IManagedDependencyGenerator {
	public int TYPE_NODEPS = 0;
	public int TYPE_COMMAND = 1;
	public int TYPE_INDEXER = 2;
	public int TYPE_EXTERNAL = 3;
	
	public IResource[] findDependencies(IResource resource, IProject project);
	public int getCalculatorType();
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info);
}
