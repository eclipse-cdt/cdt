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
 *     Marc Dumais (Ericsson) - Bug 399419
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;

/**
 *
 *  *stopped,reason="end-stepping-range",thread-id="0",frame={addr="0x08048538",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="13"}
 */
@Immutable
public class MISteppingRangeEvent extends MIStoppedEvent {

	protected MISteppingRangeEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame) {
        super(ctx, token, results, frame);
    } 
	/**
	 * @since 4.2
	 */
	protected MISteppingRangeEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame, Integer coreId) {
        super(ctx, token, results, frame, coreId);
    }  
    
    /**
     * @since 1.1
     */
    public static MISteppingRangeEvent parse(IExecutionDMContext dmc, int token, MIResult[] results)
    {
    	MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results);
    	return new MISteppingRangeEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), stoppedEvent.getCoreId());
    }
}
