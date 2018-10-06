/*******************************************************************************
 * Copyright (c) 2018 Marc-Andre Laperle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.service.commands;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakInsert;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * A command factory specific to LLDB for cases where some commands need any
 * kind of tweaks to account for discrepancies with GDB.
 */
public class LLDBCommandFactory extends CommandFactory {

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
	 * lldb-mi (as of 8.0.0-r343825) doesn't implement "-gdb-set breakpoint pending"
	 * so instead we always use "-break-insert -f" to always use pending breakpoints.
	 * Once the -gdb-set is implemented in lldb-mi, we can remove this.
	 * See also https://reviews.llvm.org/D52953
	 */
	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, String line, String tid) {
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, line, tid, true);
	}

	/**
	 * @see #createMIBreakInsert(IBreakpointsTargetDMContext, boolean, boolean, String, int, String, String)
	 */
	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, String location, String tid, boolean disabled,
			boolean isTracepoint) {
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, location, tid, disabled, isTracepoint, true);
	}

	/**
	 * @see #createMIBreakInsert(IBreakpointsTargetDMContext, boolean, boolean, String, int, String, String)
	 */
	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, String func) {
		return new MIBreakInsert(ctx, func, true);
	}
}
