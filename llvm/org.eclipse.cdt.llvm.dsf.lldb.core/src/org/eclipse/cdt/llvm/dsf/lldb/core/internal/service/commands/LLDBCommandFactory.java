/*******************************************************************************
 * Copyright (c) 2018 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.service.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakInsert;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.LLDBTrait;

/**
 * A command factory specific to LLDB for cases where some commands need any
 * kind of tweaks to account for discrepancies with GDB.
 */
public class LLDBCommandFactory extends CommandFactory {

	private DsfSession fSession;

	/**
	 * Construct a command factory specific to LLDB.
	 *
	 * @param session the debugging session
	 */
	public LLDBCommandFactory(DsfSession session) {
		fSession = session;
	}

	/**
	 * lldb-mi (as of 8.0.0-r341271) doesn't implement "-gdb-set args" but it does
	 * implement "-exec-arguments". So we just substitute here.
	 * See also https://bugs.llvm.org/show_bug.cgi?id=38834
	 */
	@Override
	public ICommand<MIInfo> createMIGDBSetArgs(IMIContainerDMContext dmc) {
		return super.createMIExecArguments(dmc, new String[0]);
	}

	/**
	 * @see #createMIGDBSetArgs(IMIContainerDMContext)
	 */
	@Override
	public ICommand<MIInfo> createMIGDBSetArgs(IMIContainerDMContext dmc, String[] arguments) {
		return super.createMIExecArguments(dmc, arguments);
	}

	/**
	 * lldb-mi implements "-gdb-set breakpoint pending" starting with 8.0.0-r345563.
	 * For earlier version we use "-break-insert -f" to always use pending
	 * breakpoints. See also https://reviews.llvm.org/D52953
	 */
	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, String line, String tid) {
		if (LLDBTrait.MISSING_GDB_SET_BREAKPOINT_PENDING.isTraitOf(fSession)) {
			return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, line, tid, true);
		}
		return super.createMIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, line, tid);
	}

	/**
	 * @see #createMIBreakInsert(IBreakpointsTargetDMContext, boolean, boolean, String, int, String, String)
	 */
	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, String location, String tid, boolean disabled,
			boolean isTracepoint) {
		if (LLDBTrait.MISSING_GDB_SET_BREAKPOINT_PENDING.isTraitOf(fSession)) {
			return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, location, tid, disabled,
					isTracepoint, true);
		}
		return super.createMIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, location, tid, disabled,
				isTracepoint);
	}

	/**
	 * @see #createMIBreakInsert(IBreakpointsTargetDMContext, boolean, boolean, String, int, String, String)
	 */
	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, String func) {
		if (LLDBTrait.MISSING_GDB_SET_BREAKPOINT_PENDING.isTraitOf(fSession)) {
			return new MIBreakInsert(ctx, func, true);
		}
		return super.createMIBreakInsert(ctx, func);
	}
}
