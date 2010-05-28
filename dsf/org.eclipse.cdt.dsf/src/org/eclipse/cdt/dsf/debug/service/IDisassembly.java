/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Disassembly service interface
 * 
 * @since 1.0
 */
public interface IDisassembly extends IDsfService {

    public interface IDisassemblyDMContext extends IDMContext {}

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
	 * @param drm
	 *            Disassembled code
	 */
    public void getInstructions(
            IDisassemblyDMContext context,
            BigInteger startAddress, 
            BigInteger endAddress,
            DataRequestMonitor<IInstruction[]> drm);

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
	 *            Number of instructions to disassemble. -1 means all
	 *            available instructions (starting at [linenum])
	 * 
	 * @param drm
	 *            Disassembled code
	 */
    public void getInstructions(
            IDisassemblyDMContext context,
            String filename, 
            int    linenum, 
            int    instrtuctionCount,
            DataRequestMonitor<IInstruction[]> drm);

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
	 * @param drm
	 *            Disassembled code
	 */
    public void getMixedInstructions(
            IDisassemblyDMContext context,
            BigInteger startAddress, 
            BigInteger endAddress,
            DataRequestMonitor<IMixedInstruction[]> drm);

	/**
	 * Gets a block of mixed disassembled code given a filename, line number, and line count.
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
	 *            Number of instructions to disassemble. -1 means all
	 *            available instructions (starting at [linenum])
	 * @param drm
	 *            Disassembled code
	 */
    public void getMixedInstructions(
            IDisassemblyDMContext context,
            String filename, 
            int    linenum, 
            int    instructionCount,
            DataRequestMonitor<IMixedInstruction[]> drm);

}
