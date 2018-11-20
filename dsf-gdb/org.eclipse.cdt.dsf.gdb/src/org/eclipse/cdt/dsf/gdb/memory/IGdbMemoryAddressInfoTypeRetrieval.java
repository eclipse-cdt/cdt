/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.memory;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IMemoryBlockAddressInfoItem;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.debug.core.model.IMemoryBlock;

/**
 * @since 5.0
 */
public interface IGdbMemoryAddressInfoTypeRetrieval {
	/**
	 * @return the String representing the type of information items being provided
	 */
	String getInfoType();

	/**
	 * The implementation provides the items of an associated type which could be pointing to a memory address
	 */
	void itemsRequest(IDMContext selectionContext, IMemoryBlock memoryBlock,
			DataRequestMonitor<IMemoryBlockAddressInfoItem[]> rm);
}
