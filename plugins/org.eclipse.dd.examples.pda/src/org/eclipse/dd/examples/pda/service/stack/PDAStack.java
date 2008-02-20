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
package org.eclipse.dd.examples.pda.service.stack;

import java.util.Hashtable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.command.CommandCache;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.service.command.PDACommandControl;
import org.eclipse.dd.examples.pda.service.command.commands.PDAFrame;
import org.eclipse.dd.examples.pda.service.command.commands.PDAStackCommand;
import org.eclipse.dd.examples.pda.service.command.commands.PDAStackCommandResult;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public class PDAStack extends AbstractDsfService implements IStack {

    private PDACommandControl fCommandControl;
    private IRunControl fRunControl;

    private CommandCache fCommandCache;

    public PDAStack(DsfSession session) {
        super(session);
    }

    @Override
    protected BundleContext getBundleContext() {
        return PDAPlugin.getBundleContext();
    }

    @Override
    public void initialize(final RequestMonitor rm) {
        super.initialize(
            new RequestMonitor(getExecutor(), rm) { 
                @Override
                protected void handleOK() {
                    doInitialize(rm);
                }});
    }

    private void doInitialize(final RequestMonitor rm) {
        fCommandControl = getServicesTracker().getService(PDACommandControl.class);
        fRunControl = getServicesTracker().getService(IRunControl.class);
        fCommandCache = new CommandCache(fCommandControl);

        getSession().addServiceEventListener(this, null);
        
        register(new String[]{IStack.class.getName(), PDAStack.class.getName()}, new Hashtable<String,String>());
        
        rm.done();
    }

    @Override
    public void shutdown(final RequestMonitor rm) {
        getSession().removeServiceEventListener(this);
        fCommandCache.reset();
        super.shutdown(rm);
    }

    public void getArguments(IFrameDMContext frameCtx, DataRequestMonitor<IVariableDMContext[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.NOT_SUPPORTED, "PDA debugger does not support function arguments.", null)); //$NON-NLS-1$
        rm.done();
    }

    public void getFrameData(final IFrameDMContext frameCtx, final DataRequestMonitor<IFrameDMData> rm) {
        if ( !(frameCtx instanceof FrameDMContext) ) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context " + frameCtx, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        fCommandCache.execute(
            new PDAStackCommand(fCommandControl.getDMContext()),
            new DataRequestMonitor<PDAStackCommandResult>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    int frameId = getData().fFrames.length - frameCtx.getLevel() - 1;
                    if (frameId < 0) {
                        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid frame level " + frameCtx, null)); //$NON-NLS-1$
                        rm.done();
                        return;
                    }
                    
                    rm.setData(new FrameDMData(getData().fFrames[frameId]));
                    rm.done();
                }
            });
    }

    public void getFrames(IDMContext context, final DataRequestMonitor<IFrameDMContext[]> rm) {
        final IExecutionDMContext execCtx = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
        if (execCtx == null) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context " + context, null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        fCommandCache.execute(
            new PDAStackCommand(fCommandControl.getDMContext()),
            new DataRequestMonitor<PDAStackCommandResult>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    IFrameDMContext[] frameCtxs = new IFrameDMContext[getData().fFrames.length];
                    for (int i = 0; i < getData().fFrames.length; i++) {
                        frameCtxs[i] = new FrameDMContext(getSession().getId(), execCtx, i);
                    }
                    rm.setData(frameCtxs);
                    rm.done();
                }
            });
    }

    public void getLocals(final IFrameDMContext frameCtx, final DataRequestMonitor<IVariableDMContext[]> rm) {
        if ( !(frameCtx instanceof FrameDMContext) ) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context " + frameCtx, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        fCommandCache.execute(
            new PDAStackCommand(fCommandControl.getDMContext()),
            new DataRequestMonitor<PDAStackCommandResult>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    int frameId = getData().fFrames.length - frameCtx.getLevel() - 1;
                    if (frameId < 0) {
                        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid frame level " + frameCtx, null)); //$NON-NLS-1$
                        rm.done();
                        return;
                    }

                    
                    PDAFrame pdaFrame = getData().fFrames[frameId];
                    IVariableDMContext[] variableCtxs = new IVariableDMContext[pdaFrame.fVariables.length];
                    for (int i = 0; i < pdaFrame.fVariables.length; i++) {
                        variableCtxs[i] = new VariableDMContext(getSession().getId(), frameCtx, pdaFrame.fVariables[i]);
                    }
                    rm.setData(variableCtxs);
                    rm.done();
                }
            });

    }

    public void getStackDepth(IDMContext context, int maxDepth, final DataRequestMonitor<Integer> rm) {
        fCommandCache.execute(
            new PDAStackCommand(fCommandControl.getDMContext()),
            new DataRequestMonitor<PDAStackCommandResult>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    rm.setData(getData().fFrames.length);
                    rm.done();
                }
            });
    }

    public void getTopFrame(IDMContext context, final DataRequestMonitor<IFrameDMContext> rm) {
        final IExecutionDMContext execCtx = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
        if (execCtx == null) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context " + context, null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        rm.setData(new FrameDMContext(getSession().getId(), execCtx, 0));
        rm.done();
    }

    public void getVariableData(IVariableDMContext variableCtx, DataRequestMonitor<IVariableDMData> rm) {
        if ( !(variableCtx instanceof VariableDMContext) ) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context " + variableCtx, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        String variable = ((VariableDMContext)variableCtx).getVariable();
        
        rm.setData(new VariableDMData(variable));
        rm.done();
    }

    public boolean isStackAvailable(IDMContext context) {
        IExecutionDMContext execCtx = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
        return execCtx != null && (fRunControl.isSuspended(execCtx) || (fRunControl.isStepping(execCtx)));
    }

    @Deprecated
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        if (dmc instanceof IFrameDMContext) {
            getFrameData((IFrameDMContext)dmc, (DataRequestMonitor<IFrameDMData>)rm);
            // getFrameData invokes rm 
        } else if (dmc instanceof IVariableDMContext) {
            getVariableData((IVariableDMContext)dmc, (DataRequestMonitor<IVariableDMData>)rm);
            // getVariablesData invokes rm 
        } else {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Unknown context type", null)); //$NON-NLS-1$
            rm.done();
        }
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(IResumedDMEvent e) {
        fCommandCache.setTargetAvailable(false);
        if (!e.getReason().equals(StateChangeReason.STEP)) {
            fCommandCache.reset();
        }
    }    


    @DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
        fCommandCache.setTargetAvailable(true);
        fCommandCache.reset();
    }
}
