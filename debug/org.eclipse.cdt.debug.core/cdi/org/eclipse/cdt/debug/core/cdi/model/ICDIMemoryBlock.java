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

package org.eclipse.cdt.debug.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * A contiguous segment of memory in an execution context. A memory
 * block is represented by a starting memory address and a length.
 * 
 * @since Jul 18, 2002
 */
public interface ICDIMemoryBlock extends ICDIObject {
	/**
	 * Returns the start address of this memory block.
	 * 
	 * @return the start address of this memory block
	 */
	BigInteger getStartAddress();
	
	/**
	 * Returns the length of this memory block in bytes.
	 * 
	 * @return the length of this memory block in bytes
	 */	
	long getLength();
	
	/**
	 * Returns the values of the bytes currently contained
	 * in this this memory block.
	 * 
	 * @return the values of the bytes currently contained
	 *  in this this memory block
	 * @exception CDIException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The CDIException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */	
	byte[] getBytes() throws CDIException;
	
	/**
	 * Sets the value of the bytes in this memory block at the specified
	 * offset within this memory block to the spcified bytes.
	 * The offset is zero based.
	 * 
	 * @param offset the offset at which to set the new values
	 * @param bytes the new values
	 * @exception CDIException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The CDIException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This memory block does not support value modification</li>
	 * <li>The specified offset is greater than or equal to the length
	 *   of this memory block, or the number of bytes specified goes
	 *   beyond the end of this memory block (index of out of range)</li>
	 * </ul>
	 */
	void setValue(long offset, byte[] bytes) throws CDIException;

	/**
	 * @return true if the block does not update.
	 */
	boolean isFrozen();

	/**
	 * A memoryBlock set frozen means that the block will
	 * not update and check for new data.
	 * @param frozen the block is frozen by default.
	 */
	void setFrozen(boolean frozen);

	/**
	 * Refresh the data, this may cause events to be trigger
	 * if the data values changed.
	 */
	void refresh() throws CDIException;

}
