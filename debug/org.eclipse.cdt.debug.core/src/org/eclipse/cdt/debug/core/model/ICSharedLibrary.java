/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

/**
 * Represents a shared library.
 */
public interface ICSharedLibrary extends IDebugElement {

	/**
	 * Returns the name of shared library file.
	 * 
	 * @return the name of shared library file
	 */
	String getFileName();

	/**
	 * Returns the start address of this library.
	 * 
	 * @return the start address of this library
	 */
	IAddress getStartAddress();

	/**
	 * Returns the end address of this library.
	 * 
	 * @return the end address of this library
	 */
	IAddress getEndAddress();

	/**
	 * Returns whether the symbols of this library are read.
	 * 
	 * @return whether the symbols of this library are read
	 */
	boolean areSymbolsLoaded();

	/**
	 * Loads the library symbols.
	 * 
	 * @throws DebugException if this method fails. Reasons include:
	 */
	void loadSymbols() throws DebugException;
}
