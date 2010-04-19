/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
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
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;

/**
 * Conveys that gdb reported the target stopped because of a breakpoint. This
 * includes catchpoints, as gdb reports them as a breakpoint-hit. The
 * async-exec-output record looks like this:
 * 
 * <code>
 *    ^stopped,reason="breakpoint-hit",bkptno="1",thread-id="0",frame={addr="0x08048468",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="4"}
 * </code>
 */
@Immutable
public class MIBreakpointHitEvent extends MIStoppedEvent {

    int bkptno;

    protected MIBreakpointHitEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame, int bkptno) {
        super(ctx, token, results, frame);
        this.bkptno = bkptno;
    }

    public int getNumber() {
        return bkptno;
    }
    
    /**
     * @param miStreamRecords 
     * @since 3.0
     */
    public static MIBreakpointHitEvent parse(IExecutionDMContext dmc, int token, MIResult[] results, MIStreamRecord[] miStreamRecords) 
    { 
       int bkptno = -1;

       for (int i = 0; i < results.length; i++) {
           String var = results[i].getVariable();
           MIValue value = results[i].getMIValue();
           String str = ""; //$NON-NLS-1$
           if (value != null && value instanceof MIConst) {
               str = ((MIConst)value).getString();
           }

           if (var.equals("bkptno")) { //$NON-NLS-1$
               try {
                   bkptno = Integer.parseInt(str.trim());
               } catch (NumberFormatException e) {
               }
           }
       }
       MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results); 
       
       // We might be here because of a catchpoint hit; in gdb >= 7.0,
       // catchpoints are reported as breakpoints. Unfortunately, there's
       // nothing in the stopped event indicating it's a catchpoint, and unlike
       // gdb < 7.0, there are no stream records that reveal it's a catchpoint.
       // The only way to determine it's a catchpoint is to map the gdb breakpoint
       // number back to the CBreakpoint (platform) instance, and that *will* reveal
       // whether it's a catchpoint or not, and even which kind of catchpoint
       // TODO: 
       /*
       CBreakpoint cbkpt = FromSomewhere.getCBreakpointForGdbBreakpoint(bkptno); <== this method doesn't exist yet
       if (cbkpt instanceof CEventBreakpoint) {
	       String eventTypeID = ((CEventBreakpoint)cbkpt).getEventType();
	       String gdbKeyword = GdbCatchpoints.eventToGdbCatchpointKeyword(eventTypeID)
	       return MICatchpointHitEvent.parse(stoppedEvent.getDMContext(), token, results, gdbKeyword);
       }
       */
       
       return new MIBreakpointHitEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), bkptno);
    }
}
