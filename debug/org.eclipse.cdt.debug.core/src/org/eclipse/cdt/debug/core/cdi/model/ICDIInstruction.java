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
public interface ICDIInstruction extends ICDIObject {
	/**
	 *  Returns the Address.
	 * @return the address.
	 */
	long getAdress();
	
	/**
	 * @return the function name.
	 */
	String getFuntionName();
	
	/**
	 * @return the instruction.
	 */
	String getInstruction();
	
	/**
	 * Returns the instruction's offset.
	 * 
	 * @return the offset of this machine instruction
	 */
	long getOffset();
}
