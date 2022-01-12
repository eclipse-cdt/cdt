/*******************************************************************************
 * Copyright (c) 2007 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Freescale Semiconductor - Initial API, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import java.math.BigInteger;

import org.eclipse.core.runtime.CoreException;

public interface ICWatchpoint2 extends ICWatchpoint {

	/**
	 * Watchpoint attribute storing the memory space associated with this
	 * watchpoint (value <code>"org.eclipse.cdt.debug.core.memoryspace"</code>).
	 * This attribute is a <code>String</code>.
	 */
	public static final String MEMORYSPACE = "org.eclipse.cdt.debug.core.memoryspace"; //$NON-NLS-1$

	/**
	 * Watchpoint attribute storing the range associated with this
	 * watchpoint (value <code>"org.eclipse.cdt.debug.core.range"</code>).
	 * This attribute is an <code>int</code>.
	 */
	public static final String RANGE = "org.eclipse.cdt.debug.core.range"; //$NON-NLS-1$

	/**
	 * Returns the watchpoint's memory space.
	 *
	 * @return the memory space of this watchpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getMemorySpace() throws CoreException;

	/**
	 * Returns the watchpoint's range.
	 *
	 * @return the range of this watchpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	BigInteger getRange() throws CoreException;
}
