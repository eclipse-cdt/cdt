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

import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.command.output.MIOutput;
import org.eclipse.dd.mi.service.command.output.MIStackListArgumentsInfo;

/**
 * 
 *    -stack-list-arguments SHOW-VALUES
 *        [ LOW-FRAME HIGH-FRAME ]
 *
 *  Display a list of the arguments for the frames between LOW-FRAME and
 * HIGH-FRAME (inclusive).  If LOW-FRAME and HIGH-FRAME are not provided,
 * list the arguments for the whole call stack.
 *
 *   The SHOW-VALUES argument must have a value of 0 or 1.  A value of 0
 * means that only the names of the arguments are listed, a value of 1
 * means that both names and values of the arguments are printed.
 * 
 */
public class MIStackListArguments extends MICommand<MIStackListArgumentsInfo> 
{
    public MIStackListArguments(IMIExecutionDMContext execDmc, boolean showValues) {
        super(execDmc, "-stack-list-arguments"); //$NON-NLS-1$
        if (showValues) {
            setParameters(new String[]{"1"}); //$NON-NLS-1$
        } else {
            setParameters(new String[]{"0"}); //$NON-NLS-1$
        }
    }

    public MIStackListArguments(IFrameDMContext frameDmc, boolean showValues) {
        super(frameDmc, "-stack-list-arguments"); //$NON-NLS-1$
        if (showValues) {
            setParameters(new String[]{"1"}); //$NON-NLS-1$
        } else {
            setParameters(new String[]{"0"}); //$NON-NLS-1$
        }
    }
    
    public MIStackListArguments(IMIExecutionDMContext execDmc, boolean showValues, int low, int high) {
        super(execDmc, "-stack-list-arguments"); //$NON-NLS-1$
        String[] params = new String[3];
        if (showValues) {
            params[0] = "1"; //$NON-NLS-1$
        } else {
            params[0] = "0"; //$NON-NLS-1$
        }
        params[1] = Integer.toString(low);
        params[2] = Integer.toString(high);
        setParameters(params);
    }
    
    @Override
    public MIStackListArgumentsInfo getResult(MIOutput out) {
        return new MIStackListArgumentsInfo(out);
    }
}
