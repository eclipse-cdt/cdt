/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Represents the disassembly of a debug target.
 */
public interface IDisassembly extends ICDebugElement {
	
	/**
	 * Returns the list of disassembly instructions associated 
	 * with the given stack frame.
	 * 
	 * @param frame the stack frame for which the instructions re required.
	 * @return the list of disassembly instructions associated with 
	 * the given stack frame
	 * @throws DebugException if this method fails.
	 */
	public IAsmInstruction[] getInstructions( ICStackFrame frame ) throws DebugException;

	/**
	 * Returns the list of disassembly instructions that begins at the given address.
	 * The size of the requested list is specified by <code>length</code>. 
	 * 
	 * @param address the start address
	 * @param length the size of the requested list
	 * @return the specified list of disassembly instructions
	 * @throws DebugException if this method fails.
	 */
	public IAsmInstruction[] getInstructions( long address, int length ) throws DebugException;
}
