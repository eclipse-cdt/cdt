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

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.DefaultVMModelProxyStrategy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckboxModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Breakpoints VM model proxy that includes an ICheckboxModelProxy implementation.
 * 
 * @since 2.1
 */
public class BreakpointVMModelProxyStrategy extends DefaultVMModelProxyStrategy implements ICheckboxModelProxy {
    
    public BreakpointVMModelProxyStrategy(AbstractVMProvider provider, Object rootElement) {
        super(provider, rootElement);
    }
    
    @Override
	public boolean setChecked(IPresentationContext context, Object viewerInput, TreePath path, boolean checked) {
        Object lastSegment = path.getLastSegment();
        if (lastSegment instanceof IBreakpointContainer) {
            IBreakpoint[] breakpoints = ((IBreakpointContainer) lastSegment).getBreakpoints();
            for (int i = 0; i < breakpoints.length; ++i) {
                try {
                    breakpoints[i].setEnabled(checked);
                } catch (CoreException e) {
                    return false;
                }
            }
            return true;
        }
        else {
            IBreakpoint breakpoint = (IBreakpoint)DebugPlugin.getAdapter(lastSegment, IBreakpoint.class);
            if (breakpoint != null) {
                try {
                    breakpoint.setEnabled(checked);
                } catch (CoreException e) {
                    return false;
                }
                return true;
            }
        } 
        return false;
    }
}
