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
 * an object only implementing the Runnable interface, the DsfRunnable is a 
 * place holder for future tracing enhancments for DSF.  
 */
abstract public class DsfRunnable implements Runnable {
    private StackTraceElement []  fStackTrace = null; 
    
    public DsfRunnable() {
        // Use assertion flag (-ea) to jre to avoid affecting performance when not debugging.
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            fStackTrace = Thread.currentThread().getStackTrace();
        }
    }
    
    public  String toString () {
        // If assertions are not turned on.
        if (fStackTrace == null) return super.toString();
        
        StringBuilder builder = new  StringBuilder() ;
        // ommit the first elements in the stack trace
        for  ( int  i= 3 ; i < fStackTrace.length; i++ ) {
            builder.append ( "\tat " ) ;
            builder.append ( fStackTrace [ i ] .toString ()) ;
            builder.append ( "\n" ) ;
        }
        return  builder.toString () ;
    } 
}
