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
public class Test8 extends CommandControlTestsBase {

    @BeforeClass
    public static void setProgram() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("pdavm/tests/vmtest8.pda"));

        fProgram = programFile.getPath();
    }

    @Test
    public void testDropFrame() throws Throwable {
        expectEvent("started");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("stack", fProgram + "|2|main|a#" + fProgram + "|8|inner|b#" + fProgram + "|12|inner2|c");
        sendCommand("drop");
        expectEvent("resumed drop");
        expectEvent("suspended drop");
        sendCommand("stack", fProgram + "|2|main|a#" + fProgram + "|7|inner|b");
        sendCommand("step");
        expectEvent("resumed step");
        expectEvent("suspended step");
        sendCommand("stack", fProgram + "|2|main|a#" + fProgram + "|8|inner|b#" + fProgram + "|10|inner2");
        sendCommand("resume");
        expectEvent("resumed client");
        expectEvent("terminated");
    }
    
}
