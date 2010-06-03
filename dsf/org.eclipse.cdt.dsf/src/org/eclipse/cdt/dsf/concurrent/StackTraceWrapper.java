/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;


/**
 * Untility class for easy pretty-printing stack traces.  Local to the 
 * concurrent package.
 * 
 * @since 1.0
 */
@Immutable
class StackTraceWrapper {
    final StackTraceElement[] fStackTraceElements;
    
    StackTraceWrapper(StackTraceElement[] elements) { fStackTraceElements = elements; }
    
    @Override
    public String toString() {
    	final int MAX_FRAMES = 10;
    	final int count = Math.min(fStackTraceElements.length, MAX_FRAMES);
        StringBuilder builder = new StringBuilder(fStackTraceElements.length * 30);
        int i = 0;
        while (true) {
           	builder.append('\t');        	
            builder.append(fStackTraceElements[i]);
            if (++i == count) {	
            	// last iteration
	            if (fStackTraceElements.length > count) {
	            	builder.append("\n\t..."); //$NON-NLS-1$
	            }
	            break;
            }
            else {
            	builder.append('\n');
            }
        }
        return builder.toString();
    }
}
