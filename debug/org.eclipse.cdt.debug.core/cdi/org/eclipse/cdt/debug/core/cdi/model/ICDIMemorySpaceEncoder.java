/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * Add-on interface for objects that implement ICDIMemorySpaceManagement.
 * Provides the string encoding and decoding of a memory space qualified
 * address. CDT provides a default encoding of [memory-space-id]:[expression].
 * If this is adequate, the CDI client need not implement this interface. This
 * method is called when having to represent a memory-space qualified address as
 * a single string.
 * 
 * @since 7.0
 */
public interface ICDIMemorySpaceEncoder extends ICDIObject {
	/**
	 * Encode an expression + memory space ID to a string.
	 * 
	 * @param expression
	 *            the expression representing a location within a memory space.
	 *            This can be a simple numeric expression like "0x10000" or
	 *            something more complex "$EAX+(gCustomerCount*100)".
	 * @param memorySpaceID
	 *            a string which represents the memory space
	 * @return the encoded string representation of the address; never null
	 */
	String encodeAddress(String expression, String memorySpaceID);

	/**
	 * The inverse of {@link #encodeAddress(String, String)}. 
	 * 
	 * @param str
	 *            the encoded string
	 * @return the result of decoding the string into its components; never null
	 * @throws CDIException
	 *             if string is not in the expected format
	 */
	DecodeResult decodeAddress(String str) throws CDIException;
	
	interface DecodeResult {
		String getMemorySpaceId();
		String getExpression();
	}
}
