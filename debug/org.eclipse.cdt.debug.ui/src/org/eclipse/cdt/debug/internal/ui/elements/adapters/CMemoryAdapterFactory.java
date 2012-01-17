/*******************************************************************************
 * Copyright (c) 2005, 2010 Freescale, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval;
import org.eclipse.cdt.debug.internal.ui.views.memory.AddMemoryBlocks;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IAddMemoryBlocksTarget;

/**
 * Provides the IAdaptable mapping for things related to the memory-space
 * support in the Memory and Memory Browser views.
 */
public class CMemoryAdapterFactory implements IAdapterFactory {

	private static IAddMemoryBlocksTarget fgAddMemoryBlocks = new AddMemoryBlocks();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.isInstance(adaptableObject)) {
			return adaptableObject;
		}

		// If the backend supports memory spaces we use a custom Add Monitor
		// dialog, though our IAddMemoryBlocksTarget may decide to invoke the
		// standard, platform dialog if the customization isn't actually
		// necessary.
		if (adapterType.equals(IAddMemoryBlocksTarget.class)) {
			if (adaptableObject instanceof IMemorySpaceAwareMemoryBlockRetrieval) {
				return fgAddMemoryBlocks;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IAddMemoryBlocksTarget.class };
	}
}
