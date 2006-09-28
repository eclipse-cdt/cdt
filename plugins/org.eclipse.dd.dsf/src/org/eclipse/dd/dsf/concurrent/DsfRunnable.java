/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.concurrent;

/**
 * A DSF-instrumented alternative to the Runnable interface.    
 * <p>
 * While it is perfectly fine for clients to call the DSF executor with
 * an object only implementing the Runnable interface, the DsfRunnable 
 * contains fields and methods that used for debugging and tracing when 
 * tracing is enabled.
 */
abstract public class DsfRunnable implements Runnable {
    private StackTraceElement []  fStackTrace = null; 
    private Runnable fSubmittedBy = null;
    
    public DsfRunnable() {
        // Use assertion flag (-ea) to jre to avoid affecting performance when not debugging.
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled || DefaultDsfExecutor.DEBUG_EXECUTOR) {
            fStackTrace = Thread.currentThread().getStackTrace();
        }
    }
    
    public  String toString () {
        StringBuilder builder = new  StringBuilder() ;
        // If assertions are not turned on.
        builder.append(super.toString());
        if (fStackTrace != null) {
            builder.append ( "\n\tCreated at" ) ;
            
            // ommit the first elements in the stack trace
            for  (int i = 3; i < fStackTrace.length && i < 13; i++) {
                if (i > 3) builder.append ( "\tat " ) ;
                builder.append( fStackTrace [ i ] .toString ()) ;
                builder.append( "\n" ) ;
            }
            if (fStackTrace.length > 13) {
                builder.append("\t at ...");
            }
        }
        if (fSubmittedBy != null) {
            builder.append("Submitted by \n");
            builder.append(fSubmittedBy.toString());
        }
        
        return  builder.toString();
    } 
    
    void setSubmittedBy(Runnable runnable) {
        fSubmittedBy = runnable;
    }
}
