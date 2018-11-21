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

import java.util.Map;

import org.eclipse.core.runtime.IPath;

/**
 *
 * This interface is used to represent the build action
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildStep {
	/**
	 * Returns an array of input types for this step
	 * @see IBuildIOType
	 *
	 * @return IBuildIOType[]
	 */
	IBuildIOType[] getInputIOTypes();

	/**
	 * Returns an array of output types for this step
	 * @see IBuildIOType
	 *
	 * @return IBuildIOType[]
	 */
	IBuildIOType[] getOutputIOTypes();

	/**
	 * Returns true if the step needs rebuild, false - otherwise
	 *
	 * @return boolean
	 */
	boolean needsRebuild();

	/**
	 * Returns the complete set of input resources for this step
	 *
	 * @return IBuildResource[]
	 */
	IBuildResource[] getInputResources();

	/**
	 * Returns the complete set of output resources for this step
	 *
	 * @return IBuildResource[]
	 */
	IBuildResource[] getOutputResources();

	/**
	 * Returns true if the step is removed (due to removal
	 * of the project resources that were ised in thie action)
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

	/**
	 * @return the set of commands used for building the step
	 * NOTE: This is a preliminary method
	 */
	IBuildCommand[] getCommands(IPath cwd, Map inStepMap, Map outStepMap, boolean resolveAll);
}
