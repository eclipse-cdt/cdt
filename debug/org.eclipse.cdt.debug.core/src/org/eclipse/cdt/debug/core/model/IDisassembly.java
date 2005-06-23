/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

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
	IDisassemblyBlock getDisassemblyBlock( ICStackFrame frame ) throws DebugException;

	/**
	 * Returns the address factory associated with this element.
	 * 
	 * @return the address factory
	 */
	IAddressFactory getAddressFactory();
}
