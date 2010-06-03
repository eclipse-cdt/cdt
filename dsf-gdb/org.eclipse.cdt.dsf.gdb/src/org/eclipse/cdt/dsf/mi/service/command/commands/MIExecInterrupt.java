/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson AB			- Modified for Execution Contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *      -exec-interrupt [--all | --thread-group ID]
 *
 *  Asynchronous command.  Interrupts the background execution of the
 *  target.  Note how the token associated with the stop message is the one
 *  for the execution command that has been interrupted.  The token for the
 *  interrupt itself only appears in the `^done' output.  If the user is
 *  trying to interrupt a non-running program, an error message will be
 *  printed.
 * 
 */
public class MIExecInterrupt extends MICommand<MIInfo> 
{
    public MIExecInterrupt(IExecutionDMContext dmc) {
        this(dmc, false);
    }

    /**
     * @since 1.1
     */
    public MIExecInterrupt(IExecutionDMContext dmc, boolean allThreads) {
        this(dmc, allThreads, null);
    }
    
    /**
	 * @since 3.0
	 */
    public MIExecInterrupt(IExecutionDMContext dmc, String groupId) {
    	this(dmc, false, groupId);
    }
    
    /*
     * The parameters allThreads and groupId are mutually exclusive.  allThreads must be false
     * if we are to use groupId.  The value of this method is to only have one place
     * where we use the hard-coded strings.
     */
    private MIExecInterrupt(IExecutionDMContext dmc, boolean allThreads, String groupId) {
        super(dmc, "-exec-interrupt"); //$NON-NLS-1$
        if (allThreads) {
        	setParameters(new String[] { "--all" }); //$NON-NLS-1$
        } else if (groupId != null) {
        	setParameters(new String[] { "--thread-group", groupId }); //$NON-NLS-1$
        }
    }
}
