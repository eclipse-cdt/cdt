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
public interface ICDIMixedInstruction extends ICDIObject {
	/**
	 * @return the line Number.
	 */
	int getLineNumber();
	
	/**
	 * @return the file name
	 */
	String getFileName();
	
	/**
	 * @return the array of instruction.
	 */
	ICDIInstruction[] getInstructions();
}
