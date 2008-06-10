/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.tests.pda.util;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.debug.core.DebugPlugin;

/**
 * 
 */
public class Launching {
    
    public static Process launchPDA(String pdaProgram, int requestPort, int eventPort) throws CoreException {
        Assert.assertTrue("Invalid request port", requestPort > 0);
        Assert.assertTrue("Invalid event port", eventPort > 0);

        List<String> commandList = new ArrayList<String>();

        // Get Java VM path
        String javaVMHome = System.getProperty("java.home");
        String javaVMExec = javaVMHome + File.separatorChar + "bin" + File.separatorChar + "java";
        File exe = new File(javaVMExec);
        if (!exe.exists()) {
            throw new CoreException(new Status(
                IStatus.ERROR, PDAPlugin.PLUGIN_ID, 0,
                MessageFormat.format("Specified java VM executable {0} does not exist.", new Object[]{javaVMExec}), null));
        }
        commandList.add(javaVMExec);

        commandList.add("-cp");
        commandList.add(File.pathSeparator + PDAPlugin.getFileInPlugin(new Path("bin")));

        commandList.add("org.eclipse.dd.examples.pdavm.PDAVirtualMachine");

        commandList.add(pdaProgram);
        
        // if in debug mode, add debug arguments - i.e. '-debug requestPort eventPort'

        commandList.add("-debug");
        commandList.add("" + requestPort);
        commandList.add("" + eventPort);
        
        String[] commandLine = commandList.toArray(new String[commandList.size()]);

        return DebugPlugin.exec(commandLine, null);
    }
    
    /**
     * Returns a free port number on localhost, or -1 if unable to find a free port.
     * 
     * @return a free port number on localhost, or -1 if unable to find a free port
     */
    public static int findFreePort() {
        ServerSocket socket= null;
        try {
            socket= new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) { 
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return -1;      
    }       

}
