/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.timers;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.service.DsfSession;

/**
 * Startup sequence for the timers session.  With only two services, this is 
 * a very simple sequence.  Last step creates the first timer and alarm.
 */
class ServicesStartupSequence extends Sequence {

    DsfSession fSession;
    private TimerService fTimerService = null;
    private AlarmService fAlarmService = null;
    

    ServicesStartupSequence(DsfSession session) {
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
                fTimerService.startTimer();
                fAlarmService.createAlarm(5);
                requestMonitor.done();
            }}
    };
    
    @Override
    public Step[] getSteps() { return fSteps; }
}
