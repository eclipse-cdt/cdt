/*****************************************************************
 * Copyright (c) 2014 Renesas Electronics and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William Riley (Renesas) - Bug 357270
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 * This interface extends the disassembly service with support for raw opcodes
 *
 * @since 2.5
 *
 */
public interface IDisassembly3 extends IDisassembly2 {

	/**
	 * Gets a block of disassembled code given an address range.
	 *
	 * @param context
	 *            Context of the disassembly code
	 * @param startAddress
	 *            Beginning address, inclusive. If null, disassemble from the
	 *            instruction pointer.
	 * @param endAddress
	 *            End address, exclusive. If null, implementation should attempt
	 *            to disassemble some reasonable, default number of
	 *            instructions. That default is implementation specific.
	 * @param opCodes
	 *            If raw opcodes should be retrieved
	 * @param drm
	 *            Disassembled code
	 */
	public void getInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			boolean opCodes, DataRequestMonitor<IInstruction[]> drm);

	/**
	 * Gets a block of disassembled code given a filename, line number, and line
	 * count.
	 *
	 * @param context
	 *            Context of the disassembly code
	 * @param filename
	 *            File to disassemble
	 * @param linenum
	 *            Starting point. 1-based line number within the file. If the
	 *            line does not represent compiled code, disassembly will start
	 *            at the first subsequent line that does.
	 * @param instructionCount
	 *            Number of instructions to disassemble. -1 means all available
	 *            instructions (starting at [linenum])
	 * @param opCodes
	 *            If raw opcodes should be retrieved
	 *
	 * @param drm
	 *            Disassembled code
	 */
	public void getInstructions(IDisassemblyDMContext context, String filename, int linenum, int instructionCount,
			boolean opCodes, DataRequestMonitor<IInstruction[]> drm);

	/**
	 * Gets a block of mixed disassembled code given an address range.
	 *
	 * @param context
	 *            Context of the disassembly code
	 * @param startAddress
	 *            Beginning address, inclusive. If null, disassemble from the
	 *            instruction pointer.
	 * @param endAddress
	 *            End address, exclusive.
	 * @param opCodes
	 *            If opcodes should be retrieved
	 * @param drm
	 *            Disassembled code
	 */
	public void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			boolean opCodes, DataRequestMonitor<IMixedInstruction[]> drm);

	/**
	 * Gets a block of mixed disassembled code given a filename, line number,
	 * and line count.
	 *
	 * @param context
	 *            Context of the disassembly code
	 * @param filename
	 *            File to disassemble
	 * @param linenum
	 *            Starting point. 1-based line number within the file. If the
	 *            line does not represent compiled code, disassembly will start
	 *            at the first subsequent line that does.
	 * @param instructionCount
	 *            Number of instructions to disassemble. -1 means all available
	 *            instructions (starting at [linenum])
	 * @param opCodes
	 *            If opcodes should be retrieved
	 * @param drm
	 *            Disassembled code
	 */
	public void getMixedInstructions(IDisassemblyDMContext context, String filename, int linenum, int instructionCount,
			boolean opCodes, DataRequestMonitor<IMixedInstruction[]> drm);

}
