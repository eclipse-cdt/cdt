/*******************************************************************************
 * Copyright (c) 2009, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;

/**
 * *stopped,reason="function-finished",thread-id="0",frame={addr="0x0804855a",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="17"},gdb-result-var="$1",return-value="10"
 */
@Immutable
public class MIFunctionFinishedEvent extends MIStoppedEvent {

    protected MIFunctionFinishedEvent(
        IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame, String gdbResult, 
        String returnValue, String returnType) 
    {
        super(ctx, token, results, frame, gdbResult, returnValue, returnType);
    }

    /**
     * @since 1.1
     */
    public static MIFunctionFinishedEvent parse(IExecutionDMContext dmc, int token, MIResult[] results) 
    {
       MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results); 
       return new MIFunctionFinishedEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(),
    		                              stoppedEvent.getGDBResultVar(), stoppedEvent.getReturnValue(), stoppedEvent.getReturnType());
    }
}
