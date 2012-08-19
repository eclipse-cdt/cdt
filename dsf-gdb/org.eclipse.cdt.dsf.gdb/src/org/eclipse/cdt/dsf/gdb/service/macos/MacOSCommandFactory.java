/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc-Andre Laperle - Fix for bug 330060
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.macos;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.IProcessMIInterpreterExecConsole;
import org.eclipse.cdt.dsf.mi.service.command.commands.macos.MacOSMIEnvironmentCD;
import org.eclipse.cdt.dsf.mi.service.command.commands.macos.MacOSProcessMIInterpreterExecConsole;
import org.eclipse.cdt.dsf.mi.service.command.commands.macos.MacOSMIVarUpdate;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarUpdateInfo;

/** @since 3.0 */
public class MacOSCommandFactory extends CommandFactory {

	@Override
	public ICommand<MIInfo> createMIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		return new MacOSMIEnvironmentCD(ctx, path);
	}
	
	@Override
	public ICommand<MIVarUpdateInfo> createMIVarUpdate(ICommandControlDMContext dmc, String name) {
		return new MacOSMIVarUpdate(dmc, name);
	}
	
	@Override
	public IProcessMIInterpreterExecConsole createProcessMIInterpreterExecConsole(IDMContext ctx, String cmd) {
		return new MacOSProcessMIInterpreterExecConsole<MIInfo>(ctx, cmd);
	}
}
