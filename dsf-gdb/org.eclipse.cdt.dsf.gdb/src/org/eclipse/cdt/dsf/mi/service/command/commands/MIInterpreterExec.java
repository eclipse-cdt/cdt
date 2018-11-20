/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson 		  	- Modified for additional features in DSF Reference implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * MIInterpreterExec
 *
 * -interpreter-exec
 *
 * -interpreter-exec interpreter command
 *
 * Execute the specified command in the given interpreter.
 *
 * -interpreter-exec console "break main"
 * &"During symbol reading, couldn't parse type; debugger out of date?.\n"
 * &"During symbol reading, bad structure-type format.\n"
 * ~"Breakpoint 1 at 0x8074fc6: file ../../src/gdb/main.c, line 743.\n"
 * ^done
 *
 */
public class MIInterpreterExec<V extends MIInfo> extends MICommand<V> {

	/**
	 * @param oper
	 */
	public MIInterpreterExec(IDMContext ctx, String interpreter, String cmd) {
		super(ctx, "-interpreter-exec", new String[] { interpreter, cmd }); //$NON-NLS-1$
	}

}
