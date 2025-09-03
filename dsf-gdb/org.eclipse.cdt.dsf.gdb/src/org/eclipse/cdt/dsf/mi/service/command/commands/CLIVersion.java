/*******************************************************************************
 * Copyright (c) 2025 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - Initial implementation (#1186)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIVersionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * CLI 'version' command to report the debugger version where the MI -gdb-version command is not available
 *
 * @since 7.3
 */
public class CLIVersion extends CLICommand<CLIVersionInfo> {

	private static final String VERSION = "version"; //$NON-NLS-1$

	public CLIVersion(ICommandControlDMContext ctx) {
		super(ctx, VERSION);
	}

	@Override
	public CLIVersionInfo getResult(MIOutput miResult) {
		return new CLIVersionInfo(miResult);
	}
}
