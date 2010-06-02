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
public class Test6 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest6.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testWatchPoints() throws Throwable {
        expectEvent("started 1");
        sendCommand("watch inner::a 1");
        sendCommand("watch main::a 2");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("vmsuspended 1 watch write main::a");
        sendCommand("stack 1", fProgram + "|4|main|a|b");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("vmsuspended 1 watch read inner::a");
        sendCommand("stack 1", fProgram + "|10|main|a|b#" + fProgram + "|25|inner|a|c");
        sendCommand("watch inner::a 0");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("exited 1");        
        expectEvent("terminated");
    }
    
    @Test
    public void testEval() throws Throwable {
        expectEvent("started 1");

        sendCommand("eval 1 test_error", "error: cannot evaluate while vm is suspended");
        
        sendCommand("set 25 0");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("suspended 1 breakpoint 25");

        sendCommand("eval 1 push%204|push%205|add");
        expectEvent("resumed 1 eval");
        expectEvent("evalresult 9");
        expectEvent("suspended 1 eval");

        sendCommand("step 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 step");
        sendCommand("stack 1", fProgram + "|10|main|a|b#" + fProgram + "|26|inner|a|c");
        sendCommand("data 1", "4|4|");
        sendCommand("eval 1 call%20other");
        expectEvent("resumed 1 eval");
        expectEvent("evalresult 15");
        expectEvent("suspended 1 eval");
        sendCommand("stack 1", fProgram + "|10|main|a|b#" + fProgram + "|26|inner|a|c");
        sendCommand("data 1", "4|4|");
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("exited 1");
        expectEvent("terminated");
    }
}
