/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * A contiguos segment of memory in an execution context. A memory 
 * block is represented by a starting memory address and a length.
 * 
 * @since Jul 18, 2002
 */
public interface ICDIMemoryBlock extends ICDIObject
{
	/**
	 * Returns the start address of this memory block.
	 * 
	 * @return the start address of this memory block
	 */
	long getStartAddress();
	
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
	 * Returns whether this memory block supports value modification
	 * 
	 * @return whether this memory block supports value modification
	 */
	boolean supportsValueModification();
	
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
	void setValue( long offset, byte[] bytes ) throws CDIException;

	boolean isFreezed();
	
	void setFreezed( boolean freezed );
}
