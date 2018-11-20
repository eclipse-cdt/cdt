/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
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
package org.eclipse.cdt.dsf.gdb.service.command;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakInsert;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;

/**
 * A command factory for commands that should only be used starting with GDB 6.8
 * @since 4.0 */
public class CommandFactory_6_8 extends CommandFactory {

	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, String func) {
		return new MIBreakInsert(ctx, func, true);
	}

	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, String line, String tid) {
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, line, tid, true);
	}

	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, String location, String tid, boolean disabled,
			boolean isTracepoint) {
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, location, tid, disabled,
				isTracepoint, true);
	}
}
