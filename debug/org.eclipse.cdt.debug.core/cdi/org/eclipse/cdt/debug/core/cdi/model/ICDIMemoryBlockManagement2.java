/*******************************************************************************
 * Copyright (c) 2005, 2007 Freescale, Inc.
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
 * Extension of ICDIMemoryBlockManagement 
 * 
 * @since May 26, 2006  
 */
public interface ICDIMemoryBlockManagement2 extends ICDIMemoryBlockManagement {

	/**
	 * Returns a memory block specified by given parameters. Differs
	 * from {@link ICDIMemoryBlockManagement#createMemoryBlock(String, int, int)}
	 * in that this support memory spaces.
	 * @param address 
	 * @param memorySpaceID - value is meaningful only to the backend
	 * @param size - number of bytes
	 * @return a memory block with the specified identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock createMemoryBlock(BigInteger address, String memorySpaceID, int size)
		throws CDIException;
}
