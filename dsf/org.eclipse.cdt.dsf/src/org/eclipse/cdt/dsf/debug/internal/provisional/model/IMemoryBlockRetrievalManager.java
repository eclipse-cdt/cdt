/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.provisional.model;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;

/**
 * <p>It's expected to have one IMemoryBlockRetrievalManager per session which instantiates and keeps the mapping between IMemoryDMContext and its
 * corresponding IMemoryBlockRetrieval. </p>
 *
 * <p>One IMemoryDMContext represents a hierarchy of elements that share the same memory e.g A process, thread and frame in Linux,
 * One session is capable of having multiple IMemoryContext e.g. Debugging multiple programs under the same session</p>
 *
 * <p>An IMemoryBlockRetrieval helps to retrieve memory blocks from the same memory and therefore it is expected to have a one to one
 * relationship with an IMemoryDMContext</p>
 *
 * <p>Functionality detecting changes to different memory context elements can resolve the corresponding IMemoryBlockRetrieval via this API.</p>
 *
 */
public interface IMemoryBlockRetrievalManager {
	/**
	 * A method to resolve the specific IMemoryBlockRetrieval associated to the IMemoryDMContext of the given IDMContext
	 *
	 * @param dmc - A context which either itself or one of its parents is an IMemoryDMContext
	 * @return - The IMemoryBlockRetrieval associated to the IMemoryDMContext resolved from the given dmc
	 */
	public IMemoryBlockRetrieval getMemoryBlockRetrieval(IDMContext dmc);

	/**
	 * Shall be called when this manager is no longer needed
	 * Any required clean up needs to be performed so this class can be
	 * garbage collected.
	 */
	public void dispose();
}
