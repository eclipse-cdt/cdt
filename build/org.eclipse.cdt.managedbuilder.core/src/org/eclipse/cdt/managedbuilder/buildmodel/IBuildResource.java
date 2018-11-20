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

import java.net.URI;

import org.eclipse.core.runtime.IPath;

/**
 *
 * This interface represents the resource used in the build
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildResource {
	/**
	 * Returns the absolute path to the resource as seen on the target machine.
	 *
	 * @return IPath
	 */
	IPath getLocation();

	/**
	 * In case the resource is a workspace resource,
	 * returns the full workspace path for the resource
	 * otherwise returns null
	 *
	 * @return IPath
	 */
	IPath getFullPath();

	/**
	 * Returns a URI that can be used by EFS to access the build resource.
	 *
	 * @return URI
	 * @since 6.0
	 */
	URI getLocationURI();

	/**
	 * Returns the output io type of the step
	 * that generates this resource.
	 * In case the resource is the project source,
	 * The returned output io type belongs to the main input step
	 *
	 * @see IBuildDescription#getInputStep()
	 *
	 * @return IBuildIOType
	 */
	IBuildIOType getProducerIOType();

	/**
	 * Returns an array of io types where this resource is used as an input
	 *
	 * @return IBuildIOType[]
	 */
	IBuildIOType[] getDependentIOTypes();

	/**
	 * Returns the step that generates this resource.
	 * In case the resource is the project source,
	 * The main input step is returned
	 *
	 * @see IBuildDescription#getInputStep()
	 *
	 * @return IBuildIOType
	 */
	IBuildStep getProducerStep();

	/**
	 * Returns an array of steps that use this resource as an input
	 *
	 * @return IBuildIOType[]
	 */
	IBuildStep[] getDependentSteps();

	/**
	 * Returns true if the resource needs rebuild
	 * this implies that all build steps dependent on this resource
	 * are to be invoked
	 *
	 * @return boolean
	 */
	boolean needsRebuild();

	/**
	 * Returns true if this resource belongs to the project
	 *
	 * @return boolean
	 *
	 */
	boolean isProjectResource();

	/**
	 * Returns true if the resource was removed from the build
	 * Note: the removed state represents is BUILD state rather than
	 * a file system state.
	 * If the build resouces is marked as removed that does not mean the
	 * resource is removed in the file system
	 * The removed state specifies that the resource is no longer used in the
	 * build process.
	 * E.g. the object file could be marked as removed if the source file was deleted
	 * in the file system
	 * The removed state information is used primarily for calculation
	 * of the project part that is to be rebuild
	 *
	 * @return boolean
	 */
	boolean isRemoved();

	/**
	 * returns a build description that holds this step
	 *
	 * @return IBuildDescription
	 */
	IBuildDescription getBuildDescription();
}
