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
package org.eclipse.cdt.tests.dsf.pda.service.command;

import java.io.File;

import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class Test9 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest9.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testThreadsWithVMRC() throws Throwable {
        expectEvent("started 1");
        sendCommand("state", "client");
        sendCommand("state 1", "vm");
        
        // Check error responses
        sendCommand("vmsuspend", "error: vm already suspended");
        sendCommand("resume 1", "error: cannot resume thread when vm is suspended");

        // Run to thread create routine
        sendCommand("threads", "1");
        sendCommand("set 2 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("vmsuspended 1 breakpoint 2");
        sendCommand("state", "1 breakpoint 2");
        sendCommand("state 1", "vm");
        
        // Step over first thread create
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("started 2");
        expectEvent("vmsuspended 1 step");
        sendCommand("state", "1 step");
        sendCommand("state 1", "vm");
        sendCommand("threads", "1 2");
        sendCommand("stack 1", fProgram + "|3|main");
        sendCommand("stack 2", fProgram + "|9|foo");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("stack 1", fProgram + "|4|main");
        sendCommand("stack 2", fProgram + "|10|foo");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectOutput("thread_created");
        expectEvent("vmsuspended 1 breakpoint 2");
        
        // Step over second thread create
        sendCommand("step 2");
        expectEvent("vmresumed step");
        expectEvent("started 3");
        expectEvent("vmsuspended 2 step");
        sendCommand("threads", "1 2 3");
        sendCommand("stack 1", fProgram + "|3|main");
        sendCommand("stack 2", fProgram + "|13|foo#" + fProgram + "|15|inner");
        sendCommand("stack 3", fProgram + "|9|foo");
        sendCommand("step 3");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 3 step");
        sendCommand("stack 1", fProgram + "|4|main");
        sendCommand("stack 2", fProgram + "|13|foo#" + fProgram + "|16|inner|b");
        sendCommand("stack 3", fProgram + "|10|foo");

        // Run to the end and watch threads starting/exiting.
        sendCommand("clear 2");
        sendCommand("vmresume");  
        expectOutput("thread_created");
        expectEvent("vmresumed client");       
        expectEvent("started 4");
        expectEvent("exited 2");
        expectEvent("started 5");       
        expectEvent("exited 3");
        expectEvent("started 6");       
        expectEvent("exited 4");
        expectEvent("exited 1");
        expectEvent("exited 5");
        expectEvent("exited 6");
        expectEvent("terminated");        
    }

    @Test
    public void testThreadsWithThreadRC() throws Throwable {
        expectEvent("started 1");
        
        // Check error responses for thread run control
        sendCommand("set 1 0");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("suspended 1 breakpoint 1");
        sendCommand("state", "running");
        sendCommand("state 1", "breakpoint 1");
        
        sendCommand("resume", "error: invalid thread");        
        sendCommand("vmresume", "error: vm already running");        
        sendCommand("clear 1");
        sendCommand("suspend 1", "error: thread already suspended");
        sendCommand("vmsuspend");
        expectEvent("vmsuspended client");
        sendCommand("state", "client");
        sendCommand("state 1", "vm");
        sendCommand("suspend 1", "error: vm already suspended");
        sendCommand("resume 1", "error: cannot resume thread when vm is suspended");        
        
        // Create breakpoints at thread create and thread entry point.
        sendCommand("set 2 0");
        sendCommand("set 10 0");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("suspended 1 breakpoint 2");

        // Create first thread, and run it to completion
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("started 2");
        expectEvent("suspended 2 breakpoint 10");
        expectEvent("suspended 1 breakpoint 2");
        sendCommand("state 1", "breakpoint 2");
        sendCommand("state 2", "breakpoint 10");
        sendCommand("threads", "1 2");
        sendCommand("resume 2");
        expectEvent("resumed 2 client");
        expectEvent("exited 2");
        sendCommand("threads", "1");

        // Create second thread, step it
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("started 3");
        expectEvent("suspended 3 breakpoint 10");
        expectEvent("suspended 1 breakpoint 2");
        sendCommand("threads", "1 3");
        sendCommand("stack 1", fProgram + "|2|main");
        sendCommand("stack 3", fProgram + "|10|foo");
        sendCommand("step 3");
        expectEvent("resumed 3 step");
        expectEvent("suspended 3 step");
        sendCommand("state 1", "breakpoint 2");
        sendCommand("state 3", "step");
        sendCommand("stack 1", fProgram + "|2|main");
        sendCommand("stack 3", fProgram + "|11|foo");
        
        // Create the rest of threads
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("started 4");
        expectEvent("suspended 4 breakpoint 10");
        expectEvent("suspended 1 breakpoint 2");
        sendCommand("threads", "1 3 4");
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("started 5");
        expectEvent("suspended 5 breakpoint 10");
        expectEvent("suspended 1 breakpoint 2");
        sendCommand("threads", "1 3 4 5");
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        
        // Main thread exits
        expectEvent("started 6");
        expectEvent("suspended 6 breakpoint 10");
        expectEvent("exited 1");
        sendCommand("threads", "3 4 5 6");
        
        // Exit
        sendCommand("exit");
        expectEvent("terminated");
    }

}
