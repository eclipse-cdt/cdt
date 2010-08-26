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

package org.eclipse.cdt.dsf.gdb.internal.service.command.events;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;

/**
 * Conveys that gdb has selected a new tracepoint record. Although this
 * is a response to an MI command, we trigger an MI event internally
 * because it should cause the same behaviour as if we stopped at a
 * breakpoint.  The output record looks like this:
 * 
 * <code>
 * ^done,found="1",tracepoint="1",traceframe="0",frame={level="0",addr="0x08048900",func="foo",args=[{name="i",value="2"}],file="file.cpp",fullname="/home/marc/file.cpp",line="505"}

 * </code>
 * @since 3.0
 */
@Immutable
public class MITracepointSelectedEvent extends MIBreakpointHitEvent {

	private int fRecNo;
	
    protected MITracepointSelectedEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame, int trptno, int recordno) {
        super(ctx, token, results, frame, trptno);
        fRecNo = recordno;
    }

	/**
	 * Returns a text to display for the reason why we show the debug view as stopped.
	 */
	public String getReason() {
		return EventMessages.Tracepoint + " " + getNumber() + //$NON-NLS-1$
		       ", " + EventMessages.Record + " " + fRecNo; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
    @ConfinedToDsfExecutor("")    
    public static MITracepointSelectedEvent parse(IExecutionDMContext dmc, int token, MIResult[] results) {
       int trptno = -1;
       int recordno = -1;

       for (int i = 0; i < results.length; i++) {
           String var = results[i].getVariable();
           MIValue value = results[i].getMIValue();
           String str = ""; //$NON-NLS-1$
           if (value != null && value instanceof MIConst) {
               str = ((MIConst)value).getString();
           }

           if (var.equals("tracepoint")) { //$NON-NLS-1$
               try {
            	   trptno = Integer.parseInt(str.trim());
               } catch (NumberFormatException e) {
               }
           } else if (var.equals("traceframe")) { //$NON-NLS-1$
               try {
            	   recordno = Integer.parseInt(str.trim());
               } catch (NumberFormatException e) {
               }
           }

       }

       MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results);
       return new MITracepointSelectedEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), trptno, recordno);
    }
}
