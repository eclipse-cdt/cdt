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
 * Maintains the list of directories to search for source files.
 * Auto update is off by default.
 * 
 */
public interface ICDISourceManagement {
	/**
	 * Set the source search paths for the debug session.
	 * @param String array of search paths
	 */
	void setSourcePaths(String[] srcPaths) throws CDIException;

	/**
	 * Return the array of source paths
	 * @return String array of search paths.
	 */
	String[] getSourcePaths() throws CDIException;

	/**
	 *  @param startAddress is the begining address
	 *  @param endAddress is the end address
	 *  @throws CDIException on failure.
	 */
	ICDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress)
		throws CDIException;

	/**
	 * @param filename is the name of the file to disassemble
	 * @param linenum is the line number to disassemble around
	 * @throws CDIException on failure
	 */
	ICDIInstruction[] getInstructions(String filename, int linenum)
		throws CDIException;

	/**
	 * @param filename is the name of the file to disassemble
	 * @param linenum is the line number to disassemble around
	 * @param lines is the number of disassembly to produced
	 * @throws CDIException on failure
	 */
	ICDIInstruction[] getInstructions(String filename, int linenum, int lines)
		throws CDIException;

	/**
	 *  @param startAddress is the begining address
	 *  @param endAddress is the end address
	 *  @throws CDIException on failure.
	 */
	ICDIMixedInstruction[] getMixedInstructions(
		BigInteger startAddress,
	    BigInteger endAddress)
		throws CDIException;

	/**
	 * @param filename is the name of the file to disassemble
	 * @param linenum is the line number to disassemble around
	 * @param lines is the number of disassembly to produced
	 * @throws CDIException on failure
	 */
	ICDIMixedInstruction[] getMixedInstructions(String filename, int linenum)
		throws CDIException;

	/**
	 * @param filename is the name of the file to disassemble
	 * @param linenum is the line number to disassemble around
	 * @param lines is the number of disassembly to produced
	 * @throws CDIException on failure
	 */
	ICDIMixedInstruction[] getMixedInstructions(
		String filename,
		int linenum,
		int lines)
		throws CDIException;

}
