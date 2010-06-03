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
package org.eclipse.cdt.examples.dsf.pda.service;

import java.util.Hashtable;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAFrame;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAFrameCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAFrameCommandResult;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAStackCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAStackCommandResult;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAStackDepthCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAStackDepthCommandResult;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * Service for retrieving PDA debugger stack data. 
 * <p>
 * This service depends on the {@link PDACommandControl} service and the 
 * {@link IRunControl} service.  These services must be initialized before 
 * this service is initialized.
 * </p>
 */
public class PDAStack extends AbstractDsfService implements IStack, ICachingService {

    /**
     * PDA stack frame contains only the stack frame level.  It is only 
     * used as an index into the frame data returned by the PDA debugger.
     */
    @Immutable
    private static class FrameDMContext extends AbstractDMContext implements IFrameDMContext {

        final private int fLevel;

        FrameDMContext(String sessionId, PDAThreadDMContext execDmc, int level) {
            super(sessionId, new IDMContext[] { execDmc });
            fLevel = level;
        }

        public int getLevel() { return fLevel; }

        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && ((FrameDMContext)other).fLevel == fLevel;
        }

        @Override
        public int hashCode() {
            return super.baseHashCode() ^ fLevel;
        }

        @Override
        public String toString() { 
            return baseToString() + ".frame[" + fLevel + "]";  //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Frame data based on the PDAFrame object returned by the PDA debugger.
     */
    @Immutable
    private static class FrameDMData implements IFrameDMData {

        final private PDAFrame fFrame;

        FrameDMData(PDAFrame frame) {
            fFrame = frame;
        }

        public String getFile() {
            return fFrame.fFilePath.lastSegment();
        }

        public String getFunction() {
            return fFrame.fFunction;
        }

        public int getLine() {
            return fFrame.fLine + 1;
        }

        public int getColumn() {
            return 0;
        }

        public IAddress getAddress() {
            return null;
        }

		public String getModule() {
			return "";//$NON-NLS-1$
		}
    }

    /**
     * Context representing a variable in a given stack frame.
     */
    @Immutable
    private static class VariableDMContext extends AbstractDMContext implements IVariableDMContext {

        final private String fVariable;

        VariableDMContext(String sessionId, FrameDMContext frameCtx, String variable) {
            super(sessionId, new IDMContext[] { frameCtx });
            fVariable = variable;
        }

        String getVariable() { return fVariable; }

        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && ((VariableDMContext)other).fVariable.equals(fVariable);
        }

        @Override
        public int hashCode() {
            return super.baseHashCode() + fVariable.hashCode();
        }

        @Override
        public String toString() { 
            return baseToString() + ".variable(" + fVariable + ")";  //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * PDA variable data, only supports returning the variable name.
     */
    @Immutable
    private static class VariableDMData implements IVariableDMData {

        final private String fVariable;

        VariableDMData(String variable) {
            fVariable = variable;
        }

        public String getName() {
            return fVariable;
        }

        public String getValue() {
            return null;
        }
    }

    // Services that this service depends on.
    private PDACommandControl fCommandControl;
    private IRunControl fRunControl;

    // Command cache 
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
                protected void handleSuccess() {
                    doInitialize(rm);
                }});
    }

    private void doInitialize(final RequestMonitor rm) {
        // Initialize service references that stack service depends on
        fCommandControl = getServicesTracker().getService(PDACommandControl.class);
        fRunControl = getServicesTracker().getService(IRunControl.class);

        // Create the commands cache
        fCommandCache = new CommandCache(getSession(), fCommandControl);
        fCommandCache.setContextAvailable(fCommandControl.getContext(), true);

        // Register to listen for run control events, to clear cache accordingly.
        getSession().addServiceEventListener(this, null);

        // Register stack service with OSGi
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
        PDAPlugin.failRequest(rm, IDsfStatusConstants.NOT_SUPPORTED, "PDA debugger does not support function arguments.");
    }

    public void getFrameData(final IFrameDMContext frameCtx, final DataRequestMonitor<IFrameDMData> rm) {
        final PDAThreadDMContext threadCtx = 
            DMContexts.getAncestorOfType(frameCtx, PDAThreadDMContext.class);
        
        if (threadCtx == null) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context" + frameCtx, null));
            rm.done();
            return;            
        }
        
        getStackDepth(
            threadCtx, -1, 
            new DataRequestMonitor<Integer>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    // PDAFrame array is ordered highest to lowest.  We need to 
                    // calculate the index based on frame level.
                    int frameNum = getData() - frameCtx.getLevel() - 1;
                    if (frameNum < 0) {
                        PDAPlugin.failRequest(rm, IDsfStatusConstants.INVALID_HANDLE, "Invalid frame level " + frameCtx);
                        return;
                    }

                    // Execute the PDA stack command, or retrieve the result from cache if already available.
                    fCommandCache.execute(
                        new PDAFrameCommand(threadCtx, frameNum),
                        new DataRequestMonitor<PDAFrameCommandResult>(getExecutor(), rm) {
                            @Override
                            protected void handleSuccess() {
                                // Create the frame data object based on the corresponding PDAFrame
                                rm.setData(new FrameDMData(getData().fFrame));
                                rm.done();
                            }
                        });
                }
            });
    }


    public void getFrames(IDMContext context, final DataRequestMonitor<IFrameDMContext[]> rm) {
        // Can only create stack frames for an execution context as a parent, 
        // however the argument context is a generic context type, so it could 
        // be an execution context, a frame, a variable, etc. Search the 
        // hierarchy of the argument context to find the execution one.
        final PDAThreadDMContext threadCtx = 
            DMContexts.getAncestorOfType(context, PDAThreadDMContext.class);
        
        if (threadCtx == null) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context" + context, null));
            rm.done();
            return;            
        }

        // Execute the stack command and create the corresponding frame contexts.
        getStackDepth(
            context, -1, 
            new DataRequestMonitor<Integer>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    IFrameDMContext[] frameCtxs = new IFrameDMContext[getData()];
                    for (int i = 0; i < getData(); i++) {
                        frameCtxs[i] = new FrameDMContext(getSession().getId(), threadCtx, i);
                    }
                    rm.setData(frameCtxs);
                    rm.done();
                }
            });
    }

    public void getFrames(IDMContext context, final int startIndex, final int endIndex, final DataRequestMonitor<IFrameDMContext[]> rm) {
        // Validate index range.
        assert startIndex >=0 && (endIndex < 0 || startIndex <= endIndex);
        
        final PDAThreadDMContext threadCtx = 
            DMContexts.getAncestorOfType(context, PDAThreadDMContext.class);
        
        if (threadCtx == null) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context" + context, null));
            rm.done();
            return;            
        }

        // Execute the stack command and create the corresponding frame contexts.
        getStackDepth(
            context, -1, 
            new DataRequestMonitor<Integer>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    int numFrames = endIndex < 0 
                        ? (getData() - startIndex) 
                        : Math.min(endIndex + 1, getData()) - startIndex;
                    IFrameDMContext[] frameCtxs = new IFrameDMContext[numFrames];
                    for (int i = 0; i < numFrames; i++) {
                        frameCtxs[i] = new FrameDMContext(getSession().getId(), threadCtx, startIndex + i);
                    }
                    rm.setData(frameCtxs);
                    rm.done();
                }
            });
    }
    
    public void getLocals(IFrameDMContext context, final DataRequestMonitor<IVariableDMContext[]> rm) {
        if (!(context instanceof FrameDMContext)) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context" + context, null));
            rm.done();
            return;            
        }
        final FrameDMContext frameCtx = (FrameDMContext)context;
        
        final PDAThreadDMContext threadCtx = 
            DMContexts.getAncestorOfType(frameCtx, PDAThreadDMContext.class);
        
        if (threadCtx == null) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context" + frameCtx, null));
            rm.done();
            return;            
        }

        fCommandCache.execute(
            new PDAStackCommand(threadCtx),
            new DataRequestMonitor<PDAStackCommandResult>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    // Find the correct PDAFrame
                    int frameId = getData().fFrames.length - frameCtx.getLevel() - 1;
                    if (frameId < 0) {
                        PDAPlugin.failRequest(rm, IDsfStatusConstants.INVALID_HANDLE, "Invalid frame level " + frameCtx);
                        return;
                    }
                    PDAFrame pdaFrame = getData().fFrames[frameId];

                    // Create variable contexts for all variables in frame.
                    IVariableDMContext[] variableCtxs = new IVariableDMContext[pdaFrame.fVariables.length];
                    for (int i = 0; i < pdaFrame.fVariables.length; i++) {
                        variableCtxs[i] = new VariableDMContext(getSession().getId(), frameCtx, pdaFrame.fVariables[i]);
                    }
                    rm.setData(variableCtxs);
                    rm.done();
                }
            });

    }

    public void getStackDepth(IDMContext context, final int maxDepth, final DataRequestMonitor<Integer> rm) {
        final PDAThreadDMContext threadCtx = 
            DMContexts.getAncestorOfType(context, PDAThreadDMContext.class);
        
        if (threadCtx == null) {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context" + context, null));
            rm.done();
            return;            
        }

        // Execute stack command and return the data's size.
        fCommandCache.execute(
            new PDAStackDepthCommand(threadCtx),
            new DataRequestMonitor<PDAStackDepthCommandResult>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    int depth= getData().fDepth;
                    if (maxDepth > 0 && maxDepth < depth) {
                    	depth = maxDepth;
                    }
					rm.setData(depth);
                    rm.done();
                }
            });
    }

    public void getTopFrame(IDMContext context, final DataRequestMonitor<IFrameDMContext> rm) {
        // Can only create stack frames for an execution context as a parent, 
        // however the argument context is a generic context type, so it could 
        // be an execution context, a frame, a variable, etc. Search the 
        // hierarchy of the argument context to find the execution one.
        final PDAThreadDMContext execCtx = DMContexts.getAncestorOfType(context, PDAThreadDMContext.class);
        if (execCtx == null) {
            PDAPlugin.failRequest(rm, IDsfStatusConstants.INVALID_HANDLE, "Invalid context " + context);
            return;
        }

        // Since the frame context only contain the level, there's no need to 
        // call the PDA debugger.  Simply create a context for level 0. 
        rm.setData(new FrameDMContext(getSession().getId(), execCtx, 0));
        rm.done();
    }

    public void getVariableData(IVariableDMContext variableCtx, DataRequestMonitor<IVariableDMData> rm) {
        if ( !(variableCtx instanceof VariableDMContext) ) {
            PDAPlugin.failRequest(rm, IDsfStatusConstants.INVALID_HANDLE, "Invalid context " + variableCtx);
            return;
        }

        // The variable data doen't contain a value.  So there's no need to 
        // go to the back end to retrieve it.
        String variable = ((VariableDMContext)variableCtx).getVariable();

        rm.setData(new VariableDMData(variable));
        rm.done();
    }

    public boolean isStackAvailable(IDMContext context) {
        // Stack is available if the program is suspended or stepping.
        IExecutionDMContext execCtx = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
        return execCtx != null && (fRunControl.isSuspended(execCtx) || (fRunControl.isStepping(execCtx)));
    }
    
    /**
     * Returns a frame context for the given thread and level;
     */
    public IFrameDMContext getFrameDMContext(PDAThreadDMContext thread, int level) {
        return new FrameDMContext(getSession().getId(), thread, level);
    }

    @DsfServiceEventHandler 
    public void eventDispatched(IResumedDMEvent e) {
        // Mark the cache as not available, so that stack commands will
        // fail.  Also reset the cache unless it was a step command.
        fCommandCache.setContextAvailable(e.getDMContext(), false);
        if (!e.getReason().equals(StateChangeReason.STEP)) {
            fCommandCache.reset(e.getDMContext());
        }
    }    


    @DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
        // Enable sending commands to target and clear the cache.
        fCommandCache.setContextAvailable(e.getDMContext(), true);
        fCommandCache.reset(e.getDMContext());
    }

	public void flushCache(IDMContext context) {
        fCommandCache.reset(context);	
	}
}
