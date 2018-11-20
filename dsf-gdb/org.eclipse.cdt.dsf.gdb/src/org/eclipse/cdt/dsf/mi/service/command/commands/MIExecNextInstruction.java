/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 *      -exec-next-instruction [count]
 *
 *   Asynchronous command.  Executes one machine instruction.  If the
 * instruction is a function call continues until the function returns.  If
 * the program stops at an instruction in the middle of a source line, the
 * address will be printed as well.
 *
 */
public class MIExecNextInstruction extends MICommand<MIInfo> {
	public MIExecNextInstruction(IExecutionDMContext dmc) {
		this(dmc, 1);
	}

	public MIExecNextInstruction(IExecutionDMContext dmc, int count) {
		super(dmc, "-exec-next-instruction", new String[] { Integer.toString(count) }); //$NON-NLS-1$
	}
}
