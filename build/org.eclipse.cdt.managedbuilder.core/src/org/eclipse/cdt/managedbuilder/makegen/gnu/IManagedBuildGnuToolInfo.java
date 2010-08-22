/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import java.util.Vector;

/**
 * This interface returns information about a Tool's inputs
 * and outputs while a Gnu makefile is being generated.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
	public Vector<String> getCommandInputs();
	
	/**
	 * Returns the raw list of tool's input file names.
	 * 
	 *  @return Vector
	 */
	public Vector<String> getEnumeratedInputs();
	
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
	public Vector<String> getCommandOutputs();
	
	/**
	 * Returns the raw list of tool's primary output file names.
	 * 
	 *  @return Vector
	 */
	public Vector<String> getEnumeratedPrimaryOutputs();
	
	/**
	 * Returns the raw list of tool's secondary output file names.
	 * 
	 *  @return Vector
	 */
	public Vector<String> getEnumeratedSecondaryOutputs();
	
	/**
	 * Returns the raw list of tool's output variable names.
	 * 
	 *  @return Vector
	 */
	public Vector<String> getOutputVariables();
	
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
	 * Dependencies are top build directory relative.
	 * 
	 *  @return Vector
	 */
	public Vector<String> getCommandDependencies();

	/**
	 * Returns the tool's additional targets as determined by the
	 * dependency calculator.
	 * Additional targets are top build directory relative
	 * 
	 *  @return Vector
	 */
	public Vector<String> getAdditionalTargets();
	
	/**
	 * Returns the raw list of tool's input dependencies.
	 * 
	 *  @return Vector
	 */
	//public Vector<String> getEnumeratedDependencies();  
	
	/**
	 * Returns <code>true</code> if this is the target tool 
	 * else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isTargetTool();  
}
