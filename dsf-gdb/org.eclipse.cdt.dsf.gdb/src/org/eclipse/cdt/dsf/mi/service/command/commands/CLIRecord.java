/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.debug.core.model.ReverseTraceMethod;
import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.IReverseTraceMethod;

/**
 * This command turns on on off the recording of "Process Record and Replay".
 * 
 * @since 3.0
 */
public class CLIRecord extends CLICommand<MIInfo> {

	public CLIRecord(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, enable ? "record" : "record stop"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @since 5.0 */
    public CLIRecord(ICommandControlDMContext ctx, IReverseTraceMethod traceMethod) {
        super(ctx, "record" + createRecordParams(traceMethod));  //$NON-NLS-1$
    }

    private static String createRecordParams(IReverseTraceMethod traceMethod)
    {
        String recordParam;

            if (traceMethod == ReverseTraceMethod.STOP_TRACE)
                recordParam = " stop"; //$NON-NLS-1$
            else if (traceMethod == ReverseTraceMethod.FULL_TRACE) // full trace
                recordParam = " full"; //$NON-NLS-1$
            else if (traceMethod == ReverseTraceMethod.BRANCH_TRACE) // branch trace
                recordParam = " btrace bts"; //$NON-NLS-1$
            else if (traceMethod == ReverseTraceMethod.PROCESSOR_TRACE) // processor trace
                recordParam = " btrace pt"; //$NON-NLS-1$
            else if (traceMethod == ReverseTraceMethod.GDB_TRACE) // gdb selected trace
                recordParam = " btrace"; //$NON-NLS-1$
            else // no trace method defined
                recordParam = ""; //$NON-NLS-1$

        return recordParam;
    }
}
