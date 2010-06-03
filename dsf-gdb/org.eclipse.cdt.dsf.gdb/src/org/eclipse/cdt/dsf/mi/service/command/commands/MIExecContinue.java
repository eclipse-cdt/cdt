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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *      -exec-continue [--all | --thread-group ID]
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
    	this(dmc, allThreads, null);
    }

    /**
	 * @since 3.0
	 */
    public MIExecContinue(IExecutionDMContext dmc, String groupId) {
    	this(dmc, false, groupId);
    }
    
    /*
     * The parameters allThreads and groupId are mutually exclusive.  allThreads must be false
     * if we are to use groupId.  The value of this method is to only have one place
     * where we use the hard-coded strings.
     */
    private MIExecContinue(IExecutionDMContext dmc, boolean allThreads, String groupId) {
        super(dmc, "-exec-continue"); //$NON-NLS-1$
        if (allThreads) {
        	setParameters(new String[] { "--all" }); //$NON-NLS-1$
        } else if (groupId != null) {
        	setParameters(new String[] { "--thread-group", groupId }); //$NON-NLS-1$
        }
    }
}
