/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIITSetViewInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 5.0
 */
public class MIITSetView extends MIInterpreterExecConsole<MIITSetViewInfo> {
	
	private static final String COMMAND = "itset view"; //$NON-NLS-1$
	
//	public MIITSetView(ICommandControlDMContext ctx) {
//		super(ctx, COMMAND);
//	}
	
	public MIITSetView(ICommandControlDMContext ctx, String name) {
		super(ctx, COMMAND + " " + name); //$NON-NLS-1$
	}
	
	@Override
	public MIITSetViewInfo getResult(MIOutput miResult) {
		return new MIITSetViewInfo(miResult);
	}
}
