/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoItsetsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 5.0
 */
public class MIInfoItsets extends MIInterpreterExecConsole<MIInfoItsetsInfo> {

	private static final String COMMAND = "info itsets"; //$NON-NLS-1$
	
	public MIInfoItsets(ICommandControlDMContext ctx) {
		super(ctx, COMMAND);
	}

	public MIInfoItsets(ICommandControlDMContext ctx, String setId) {
		// hack - remove first character from it set id - it will usually be a "u". 
		// The "u" is present in the MI id, but not expected in the CLI of the current command...
		super(ctx, COMMAND + " " + setId.substring(1)); //$NON-NLS-1$
	}
	
	@Override
	public MIInfoItsetsInfo getResult(MIOutput miResult) {
		return new MIInfoItsetsInfo(miResult);
	}
}
