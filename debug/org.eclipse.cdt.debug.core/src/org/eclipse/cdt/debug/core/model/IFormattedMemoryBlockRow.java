/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.core.IAddress;

/**
 * 
 * Represents a row in the output table of formatted memory block.
 * 
 * @since Jul 31, 2002
 */
public interface IFormattedMemoryBlockRow
{
	/**
	 * Returns the address of this row.
	 * 
	 * @return the address of this row
	 */
	IAddress getAddress();

	/**
	 * Returns the array of memory words.
	 * 
	 * @return the array of memory words
	 */
	String[] getData();
	
	/**
	 * Returns the ASCII dump for this row.
	 * 
	 * @return the ASCII dump for this row
	 */
	String getASCII();
}
