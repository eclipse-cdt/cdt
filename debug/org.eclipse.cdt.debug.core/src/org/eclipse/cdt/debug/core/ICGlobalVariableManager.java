/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.core; 

import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.debug.core.DebugException;
 
/**
 * Manages the collection of global variables added to a debug target.
 */
public interface ICGlobalVariableManager {

	/**
	 * Registers with this manager the global variables specified by given descriptors.
	 * 
	 * @param descriptors the descriptors of global variables to register with this manager	  
	 * @throws DebugException
	 */
	public void addGlobals( IGlobalVariableDescriptor[] descriptors ) throws DebugException;

	/**
	 * Removes specified global variables from this manager.
	 * 
	 * @param globals global variables to remove
	 */
	public void removeGlobals( ICGlobalVariable[] globals );

	/**
	 * Removes all global variables from this manager.
	 */
	public void removeAllGlobals();

	/**
	 * Returns the array of descriptors of global varibales added to this manager.
	 * 
	 * @return the array of descriptors
	 */
	public IGlobalVariableDescriptor[] getDescriptors();
}
