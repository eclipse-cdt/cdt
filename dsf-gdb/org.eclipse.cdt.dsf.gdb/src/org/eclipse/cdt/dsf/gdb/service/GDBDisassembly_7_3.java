/*******************************************************************************
 * Copyright (c) 2008, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     William Riley (Renesas) - Bug 357270
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.math.BigInteger;
import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IDisassembly2;
import org.eclipse.cdt.dsf.debug.service.IDisassembly3;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.MIDisassembly;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataDisassemble;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.4
 */
public class GDBDisassembly_7_3 extends MIDisassembly implements IDisassembly3 {

	public GDBDisassembly_7_3(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
		register(new String[] { IDisassembly.class.getName(), IDisassembly2.class.getName(),
				IDisassembly3.class.getName(), MIDisassembly.class.getName(), GDBDisassembly_7_3.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	// /////////////////////////////////////////////////////////////////////////
	// IDisassembly3
	// /////////////////////////////////////////////////////////////////////////
	@Override
	public void getInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			DataRequestMonitor<IInstruction[]> drm) {
		// Ask for opCodes by default
		getInstructions(context, startAddress, endAddress, true, drm);
	}

	@Override
	public void getInstructions(IDisassemblyDMContext context, String filename, int linenum, int lines,
			DataRequestMonitor<IInstruction[]> drm) {
		// Ask for opCodes by default
		getInstructions(context, filename, linenum, lines, true, drm);
	}

	@Override
	public void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			DataRequestMonitor<IMixedInstruction[]> drm) {
		// Ask for opCodes by default
		getMixedInstructions(context, startAddress, endAddress, true, drm);
	}

	@Override
	public void getMixedInstructions(IDisassemblyDMContext context, String filename, int linenum, int lines,
			DataRequestMonitor<IMixedInstruction[]> drm) {
		// Ask for opCodes by default
		getMixedInstructions(context, filename, linenum, lines, true, drm);
	}

	@Override
	public void getInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			boolean opCodes, DataRequestMonitor<IInstruction[]> drm) {
		getInstructions(context, startAddress, endAddress,
				opCodes ? MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY_OPCODES
						: MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY,
				drm);
	}

	@Override
	public void getInstructions(IDisassemblyDMContext context, String filename, int linenum, int lines, boolean opCodes,
			DataRequestMonitor<IInstruction[]> drm) {
		getInstructions(context, filename, linenum, lines,
				opCodes ? MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY_OPCODES
						: MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY,
				drm);
	}

	@Override
	public void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			boolean opCodes, DataRequestMonitor<IMixedInstruction[]> drm) {
		getMixedInstructions(context, startAddress, endAddress,
				opCodes ? MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED_OPCODES
						: MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED,
				drm);
	}

	@Override
	public void getMixedInstructions(IDisassemblyDMContext context, String filename, int linenum, int lines,
			boolean opCodes, DataRequestMonitor<IMixedInstruction[]> drm) {
		getMixedInstructions(context, filename, linenum, lines,
				opCodes ? MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED_OPCODES
						: MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED,
				drm);
	}

	@Override
	public void alignOpCodeAddress(IDisassemblyDMContext context, BigInteger address,
			DataRequestMonitor<BigInteger> drm) {
		drm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
	}
}
