/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
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
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.debug.core.DebugException;

/**
 * Represents the disassembly of a debug target.
 */
public interface IDisassembly extends ICDebugElement {

	/**
	 * Returns the disassembly block for given stack frame.
	 *
	 * @param frame the stack frame for which the disassembly is required
	 * @return the disassembly block for given stack frame
	 * @throws DebugException if this method fails.
	 */
	IDisassemblyBlock getDisassemblyBlock(ICStackFrame frame) throws DebugException;

	/**
	 * Returns the disassembly block for given stack frame.
	 *
	 * @param address the address from which the disassembly starts
	 * @return the disassembly block for given memory address
	 * @throws DebugException if this method fails.
	 * @since 6.0
	 */
	IDisassemblyBlock getDisassemblyBlock(IAddress address) throws DebugException;

	/**
	 * Returns the address factory associated with this element.
	 *
	 * @return the address factory
	 */
	IAddressFactory getAddressFactory();
}
