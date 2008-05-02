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
package org.eclipse.dd.examples.dsf.requestmonitor.answers;

import java.util.concurrent.Executor;

import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;

/**
 * "Hello world" example which uses an asynchronous method to print out
 * the result.  
 * <p>
 * The main method uses an immediate executor, which executes runnables
 * as soon as they are submitted, in creating its request monitor. 
 * 
 */
public class AsyncHelloWorld {

    public static void main(String[] args) {
        Executor executor = ImmediateExecutor.getInstance();
        RequestMonitor rm = new RequestMonitor(executor, null);
        asyncHelloWorld(rm);
    }

    static void asyncHelloWorld(RequestMonitor rm) {
        System.out.println("Hello world");
        RequestMonitor rm2 = new RequestMonitor(ImmediateExecutor.getInstance(), rm);
        asyncHelloWorld2(rm2);
    }
    
    static void asyncHelloWorld2(RequestMonitor rm) {
        System.out.println("Hello world 2");
        rm.done();
    }
}
