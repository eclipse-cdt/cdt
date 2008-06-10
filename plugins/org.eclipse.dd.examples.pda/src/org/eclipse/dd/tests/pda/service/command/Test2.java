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
package org.eclipse.dd.tests.pda.service.command;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.dd.examples.pda.PDAPlugin;
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
        expectEvent("started");
        // test step
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        // test breakpoint
        sendCommand("set 4");
        sendCommand("data", "6|");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("suspended breakpoint 4");
        // test data stack
        sendCommand("data", "6|7|8|9|");
        sendCommand("popdata");
        sendCommand("data", "6|7|8|");
        sendCommand("pushdata 11");
        sendCommand("data", "6|7|8|11|");
        sendCommand("setdata 1 2");
        sendCommand("data", "6|2|8|11|");
        // test call stack
        sendCommand("set 12");
        sendCommand("set 19");
        sendCommand("stepreturn");
        expectEvent("resumed client");
        expectEvent("suspended breakpoint 12");
        sendCommand("clear 19");
        sendCommand("stack", fProgram + "|6|main#" + fProgram + "|18|sub1|m|n#" + fProgram + "|12|sub2" );
        sendCommand("stepreturn");
        expectEvent("resumed client");
        expectEvent("suspended step");
        sendCommand("stack", fProgram + "|6|main#" + fProgram + "|18|sub1|m|n#" + fProgram + "|13|sub2" );
        sendCommand("stepreturn");
        expectEvent("resumed client");
        expectEvent("suspended step");
        sendCommand("stack", fProgram + "|6|main#" + fProgram + "|22|sub1|m|n" );
        sendCommand("set 6");
        sendCommand("stepreturn");
        expectEvent("resumed client");
        expectEvent("suspended breakpoint 6");
        // test set and clear
        sendCommand("set 27");
        sendCommand("set 29");
        sendCommand("set 33");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("suspended breakpoint 33");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("suspended breakpoint 27");
        sendCommand("clear 33");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("suspended breakpoint 29");
        // test var and setvar
        sendCommand("set 47");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("suspended breakpoint 47");
        sendCommand("var 1 b", "4");
        sendCommand("var 2 b", "2");
        sendCommand("var 1 a", "0");
        sendCommand("setvar 1 a 99");
        sendCommand("data", "6|2|8|11|27|1|4|");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("var 1 a", "99");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("data", "6|2|8|11|27|1|4|99|");
        // test exit
        sendCommand("exit");
        expectEvent("terminated");
    }
}
