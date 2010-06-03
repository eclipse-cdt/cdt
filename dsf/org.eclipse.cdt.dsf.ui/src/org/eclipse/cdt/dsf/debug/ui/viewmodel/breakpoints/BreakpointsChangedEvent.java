/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;

/**
 * @since 2.1
 */
public class BreakpointsChangedEvent {
    public enum Type { ADDED, REMOVED, CHANGED };
    
    private final Type fType;
    private final IBreakpoint[] fBreakpoints;
    
    public BreakpointsChangedEvent(Type type, IBreakpoint[] breakpoints) {
        fType = type;
        fBreakpoints = breakpoints;
    }
    
    public Type getType() { 
        return fType;
    }
    
    public IBreakpoint[] getBreakpoints() {
        return fBreakpoints;
    }
}
