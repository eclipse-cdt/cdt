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
package org.eclipse.dd.dsf.debug.ui.viewmodel.launch;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode.IRootVMC;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;

/**
 * Layout node for the standard platform debug model IProcess object. This 
 * node requires that an ILaunch object be found as an ancestor of this node.  
 * It does not implement the label provider functionality, so the default 
 * adapters should be used to retrieve the label.  
 */
@SuppressWarnings("restriction")
public class StandardProcessLayoutNode extends AbstractVMLayoutNode {
    
    /**
     * VMC element implementation, it is a proxy for the IProcess class, to 
     * allow the standard label adapter to be used with this object. 
     */
    private class VMC implements IVMContext, IProcess
    {
        private final IVMContext fParentVmc;
        private final IProcess fProcess;
        
        VMC(IVMContext parentVmc, IProcess process) {
            fParentVmc = parentVmc;
            fProcess = process;
        }
        
        public IVMContext getParent() { return fParentVmc; }
        public IVMLayoutNode getLayoutNode() { return StandardProcessLayoutNode.this; }        
        @SuppressWarnings("unchecked") public Object getAdapter(Class adapter) { return fProcess.getAdapter(adapter); }
        public String toString() { return "IProcess " + fProcess.toString(); } //$NON-NLS-1$

        public String getAttribute(String key) { return fProcess.getAttribute(key); }
        public int getExitValue() throws DebugException { return fProcess.getExitValue(); }
        public String getLabel() { return fProcess.getLabel(); }
        public ILaunch getLaunch() { return fProcess.getLaunch(); }
        public IStreamsProxy getStreamsProxy() { return fProcess.getStreamsProxy(); }
        public void setAttribute(String key, String value) { fProcess.setAttribute(key, value); }
        public boolean canTerminate() { return fProcess.canTerminate(); }
        public boolean isTerminated() { return fProcess.isTerminated(); }
        public void terminate() throws DebugException { fProcess.terminate(); }
        
        public boolean equals(Object other) { return fProcess.equals(other); }
        public int hashCode() { return fProcess.hashCode(); }
    }

    public StandardProcessLayoutNode(DsfExecutor executor) {
        super(executor);
    }

    // @see org.eclipse.dd.dsf.ui.viewmodel.IViewModelLayoutNode#getElements(org.eclipse.dd.dsf.ui.viewmodel.IVMContext, org.eclipse.dd.dsf.concurrent.GetDataDone)
    public void getElements(IVMContext parentVmc, GetDataDone<IVMContext[]> done) {
        ILaunch launch = findLaunch(parentVmc);
        if (launch == null) {
            /*
             * There is no launch in the parent of this node.  This means that the 
             * layout is misconfigured.  
             */
            assert false; 
            done.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Can't get list of processes, because there is no launch.", null)); //$NON-NLS-1$
            getExecutor().execute(done);
            return;
        }
        
        /*
         * Assume that the process objects are stored within the launch, and 
         * retrieve them on dispatch thread.  
         */
        IProcess[] processes = launch.getProcesses();
        IVMContext[] processVmcs = new IVMContext[processes.length];
        for (int i = 0; i < processes.length; i++) {
            processVmcs[i] = new VMC(parentVmc, processes[i]);
        }
        done.setData(processVmcs);
        getExecutor().execute(done);
    }

    // @see org.eclipse.dd.dsf.ui.viewmodel.IViewModelLayoutNode#hasElements(org.eclipse.dd.dsf.ui.viewmodel.IVMContext, org.eclipse.dd.dsf.concurrent.GetDataDone)
    public void hasElements(IVMContext parentVmc, GetDataDone<Boolean> done) {
        ILaunch launch = findLaunch(parentVmc);
        if (launch == null) {
            assert false; 
            done.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR, "Can't get list of processes, because there is no launch.", null)); //$NON-NLS-1$
            getExecutor().execute(done);
            return;
        }

        done.setData(launch.getProcesses().length != 0);
        getExecutor().execute(done);
    }

    // @see org.eclipse.dd.dsf.ui.viewmodel.IViewModelLayoutNode#retrieveLabel(org.eclipse.dd.dsf.ui.viewmodel.IVMContext, org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor)
    public void retrieveLabel(IVMContext vmc, ILabelRequestMonitor result, String[] columns) {
        
        /*
         * The implementation of IAdapterFactory that uses this node should not
         * register a label adapter for IProcess.  This will cause the default
         * label provider to be used instead, and this method should then never
         * be called.
         */
        assert false;  
        result.done();
    }

    /**
     * Recursively searches the VMC for Launch VMC, and returns its ILaunch.  
     * Returns null if an ILaunch is not found.
     */
    private ILaunch findLaunch(IVMContext vmc) {
        if (vmc == null) {
            return null;
        } else if (vmc instanceof IRootVMC || ((IRootVMC)vmc).getInputObject() instanceof ILaunch) {
            return (ILaunch)(((IRootVMC)vmc)).getInputObject();
        } else {
            return findLaunch(vmc.getParent());
        }
    }
    
    @Override
    public boolean hasDeltaFlags(Object e) {
        if (e instanceof DebugEvent) {
            DebugEvent de = (DebugEvent)e;
            return de.getSource() instanceof IProcess && 
                   (de.getKind() == DebugEvent.CHANGE || 
                    de.getKind() == DebugEvent.CREATE || 
                    de.getKind() == DebugEvent.TERMINATE);  
        }
        return super.hasDeltaFlags(e);
    }
    
    @Override
    public void buildDelta(Object e, VMDelta parent, Done done) {
        if (e instanceof DebugEvent && ((DebugEvent)e).getSource() instanceof IProcess) {
            DebugEvent de = (DebugEvent)e;
            if (de.getKind() == DebugEvent.CHANGE) {
                handleChange(de, parent);
            } else if (de.getKind() == DebugEvent.CREATE) {
                handleCreate(de, parent);
            } else if (de.getKind() == DebugEvent.TERMINATE) {
                handleTerminate(de, parent);
            }
            /*
             * No other node should need to process events related to process.
             * Therefore, just invoke done, without calling super.buildDelta().
             */
            getExecutor().execute(done);
        } else {
            super.buildDelta(e, parent, done);
        }
    }
    
    protected void handleChange(DebugEvent event, VMDelta parent) {
        parent.addNode(new VMC(parent.getVMC(), (IProcess)event.getSource()), IModelDelta.STATE);
    }

    protected void handleCreate(DebugEvent event, VMDelta parent) {
        // do nothing - Launch change notification handles this
    }

    protected void handleTerminate(DebugEvent event, VMDelta parent) {
        handleChange(event, parent);
    }

}
