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
 * This interface is used to combine a set of build resources
 * that are inputs or outputs for the given action
 * into one group 
 * 
 */
public interface IBuildIOType {
	/**
	 * Specifies whether this argument is Action input or output
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
	 * Specifies the build action this argument belongs to
	 * 
	 * @return IBuildAction
	 */
	IBuildStep getStep();
}
