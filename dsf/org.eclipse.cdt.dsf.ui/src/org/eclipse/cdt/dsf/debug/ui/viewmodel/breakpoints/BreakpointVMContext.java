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

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * @since 2.1
 */
public class BreakpointVMContext extends AbstractVMContext {

    private final IBreakpoint fBreakpoint;
    
    public BreakpointVMContext(BreakpointVMNode node, IBreakpoint breakpoint) {
        super(node);
        fBreakpoint = breakpoint;
    }

    public IBreakpoint getBreakpoint() {
        return fBreakpoint;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        if (adapter.isInstance(fBreakpoint)) {
            return fBreakpoint;
        }
        return super.getAdapter(adapter);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof BreakpointVMContext &&
        getBreakpoint().equals( ((BreakpointVMContext)obj).getBreakpoint() ) &&
        fBreakpoint.equals(((BreakpointVMContext)obj).fBreakpoint); 
    }

    @Override
    public int hashCode() {
        return fBreakpoint.hashCode();
    }

    @Override
    public String toString() {
        return fBreakpoint.toString();
    }
}
