/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;


/**
 * Extension interface to provide access to the addressable size of a memory context
 * 
 * @since 4.4
 */
public interface IGDBMemory2 extends IGDBMemory {
	
	/**
	 * Returns the addressable size (in octets) of the memory specified by the given context
	 */
	public int getAddressableSize(IMemoryDMContext context);
	
}
