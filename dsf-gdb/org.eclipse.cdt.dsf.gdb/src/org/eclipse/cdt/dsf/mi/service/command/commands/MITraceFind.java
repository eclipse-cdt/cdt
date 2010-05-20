/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceFindInfo;

/**
 * -trace-find MODE [PARAMS...]
 * 
 * Find a trace frame using criteria defined by MODE and PARAMS. The following
 * lists permissible modes and their parameters.
 * 
 * none              - No parameters are required. Stops examining trace frames.
 * frame-number      - An integer is required as parameter. Selects tracepoint frame 
 *                     with that index.
 * tracepoint-number - An integer is required as parameter. Finds next trace 
 *                     frame that corresponds to tracepoint with the specified number.
 * pc                - An integer address is required as parameter. Finds next trace
 *                     frame that corresponds to any tracepoint at the specified address.
 * pc-inside-range   - Two integer addresses are required as parameters. Finds next 
 *                     trace frame that corresponds to a tracepoint at an address inside 
 *                     the specified range.
 * pc-outside-range  - Two integer addresses are required as parameters. Finds next 
 *                     trace frame that corresponds to a tracepoint at an address outside
 *                     the specified range.
 * line              - Line specification is required as parameter. 
 *                     Finds next trace frame that corresponds to a tracepoint at the 
 *                     specified location.
 * 
 * If the 'none' was passed as mode, the response does not have fields.  Otherwise, the
 * response may have the following fields:
 *
 * found      - This field has either 0 or 1 as the value, depending on whether a matching
 *              tracepoint was found.
 * traceframe - The index of the found traceframe. This field is present if the 'found' 
 *              field has value of 1.
 * tracepoint - The index of the found tracepoint. This field is present if the 'found' 
 *              field has value of 1.
 * frame      - The stack frame when the traceframe was collected
 * 
 * @since 3.0
 */
public class MITraceFind extends MICommand<MITraceFindInfo> {
	public MITraceFind(ITraceTargetDMContext ctx, String[] params) {
		super(ctx, "-trace-find", null, params); //$NON-NLS-1$
	}
	
    @Override
    public MITraceFindInfo getResult(MIOutput out) {
        return new MITraceFindInfo(out);
    }
}
