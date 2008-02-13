/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.runcontrol;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;

@Immutable 
class ResumedEvent extends AbstractDMEvent<IExecutionDMContext> 
    implements IResumedDMEvent
{
    private final String fPDAEvent;

    ResumedEvent(IExecutionDMContext ctx, String pdaEvent) { 
        super(ctx);
        fPDAEvent = pdaEvent;
    }
    
    public StateChangeReason getReason() {
        if (fPDAEvent.startsWith("resumed breakpoint") || fPDAEvent.startsWith("suspended watch")) {
            return StateChangeReason.BREAKPOINT;
        } else if (fPDAEvent.equals("resumed step") || fPDAEvent.equals("resumed drop")) {
            return StateChangeReason.STEP;
        } else if (fPDAEvent.equals("resumed client")) {
            return StateChangeReason.USER_REQUEST;
        } else {
            return StateChangeReason.UNKNOWN;
        } 
    }
}