/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for handling of execution contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *      -exec-step [count]
 *
 *   Asynchronous command.  Resumes execution of the inferior program,
 * stopping when the beginning of the next source line is reached, if the
 * next source line is not a function call.  If it is, stop at the first
 * instruction of the called function.
 * 
 */
public class MIExecStep extends MICommand<MIInfo> 
{
    public MIExecStep(IExecutionDMContext dmc) {
        this(dmc, 1);
    }

    public MIExecStep(IExecutionDMContext dmc, int count) {
        super(dmc, "-exec-step", new String[] { Integer.toString(count) }); //$NON-NLS-1$
    }
}
