/*****************************************************************
 * Copyright (c) 2014 Renesas Electronics and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William Riley (Renesas) - Bug 357270
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.math.BigInteger;
import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IDisassembly3;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIDisassembly;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataDisassembleInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.4
 */
public class GDBDisassembly_7_3 extends MIDisassembly implements IDisassembly3 {
	private ICommandControl fConnection;
	private CommandFactory fCommandFactory;

	public GDBDisassembly_7_3(DsfSession session) {
		super(session);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse
	 * .cdt.dsf.concurrent.RequestMonitor)
	 */
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
		// Get the services references
		fConnection = getServicesTracker().getService(ICommandControl.class);
		fCommandFactory = getServicesTracker().getService(
				IMICommandControl.class).getCommandFactory();

		// Register this service
		register(new String[] { IDisassembly.class.getName(),
				IDisassembly3.class.getName(), MIDisassembly.class.getName(),
				GDBDisassembly_7_3.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	// /////////////////////////////////////////////////////////////////////////
	// IDisassembly3
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public void getInstructions(IDisassemblyDMContext context,
			BigInteger startAddress, BigInteger endAddress, boolean opcodes,
			final DataRequestMonitor<IInstruction[]> drm) {
		// Validate the context
		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
			drm.done();
			return;
		}

		// Go for it
		String start = (startAddress != null) ? startAddress.toString() : "$pc"; //$NON-NLS-1$
		String end = (endAddress != null) ? endAddress.toString() : "$pc + 100"; //$NON-NLS-1$
		// Mode 0: disassembly or mode 2: disassembly with raw opcodes
		fConnection.queueCommand(fCommandFactory.createMIDataDisassemble(
				context, start, end, opcodes ? 2 : 0),
				new DataRequestMonitor<MIDataDisassembleInfo>(getExecutor(),
						drm) {
					@Override
					protected void handleSuccess() {
						IInstruction[] result = getData().getMIAssemblyCode();
						drm.setData(result);
						drm.done();
					}
				});
	}

	@Override
	public void getInstructions(IDisassemblyDMContext context, String filename,
			int linenum, int lines, boolean opcodes,
			final DataRequestMonitor<IInstruction[]> drm) {
		// Validate the context
		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
			drm.done();
			return;
		}

		// Go for it
		// Mode 0: disassembly or mode 2: disassembly with raw opcodes
		fConnection.queueCommand(fCommandFactory.createMIDataDisassemble(
				context, filename, linenum, lines, opcodes ? 2 : 0),
				new DataRequestMonitor<MIDataDisassembleInfo>(getExecutor(),
						drm) {
					@Override
					protected void handleSuccess() {
						IInstruction[] result = getData().getMIAssemblyCode();
						drm.setData(result);
						drm.done();
					}
				});
	}

	@Override
	public void getMixedInstructions(IDisassemblyDMContext context,
			BigInteger startAddress, BigInteger endAddress, boolean opcodes,
			final DataRequestMonitor<IMixedInstruction[]> drm) {
		// Validate the context
		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
			drm.done();
			return;
		}

		// Go for it
		String start = (startAddress != null) ? startAddress.toString() : "$pc"; //$NON-NLS-1$
		String end = (endAddress != null) ? endAddress.toString() : "$pc + 100"; //$NON-NLS-1$
		// Mode 1: mixed source and disassembly or mode 3: mixed source and
		// disassembly with raw opcodes
		fConnection.queueCommand(fCommandFactory.createMIDataDisassemble(
				context, start, end, opcodes ? 3 : 1),
				new DataRequestMonitor<MIDataDisassembleInfo>(getExecutor(),
						drm) {
					@Override
					protected void handleSuccess() {
						IMixedInstruction[] result = getData().getMIMixedCode();
						drm.setData(result);
						drm.done();
					}
				});
	}

	@Override
	public void getMixedInstructions(IDisassemblyDMContext context,
			String filename, int linenum, int lines, boolean opcodes,
			final DataRequestMonitor<IMixedInstruction[]> drm) {
		// Validate the context
		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
			drm.done();
			return;
		}

		// Go for it
		// Mode 1: mixed source and disassembly or mode 3: mixed source and
		// disassembly with raw opcodes
		fConnection.queueCommand(fCommandFactory.createMIDataDisassemble(
				context, filename, linenum, lines, opcodes ? 3 : 1),
				new DataRequestMonitor<MIDataDisassembleInfo>(getExecutor(),
						drm) {
					@Override
					protected void handleSuccess() {
						IMixedInstruction[] result = getData().getMIMixedCode();
						drm.setData(result);
						drm.done();
					}
				});
	}

}
