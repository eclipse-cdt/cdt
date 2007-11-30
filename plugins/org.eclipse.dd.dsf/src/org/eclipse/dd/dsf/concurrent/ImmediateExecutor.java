/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.concurrent;

import java.util.concurrent.Executor;

/**
 * Executor that executes a runnable immediately as it is submitted.  This 
 * executor is useful for clients that need to create <code>RequestMonitor</code> 
 * objects, but which do not have their own executor.
 * @see RequestMonitor
 */
public class ImmediateExecutor implements Executor {
    private static ImmediateExecutor fInstance = new ImmediateExecutor();
    
    private static Executor getInstance() {
        return fInstance;
    }
    
    public void execute(Runnable command) {
        command.run();
    }
}
