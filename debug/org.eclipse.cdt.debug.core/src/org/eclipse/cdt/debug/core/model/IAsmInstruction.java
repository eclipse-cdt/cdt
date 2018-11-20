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

/**
 * An instruction of disassemby.
 */
public interface IAsmInstruction {

	/**
	 * Returns the address of this instruction.
	 *
	 * @return the address of this instruction
	 */
	IAddress getAdress();

	/**
	 * Returns the function name of this instruction,
	 * or empty string if function is not available.
	 *
	 * @return the function name of this instruction
	 */
	String getFunctionName();

	/**
	 * Returns the instruction's text.
	 *
	 * @return the instruction's text.
	 */
	String getInstructionText();

	/**
	 * Returns the opcode of this instruction.
	 *
	 * @return the opcode of this instruction
	 */
	String getOpcode();

	/**
	 * Returns the arguments to the opcode.
	 *
	 * @return the arguments to the opcode
	 */
	String getArguments();

	/**
	 * Returns the offset of this machine instruction.
	 *
	 * @return the offset of this machine instruction
	 */
	long getOffset();
}
