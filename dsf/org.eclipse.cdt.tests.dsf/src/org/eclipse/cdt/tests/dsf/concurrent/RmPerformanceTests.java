/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.concurrent;

import junit.framework.TestCase;

import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;

/**
 * Tests to measure the performance of the viewer updates.  
 */
public class RmPerformanceTests extends TestCase {
    
    public RmPerformanceTests(String name) {
        super(name);
    }

    public void testCreateAndGcObject() {
        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int x = 0; x < 100; x++) {
                System.gc();
                meter.start();
                for (int i = 0; i < 10000; i++) {
                    new Object();
                }
                meter.stop();
                System.gc();
            }            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }        
    }

    public void testCreateAndGcRm() {
        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int x = 0; x < 100; x++) {
                System.gc();
                meter.start();
                for (int i = 0; i < 10000; i++) {
                    RequestMonitor rm = new RequestMonitor(ImmediateExecutor.getInstance(), null);
                    rm.done();
                }
                meter.stop();
                System.gc();
            }            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }        
    }

    public void testCreateAndGcRmWithParent() {
        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        RequestMonitor parentRm = new RequestMonitor(ImmediateExecutor.getInstance(), null);
        try {
            for (int x = 0; x < 100; x++) {
                System.gc();
                meter.start();
                for (int i = 0; i < 10000; i++) {
                    RequestMonitor rm = new RequestMonitor(ImmediateExecutor.getInstance(), parentRm) {
                        @Override
                        protected void handleCompleted() {
                            // do not call parent so it can be reused
                        };
                    };
                    rm.done();
                }
                meter.stop();
                System.gc();
            }            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }        
    }
}
