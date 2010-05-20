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
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.core.runtime.CoreException;

/**
 * A memory service that is memory space aware.
 * 
 * <p>
 * Memory contexts are not meant to be represented in tree or table views, so it
 * doesn't need to implement IDMService interface.
 * 
 * @author Alain Lee and John Cortell
 * @since 2.1
 */
public interface IMemorySpaces extends IDsfService{

	public interface IMemorySpaceDMContext extends IMemoryDMContext {
		public String getMemorySpaceId();
	}

	/**
	 * Optionally provides the string encoding of a memory space qualified
	 * address. CDT provides a default encoding of
	 * [memory-space-id]:[expression]. If this is adequate, the client can
	 * return null from this method. This method is called when having to
	 * represent a memory-space qualified address as a single string.
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
	 * The inverse of {@link #encodeAddress(String, String)}. Client should
	 * provide decoding if it provides encoding. Conversely, it should not
	 * provide decoding if it doesn't provide encoding.
	 * 
	 * @param str
	 *            the encoded string
	 * @return the result of decoding the string into its components, or null to
	 *         indicate no custom decoding is required
	 * @throws CoreException
	 *             if decoding and string is not in the expected format
	 */
	DecodeResult decodeAddress(String str) throws CoreException;
	
	interface DecodeResult {
		String getMemorySpaceId();
		String getExpression();
	}

	/**
	 * Provides the memory spaces available in the given context.
	 * 
	 * @param ctx
	 *            a DM context
	 * @param rm
	 *            the asynchronous data request monitor. Returns a collection of
	 *            memory space IDs.
	 */
	void getMemorySpaces(IDMContext context, final DataRequestMonitor<String[]> rm);
	
	/**
	 * Return true if creating a memory block with a null memory space ID is NOT
	 * supported. Some debuggers may not have the ability to infer the memory
	 * space from an expression, in which case the user should be forced to
	 * select a memory space when being prompted for a memory location.
	 */
	public boolean creatingBlockRequiresMemorySpaceID();
}
