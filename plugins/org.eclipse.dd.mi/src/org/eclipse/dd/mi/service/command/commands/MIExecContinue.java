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
import org.eclipse.dd.mi.service.command.output.MIInfo;

/**
 * 
 *      -exec-continue [--all]
 * 
 *   Asynchronous command.  Resumes the execution of the inferior program
 *   until a breakpoint is encountered, or until the inferior exits.
 * 
 */
public class MIExecContinue extends MICommand<MIInfo> 
{
    public MIExecContinue(IExecutionDMContext dmc) {
        this(dmc, false);
    }

    /**
     * @since 1.1
     */
    public MIExecContinue(IExecutionDMContext dmc, boolean allThreads) {
        super(dmc, "-exec-continue"); //$NON-NLS-1$
        if (allThreads) {
        	setParameters(new String[] { "--all" }); //$NON-NLS-1$
        }
    }
}
