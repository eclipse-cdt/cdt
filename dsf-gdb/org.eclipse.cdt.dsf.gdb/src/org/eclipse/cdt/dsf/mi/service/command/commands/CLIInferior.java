/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private static final String INFERIOR = "inferior ";  //$NON-NLS-1$

	public CLIInferior(ICommandControlDMContext ctx, String inferiorId) {
		super(ctx, INFERIOR + inferiorId);
	}
}
