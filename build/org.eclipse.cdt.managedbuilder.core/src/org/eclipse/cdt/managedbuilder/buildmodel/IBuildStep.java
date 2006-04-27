/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * 
	 * Returns the set of commands used for building the step
	 * 
	 * NOTE: This is a preliminary method
	 * 
	 * @param cwd
	 * @param inStepMap
	 * @param outStepMap
	 * @param resolveAll
	 * @return
	 */
	IBuildCommand[] getCommands(IPath cwd, Map inStepMap, Map outStepMap, boolean resolveAll);
}
