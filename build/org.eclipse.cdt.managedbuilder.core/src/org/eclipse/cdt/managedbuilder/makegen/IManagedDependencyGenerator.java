/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
@Deprecated
public interface IManagedDependencyGenerator extends IManagedDependencyGeneratorType {

	public IResource[] findDependencies(IResource resource, IProject project);

	public String getDependencyCommand(IResource resource, IManagedBuildInfo info);
}
