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
public class Test6 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest6.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testWatchPoints() throws Throwable {
        expectEvent("started");
        sendCommand("watch inner::a 1");
        sendCommand("watch main::a 2");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("suspended watch write main::a");
        sendCommand("stack", fProgram + "|4|main|a|b");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("suspended watch read inner::a");
        sendCommand("stack", fProgram + "|10|main|a|b#" + fProgram + "|25|inner|c|a");
        sendCommand("watch inner::a 0");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("terminated");
    }
    
    @Test
    public void testEval() throws Throwable {
        expectEvent("started");
        sendCommand("set 25");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("suspended breakpoint 25");

        sendCommand("eval push%204|push%205|add");
        expectEvent("resumed client");
        expectEvent("evalresult 9");
        expectEvent("suspended eval");

        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("stack", fProgram + "|10|main|a|b#" + fProgram + "|26|inner|c|a");
        sendCommand("data", "4|4|");
        sendCommand("eval call%20other");
        expectEvent("resumed client");
        expectEvent("evalresult 15");
        expectEvent("suspended eval");
        sendCommand("stack", fProgram + "|10|main|a|b#" + fProgram + "|26|inner|c|a");
        sendCommand("data", "4|4|");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("terminated");
    }
}
