/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import java.util.Vector;
import java.util.HashMap;

/**
 * This interface returns information about a Tool's inputs
 * and outputs while a Gnu makefile is being generated.
 */
public interface IManagedBuildGnuToolInfo {
	public final String DOT = ".";	//$NON-NLS-1$

	/**
	 * Returns <code>true</code> if the tool's inputs have been calculated, 
	 * else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean areInputsCalculated();
	
	/**
	 * Returns the tool's inputs in command line format.  This will use
	 * variables rather than actual file names as appropriate.
	 * 
	 *  @return Vector
	 */
	public Vector getCommandInputs();
	
	/**
	 * Returns the raw list of tool's input file names.
	 * 
	 *  @return Vector
	 */
	public Vector getEnumeratedInputs();
	
	/**
	 * Returns <code>true</code> if the tool's outputs have been calculated, 
	 * else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean areOutputsCalculated();  

	/**
	 * Returns the tool's outputs in command line format.  This will use
	 * variables rather than actual file names as appropriate.
	 * 
	 *  @return Vector
	 */
	public Vector getCommandOutputs();
	
	/**
	 * Returns the raw list of tool's output file names.
	 * 
	 *  @return Vector
	 */
	public Vector getEnumeratedOutputs();
	
	/**
	 * Returns the raw list of tool's output variable names.
	 * 
	 *  @return Vector
	 */
	public Vector getOutputVariables();
	
	/**
	 * Returns <code>true</code> if the tool's dependencies have been calculated, 
	 * else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean areDependenciesCalculated();
	
	/**
	 * Returns the tool's dependencies in command line format.  This will use
	 * variables rather than actual file names as appropriate.
	 * 
	 *  @return Vector
	 */
	public Vector getCommandDependencies();
	
	/**
	 * Returns the raw list of tool's input dependencies.
	 * 
	 *  @return Vector
	 */
	//public Vector getEnumeratedDependencies();  
	
	/**
	 * Returns <code>true</code> if this is the target tool 
	 * else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isTargetTool();  
}
