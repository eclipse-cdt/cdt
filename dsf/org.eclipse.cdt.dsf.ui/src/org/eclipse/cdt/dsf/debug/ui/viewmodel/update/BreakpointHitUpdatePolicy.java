/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.update;

import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ManualUpdatePolicy;

/**
 * 
 */
public class BreakpointHitUpdatePolicy extends ManualUpdatePolicy {
    
    public static String BREAKPOINT_HIT_UPDATE_POLICY_ID = "org.eclipse.cdt.dsf.debug.ui.viewmodel.update.breakpointHitUpdatePolicy";  //$NON-NLS-1$
    
    @Override
    public String getID() {
        return BREAKPOINT_HIT_UPDATE_POLICY_ID;
    }
    
    @Override
    public String getName() {
        return MessagesForVMUpdate.BreakpointHitUpdatePolicy_name;
    }
    
    @Override
    public IElementUpdateTester getElementUpdateTester(Object event) {
        if(event instanceof ISuspendedDMEvent) {
            ISuspendedDMEvent suspendedEvent = (ISuspendedDMEvent)event; 
            if(suspendedEvent.getReason().equals(StateChangeReason.BREAKPOINT)) {
                return super.getElementUpdateTester(REFRESH_EVENT);
            }
        }
        return super.getElementUpdateTester(event);
    }
}
