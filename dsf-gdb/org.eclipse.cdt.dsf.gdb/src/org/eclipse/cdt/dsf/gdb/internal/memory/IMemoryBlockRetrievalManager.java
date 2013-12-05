/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 */
package org.eclipse.cdt.dsf.gdb.internal.memory;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;

public interface IMemoryBlockRetrievalManager {
	/**
	 * A method to resolve the specific IMemoryBlockRetrieval associated to the IMemoryDMContext of the given IDMContext
	 * 
	 * @param dmc - A context which either itself or one of its parents is an IMemoryDMContext
	 * @return - The IMemoryBlockRetrieval associated to the IMemoryDMContext resolved from the given dmc
	 */
	public IMemoryBlockRetrieval getMemoryBlockRetrieval(IDMContext dmc);
}
