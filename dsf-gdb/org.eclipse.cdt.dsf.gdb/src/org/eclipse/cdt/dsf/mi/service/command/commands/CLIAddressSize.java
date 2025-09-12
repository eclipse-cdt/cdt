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
 *     John Dallaway - Initial implementation (#1191)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIAddressSizeInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * CLI command to report address size where the MI expression evaluator is not available
 *
 * @since 7.3
 */
public class CLIAddressSize extends MIInterpreterExecConsole<CLIAddressSizeInfo> {

	private static final String COMMAND = "p/x sizeof(void*)"; //$NON-NLS-1$

	public CLIAddressSize(IMemoryDMContext ctx) {
		super(ctx, COMMAND);
	}

	@Override
	public CLIAddressSizeInfo getResult(MIOutput miResult) {
		return new CLIAddressSizeInfo(miResult);
	}
}
