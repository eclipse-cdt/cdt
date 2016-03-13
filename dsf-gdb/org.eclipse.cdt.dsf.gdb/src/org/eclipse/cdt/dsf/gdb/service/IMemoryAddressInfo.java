/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IMemoryBlockAddressInfoItem;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.debug.core.model.IMemoryBlock;

/**
 * Provides handles to the available Memory Address Information providers
 * @since 5.0
 */
public interface IMemoryAddressInfo extends IDsfService  {
	
	public interface IGdbMemoryAddressInfoTypeRetrieval {
		/**
		 * @return the String representing the type of information items being provided
		 */
		String getInfoType();
		/**
		 * The implementation provides the items of an associated type which could be pointing to a memory address
		 */
		void itemsRequest(IDMContext selectionContext, IMemoryBlock memoryBlock, DataRequestMonitor<IMemoryBlockAddressInfoItem[]> rm);
	}

	/**
	 * @return - All existing Memory Address Information providers
	 */
	IGdbMemoryAddressInfoTypeRetrieval[] getMemoryAddressInfoProviders();
}
