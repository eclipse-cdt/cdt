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
public class Test2 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest2.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testCommonDebugCommands() throws Throwable {
        expectEvent("started 1");
        // test step
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        // test breakpoint
        sendCommand("set 4 1");
        sendCommand("data 1", "6|");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("vmsuspended 1 breakpoint 4");
        // test data stack
        sendCommand("data 1", "6|7|8|9|");
        sendCommand("popdata 1");
        sendCommand("data 1", "6|7|8|");
        sendCommand("pushdata 1 11");
        sendCommand("data 1", "6|7|8|11|");
        sendCommand("setdata 1 1 2");
        sendCommand("data 1", "6|2|8|11|");
        // test call stack
        sendCommand("set 12 1");
        sendCommand("set 19 1");
        sendCommand("stepreturn 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 breakpoint 12");
        sendCommand("clear 19");
        sendCommand("stack 1", fProgram + "|6|main#" + fProgram + "|18|sub1|m|n#" + fProgram + "|12|sub2" );
        sendCommand("stackdepth 1", "3");
        sendCommand("frame 1 0", fProgram + "|6|main");
        sendCommand("frame 1 1", fProgram + "|18|sub1|m|n");
        sendCommand("frame 1 2", fProgram + "|12|sub2" );
        sendCommand("stepreturn 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("stack 1", fProgram + "|6|main#" + fProgram + "|18|sub1|m|n#" + fProgram + "|13|sub2" );
        sendCommand("stepreturn 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("stack 1", fProgram + "|6|main#" + fProgram + "|22|sub1|m|n" );
        sendCommand("set 6 1");
        sendCommand("stepreturn 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 breakpoint 6");
        // test set and clear
        sendCommand("set 27 1");
        sendCommand("set 29 1");
        sendCommand("set 33 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("vmsuspended 1 breakpoint 33");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("vmsuspended 1 breakpoint 27");
        sendCommand("clear 33");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("vmsuspended 1 breakpoint 29");
        // test var and setvar
        sendCommand("set 47 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("vmsuspended 1 breakpoint 47");
        sendCommand("var 1 1 b", "4");
        sendCommand("var 1 2 b", "2");
        sendCommand("var 1 1 a", "0");
        sendCommand("setvar 1 1 a 99");
        sendCommand("data 1", "6|2|8|11|27|1|4|");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("var 1 1 a", "99");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("data 1", "6|2|8|11|27|1|4|99|");
        sendCommand("var 1 1 x", "error: variable undefined");
        sendCommand("setvar 1 1 x 100");
        sendCommand("var 1 1 x", "100");
        // test exit
        sendCommand("exit");
        expectEvent("terminated");
    }
    
    @Test
    public void testCommonDebugCommandsWithThreadRC() throws Throwable {
        expectEvent("started 1");
        // test breakpoint
        sendCommand("set 3 0");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("suspended 1 breakpoint 3");
        sendCommand("data 1", "6|7|8|");
        // test step
        sendCommand("step 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 step");
        // test data stack
        sendCommand("data 1", "6|7|8|9|");
        sendCommand("popdata 1");
        sendCommand("data 1", "6|7|8|");
        sendCommand("pushdata 1 11");
        sendCommand("data 1", "6|7|8|11|");
        sendCommand("setdata 1 1 2");
        sendCommand("data 1", "6|2|8|11|");
        // test call stack
        sendCommand("set 12 0");
        sendCommand("set 19 0");
        sendCommand("stepreturn 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 breakpoint 12");
        sendCommand("clear 19");
        sendCommand("stack 1", fProgram + "|6|main#" + fProgram + "|18|sub1|m|n#" + fProgram + "|12|sub2" );
        sendCommand("stackdepth 1", "3");
        sendCommand("frame 1 0", fProgram + "|6|main");
        sendCommand("frame 1 1", fProgram + "|18|sub1|m|n");
        sendCommand("frame 1 2", fProgram + "|12|sub2" );
        sendCommand("stepreturn 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 step");
        sendCommand("stack 1", fProgram + "|6|main#" + fProgram + "|18|sub1|m|n#" + fProgram + "|13|sub2" );
        sendCommand("stepreturn 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 step");
        sendCommand("stack 1", fProgram + "|6|main#" + fProgram + "|22|sub1|m|n" );
        sendCommand("set 6 0");
        sendCommand("stepreturn 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 breakpoint 6");
        // test set and clear
        sendCommand("set 27 0");
        sendCommand("set 29 0");
        sendCommand("set 33 0");
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("suspended 1 breakpoint 33");
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("suspended 1 breakpoint 27");
        sendCommand("clear 33");
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("suspended 1 breakpoint 29");
        // test var and setvar
        sendCommand("set 47 0");
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("suspended 1 breakpoint 47");
        sendCommand("var 1 1 b", "4");
        sendCommand("var 1 2 b", "2");
        sendCommand("var 1 1 a", "0");
        sendCommand("setvar 1 1 a 99");
        sendCommand("data 1", "6|2|8|11|27|1|4|");
        sendCommand("step 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 step");
        sendCommand("var 1 1 a", "99");
        sendCommand("step 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 step");
        sendCommand("data 1", "6|2|8|11|27|1|4|99|");
        // test exit
        sendCommand("exit");
        expectEvent("terminated");
    }

}
