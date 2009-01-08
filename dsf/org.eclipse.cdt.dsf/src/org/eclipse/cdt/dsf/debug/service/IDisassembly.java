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
 */
public interface IDisassembly extends IDsfService {

    public interface IDisassemblyDMContext extends IDMContext {}

    /**
     * Gets the disassembled code from an address range.
     * If [startAddress] == null, disassemble from the instruction pointer.
     * 
     * @param context       Context of the disassembly code
     * @param startAddress  Beginning address
     * @param endAddress    End address
     * @param drm           Disassembled code
     */
    public void getInstructions(
            IDisassemblyDMContext context,
            BigInteger startAddress, 
            BigInteger endAddress,
            DataRequestMonitor<IInstruction[]> drm);

    /**
     * Gets the disassembled code from a file location.
     * If [lines] == -1, the whole function is disassembled.
     * 
     * @param context       Context of the disassembly code
     * @param filename      File to disassemble
     * @param linenum       Line number within the file
     * @param lines         Number of lines of disassembled code to produce
     * @param drm           Disassembled code
     */
    public void getInstructions(
            IDisassemblyDMContext context,
            String filename, 
            int    linenum, 
            int    lines,
            DataRequestMonitor<IInstruction[]> drm);

    /**
     * Gets the mixed disassembled code from an address range.
     * If [startAddress] == null, disassemble from the instruction pointer.
     * 
     * @param context       Context of the disassembly code
     * @param startAddress  Beginning address
     * @param endAddress    End address
     * @param drm           Disassembled code
     */
    public void getMixedInstructions(
            IDisassemblyDMContext context,
            BigInteger startAddress, 
            BigInteger endAddress,
            DataRequestMonitor<IMixedInstruction[]> drm);

    /**
     * Gets the mixed disassembled code from a file location.
     * If [lines] == -1, the whole function is disassembled.
     * 
     * @param context       Context of the disassembly code
     * @param filename      File to disassemble
     * @param linenum       Line number within the file
     * @param lines         Number of lines of disassembled code to produce
     * @param drm           Disassembled code
     */
    public void getMixedInstructions(
            IDisassemblyDMContext context,
            String filename, 
            int    linenum, 
            int    lines,
            DataRequestMonitor<IMixedInstruction[]> drm);

}
