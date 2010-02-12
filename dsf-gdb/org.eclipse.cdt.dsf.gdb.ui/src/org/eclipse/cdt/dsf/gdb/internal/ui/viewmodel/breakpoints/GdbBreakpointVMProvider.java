/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints.BreakpointVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * @since 3.0
 */
public class GdbBreakpointVMProvider extends BreakpointVMProvider {

    public GdbBreakpointVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext) {
        super(adapter, presentationContext);
    }
    
    @Override
    protected void calcFileteredBreakpoints(DataRequestMonitor<IBreakpoint[]> rm) {
        if (getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION) != null) {
            IBreakpoint[] allBreakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
            List<IBreakpoint> filteredBPs = new ArrayList<IBreakpoint>(allBreakpoints.length);
            for (IBreakpoint bp : allBreakpoints) {
                if (bp instanceof ICBreakpoint && bp.getModelIdentifier().equals(CDebugCorePlugin.PLUGIN_ID)) {
                    filteredBPs.add(bp);
                }
            }
            rm.setData( filteredBPs.toArray(new IBreakpoint[filteredBPs.size()]) );
            rm.done();
        } else {
            super.calcFileteredBreakpoints(rm);
        }
    }
}
