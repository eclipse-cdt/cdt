/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation 
 *     							 
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.internal.core.CMemoryBlockRetrievalExtension;
import org.eclipse.cdt.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.cdt.debug.internal.ui.views.memory.AddMemoryBlocks;
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryBlockLabelDecorator;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IAddMemoryBlocksTarget;
import org.eclipse.jface.viewers.ILabelDecorator;

/**
 * Provides the IAdaptable mapping for things related to the memory-space
 * extension of the platform's Memory view
 */
public class CMemoryAdapterFactory implements IAdapterFactory {

	private static IAddMemoryBlocksTarget fgAddMemoryBlocks = new AddMemoryBlocks();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
	 *      java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.isInstance(adaptableObject)) {
			return adaptableObject;
		}

		if (adapterType.equals(IAddMemoryBlocksTarget.class)) {
			if (adaptableObject instanceof CMemoryBlockRetrievalExtension) {
				if (((CMemoryBlockRetrievalExtension)adaptableObject).hasMemorySpaces())
					return fgAddMemoryBlocks;
			}
		}

		if (adapterType.equals(ILabelDecorator.class)) {
			if (adaptableObject instanceof CMemoryBlockExtension) {
				// If a memory space isn't involved, the standard label is fine 
				CMemoryBlockExtension memBlock = (CMemoryBlockExtension)adaptableObject;
				if (memBlock.getMemorySpaceID() != null)
					return new MemoryBlockLabelDecorator(memBlock);
			}
		}
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IAddMemoryBlocksTarget.class, ILabelDecorator.class };
	}
}
