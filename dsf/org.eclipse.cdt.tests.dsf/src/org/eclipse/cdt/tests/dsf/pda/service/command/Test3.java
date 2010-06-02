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
public class Test3 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest3.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testUncaughtEvents() throws Throwable {
        expectEvent("started 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("unimplemented instruction foobar");
        expectEvent("no such label zippy");
        expectEvent("no such label swishy");
        expectEvent("exited 1");
        expectEvent("terminated");
    }

    @Test
    public void testCaughtUnimpinstrEvents() throws Throwable {
        expectEvent("started 1");
        sendCommand("eventstop unimpinstr 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("unimplemented instruction foobar");
        expectEvent("vmsuspended 1 event unimpinstr");
        sendCommand("eventstop unimpinstr 0");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("unimplemented instruction foobar");
        expectEvent("no such label zippy");
        expectEvent("no such label swishy");
        expectEvent("exited 1");
        expectEvent("terminated");
    }

    @Test
    public void testCaughtNosuchlabelEvents() throws Throwable {
        expectEvent("started 1");
        sendCommand("eventstop nosuchlabel 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("unimplemented instruction foobar");
        expectEvent("no such label zippy");
        expectEvent("vmsuspended 1 event nosuchlabel");
        sendCommand("eventstop nosuchlabel 0");
        sendCommand("set 11 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("no such label zippy");
        expectEvent("vmsuspended 1 breakpoint 11");
        sendCommand("eventstop nosuchlabel 1");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("no such label swishy");
        expectEvent("vmsuspended 1 event nosuchlabel");
        sendCommand("eventstop nosuchlabel 0");
        sendCommand("vmresume");
        expectEvent("vmresumed client");
        expectEvent("no such label swishy");
        expectEvent("exited 1");
        expectEvent("terminated");
    }

}
