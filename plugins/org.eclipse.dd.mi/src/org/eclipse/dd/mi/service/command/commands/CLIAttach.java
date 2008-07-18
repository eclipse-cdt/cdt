/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.mi.service.IMIProcessDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;

/**
 * This command connects to a remote target.
 */
public class CLIAttach extends CLICommand<MIInfo> {

	@Deprecated
	public CLIAttach(IDMContext ctx, int pid) {
		super(ctx, "attach " + Integer.toString(pid)); //$NON-NLS-1$
	}
	
	public CLIAttach(IMIProcessDMContext ctx) {
		super(ctx, "attach " + ctx.getProcId()); //$NON-NLS-1$
	}

}