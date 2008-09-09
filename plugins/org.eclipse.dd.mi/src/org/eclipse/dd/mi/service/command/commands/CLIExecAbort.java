/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;

/**
 * 
 *     kill
 *
 *  Terminates the user (MI inferior) process
 * 
 */
public class CLIExecAbort extends CLICommand<MIInfo> 
{
    public CLIExecAbort(ICommandControlDMContext ctx) {
        super(ctx, "kill"); //$NON-NLS-1$
    }
}
