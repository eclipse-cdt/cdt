/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * This command connects to a remote target.
 */
public class CLIAttach extends CLICommand<MIInfo> {

	public CLIAttach(IDMContext ctx, int pid) {
		super(ctx, "attach " + Integer.toString(pid)); //$NON-NLS-1$
	}
	
	/**
     * @since 1.1
     */
	public CLIAttach(ICommandControlDMContext ctx, String pid) {
		super(ctx, "attach " + pid); //$NON-NLS-1$
	}
}
