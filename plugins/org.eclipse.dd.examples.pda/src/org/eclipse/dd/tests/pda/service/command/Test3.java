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
public class Test3 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest3.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testUncaughtEvents() throws Throwable {
        expectEvent("started");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("unimplemented instruction foobar");
        expectEvent("no such label zippy");
        expectEvent("terminated");
    }
    
    @Test
    public void testCaughtUnimpinstrEvents() throws Throwable {
        expectEvent("started");
        sendCommand("eventstop unimpinstr 1");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("unimplemented instruction foobar");
        expectEvent("suspended event unimpinstr");
        sendCommand("eventstop unimpinstr 0");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("unimplemented instruction foobar");
        expectEvent("no such label zippy");
        expectEvent("terminated");
    }

    @Test
    public void testCaughtNosuchlabelEvents() throws Throwable {
        expectEvent("started");
        sendCommand("eventstop nosuchlabel 1");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("unimplemented instruction foobar");
        expectEvent("no such label zippy");
        expectEvent("suspended event nosuchlabel");
        sendCommand("eventstop nosuchlabel 0");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("no such label zippy");
        expectEvent("terminated");
    }

}
