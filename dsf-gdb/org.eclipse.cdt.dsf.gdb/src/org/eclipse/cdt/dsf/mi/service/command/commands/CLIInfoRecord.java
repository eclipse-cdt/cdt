/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoRecordInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 5.0
 */
public class CLIInfoRecord extends MIInterpreterExecConsole<CLIInfoRecordInfo> {
	private static final String COMMAND = "info record"; //$NON-NLS-1$

	public CLIInfoRecord(ICommandControlDMContext ctx) {
		super(ctx, COMMAND);
	}

	@Override
	public MIInfo getResult(MIOutput out) {
		return new CLIInfoRecordInfo(out);
	}
}
