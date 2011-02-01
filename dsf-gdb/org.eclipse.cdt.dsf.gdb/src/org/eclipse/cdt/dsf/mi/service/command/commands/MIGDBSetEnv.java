/*******************************************************************************
 * Copyright (c) 2010,, 2011 Ericsson and others.
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
    	// We need to avoid putting "" around the variable.
    	// -gdb-set env "MYVAR=MY VAR"
    	// will not set MYVAR to MY VAR, but instead will create an empty variable with name "MYVAR=MY VAR"
    	// This is because "" are automatically inserted if there is a space in the parameter.
    	// What we really want to send is:
    	// -gdb-set env MYVAR=MY VAR
    	// To achieve that, we split the value into separate parameters
        super(dmc, null);
        
        if (value == null || value.length() == 0) {
        	setParameters(new String[] { "env", name }); //$NON-NLS-1$
        } else {
        	String[] splitValue = value.split(" "); //$NON-NLS-1$
        	String[] params = new String[splitValue.length+3];
        	params[0] = "env"; //$NON-NLS-1$
        	params[1] = name;
        	params[2] = "="; //$NON-NLS-1$
        	for (int i=3; i<params.length; i++) {
        		params[i] = splitValue[i-3];
        	}
        	
        	setParameters(params);
        }
    } 
}
