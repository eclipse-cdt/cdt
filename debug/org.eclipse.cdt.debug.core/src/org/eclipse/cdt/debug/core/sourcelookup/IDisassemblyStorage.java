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
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.core.resources.IStorage;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Defines methods specific to disassembly.
 */
public interface IDisassemblyStorage extends IStorage {

	/**
	 * Returns the debug target of this disassembly.
	 * 
	 * @return the debug target of this disassembly
	 */
	IDebugTarget getDebugTarget();

	/**
	 * Returns whether this storage contains the instructions at given address.
	 * 
	 * @param address - an address
	 * @return whether this storage contains the instructions at given address
	 */
	boolean containsAddress( long address );

	/**
	 * Returns the line number for given address.
	 * 
	 * @param address - an address
	 * @return the line number for given address
	 */
	int getLineNumber( long address );

	/**
	 * Returns the address of instruction at given line.
	 * 
	 * @param lineNumber - a line number
	 * @return the address of instruction at given line
	 */
	long getAddress( int lineNumber );
}