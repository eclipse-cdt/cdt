/*******************************************************************************
 * Copyright (c) 2021 Trande UG.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Trande UG. - Added Functions Call History support
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIFunctionsCallHistoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 6.5
 */
public class CLIFunctionsCallHistory extends MIInterpreterExecConsole<CLIFunctionsCallHistoryInfo> {
	private static final String COMMAND = "record function-call-history /ilc"; //$NON-NLS-1$

	public CLIFunctionsCallHistory(ICommandControlDMContext ctx) {
		super(ctx, COMMAND);
	}

	@Override
	public MIInfo getResult(MIOutput out) {
		return new CLIFunctionsCallHistoryInfo(out);
	}
}