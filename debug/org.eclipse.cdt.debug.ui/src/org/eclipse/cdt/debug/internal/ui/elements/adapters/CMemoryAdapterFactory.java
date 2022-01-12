/*******************************************************************************
 * Copyright (c) 2005, 2016 Freescale, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.isInstance(adaptableObject)) {
			return (T) adaptableObject;
		}

		// If the backend supports memory spaces we use a custom Add Monitor
		// dialog, though our IAddMemoryBlocksTarget may decide to invoke the
		// standard, platform dialog if the customization isn't actually
		// necessary.
		if (adapterType.equals(IAddMemoryBlocksTarget.class)) {
			if (adaptableObject instanceof IMemorySpaceAwareMemoryBlockRetrieval) {
				return (T) fgAddMemoryBlocks;
			}
		}

		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IAddMemoryBlocksTarget.class };
	}
}
