/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInterpreterExecConsole;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBVersionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * We use -interpreter-exec console "show version" instead
 * of -gdb-version to avoid having the output automatically printed
 * to our console.
 *
 */
public class CLIGDBVersion extends MIInterpreterExecConsole<MIGDBVersionInfo> {
	private static final String COMMAND = "show version"; //$NON-NLS-1$

	public CLIGDBVersion(ICommandControlDMContext ctx) {
		super(ctx, COMMAND);
	}

	@Override
	public MIInfo getResult(MIOutput out) {
		return new MIGDBVersionInfo(out);
	}
}
