/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.provisional.model;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;

/**
 * A common MemoryBlockRetrievalFactory across debug sessions
 * which resolves (adapts) an element context to its corresponding 
 * IMemoryBlockRetrieval via the session's IMemoryBlockRetrievalManager
 * 
 * (non-Javadoc)
 * @see org.eclipse.cdt.dsf.debug.internal.provisional.model.IMemoryBlockRetrievalManager
 * 
 */
public class MemoryBlockRetrievalFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		IMemoryBlockRetrieval memRetrieval = null;
		
		if (adaptableObject instanceof IDMContext) {
			if (adapterType.equals(IMemoryBlockRetrieval.class)) {
				IAdaptable adaptable = (IAdaptable) adaptableObject;
				//Retrieve the session's Memory Block Retrieval Manager
				IMemoryBlockRetrievalManager retrievalManager = (adaptable
						.getAdapter(IMemoryBlockRetrievalManager.class));
				if (retrievalManager != null) {
					//resolve the specific Memory Block Retrieval associated to the memory context of adaptableObject
					memRetrieval = retrievalManager.getMemoryBlockRetrieval((IDMContext) adaptableObject);					
				} 
			}
		}
		
		return (T)memRetrieval;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IMemoryBlockRetrieval.class };
	}
}
