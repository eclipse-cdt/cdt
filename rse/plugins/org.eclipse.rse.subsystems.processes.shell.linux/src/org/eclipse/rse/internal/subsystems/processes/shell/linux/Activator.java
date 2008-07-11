/*******************************************************************************
 * Copyright (c) 2006, 2008 MontaVista Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Yu-Fen Kuo       (MontaVista) - initial API and implementation
 * Martin Oberhuber (Wind River) - [refactor] "shell" instead of "ssh" everywhere 
 * Anna Dushistova  (MontaVista) - [239159] The shell process subsystem not working without the shells subsystem present for the systemType
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.processes.shell.linux;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystem;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.rse.subsystems.processes.shell.linux"; //$NON-NLS-1$

    /**
     * string to be echo'ed when running command in shell, used to indicate that
     * the command has finished running
     */
    public static String DONE_MARKUP_STRING = "--RSE:donedonedone:--"; //$NON-NLS-1$

    /**
     * command delimiter for shell
     */
    public final static String CMD_DELIMITER = ";"; //$NON-NLS-1$

    private final static String SHELL_EXIT_CMD = " exit "; //$NON-NLS-1$

    private final static String SHELL_ECHO_CMD = " echo "; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Convenience method which returns the unique identifier of this plugin.
     */
    public static String getUniqueIdentifier() {
        if (getDefault() == null) {
            // If the default instance is not yet initialized,
            // return a static identifier. This identifier must
            // match the plugin id defined in plugin.xml
            return PLUGIN_ID;
        }
        return getDefault().getBundle().getSymbolicName();
    }

    /**
     * Logs the specified status with this plug-in's log.
     * 
     * @param status
     *            status to log
     */
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    /**
     * Logs an internal error with the specified message.
     * 
     * @param message
     *            the error message to log
     */
    public static void logErrorMessage(String message) {
        log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR,
                message, null));
    }

    /**
     * Logs an internal error with the specified throwable
     * 
     * @param e
     *            the exception to be logged
     */
    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e
                .getMessage(), e));
    }

    /**
     * format the command to be sent into the shell command with the done markup
     * string and the exit string. The done markup string is needed so we can
     * tell that end of output has been reached.
     * 
     * @param cmd
     * @return formatted command string
     */
    public static String formatShellCommand(String cmd) {
        if (cmd == null || cmd.equals("")) //$NON-NLS-1$
            return cmd;
        StringBuffer formattedCommand = new StringBuffer();
        formattedCommand.append(cmd).append(CMD_DELIMITER);
        formattedCommand.append(SHELL_ECHO_CMD).append(DONE_MARKUP_STRING);
        formattedCommand.append(CMD_DELIMITER).append(SHELL_EXIT_CMD);
        return formattedCommand.toString();
    }

    /**
     * Find the first shell service associated with the host.
     * 
     * @param host the connection
     * @return shell service object, or <code>null</code> if not found.
     */
    public static IShellService getShellService(IHost host) {
    	ISubSystem ss = getSuitableSubSystem(host);
    	if (ss!=null) {
    		return (IShellService)ss.getSubSystemConfiguration().getService(host).getAdapter(IShellService.class);
    	}
        return null;
    }

    /**
     * Find the first IShellServiceSubSystem service associated with the host.
     * 
     * @param host the connection 
     * @return shell service subsystem, or <code>null</code> if not found.
     */
    public static ISubSystem getSuitableSubSystem(IHost host) {
        if (host == null)
            return null;
        ISubSystem[] subSystems = host.getSubSystems();
        IShellService ssvc = null;
        for (int i = 0; subSystems != null && i < subSystems.length; i++) {
        	IService svc = subSystems[i].getSubSystemConfiguration().getService(host);
        	if (svc!=null) {
        		ssvc = (IShellService)svc.getAdapter(IShellService.class);
        		if (ssvc != null) {
        		    return subSystems[i];	
        		}	
        	}
        }
        return null;
    }
    
    /**
     * Find the first IProcessServiceSubSystem service associated with the host.
     * 
     * @param host the connection 
     * @return shell service subsystem, or <code>null</code> if not found.
     */
    public static IProcessServiceSubSystem getProcessServiceSubSystem(IHost host) {
        if (host == null)
            return null;
        ISubSystem[] subSystems = host.getSubSystems();
        for (int i = 0; subSystems != null && i < subSystems.length; i++) {
            if (subSystems[i] instanceof IProcessServiceSubSystem) {
                return (IProcessServiceSubSystem)subSystems[i];
            }
        }
        return null;
    }

    /**
     * append the error message into a string from reading the error Stream.
     * 
     * @param errStream
     * @return error message
     */
    public static String getErrorMessage(InputStream errStream) {

        // error buffer
        StringBuffer errBuf = new StringBuffer();

        byte[] bytes = null;

        int numOfBytesRead = 0;
        try {
            int available = errStream.available();
            while (available > 0) {

                bytes = new byte[available];

                numOfBytesRead = errStream.read(bytes);

                if (numOfBytesRead > -1) {
                    errBuf.append(new String(bytes, 0, numOfBytesRead));
                } else {
                    break;
                }

                available = errStream.available();
            }

            return errBuf.toString();
        } catch (IOException e) {
            Activator.log(e);
        }
        return null;
    }
}
