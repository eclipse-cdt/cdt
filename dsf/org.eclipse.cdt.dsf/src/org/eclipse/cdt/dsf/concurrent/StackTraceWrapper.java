/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems and others.
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
        StringBuilder builder = new StringBuilder(fStackTraceElements.length * 30);
        for (int i = 0; i < fStackTraceElements.length && i < 10; i++) {
            builder.append(fStackTraceElements[i]);
            if (i < fStackTraceElements.length && i < 10) builder.append("\n       at "); //$NON-NLS-1$
        }
        return builder.toString();
    }
}
