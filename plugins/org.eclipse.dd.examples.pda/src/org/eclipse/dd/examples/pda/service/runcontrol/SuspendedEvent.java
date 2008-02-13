package org.eclipse.dd.examples.pda.service.runcontrol;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;

/**
 * Indicates that the given thread has been suspended.
 */
@Immutable
class SuspendedEvent extends AbstractDMEvent<IExecutionDMContext> 
    implements ISuspendedDMEvent
{
    private final String fPDAEvent;
    
    SuspendedEvent(IExecutionDMContext ctx, String pdaEvent) { 
        super(ctx);
        fPDAEvent = pdaEvent;
    }
    
    public StateChangeReason getReason() {
        if (fPDAEvent.startsWith("suspended breakpoint") || fPDAEvent.startsWith("suspended watch")) {
            return StateChangeReason.BREAKPOINT;
        } else if (fPDAEvent.equals("suspended step") || fPDAEvent.equals("suspended drop")) {
            return StateChangeReason.STEP;
        } else if (fPDAEvent.equals("suspended client")) {
            return StateChangeReason.USER_REQUEST;
        } else {
            return StateChangeReason.UNKNOWN;
        } 
    }
}