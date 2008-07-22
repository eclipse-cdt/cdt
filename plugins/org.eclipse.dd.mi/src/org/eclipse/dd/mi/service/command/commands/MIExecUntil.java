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
 *     Ericsson				- Modified for handling of execution contexts
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;

/**
 * 
 *     -exec-until [--thread <tid>] [ LOCATION ]
 *
 *  Asynchronous command.  Executes the inferior until the LOCATION
 * specified in the argument is reached.  If there is no argument, the
 * inferior executes until a source line greater than the current one is
 * reached.  The reason for stopping in this case will be
 * `location-reached'.
 * 
 */
public class MIExecUntil extends MICommand<MIInfo> 
{
    public MIExecUntil(IExecutionDMContext dmc) {
        super(dmc, "-exec-until"); //$NON-NLS-1$
    }

    public MIExecUntil(IExecutionDMContext dmc, String loc) {
        super(dmc, "-exec-until", new String[] { loc }); //$NON-NLS-1$
    }

    public MIExecUntil(IMIExecutionDMContext dmc, boolean setThread) {
        super(dmc, "-exec-until"); //$NON-NLS-1$
        if (setThread) {
        	setParameters(new String[] { "--thread", Integer.toString(dmc.getThreadId()) }); //$NON-NLS-1$
        }
    }

    public MIExecUntil(IMIExecutionDMContext dmc, boolean setThread, String loc) {
        super(dmc, "-exec-until"); //$NON-NLS-1$
        if (setThread) {
        	setParameters(new String[] { "--thread", Integer.toString(dmc.getThreadId()), loc }); //$NON-NLS-1$
        } else {
        	setParameters(new String[] { loc });
        }
    }
}
