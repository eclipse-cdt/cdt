/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.core.cdi;

import java.io.File;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;

/**
 * 
 * Maintains the list of directories to search for source files.
 * 
 * @since Jul 9, 2002
 */
public interface ICDISourceManager extends ICDISessionObject
{
	/**
	 * Set the source search paths for the debug session.
	 * @param String array of search paths
	 */
	void addSourcePaths(String[] srcPaths) throws CDIException;

	/**
	 * Return the array of source paths
	 * @return String array of search paths.
	 */
	String[] getSourcePaths() throws CDIException;

	/**
	 * Returns an array of directories. Returns the empty array 
	 * if the source path is empty.
	 * 
	 * @return an array of directories
	 * @throws CDIException on failure. Reasons include:
	 */
	File[] getDirectories() throws CDIException;
	
	/**
	 * Sets the source path according to the given array of directories.
	 * 
	 * @param directories - the array of directories
	 * @throws CDIException on failure. Reasons include:
	 */
	void set( File[] directories ) throws CDIException;
	
	/**
	 * Reset the source path to empty.
	 * 
	 * @throws CDIException on failure. Reasons include:
	 */
	void reset() throws CDIException;

	/**
	 *  @param startAddress is the begining address
	 *  @param endAddress is the end address
	 *  @throws CDIException on failure.
	 */
	ICDIInstruction[] getInstructions(long startAddress, long endAddress)  throws CDIException;

	/**
	 * @param filename is the name of the file to disassemble
	 * @param linenum is the line number to disassemble around
	 * @throws CDIException on failure
	 */
	ICDIInstruction[] getInstructions(String filename,  int linenum)  throws CDIException;

	/**
	 * @param filename is the name of the file to disassemble
	 * @param linenum is the line number to disassemble around
	 * @param lines is the number of disassembly to produced
	 * @throws CDIException on failure
	 */
	ICDIInstruction[] getInstructions(String filename,  int linenum, int lines) throws CDIException;

	/**
	 *  @param startAddress is the begining address
	 *  @param endAddress is the end address
	 *  @throws CDIException on failure.
	 */
	ICDIMixedInstruction[] getMixedInstructions(long startAddress, long endAddress)  throws CDIException;

	/**
	 * @param filename is the name of the file to disassemble
	 * @param linenum is the line number to disassemble around
	 * @param lines is the number of disassembly to produced
	 * @throws CDIException on failure
	 */
	ICDIMixedInstruction[] getMixedInstructions(String filename,  int linenum) throws CDIException;

	/**
	 * @param filename is the name of the file to disassemble
	 * @param linenum is the line number to disassemble around
	 * @param lines is the number of disassembly to produced
	 * @throws CDIException on failure
	 */
	ICDIMixedInstruction[] getMixedInstructions(String filename,  int linenum, int lines) throws CDIException;


}
