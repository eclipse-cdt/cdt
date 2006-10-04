/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.tm.terminal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * A simple logger class.  Every method in this class is static, so they can be called
 * from both class and instance methods.  To use this class, write code like this: <p>
 *
 * <pre>
 *      Logger.log("something has happened");
 *      Logger.log("counter is " + counter);
 * </pre>
 *
 * @author Fran Litterio <francis.litterio@windriver.com>
 */
public final class Logger
{
    /**
     * UNDER CONSTRUCTION
     */
    private static PrintStream logStream;

    static
    {
        String logFile = null;
        File logDirWindows = new File("C:\\wblogs"); //$NON-NLS-1$
        File logDirUNIX = new File("/tmp/wblogs"); //$NON-NLS-1$

        if (logDirWindows.isDirectory())
        {
            logFile = logDirWindows + "\\wbterminal.log"; //$NON-NLS-1$
        }
        else if (logDirUNIX.isDirectory())
        {
            logFile = logDirUNIX + "/wbterminal.log"; //$NON-NLS-1$
        }

        if (logFile != null)
        {
            try
            {
                logStream = new PrintStream(new FileOutputStream(logFile, true));
            }
            catch (Exception ex)
            {
                logStream = System.err;
                logStream.println("Exception when opening log file -- logging to stderr!"); //$NON-NLS-1$
                ex.printStackTrace(logStream);
            }
        }
    }

    /**
     * Logs the specified message.  Do not append a newline to parameter
     * <i>message</i>.  This method does that for you.
     *
     * @param message           A String containing the message to log.
     */
    public static final void log(String message)
    {
        if (logStream != null)
        {
            // Read my own stack to get the class name, method name, and line number of
            // where this method was called.

            StackTraceElement caller = new Throwable().getStackTrace()[1];
            int lineNumber = caller.getLineNumber();
            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            className = className.substring(className.lastIndexOf('.') + 1);

            logStream.println(className + "." + methodName + ":" + lineNumber + ": " + message);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            logStream.flush();
        }
    }

    /**
     * Writes a stack trace for an exception to both Standard Error and to the log file.
     */
    public static final void logException(Exception ex)
    {
        // Read my own stack to get the class name, method name, and line number of
        // where this method was called.

        StackTraceElement caller = new Throwable().getStackTrace()[1];
        int lineNumber = caller.getLineNumber();
        String className = caller.getClassName();
        String methodName = caller.getMethodName();
        className = className.substring(className.lastIndexOf('.') + 1);

        PrintStream tmpStream = System.err;

        if (logStream != null)
        {
            tmpStream = logStream;
        }

        tmpStream.println(className + "." + methodName + ":" + lineNumber + ": " +  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                          "Caught exception: " + ex); //$NON-NLS-1$
        ex.printStackTrace(tmpStream);
    }
}
