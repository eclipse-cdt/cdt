/*******************************************************************************
 * Copyright (c) 2010, Texas Instruments, Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments, Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model.provisional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;

/**
 * An extension of the IMemoryBlockRetrievalExtension interface that supports
 * memory spaces. The memory space interface is provisional, thus this class
 * cannot yet be API.
 * 
 * @author Alain Lee and John Cortell
 */
public interface IMemorySpaceAwareMemoryBlockRetrieval extends IMemoryBlockRetrievalExtension {

	/**
	 * Caller to {@link #getMemorySpaces()} provides one of these, as that
	 * method may need to consult the debugger backend, and thus needs to be
	 * asynchronous
	 */
	interface GetMemorySpacesRequest extends IRequest {
		String[] getMemorySpaces();
		void setMemorySpaces(String[] memorySpaceIds);
	}

	/**
	 * Provides the string encoding of a memory space qualified address. This
	 * method is called when having to represent a memory-space qualified
	 * address as a single string.
	 * 
	 * @param expression
	 *            the expression representing a location within a memory space.
	 *            This can be a simple numeric expression like "0x10000" or
	 *            something more complex "$EAX+(gCustomerCount*100)".
	 * @param memorySpaceID
	 *            a string which represents the memory space
	 * @return the encoded string representation of the address, or null to
	 *         indicate no custom encoding is required
	 */
	String encodeAddress(String expression, String memorySpaceID);

	/**
	 * The inverse of {@link #encodeAddress(String, String)}.
	 * 
	 * @param str
	 *            the encoded string
	 * @return the result of decoding the string into its components; never null
	 * @throws CoreException
	 *             if decoding and string is not in the expected format
	 */
	DecodeResult decodeAddress(String str) throws CoreException;

	interface DecodeResult {
		String getMemorySpaceId();
		String getExpression();
	}
	
	/**
	 * Provides the memory spaces available for the given debug context.
	 * 
	 * @param context
	 *            a debug context
	 * @param request
	 *            the asynchronous data request object
	 * @return an array of memory space identifiers
	 */
	void getMemorySpaces(Object context, GetMemorySpacesRequest request);

	/**
	 * Retrieves and returns a memory block.
	 * 
	 * @param expression
	 *            expression to be evaluated to an address
	 * @param context
	 *            a debug context
	 * @param memorySpaceID
	 *            the memory space the block is being requested for, or null if
	 *            n/a
	 * @return a memory block based on the given parameters
	 * @throws DebugException
	 *             if unable to retrieve the specified memory
	 */
	public IMemorySpaceAwareMemoryBlock getMemoryBlock(String expression, Object context, String memorySpaceID) throws DebugException;
	
}
