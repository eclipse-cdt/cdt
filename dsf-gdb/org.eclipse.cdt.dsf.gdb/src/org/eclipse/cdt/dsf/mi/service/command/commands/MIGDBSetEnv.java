/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public class MIGDBSetEnv extends MIGDBSet {
	public MIGDBSetEnv(ICommandControlDMContext dmc, String name) {
		this(dmc, name, null);
	}

	public MIGDBSetEnv(ICommandControlDMContext dmc, String name, String value) {
		// MICommand wraps a parameter with double quotes if it contains a space. If the
		// value of the environment variable has a space, and we bundle the var name, the
		// '=' and the value as a single parameter, then we'll end up with something like
		//
		//    -gdb-set env "MYVAR=MY VAR"
		//
		// which defines an environment variable named "MYVAR=MY VAR", with an empty
		// string for a value. To avoid this, we send each element as a separate parameter
		//
		//    -gdb-set env MYVAR = MY VAR
		super(dmc, null);

		if (value == null || value.length() == 0) {
			setParameters(new String[] { "env", name }); //$NON-NLS-1$
		} else {
			String[] splitValue = value.split(" "); //$NON-NLS-1$
			String[] params = new String[splitValue.length + 3];
			params[0] = "env"; //$NON-NLS-1$
			params[1] = name;
			params[2] = "="; //$NON-NLS-1$
			for (int i = 3; i < params.length; i++) {
				params[i] = splitValue[i - 3];
			}

			setParameters(params);
		}
	}
}
