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

/**
 *
 * This interface is used to combine a set of build resources
 * that are inputs or outputs for the given action
 * into one group
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildIOType {
	/**
	 * Specifies whether this argument is Step input or output
	 *
	 * @return boolean
	 */
	boolean isInput();

	/**
	 * Specifies a set of resources associated with this argument
	 *
	 * @return IBuildResource[]
	 */
	IBuildResource[] getResources();

	/**
	 * Specifies the build step this argument belongs to
	 *
	 * @return IBuildStep
	 */
	IBuildStep getStep();
}
