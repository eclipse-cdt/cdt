/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;



/**
 * 
 *      -exec-step-instruction

 *  Asynchronous command.  Resumes the inferior which executes one
 * machine instruction.  The output, once GDB has stopped, will vary
 * depending on whether we have stopped in the middle of a source line or
 * not.  In the former case, the address at which the program stopped will
 * be printed as well.
 * 
 */
public class MIExecStepInstruction extends MICommand 
{
	public MIExecStepInstruction() {
		super("-exec-step-instruction"); //$NON-NLS-1$
	}

	public MIExecStepInstruction(int count) {
		super("-exec-step-instruction", new String[] { Integer.toString(count) }); //$NON-NLS-1$
	}
}
