/*******************************************************************************
 * Copyright (c) 2010, Texas Instruments, Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public interface IMemorySpaces extends IDsfService {

	/**
	 * A context that represents a particular memory space. Simple targets have
	 * a single, implicit memory space, but some have multiple, e.g., code,
	 * data, virtual, physical.
	 */
	public interface IMemorySpaceDMContext extends IMemoryDMContext {

		/**
		 * The string-based handle used to refer to the memory space, as per
		 * what's returned in
		 * {@link IMemorySpaces#getMemorySpaces(IDMContext, DataRequestMonitor)
		 */
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
	 *            a context which might <i>contain</i> one or more memory
	 *            spaces. Contexts that may be <i>associated</i> with a memory
	 *            space should not be passed in. E.g., an expression might be
	 *            associated with a memory space, but it does not contain memory
	 *            spaces, and is thus not an appropriate context for this
	 *            method.
	 * @param rm
	 *            the asynchronous data request monitor. Returns a collection of
	 *            memory space IDs. Never null, but may be empty.
	 */
	void getMemorySpaces(IDMContext context, final DataRequestMonitor<String[]> rm);

	/**
	 * Return true if creating a memory block with a null memory space ID is NOT
	 * supported. Some debuggers may not have the ability to infer the memory
	 * space from an expression, in which case the user should be forced to
	 * select a memory space when being prompted for a memory location.
	 */
	public boolean creatingBlockRequiresMemorySpaceID();

	/**
	 * Provides the default memory space to be used in the given context.
	 *
	 * @param ctx
	 *            a context which might <i>contain</i> one or more memory
	 *            spaces. Contexts that may be <i>associated</i> with a memory
	 *            space should not be passed in. E.g., an expression might be
	 *            associated with a memory space, but it does not contain memory
	 *            spaces, and is thus not an appropriate context for this
	 *            method.
	 * @param rm
	 *            the asynchronous data request monitor. Returns a memory space ID.
	 *            Never null, but may be empty.
	 * @since 2.7
	 */
	default void getDefaultMemorySpace(IDMContext context, final DataRequestMonitor<String> rm) {
		rm.setData(""); //$NON-NLS-1$
		rm.done();
	}
}
