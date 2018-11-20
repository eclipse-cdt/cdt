/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 *
 * -gdb-set python print-stack [ none | message | full ]
 *
 * By default, gdb will print only the message component of a Python exception when an error occurs
 * in a Python script. This can be controlled using set python print-stack: if full, then full Python
 * stack printing is enabled; if none, then Python stack and message printing is disabled; if message,
 * the default, only the message component of the error is printed.
 *
 * Available with GDB 7.4
 *
 * @since 4.1
 *
 */
public class MIGDBSetPythonPrintStack extends MIGDBSet {
	public MIGDBSetPythonPrintStack(ICommandControlDMContext ctx, String option) {
		super(ctx, new String[] { "python", "print-stack", option }); //$NON-NLS-1$//$NON-NLS-2$
	}
}