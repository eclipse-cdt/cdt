/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

/**
 * 
 * Represents a machine instruction.
 * 
 * @since Jul 10, 2002
 */
public interface ICInstruction extends ICObject
{
	/**
	 * Returns the instruction's offset.
	 * 
	 * @return the offset of this machine instruction
	 */
	long getOffset();
}
