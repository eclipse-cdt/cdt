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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.debug.core.DebugPlugin;

/**
 * 
 */
public class Launching {
    public static String getPerlPath() {
        // Perl executable
        IValueVariable perl = VariablesPlugin.getDefault().getStringVariableManager().getValueVariable(PDAPlugin.VARIALBE_PERL_EXECUTABLE);
        Assert.assertNotNull("Perl executable location undefined. Check value of ${dsfPerlExecutable}.", perl); 

        String path = perl.getValue();
        Assert.assertNotNull("Perl executable location undefined. Check value of ${dsfPerlExecutable}.", path); 
        Assert.assertTrue(
            MessageFormat.format("Specified Perl executable {0} does not exist. Check value of $dsfPerlExecutable.", new Object[]{path}), 
            new File(path).exists());

        return path;
    }
    
    public static Process launchPDA(String pdaProgram, int requestPort, int eventPort) throws CoreException {
        Assert.assertTrue("Invalid request port", requestPort > 0);
        Assert.assertTrue("Invalid event port", eventPort > 0);

        List<String> commandList = new ArrayList<String>();

        commandList.add(getPerlPath());
       
        File pdaVM = PDAPlugin.getFileInPlugin(new Path("pdavm/pda.pl"));
        Assert.assertNotNull("File " + pdaVM + " not found in plugin.", pdaVM);
        commandList.add(pdaVM.getAbsolutePath());

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
