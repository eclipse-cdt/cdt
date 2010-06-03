/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * This command turns on on off the recording of "Process Record and Replay".
 * 
 * @since 3.0
 */
public class CLIRecord extends CLICommand<MIInfo> {
	public CLIRecord(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, enable ? "record" : "record stop"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
