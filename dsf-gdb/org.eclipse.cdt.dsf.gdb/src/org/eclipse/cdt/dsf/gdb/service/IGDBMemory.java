/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IMemory;

/**
 * Provides access to memory related data such as address size and endianness.
 * 
 * @since 4.2
 */
public interface IGDBMemory extends IMemory {

	/**
	 * This method is required to retrieve data from GDB asynchronously. The Platform's 
	 * memory related API is not asynchronous, so the data can not be acquired on demand.
	 */
	public void initializeMemoryData(IMemoryDMContext ctx, RequestMonitor rm);
	
	/**
	 * Returns the address size (in bytes) of the memory specified by the given context.
	 */
	public int getAddressSize(IMemoryDMContext context);

	/**
	 * Returns whether the memory specified by the given context is big endian.
	 */
	public boolean isBigEndian(IMemoryDMContext context);
}
