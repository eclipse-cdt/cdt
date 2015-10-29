/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIITFocusInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 4.9
 */
public class MIITFocus extends MIInterpreterExecConsole<MIITFocusInfo> {

	private static final String COMMAND = "itfocus"; //$NON-NLS-1$
	
	public MIITFocus(ICommandControlDMContext ctx) {
		super(ctx, COMMAND);
	}

	public MIITFocus(ICommandControlDMContext ctx, String focus) {
		super(ctx, COMMAND + " " + focus); //$NON-NLS-1$
	}
	
	@Override
	public MIITFocusInfo getResult(MIOutput miResult) {
		return new MIITFocusInfo(miResult);
	}
}
