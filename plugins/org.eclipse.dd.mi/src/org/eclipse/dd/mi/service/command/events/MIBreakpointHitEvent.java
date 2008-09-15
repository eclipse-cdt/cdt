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
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.events;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.mi.service.MIRunControl;
import org.eclipse.dd.mi.service.command.output.MIConst;
import org.eclipse.dd.mi.service.command.output.MIFrame;
import org.eclipse.dd.mi.service.command.output.MIResult;
import org.eclipse.dd.mi.service.command.output.MIValue;

/**
 * ^stopped,reason="breakpoint-hit",bkptno="1",thread-id="0",frame={addr="0x08048468",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="4"}
 *
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

    @Deprecated
    public static MIBreakpointHitEvent parse(
        MIRunControl runControl, IContainerDMContext containerDmc, int token, MIResult[] results) 
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
        MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(runControl, containerDmc, token, results); 
        return new MIBreakpointHitEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), bkptno);
    }
    
    /**
     * @since 1.1
     */
    public static MIBreakpointHitEvent parse(IExecutionDMContext dmc, int token, MIResult[] results) 
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
       return new MIBreakpointHitEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), bkptno);
    }
}
