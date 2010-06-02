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
public class Test8 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest8.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testDropFrame() throws Throwable {
        expectEvent("started 1");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("stack 1", fProgram + "|2|main|a#" + fProgram + "|8|inner|b#" + fProgram + "|12|inner2|c");
        sendCommand("drop 1");
        expectEvent("vmresumed drop");
        expectEvent("vmsuspended 1 drop");
        sendCommand("stack 1", fProgram + "|2|main|a#" + fProgram + "|7|inner|b");
        sendCommand("step 1");
        expectEvent("vmresumed step");
        expectEvent("vmsuspended 1 step");
        sendCommand("stack 1", fProgram + "|2|main|a#" + fProgram + "|8|inner|b#" + fProgram + "|10|inner2");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("exited 1");
        expectEvent("terminated");
    }

    @Test
    public void testDropFrameWithThreadRC() throws Throwable {
        expectEvent("started 1");
        sendCommand("set 12 0");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("suspended 1 breakpoint 12");
        sendCommand("stack 1", fProgram + "|2|main|a#" + fProgram + "|8|inner|b#" + fProgram + "|12|inner2|c");
        sendCommand("drop 1");
        expectEvent("resumed 1 drop");
        expectEvent("suspended 1 drop");
        sendCommand("stack 1", fProgram + "|2|main|a#" + fProgram + "|7|inner|b");
        sendCommand("step 1");
        expectEvent("resumed 1 step");
        expectEvent("suspended 1 step");
        sendCommand("stack 1", fProgram + "|2|main|a#" + fProgram + "|8|inner|b#" + fProgram + "|10|inner2");
        sendCommand("clear 12");
        sendCommand("resume 1");
        expectEvent("resumed 1 client");
        expectEvent("exited 1");
        expectEvent("terminated");
    }

}
