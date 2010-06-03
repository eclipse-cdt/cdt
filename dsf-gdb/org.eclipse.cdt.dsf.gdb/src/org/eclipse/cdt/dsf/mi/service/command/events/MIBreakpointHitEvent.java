/*******************************************************************************
 * Copyright (c) 2009, 2010 QNX Software Systems and others.
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

import org.eclipse.cdt.debug.internal.core.breakpoints.CEventBreakpoint;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.gdb.internal.eventbkpts.GdbCatchpoints;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;

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
    
    @ConfinedToDsfExecutor("")    
    public static MIBreakpointHitEvent parse(IExecutionDMContext dmc, int token, MIResult[] results) {
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

       // We might be here because of a catchpoint hit; in gdb >= 7.0,
       // catchpoints are reported as breakpoints. Unfortunately, there's
       // nothing in the stopped event indicating it's a catchpoint, and unlike
		// gdb < 7.0, there are no stream records that tell us so. The only way
		// to determine it's a catchpoint is to map the gdb breakpoint number
		// back to the CBreakpoint (platform) object.
       IBreakpointsTargetDMContext bpsTarget = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
       if (bpsTarget != null) {
    	   MIBreakpointDMContext bkpt = new MIBreakpointDMContext(dmc.getSessionId(), new IDMContext[] {bpsTarget}, bkptno);
    	   DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), dmc.getSessionId());
    	   try {
	    	   MIBreakpointsManager bkptMgr = tracker.getService(MIBreakpointsManager.class);
	    	   if (bkptMgr != null) {
		    	   IBreakpoint platformBkpt = bkptMgr.findPlatformBreakpoint(bkpt);
		    	   if (platformBkpt instanceof CEventBreakpoint) {
		    		   try {
		    			   String eventTypeID = ((CEventBreakpoint)platformBkpt).getEventType();
		    			   String gdbKeyword = GdbCatchpoints.eventToGdbCatchpointKeyword(eventTypeID);
		    			   return MICatchpointHitEvent.parse(dmc, token, results, bkptno, gdbKeyword);
		    		   } catch (DebugException e) {
		    		   }
		    	   }
	    	   }
    	   }
    	   finally {
    		   tracker.dispose();
    	   }
       }

       MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results);
       return new MIBreakpointHitEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), bkptno);
    }
}
