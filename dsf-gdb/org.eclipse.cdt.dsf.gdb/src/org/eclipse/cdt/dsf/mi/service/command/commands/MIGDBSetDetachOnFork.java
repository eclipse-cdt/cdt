/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
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

/**
 * 
 * -gdb-set detach-on-fork [on|off]
 * 
 * When 'on', tells GDB to detach from process that has been forked.
 * When 'off', automatically starts debugging a forked process in a multi-process session.
 * 
 * @since 4.0
 */
public class MIGDBSetDetachOnFork extends MIGDBSet 
{
    public MIGDBSetDetachOnFork(ICommandControlDMContext ctx, boolean detach) {
        super(ctx, new String[] {"detach-on-fork", detach ? "on" : "off"});//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }
}