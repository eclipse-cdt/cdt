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
package org.eclipse.dd.dsf.debug.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.INativeProcesses;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.INativeProcesses.IThreadDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.mi.service.MIRunControl;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.DMContextVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.DMContextVMLayoutNode.DMContextVMContext;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;


@SuppressWarnings("restriction")
public class ThreadLayoutNode extends DMContextVMLayoutNode {
    
    public ThreadLayoutNode(DsfSession session) {
        super(session, IRunControl.IExecutionDMContext.class);
    }
    
    public void hasElements(IVMContext parentVmc, final GetDataDone<Boolean> done) {
        done.setData(Boolean.TRUE);
        getExecutor().execute(done);
    }

    public void getElements(final IVMContext parentVmc, final GetDataDone<IVMContext[]> done) {
        if (getServicesTracker().getService(MIRunControl.class) == null) {
            done.setData(new IVMContext[0]);
            getExecutor().execute(done);
            return;
        }
        
        IExecutionDMContext execCtx = getServicesTracker().getService(MIRunControl.class).getExecutionDMC();
        done.setData(dmcs2vmcs(parentVmc, new IExecutionDMContext[] { execCtx }));
        getExecutor().execute(done);
    }
    
    public void retrieveLabel(IVMContext vmc, final ILabelRequestMonitor result) {
        IExecutionDMContext dmc = (IExecutionDMContext)((DMContextVMContext)vmc).getDMC();
        INativeProcesses processes = getServicesTracker().getService(INativeProcesses.class);

        String imageKey = null;
        IRunControl rc = getServicesTracker().getService(IRunControl.class);
        if (rc.isSuspended(dmc)) {
            imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
        } else {
            imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
        }            
        result.setImageDescriptors(new ImageDescriptor[] { DebugUITools.getImageDescriptor(imageKey) });
        
        processes.getModelData(
            processes.getThreadForExecutionContext(dmc), 
            new GetDataDone<IThreadDMData>() { 
                public void run() {
                    if (!getStatus().isOK() || !getData().isValid()) {
                        result.done();
                        return;
                    }
                    result.setLabels(new String[] { getData().getName() });
                    result.done();
                }
            });
    }

    public boolean hasDeltaFlagsForDMEvent(IDMEvent e) {
        // This node generates delta if the timers have changed, or if the 
        // label has changed.
        return e instanceof IRunControl.IResumedDMEvent ||
               e instanceof IRunControl.ISuspendedDMEvent ||
               super.hasDeltaFlagsForDMEvent(e);
    }

    public void buildDeltaForDMEvent(final IDMEvent e, final VMDelta parent, final Done done) {
        if (e instanceof IRunControl.IResumedDMEvent) {
            // Add delta indicating that the VMC for the given timer context 
            // has changed.
            parent.addNode(
                new DMContextVMContext(parent.getVMC(), e.getDMContext()), 
                IModelDelta.STATE);
        } else if (e instanceof IRunControl.ISuspendedDMEvent) {
            parent.addNode(
                new DMContextVMContext(parent.getVMC(), e.getDMContext()), 
                IModelDelta.STATE);
        }
        super.buildDeltaForDMEvent(e, parent, done);
    }
}
