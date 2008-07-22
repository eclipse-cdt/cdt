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

import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;

/**
 * 
 *      -exec-step-instruction [--thread <tid>] [count]

 *  Asynchronous command.  Resumes the inferior which executes one
 * machine instruction.  The output, once GDB has stopped, will vary
 * depending on whether we have stopped in the middle of a source line or
 * not.  In the former case, the address at which the program stopped will
 * be printed as well.
 * 
 */
public class MIExecStepInstruction extends MICommand<MIInfo> 
{
    public MIExecStepInstruction(IExecutionDMContext dmc) {
        this(dmc, 1);
    }

    public MIExecStepInstruction(IExecutionDMContext dmc, int count) {
        super(dmc, "-exec-step-instruction", new String[] { Integer.toString(count) }); //$NON-NLS-1$
    }

    public MIExecStepInstruction(IMIExecutionDMContext dmc, boolean setThread) {
        this(dmc, setThread, 1);
    }

    public MIExecStepInstruction(IMIExecutionDMContext dmc, boolean setThread, int count) {
        super(dmc, "-exec-step-instruction");	//$NON-NLS-1$
        if (setThread) {
        	setParameters(new String[] { "--thread", Integer.toString(dmc.getThreadId()), Integer.toString(count) }); //$NON-NLS-1$
        } else {
        	setParameters(new String[] { Integer.toString(count) });
        }
    }
}
