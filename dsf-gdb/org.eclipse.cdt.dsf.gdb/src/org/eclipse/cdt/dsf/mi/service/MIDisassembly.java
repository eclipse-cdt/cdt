/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service;

import java.math.BigInteger;
import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataDisassemble;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataDisassembleInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class MIDisassembly extends AbstractDsfService implements IDisassembly {

	// Services
	ICommandControl fConnection;
	private CommandFactory fCommandFactory;

	///////////////////////////////////////////////////////////////////////////
	// AbstractDsfService
	///////////////////////////////////////////////////////////////////////////

	/**
	 * The service constructor
	 *
	 * @param session The debugging session
	 */
	public MIDisassembly(DsfSession session) {
		super(session);
	}

	/**
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
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
		fConnection = getServicesTracker().getService(ICommandControl.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		register(new String[] { IDisassembly.class.getName(), MIDisassembly.class.getName() },
				new Hashtable<String, String>());
		rm.done();
	}

	/**
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#shutdown(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void shutdown(RequestMonitor rm) {
		unregister();
		super.shutdown(rm);
	}

	/**
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#getBundleContext()
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	///////////////////////////////////////////////////////////////////////////
	// IDisassembly
	///////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.math.BigInteger, java.math.BigInteger, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void getInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			DataRequestMonitor<IInstruction[]> drm) {
		getInstructions(context, startAddress, endAddress, MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY, drm);
	}

	/**
	 * Helper method to allow getting disassembly instructions not in mixed mode.
	 * @since 4.4
	 */
	protected void getInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			int mode, final DataRequestMonitor<IInstruction[]> drm) {
		// Checking what we don't support instead of what we do support allows
		// others to extend the 'mode' field with new values.
		assert mode != MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED
				|| mode != MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED_OPCODES;

		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$
			drm.done();
			return;
		}

		String start = (startAddress != null) ? startAddress.toString() : "$pc"; //$NON-NLS-1$
		String end = (endAddress != null) ? endAddress.toString() : start + " + 100"; //$NON-NLS-1$
		fConnection.queueCommand(fCommandFactory.createMIDataDisassemble(context, start, end, mode),
				new DataRequestMonitor<MIDataDisassembleInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						IInstruction[] result = getData().getMIAssemblyCode();
						drm.setData(result);
						drm.done();
					}
				});
	}

	/**
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.lang.String, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void getInstructions(IDisassemblyDMContext context, String filename, int linenum, int lines,
			DataRequestMonitor<IInstruction[]> drm) {
		getInstructions(context, filename, linenum, lines, MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY, drm);
	}

	/**
	 * Helper method to allow getting disassembly instructions not in mixed mode.
	 * @since 4.4
	 */
	protected void getInstructions(IDisassemblyDMContext context, String filename, int linenum, int lines, int mode,
			final DataRequestMonitor<IInstruction[]> drm) {
		// Checking what we don't support instead of what we do support allows
		// others to extend the 'mode' field with new values.
		assert mode != MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED
				|| mode != MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED_OPCODES;

		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$
			drm.done();
			return;
		}

		fConnection.queueCommand(fCommandFactory.createMIDataDisassemble(context, filename, linenum, lines, mode),
				new DataRequestMonitor<MIDataDisassembleInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						IInstruction[] result = getData().getMIAssemblyCode();
						drm.setData(result);
						drm.done();
					}
				});
	}

	/**
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getMixedInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.math.BigInteger, java.math.BigInteger, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			final DataRequestMonitor<IMixedInstruction[]> drm) {
		getMixedInstructions(context, startAddress, endAddress, MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED, drm);
	}

	/**
	 * Helper method to allow getting disassembly instructions in mixed mode.
	 * @since 4.4
	 */
	protected void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			int mode, final DataRequestMonitor<IMixedInstruction[]> drm) {
		// Checking what we don't support instead of what we do support allows
		// others to extend the 'mode' field with new values.
		assert mode != MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY
				|| mode != MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY_OPCODES;

		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$
			drm.done();
			return;
		}

		String start = (startAddress != null) ? startAddress.toString() : "$pc"; //$NON-NLS-1$
		String end = (endAddress != null) ? endAddress.toString() : start + " + 100"; //$NON-NLS-1$
		fConnection.queueCommand(fCommandFactory.createMIDataDisassemble(context, start, end, mode),
				new DataRequestMonitor<MIDataDisassembleInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						IMixedInstruction[] result = getData().getMIMixedCode();
						drm.setData(result);
						drm.done();
					}
				});
	}

	/**
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getMixedInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.lang.String, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void getMixedInstructions(IDisassemblyDMContext context, String filename, int linenum, int lines,
			final DataRequestMonitor<IMixedInstruction[]> drm) {
		getMixedInstructions(context, filename, linenum, lines, MIDataDisassemble.DATA_DISASSEMBLE_MODE_MIXED, drm);
	}

	/**
	 * Helper method to allow getting disassembly instructions in mixed mode.
	 * @since 4.4
	 */
	protected void getMixedInstructions(IDisassemblyDMContext context, String filename, int linenum, int lines,
			int mode, final DataRequestMonitor<IMixedInstruction[]> drm) {
		// Checking what we don't support instead of what we do support allows
		// others to extend the 'mode' field with new values.
		assert mode != MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY
				|| mode != MIDataDisassemble.DATA_DISASSEMBLE_MODE_DISASSEMBLY_OPCODES;

		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$
			drm.done();
			return;
		}

		fConnection.queueCommand(fCommandFactory.createMIDataDisassemble(context, filename, linenum, lines, mode),
				new DataRequestMonitor<MIDataDisassembleInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						IMixedInstruction[] result = getData().getMIMixedCode();
						drm.setData(result);
						drm.done();
					}
				});
	}
}
