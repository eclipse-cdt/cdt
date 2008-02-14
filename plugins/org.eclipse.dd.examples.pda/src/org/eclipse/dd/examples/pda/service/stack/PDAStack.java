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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

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
import org.eclipse.dd.examples.pda.service.command.PDACommand;
import org.eclipse.dd.examples.pda.service.command.PDACommandControl;
import org.eclipse.dd.examples.pda.service.command.PDACommandResult;
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

    public void getFrameData(IFrameDMContext frameCtx, DataRequestMonitor<IFrameDMData> rm) {
        if ( !(frameCtx instanceof FrameDMContext) ) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context " + frameCtx, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        PDAFrame pdaFrame = ((FrameDMContext)frameCtx).getFrame();
        rm.setData(new FrameDMData(pdaFrame));
        rm.done();
    }

    public void getFrames(IDMContext context, final DataRequestMonitor<IFrameDMContext[]> rm) {
        final IExecutionDMContext execCtx = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
        if (execCtx == null) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context " + context, null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        fCommandControl.queueCommand(
            new PDACommand(execCtx, "stack"),
            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    PDAFrame[] frames = parseStackResponse(getData().fResponseText);
                    IFrameDMContext[] frameCtxs = new IFrameDMContext[frames.length];
                    for (int i = 0; i < frames.length; i++) {
                        frameCtxs[i] = new FrameDMContext(getSession().getId(), execCtx, i, frames[i]);
                    }
                    rm.setData(frameCtxs);
                    rm.done();
                }
            });
    }

    public void getLocals(IFrameDMContext frameCtx, DataRequestMonitor<IVariableDMContext[]> rm) {
        if ( !(frameCtx instanceof FrameDMContext) ) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context " + frameCtx, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        PDAFrame pdaFrame = ((FrameDMContext)frameCtx).getFrame();
        IVariableDMContext[] variableCtxs = new IVariableDMContext[pdaFrame.fVariables.length];
        for (int i = 0; i < pdaFrame.fVariables.length; i++) {
            variableCtxs[i] = new VariableDMContext(getSession().getId(), frameCtx, pdaFrame.fVariables[i]);
        }
        rm.setData(variableCtxs);
        rm.done();
    }

    public void getStackDepth(IDMContext context, int maxDepth, final DataRequestMonitor<Integer> rm) {
        getFrames(
            context, 
            new DataRequestMonitor<IFrameDMContext[]>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    rm.setData(getData().length);
                    rm.done();
                }
            });
    }

    public void getTopFrame(IDMContext context, final DataRequestMonitor<IFrameDMContext> rm) {
        getFrames(
            context, 
            new DataRequestMonitor<IFrameDMContext[]>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    rm.setData(getData()[0]);
                    rm.done();
                }
            });
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
    
    public static class PDAFrame {
        PDAFrame(String frameString) {
            StringTokenizer st = new StringTokenizer(frameString, "|");
            
            fFilePath = st.nextToken();
            fLine = Integer.parseInt(st.nextToken());
            fFunction = st.nextToken();
            
            List<String> variablesList = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                variablesList.add(st.nextToken());
            }
            fVariables = variablesList.toArray(new String[variablesList.size()]);
        }
        
        final public String fFilePath;
        final public int fLine;
        final public String fFunction;
        final public String[] fVariables;
    }

    private PDAFrame[] parseStackResponse(String response) { 
        StringTokenizer st = new StringTokenizer(response, "#");
        List<PDAFrame> framesList = new ArrayList<PDAFrame>();
        
        while (st.hasMoreTokens()) {
            framesList.add(new PDAFrame(st.nextToken()));
        }
        return framesList.toArray(new PDAFrame[framesList.size()]);
    }    

}
