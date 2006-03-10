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

/**
 * 
 * This interface represents the build group.
 * The build group is a group of actions
 *
 */
public interface IBuildGroup {
	/**
	 * 
	 * Returns the array of build steps that belong to this group
	 * 
	 * @return IBuildStep[]
	 */
	IBuildStep[] getSteps();
	
	/**
	 * 
	 * Returns true if the build group contains the given step,
	 * false - otherwise
	 * 
	 * @param action
	 * @return boolean
	 */
	boolean contains(IBuildStep action);
	
	/**
	 * returns true is the build group needs rebuild,
	 * false - otherwise
	 * 
	 * @return boolean
	 */
	boolean needsRebuild();
}
