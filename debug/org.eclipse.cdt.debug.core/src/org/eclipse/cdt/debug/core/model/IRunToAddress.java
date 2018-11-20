/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to run a debug target to the given address.
 */
public interface IRunToAddress {

	/**
	 * Returns whether this operation is currently available for this element.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canRunToAddress(IAddress address);

	/**
	 * Causes this element to run to specified address.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToAddress(IAddress address, boolean skipBreakpoints) throws DebugException;
}
