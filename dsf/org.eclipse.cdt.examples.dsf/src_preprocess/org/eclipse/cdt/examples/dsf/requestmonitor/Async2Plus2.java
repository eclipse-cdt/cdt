/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
//#ifdef exercises
package org.eclipse.cdt.examples.dsf.requestmonitor;
//#else
//#package org.eclipse.cdt.examples.dsf.requestmonitor.answers;
//#endif

import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;

/**
 * Example of using a DataRequestMonitor to retrieve a result from an 
 * asynchronous method.
 */
public class Async2Plus2 {
    
    public static void main(String[] args) {
        Executor executor = ImmediateExecutor.getInstance();
        DataRequestMonitor<Integer> rm = 
            new DataRequestMonitor<Integer>(executor, null) {
                @Override
                protected void handleCompleted() {
                    System.out.println("2 + 2 = " + getData());
                }
            };
        asyncAdd(2, 2, rm);
    }

    static void asyncAdd(int value1, int value2, DataRequestMonitor<Integer> rm) {
        rm.setData(value1 + value2);
        rm.done();
    }
}
