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
public class Test10 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest10.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testRegisters() throws Throwable {
        expectEvent("started 1");
        // run to the end of register definitions
        sendCommand("set 10 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("registers");
        expectEvent("registers");
        expectEvent("registers");
        expectEvent("registers");
        expectEvent("registers");
        expectEvent("registers");
        expectEvent("registers");
        expectEvent("registers");
        expectEvent("registers");
        expectEvent("vmsuspended 1 breakpoint 10");

        // Test the definitions commands
        sendCommand("groups", "group1|group2|");
        sendCommand("registers group1", "reg1 true|field1 0 2 |field2 2 2 zero 0 one 1 two 2 three 3 #reg2 false#");
        sendCommand("registers group2", "reg3 true#");

        // Run to the end of the program        
        sendCommand("set 37 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectOutput("1");
        expectOutput("2");
        expectOutput("0");
        expectOutput("4");
        expectOutput("0");
        expectOutput("0");
        expectOutput("2");
        expectOutput("8");
        expectEvent("vmsuspended 1 breakpoint 37");

        // Test var get/set commands
        sendCommand("var 1 1 $reg1", "8");
        sendCommand("var 1 1 $reg1.field1", "0");
        sendCommand("var 1 1 $reg1.field2", "2");
        sendCommand("setvar 1 1 $reg1.field2 3");
        sendCommand("var 1 1 $reg1.field2", "3");
        sendCommand("setvar 1 1 $reg1 1");
        sendCommand("var 1 1 $reg1", "1");
        
        // exit
        sendCommand("exit");
        expectEvent("terminated");
    }
}
