/*******************************************************************************
 * Copyright (c) 2007 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;
 
import java.math.BigInteger;
import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * Represents a watchpoint.
 * 
 */
public interface ICDIWatchpoint2 extends ICDIWatchpoint {
	
	/**
	 * Returns the memory space associated with this 
	 * watchpoint's start address, or null if there is no memory space.
	 * 
	 * @return the memory space Id associated with the address of 
	 * 			this watchpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getMemorySpace() throws CDIException;

	/**
	 * Returns the range for this watchpoint in addressable units.
	 * 
	 * @return the range of the watchpoint.
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	BigInteger getRange() throws CDIException;
}
