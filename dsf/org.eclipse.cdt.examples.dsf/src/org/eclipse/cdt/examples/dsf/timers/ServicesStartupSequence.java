/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.timers;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Startup sequence for the timers session.  With only two services, this is 
 * a very simple sequence. 
 */
public class ServicesStartupSequence extends Sequence {

    final private DsfSession fSession;
    
    // The reference to the services are saved to use in the last step.
    private TimerService fTimerService = null;
    private AlarmService fAlarmService = null;
    

    public ServicesStartupSequence(DsfSession session) {
        super(session.getExecutor());
        fSession = session;
    }

    Step[] fSteps = new Step[] {
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                fTimerService = new TimerService(fSession);
                fTimerService.initialize(requestMonitor);
            }},
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                fAlarmService = new AlarmService(fSession);
                fAlarmService.initialize(requestMonitor);
            }},
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Create the first timer and trigger.
                fTimerService.startTimer();
                fAlarmService.createTrigger(5);
                requestMonitor.done();
            }}
    };
    
    @Override
    public Step[] getSteps() { return fSteps; }
}
