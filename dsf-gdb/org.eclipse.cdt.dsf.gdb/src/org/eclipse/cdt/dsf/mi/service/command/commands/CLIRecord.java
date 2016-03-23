/*******************************************************************************
 * Copyright (c) 2009, 2016 Ericsson and others.
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
import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;

/**
 * This command turns on on off the recording of "Process Record and Replay".
 * 
 * @since 3.0
 */
public class CLIRecord extends CLICommand<MIInfo> {

	public CLIRecord(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, enable ? "record" : "record stop"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Only available for GDB >= 7.10 */
	/** @since 5.0 */
    public CLIRecord(ICommandControlDMContext ctx, ReverseDebugMethod traceMethod) {
        super(ctx, "record" + createRecordParams(traceMethod));  //$NON-NLS-1$
    }

    private static String createRecordParams(ReverseDebugMethod traceMethod)
    {
        String recordParam;

        if (traceMethod == ReverseDebugMethod.OFF) {
        	recordParam = " stop"; //$NON-NLS-1$
        } else if (traceMethod == ReverseDebugMethod.SOFTWARE) {
        	recordParam = " full"; //$NON-NLS-1$
        } else if (traceMethod == ReverseDebugMethod.BRANCH_TRACE) {
        	recordParam = " btrace bts"; //$NON-NLS-1$
        } else if (traceMethod == ReverseDebugMethod.PROCESSOR_TRACE) {
        	recordParam = " btrace pt"; //$NON-NLS-1$
        } else if (traceMethod == ReverseDebugMethod.GDB_TRACE) {
        	recordParam = " btrace"; //$NON-NLS-1$
        } else {// no trace method defined
        	recordParam = ""; //$NON-NLS-1$
        }

        return recordParam;
    }
}
