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
package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.service.IDsfService;

/**
 * Note: This interface is just a place-holder. 
 */
public interface IBreakpoints extends IDsfService {
   
    public class BreakpointEvent {
        public final int fLineNumber;
        public BreakpointEvent(int line) {
            fLineNumber = line; 
        }
    }    
}

