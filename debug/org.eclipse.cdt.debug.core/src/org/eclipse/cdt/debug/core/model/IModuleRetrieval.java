/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model; 

import org.eclipse.debug.core.DebugException;
 
/**
 * Comment for .
 */
public interface IModuleRetrieval {
	
	/**
	 * Returns whether there are modules currently loaded in this debug target.
	 * 
	 * @return whether there are modules currently loaded in this debug target
	 * 
	 * @throws DebugException
	 */
	public boolean hasModules() throws DebugException;

	/**
	 * Returns the array of the currently loaded modules.
	 *  
	 * @return the array of the currently loaded modules
	 * @throws DebugException if this method fails. Reasons include:
	 */
	public ICModule[] getModules() throws DebugException;

	/**
	 * Load symbols for all currently loaded modules.
	 * 
	 * @throws DebugException if this method fails. Reasons include:
	 */
	public void loadSymbolsForAllModules() throws DebugException;
}
