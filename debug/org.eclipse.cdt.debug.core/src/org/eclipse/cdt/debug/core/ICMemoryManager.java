/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;

/**
 * Enter type comment.
 * 
 * @since: Oct 15, 2002
 */
public interface ICMemoryManager extends IAdaptable
{
	void addBlock( IMemoryBlock memoryBlock ) throws DebugException;

	void removeBlock( IMemoryBlock memoryBlock ) throws DebugException;

	void removeAllBlocks() throws DebugException;

	IMemoryBlock getBlock( int index );

	IMemoryBlock[] getBlocks();
}
