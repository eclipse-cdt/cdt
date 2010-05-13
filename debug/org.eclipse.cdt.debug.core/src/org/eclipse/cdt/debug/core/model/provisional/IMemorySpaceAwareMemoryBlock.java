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

import org.eclipse.debug.core.model.IMemoryBlockExtension;

/**
 * An extension of IMemoryBlockExtension that supports memory spaces. An
 * instance of this is returned from IMemorySpaceAwareMemoryBlockRetrieval. The
 * memory space interface is provisional, thus this class cannot yet be API.
 * 
 * @author Alain Lee and John Cortell
 */
public interface IMemorySpaceAwareMemoryBlock extends IMemoryBlockExtension {

	/**
	 * Returns the memory space associated with this block
	 * 
	 * @return a memory space ID, or null if the block was created without a
	 *         memory space identifier. These IDs originate from the backend. See
	 *         {@link IMemorySpaceAwareMemoryBlockRetrieval#getMemorySpaces(Object, IRequestListener)}
	 */
	public String getMemorySpaceID();
}
