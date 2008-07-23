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
import org.eclipse.dd.mi.service.IMIRunControl;
import org.eclipse.dd.mi.service.command.output.MIFrame;
import org.eclipse.dd.mi.service.command.output.MIResult;

/**
 *
 */
@Immutable
public class MISharedLibEvent extends MIStoppedEvent {

    protected MISharedLibEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame) {
        super(ctx, token, results, frame);
    }

    @Deprecated
    public static MIStoppedEvent parse(
        IMIRunControl runControl, IContainerDMContext containerDmc, int token, MIResult[] results) 
    {
        MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(runControl, containerDmc, token, results); 
        return new MISharedLibEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame());
    }

    public static MIStoppedEvent parse(IExecutionDMContext dmc, int token, MIResult[] results) 
    {
       MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results); 
       return new MISharedLibEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame());
    }

}
