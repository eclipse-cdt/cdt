/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;

/**
 * 
 * Represents a location in the debuggable program.
 * 
 * @since Jul 9, 2002
 */
public interface ICDILocation
{
	/**
	 * Returns the address of this location.
	 * 
	 * @return the address of this location
	 */
	long getAddress();
	
	/**
	 * Returns the source file of this location or <code>null</code>
	 * if the source file is unknown.
	 *  
	 * @return the source file of this location
	 */
	String getFile();

	/**
	 * Returns the function of this location or <code>null</code>
	 * if the function is unknown.
	 *  
	 * @return the function of this location
	 */
	String getFunction();

	/**
	 * Returns the line number of this location or <code>null</code>
	 * if the line number is unknown.
	 *  
	 * @return the line number of this location
	 */
	int getLineNumber();
	
	/**
	 * Returns an array of the machine instructions of the function
	 * surrounding the address of this location.
	 *  
	 * @return an array of the machine instructions
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIInstruction[] getInstructions() throws CDIException;
	
	/**
	 * Returns an array of the machine instructions of the function
	 * surrounding the address of this location. If the number of 
	 * instructions is greater than maxCount the size of the returning
	 * array is limited by maxCount.
	 *  
	 * @param maxCount - maximum number of instructions to read
	 * @return an array of the machine instructions
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIInstruction[] getInstructions( int maxCount ) throws CDIException;
	
	/**
	 * Return true if the both location refers to the same
	 * place.
	 */
	boolean equals(ICDILocation location);
}
