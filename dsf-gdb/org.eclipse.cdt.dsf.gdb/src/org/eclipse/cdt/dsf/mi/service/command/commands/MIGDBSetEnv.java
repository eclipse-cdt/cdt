/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
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
 * -gdb-set env VARNAME [=VALUE]
 *
 * Set environment variable VARNAME to VALUE. The value changes for your program only, 
 * not for gdb itself. VALUE may be any string; the values of environment variables are 
 * just strings, and any interpretation is supplied by your program itself. The VALUE
 * parameter is optional; if it is omitted, the variable is set to a null value.
 *
 * @since 3.0
 * 
 */
public class MIGDBSetEnv extends MIGDBSet 
{
    public MIGDBSetEnv(ICommandControlDMContext dmc, String name) {
    	this(dmc, name, null);
    }

    public MIGDBSetEnv(ICommandControlDMContext dmc, String name, String value) {
        super(dmc, new String[] { "env", name + (value != null && value.length() > 0 ? "=" + value : "")}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}