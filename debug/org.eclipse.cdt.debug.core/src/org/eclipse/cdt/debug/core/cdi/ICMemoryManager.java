/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;

/**
 * 
 * The memory manager manages the collection of memory blocks 
 * specified for the debug session.
 * 
 * @since Jul 9, 2002
 */
public interface ICMemoryManager extends ICSessionObject
{
	/**
	 * Adds the given memory block to the debug session.
	 * 
	 * @param memoryBlock - the memory block to be added
	 * @exception CDIException on failure. Reasons include:
	 */
	void addBlock( ICMemoryBlock memoryBlock ) throws CDIException;
	
	/**
	 * Removes the given memory block from the debug session.
	 * 
	 * @param memoryBlock - the memory block to be removed
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeBlock( ICMemoryBlock memoryBlock );
	
	/**
	 * Removes the given array of memory blocks from the debug session.
	 * 
	 * @param memoryBlock - the array of memory blocks to be removed
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeBlocks( ICMemoryBlock[] memoryBlocks ) throws CDIException;;
	
	/**
	 * Removes all memory blocks from the debug session.
	 * 
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeAllBlocks() throws CDIException;

	/**
	 * Returns a memory block specified by given identifier.
	 * 
	 * @param id - the block identifier
	 * @return a memory block with the specified identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICMemoryBlock getBlock( String id ) throws CDIException;
	
	/**
	 * Returns an array of all memory blocks set for this debug session.
	 *
	 * @return an array of all memory blocks set for this debug session
	 * @throws CDIException on failure. Reasons include:
	 */
	ICMemoryBlock[] getBlocks() throws CDIException;
}
