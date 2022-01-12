/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.buildmodel;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;

/**
 *
 * this interface represents the build description factory
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildDescriptionFactory {

	/**
	 * creates the build description
	 * @param cfg the build configuration for which the description is to be
	 * created
	 * @param delta the resource delta or null if none
	 * @param flags specifies how the build description should be generated
	 * and what information it should contain.
	 * Can contain the following flags:
	 * BuildDescriptionManager.REBUILD,
	 * BuildDescriptionManager.REMOVED,
	 * BuildDescriptionManager.DEPS,
	 * BuildDescriptionManager.DEPFILES,
	 *
	 *
	 * @see BuildDescriptionManager#REBUILD
	 * @see BuildDescriptionManager#REMOVED
	 * @see BuildDescriptionManager#DEPS
	 * @see BuildDescriptionManager#DEPFILES
	 * @return IBuildDescription
	 * @throws CoreException if the build description creation fails
	 */
	IBuildDescription createBuildDescription(IConfiguration cfg, IResourceDelta delta, int flags) throws CoreException;

	/**
	 *
	 * returns the supported methods of generation the build description
	 *
	 * @see BuildDescriptionManager#REBUILD
	 * @see BuildDescriptionManager#REMOVED
	 * @see BuildDescriptionManager#DEPS
	 * @see BuildDescriptionManager#DEPFILES
	 *
	 * @return int
	 */
	int getSupportedMethods();
}
