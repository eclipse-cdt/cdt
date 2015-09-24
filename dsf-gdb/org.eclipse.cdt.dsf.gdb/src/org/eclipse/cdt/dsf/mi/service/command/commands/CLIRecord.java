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

/**
 * This command turns on on off the recording of "Process Record and Replay".
 * 
 * @since 3.0
 */
public class CLIRecord extends CLICommand<MIInfo> {
	public CLIRecord(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, enable ? "record" : "record stop"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
     * @since 4.8
     */
    public CLIRecord(ICommandControlDMContext ctx, int traceMethod) {
        super(ctx, "record" + createRecordParams(traceMethod));  //$NON-NLS-1$
    }

    private static String createRecordParams(int traceMethod)
    {
        String recordParam;
            switch (traceMethod) {
            case -1:
                recordParam = " stop"; //$NON-NLS-1$
                break;
            case 0: // full trace
                recordParam = " full"; //$NON-NLS-1$
                break;
            case 1: // branch trace
                recordParam = " btrace bts"; //$NON-NLS-1$
                break;
            case 2: // processor trace
                recordParam = " btrace pt"; //$NON-NLS-1$
                break;
            case 3: // gdb selected trace
                recordParam = " btrace"; //$NON-NLS-1$
                break;
            default: // no trace method defined
                recordParam = ""; //$NON-NLS-1$
                break;
            }
        return recordParam;
    }
}
