/*******************************************************************************
 * Copyright (c) 2018 Marc-Andre Laperle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.service.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
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
}
