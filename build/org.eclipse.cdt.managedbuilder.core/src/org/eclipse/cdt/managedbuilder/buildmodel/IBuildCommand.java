/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
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
 * This interface represents a command to be invoked for building the step
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildCommand {
	
	/**
	 * returns the executable path
	 * the paths could be either relative or absolute
	 * 
	 * @return IPath
	 */
	IPath getCommand();
	
	/**
	 * Returns the array of arguments to be passed to the executable
	 * 
	 * @return String[]
	 */
	String[] getArgs();
	
	/**
	 * Returns the Map representing the environment to be used for the executable process
	 * The map contains the String to String pairs representing the variable name and value respectively
	 * 
	 * @return Map
	 */
	Map<String, String> getEnvironment();
	
	/**
	 * Returns the working directory to be used for the process
	 * 
	 * @return IPath
	 */
	IPath getCWD();
}
