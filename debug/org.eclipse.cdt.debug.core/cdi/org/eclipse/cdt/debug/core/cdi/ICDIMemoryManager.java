/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;

/**
 * 
 * The memory manager manages the collection of memory blocks 
 * specified for the debug session.
 * Auto update is on by default.
 * @since Jul 9, 2002
 */
public interface ICDIMemoryManager extends ICDIManager {

	/**
	 * Returns a memory block specified by given identifier.
	 * 
	 * @param address 
	 * @param length - how much for address
	 * @return a memory block with the specified identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock createMemoryBlock(String address, int length)
		throws CDIException;

	/**
	 * Returns a memory block specified by given identifier.
	 * 
	 * @param address 
	 * @param length - how much for address
	 * @return a memory block with the specified identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock createMemoryBlock(IAddress address, int length)
		throws CDIException;

	/**
	 * Removes the given memory block from the debug session.
	 * 
	 * @param memoryBlock - the memory block to be removed
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeBlock(ICDIMemoryBlock memoryBlock) throws CDIException;

	/**
	 * Removes the given array of memory blocks from the debug session.
	 * 
	 * @param memoryBlock - the array of memory blocks to be removed
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException;
	;

	/**
	 * Removes all memory blocks from the debug session.
	 * 
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeAllBlocks() throws CDIException;

	/**
	 * Returns an array of all memory blocks set for this debug session.
	 *
	 * @return an array of all memory blocks set for this debug session
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock[] getMemoryBlocks() throws CDIException;

}
