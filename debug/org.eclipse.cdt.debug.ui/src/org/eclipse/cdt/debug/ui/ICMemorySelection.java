/*******************************************************************************
 * Copyright (c) 2007 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui;

import java.math.BigInteger;

import org.eclipse.debug.core.model.IMemoryBlockExtension;

/**
 * Defines an interface that allows the implementor to specify the currently
 * selected memory block.
 */
public interface ICMemorySelection {

	/**
	 * @return the block of memory that contains the selection
	 */
	IMemoryBlockExtension getContainingBlock();
	
	/**
	 * @return the beginning address of the selection
	 */
	BigInteger getAddress();
	
	/**
	 * @return the length in units of the selection
	 */
	BigInteger getUnits();

}
