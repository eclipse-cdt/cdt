/*******************************************************************************
 * Copyright (c) 2005, 2010 Freescale, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * The memory space manager provides varous memory-space related operations.
 * The backend implementation of ICDITarget should implement this interface   
 * as well if the target supports memory spaces.
 */
public interface ICDIMemorySpaceManagement extends ICDIObject {
	/**
	 * Optionally provides the string encoding of a memory space qualified
	 * address. CDT provides a default encoding of
	 * <memory-space-id>:<address(hex)>. If this is adequate, the client can
	 * return null from this function.
	 * 
	 * @param address
	 *            a numeric address
	 * @param memorySpaceID
	 *            a string which represents the memory space
	 * @return the encoded string representation of the address or null
	 * @deprecated CDI clients should implement ICDIMemorySpaceEncoder
	 */
	@Deprecated
	String addressToString(BigInteger address, String memorySpaceID);

	/**
	 * The inverse of addressToString. Optionally decodes a memoryspace/address
	 * string to its components. Client must provide decoding if it provides
	 * encoding in addressToString. Conversely, it should return null if
	 * addressToString returns null.
	 * 
	 * @param str
	 *            the encoded string (contains memory space + hex address
	 *            value)
	 * @param memorySpaceID_out
	 *            the memory space ID
	 * @return the BigInteger part of str; client should return null if the
	 *         default decoding provided by CDT is sufficient
	 *         (<memory-space-id>:<address(hex)>)
	 * @throws CDIException
	 *             if string is not in the expected format
	 * @deprecated CDI clients should implement ICDIMemorySpaceEncoder 
	 */
	@Deprecated
	BigInteger stringToAddress(String str, StringBuffer memorySpaceID_out) throws CDIException;
	
	/**
	 * Provides the memory spaces available.
	 * 
	 * @return an array of memory space identifiers
	 */
	String [] getMemorySpaces();	

}
