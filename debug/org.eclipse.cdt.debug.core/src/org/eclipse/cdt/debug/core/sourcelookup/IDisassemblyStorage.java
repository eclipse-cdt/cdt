/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.core.resources.IStorage;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Defines methods specific to disassembly.
 * 
 * @since: Oct 8, 2002
 */
public interface IDisassemblyStorage extends IStorage
{
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
	 * @param address - an address 
	 * @return the line number for given address
	 */
	int getLineNumber( long address ) ;
}
