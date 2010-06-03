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
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.breakpoints;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints.BreakpointVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints.RawBreakpointVMNode;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @since 3.0
 */
public class GdbBreakpointVMProvider extends BreakpointVMProvider {

    final private DsfSession fSession;
    
    final private DsfServicesTracker fServicesTracker;
    
    public GdbBreakpointVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext, DsfSession session) {
        super(adapter, presentationContext);
        fSession = session;
        fServicesTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
    }
    
    @Override
    public void dispose() {
        fServicesTracker.dispose();
        super.dispose();
    }
    
    @Override
    protected void calcFileteredBreakpoints(DataRequestMonitor<IBreakpoint[]> rm) {
        if (Boolean.TRUE.equals(getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION))) {
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

    @Override
    protected IVMNode createBreakpointVMNode() {
        return new RawBreakpointVMNode(this);
    }
    
    @Override
    public void getBreakpointsForDebugContext(ISelection debugContext, final DataRequestMonitor<IBreakpoint[]> rm) {
        IExecutionDMContext _execCtx = null;
        if (debugContext instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection)debugContext).getFirstElement();
            if (element instanceof IDMVMContext) {
                _execCtx = DMContexts.getAncestorOfType( ((IDMVMContext)element).getDMContext(), IExecutionDMContext.class);
            }
        }
        
        if (_execCtx == null || !fSession.getId().equals(_execCtx.getSessionId())) {
            rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Debug context doesn't contain a thread", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        final IExecutionDMContext execCtx = _execCtx;
        
        try {
            fSession.getExecutor().execute(new DsfRunnable() {
                public void run() {
                    IBreakpointsExtension bpService = fServicesTracker.getService(IBreakpointsExtension.class);
                    if (bpService == null) {
                        rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Breakpoints service not available", null)); //$NON-NLS-1$
                        rm.done();                    
                        return;
                    }
                    bpService.getExecutionContextBreakpoints(
                        execCtx, 
                        new DataRequestMonitor<IBreakpoints.IBreakpointDMContext[]>(fSession.getExecutor(), rm) {
                            @Override
                            protected void handleSuccess() {
                                MIBreakpointsManager bpManager = fServicesTracker.getService(MIBreakpointsManager.class);
                                if (bpManager == null) {
                                    rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Breakpoints service not available", null)); //$NON-NLS-1$
                                    rm.done();                    
                                    return;
                                }
                                
                                IBreakpoint bp = null;
    
                                
                                if (getData().length > 0) {
                                    bp = bpManager.findPlatformBreakpoint(getData()[0]);
                                }
                                
                                if (bp != null) {
                                    rm.setData(new IBreakpoint[] { bp });
                                } else {
                                    rm.setData(new IBreakpoint[0]);
                                }
                                rm.done();
                            }
                        });
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Request for monitor: '" + toString() + "' resulted in a rejected execution exception.", e)); //$NON-NLS-1$ //$NON-NLS-2$);
            rm.done();
        }
    }
}
