/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * Selects the specified inferior in GDB.
 *
 * @since 5.2
 */
public class CLIInferior extends MIInterpreterExecConsole<MIInfo> {

	private static final String INFERIOR = "inferior "; //$NON-NLS-1$

	public CLIInferior(ICommandControlDMContext ctx, String inferiorId) {
		super(ctx, INFERIOR + inferiorId);
	}
}
