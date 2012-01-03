/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Modified for handling of multiple execution contexts	
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.IMIDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIArg;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackInfoDepthInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListArgumentsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListFramesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackListLocalsInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class MIStack extends AbstractDsfService
	implements IStack, ICachingService
{
    protected static class MIFrameDMC extends AbstractDMContext 
        implements IFrameDMContext
    {
        private final int fLevel;
        public MIFrameDMC(String sessionId, IExecutionDMContext execDmc, int level) {
            super(sessionId, new IDMContext[] { execDmc });
            fLevel = level;
        }
        
    	@Override
        public int getLevel() { return fLevel; }
        
        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && ((MIFrameDMC)other).fLevel == fLevel;
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
       
    protected static class MIVariableDMC extends AbstractDMContext
        implements IVariableDMContext
    {
        public enum Type { ARGUMENT, LOCAL }
        final private Type fType;
        final private int fIndex;

        public MIVariableDMC(MIStack service, IFrameDMContext frame, Type type, int index) {
            super(service, new IDMContext[] { frame });
            fIndex = index;
            fType = type;
        }
        
        public int getIndex() { return fIndex; }
        public Type getType() { return fType; }
        
        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && 
                   ((MIVariableDMC)other).fType == fType && 
                   ((MIVariableDMC)other).fIndex == fIndex;
        }
        
        @Override
        public int hashCode() {
            int typeFactor = 0;
            if (fType == Type.LOCAL) typeFactor = 2;
            else if (fType == Type.ARGUMENT) typeFactor = 3;
            return super.baseHashCode() ^ typeFactor ^ fIndex;
        }
        
        @Override
        public String toString() { 
            return baseToString() + ".variable(" + fType + ")[" + fIndex + "]";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }            
    }
    
    /**
     * Class to track stack depth requests for our internal cache
     */
    private class StackDepthInfo {
    	// The maximum depth we requested
    	public int maxDepthRequested;
    	// The actual depth we received
    	public int returnedDepth;
    	
    	StackDepthInfo(int requested, int returned) {
    		maxDepthRequested = requested;
    		returnedDepth = returned;
    	}
    }
    
    /**
     * A HashMap for our StackDepth cache, that can clear based on a context.
     */
    @SuppressWarnings("serial")
    private class StackDepthHashMap<V,T> extends HashMap<V,T> {
    	public void clear(IDMContext context) {
            final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
            if (execDmc != null) {
            	remove(execDmc.getThreadId());
            } else {
            	clear();
            };
    	}
    }

	private CommandCache fMICommandCache;
	private CommandFactory fCommandFactory;

	// Two commands such as 
	//  -stack-info-depth 11
	//  -stack-info-depth 2
	// would both be sent to GDB because the command cache sees them as different.
	// This stackDepthCache allows us to know that if we already ask for a stack depth
	// we can potentially re-use the answer.
	private StackDepthHashMap<Integer, StackDepthInfo> fStackDepthCache = new StackDepthHashMap<Integer, StackDepthInfo>();

    private MIStoppedEvent fCachedStoppedEvent;
    private IRunControl fRunControl;

	/** 
	 * Indicates that we are currently visualizing trace data.
	 * In this case, some errors should not be reported.
	 */
	private boolean fTraceVisualization;

	public MIStack(DsfSession session) 
	{
		super(session);
	}

    @Override
    protected BundleContext getBundleContext() 
    {
        return GdbPlugin.getBundleContext();
    }
    
    @Override
    public void initialize(final RequestMonitor rm) {
        super.initialize(
            new ImmediateRequestMonitor(rm) { 
                @Override
                protected void handleSuccess() {
                    doInitialize(rm);
                }
            });
    }

    private void doInitialize(RequestMonitor rm) {
    	ICommandControlService commandControl = getServicesTracker().getService(ICommandControlService.class);
		BufferedCommandControl bufferedCommandControl = new BufferedCommandControl(commandControl, getExecutor(), 2);
		
		// This cache stores the result of a command when received; also, this cache
		// is manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
        fMICommandCache = new CommandCache(getSession(), bufferedCommandControl);
        fMICommandCache.setContextAvailable(commandControl.getContext(), true);
        fRunControl = getServicesTracker().getService(IRunControl.class);

        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

        getSession().addServiceEventListener(this, null);
        register(new String[]{IStack.class.getName(), MIStack.class.getName()}, new Hashtable<String,String>());
        rm.done();
    }

    @Override
    public void shutdown(RequestMonitor rm) 
    {
        unregister();
        getSession().removeServiceEventListener(this);
        fMICommandCache.reset();
        super.shutdown(rm);
    }

    /**
     * Creates a frame context.  This method is intended to be used by other MI 
     * services and sub-classes which need to create a frame context directly.
     * <p>
     * Sub-classes can override this method to provide custom stack frame 
     * context implementation. 
     * </p>
     * @param execDmc Execution context that this frame is to be a child of.
     * @param level Level of the new context.
     * @return A new frame context.
     */
    public IFrameDMContext createFrameDMContext(IExecutionDMContext execDmc, int level) {
        return new MIFrameDMC(getSession().getId(), execDmc, level);
    }
    
	@Override
    public void getFrames(final IDMContext ctx, final DataRequestMonitor<IFrameDMContext[]> rm) {
    	getFrames(ctx, 0, ALL_FRAMES, rm);
    }

	@Override
	public void getFrames(final IDMContext ctx, final int startIndex, final int endIndex, final DataRequestMonitor<IFrameDMContext[]> rm) {

	    if (startIndex < 0 || endIndex > 0 && endIndex < startIndex) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid stack frame range [" + startIndex + ',' + endIndex + ']', null)); //$NON-NLS-1$
            rm.done();
            return;
	    }

		final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(ctx, IMIExecutionDMContext.class);
	    
	    if (execDmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context " + ctx, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

	    // Make sure the thread is stopped but only if we are not visualizing trace data
	    if (!fTraceVisualization && !fRunControl.isSuspended(execDmc)) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context is running: " + ctx, null)); //$NON-NLS-1$
	    	rm.done();
	    	return;
	    }

	    if (startIndex == 0 && endIndex == 0) {
	        // Try to retrieve the top stack frame from the cached stopped event.
	        if (fCachedStoppedEvent != null && 
	            fCachedStoppedEvent.getFrame() != null && 
	            execDmc.equals(fCachedStoppedEvent.getDMContext())) 
	        {
	            rm.setData(new IFrameDMContext[] { createFrameDMContext(execDmc, fCachedStoppedEvent.getFrame().getLevel()) });
	            rm.done();
	            return;
	        }
	    }

	    final ICommand<MIStackListFramesInfo> miStackListCmd;
	    // firstIndex is the first index retrieved
	    final int firstIndex;
	    if (endIndex >= 0) {
	    	miStackListCmd = fCommandFactory.createMIStackListFrames(execDmc, startIndex, endIndex);
	    	firstIndex = startIndex;
	    } else {
	    	miStackListCmd = fCommandFactory.createMIStackListFrames(execDmc);
	    	firstIndex = 0;
	    }
		fMICommandCache.execute(
            miStackListCmd,
            new DataRequestMonitor<MIStackListFramesInfo>(getExecutor(), rm) { 
                @Override
                protected void handleSuccess() {
                    rm.setData(getFrames(execDmc, getData(), firstIndex, endIndex, startIndex));
                    rm.done();
                }
            });
	}
    
	@Override
    public void getTopFrame(final IDMContext ctx, final DataRequestMonitor<IFrameDMContext> rm) {     
        final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(ctx, IMIExecutionDMContext.class);
        if (execDmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context" + ctx, null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        // Try to retrieve the top stack frame from the cached stopped event.
        if (fCachedStoppedEvent != null && 
            fCachedStoppedEvent.getFrame() != null && 
            execDmc.equals(fCachedStoppedEvent.getDMContext())) 
        {
            rm.setData(createFrameDMContext(execDmc, fCachedStoppedEvent.getFrame().getLevel()));
            rm.done();
            return;
        }
        
        // If stopped event is not available or doesn't contain frame info, 
        // query top stack frame
        getFrames(
            ctx, 
            0,
            0,
            new DataRequestMonitor<IFrameDMContext[]>(getExecutor(), rm) { 
                @Override
                protected void handleSuccess() {
                    rm.setData(getData()[0]);
                    rm.done();
                }
            });
    }
    
    private IFrameDMContext[] getFrames(IMIExecutionDMContext execDmc, MIStackListFramesInfo info, int firstIndex, int lastIndex, int startIndex) {
        int length = info.getMIFrames().length;
        if (lastIndex > 0) {
        	int limit= lastIndex - startIndex + 1;
        	if (limit < length) {
        		length = limit;
        	}
        }
		IFrameDMContext[] frameDMCs = new MIFrameDMC[length];
        for (int i = 0; i < length; i++) {
	        //frameDMCs[i] = new MIFrameDMC(this, info.getMIFrames()[i].getLevel()); 
        	final MIFrame frame= info.getMIFrames()[i + startIndex - firstIndex];
			assert startIndex + i == frame.getLevel();
            frameDMCs[i] = createFrameDMContext(execDmc, frame.getLevel()); 
        }
        return frameDMCs;
    }
    

    
	@Override
    public void getFrameData(final IFrameDMContext frameDmc, final DataRequestMonitor<IFrameDMData> rm) {
        if (!(frameDmc instanceof MIFrameDMC)) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context type " + frameDmc, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        final MIFrameDMC miFrameDmc = (MIFrameDMC)frameDmc;
        
        final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);
        if (execDmc == null) { 
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "No execution context found in " + frameDmc, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        /**
         * Base class for the IFrameDMData object that uses an MIFrame object to 
         * provide the data.  Sub-classes must provide the MIFrame object
         */
        abstract class FrameData implements IFrameDMData
        {
            abstract protected MIFrame getMIFrame();

        	@Override
            public IAddress getAddress() {
                String addr = getMIFrame().getAddress();
                if (addr == null || addr.length() == 0) {
                	return new Addr32(0);
                }
                if (addr.startsWith("0x")) { //$NON-NLS-1$
                    addr = addr.substring(2);
                }
                if (addr.length() <= 8) {
                    return new Addr32(getMIFrame().getAddress());
                } else {
                    return new Addr64(getMIFrame().getAddress());
                }                    
            }

        	@Override
            public int getColumn() { return 0; }

        	@Override
            public String getFile() { return getMIFrame().getFile(); }
        	@Override
            public int getLine() { return getMIFrame().getLine(); }
        	@Override
            public String getFunction() { return getMIFrame().getFunction(); }
        	@Override
            public String getModule() { return ""; }//$NON-NLS-1$
            
            @Override
            public String toString() { return getMIFrame().toString(); }
        }

        // If requested frame is the top stack frame, try to retrieve it from 
        // the stopped event data.
        class FrameDataFromStoppedEvent extends FrameData {
            private final MIStoppedEvent fEvent;
            FrameDataFromStoppedEvent(MIStoppedEvent event) { fEvent = event; }
            @Override
            protected MIFrame getMIFrame() { return fEvent.getFrame(); }
        }
        
        // Retrieve the top stack frame from the stopped event only if the selected thread is the one on which stopped event 
        // is raised
        if (miFrameDmc.fLevel == 0) {
        	if (fCachedStoppedEvent != null && fCachedStoppedEvent.getFrame() != null && 
        		(execDmc.equals(fCachedStoppedEvent.getDMContext()) || fTraceVisualization))
        	{
                rm.setData(new FrameDataFromStoppedEvent(fCachedStoppedEvent));
                rm.done();
                return;
        	}
        }

        // If not, retrieve the full list of frame data.
        class FrameDataFromMIStackFrameListInfo extends FrameData {
            private MIStackListFramesInfo fFrameDataCacheInfo;
            private int fFrameIndex;

            FrameDataFromMIStackFrameListInfo(MIStackListFramesInfo info, int index) {
                fFrameDataCacheInfo = info;
                fFrameIndex = index;
            }

            @Override
            protected MIFrame getMIFrame() { return fFrameDataCacheInfo.getMIFrames()[fFrameIndex]; }
        }

        fMICommandCache.execute(
        	fCommandFactory.createMIStackListFrames(execDmc),
            new DataRequestMonitor<MIStackListFramesInfo>(getExecutor(), rm) { 
                @Override
                protected void handleSuccess() {
                    // Find the index to the correct MI frame object.
                    int idx = findFrameIndex(getData().getMIFrames(), miFrameDmc.fLevel);
                    if (idx == -1) {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid frame " + frameDmc, null));  //$NON-NLS-1$
                        rm.done();
                        return;
                    }
                    
                    // Create the data object.
                    rm.setData(new FrameDataFromMIStackFrameListInfo(getData(), idx));
                    rm.done();
                }
                
                @Override
				protected void handleError() {                
					// We're seeing gdb in some cases fail when it's
					// being asked for the stack depth or stack
					// frames, but the same command succeeds if
					// the request is limited to one frame. So try
					// again with a limit of 1. It's better to show
					// just one frame than none at all
                	if (miFrameDmc.fLevel == 0) {
	                    fMICommandCache.execute(
	                    	fCommandFactory.createMIStackListFrames(execDmc, 0, 0),
	                            new DataRequestMonitor<MIStackListFramesInfo>(getExecutor(), rm) { 
	                                @Override
	                                protected void handleSuccess() {
	                                    // Find the index to the correct MI frame object.
	                                    int idx = findFrameIndex(getData().getMIFrames(), miFrameDmc.fLevel);
	                                    if (idx == -1) {
	                                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid frame " + frameDmc, null));  //$NON-NLS-1$
	                                        rm.done();
	                                        return;
	                                    }
	                                    
	                                    // Create the data object.
	                                    rm.setData(new FrameDataFromMIStackFrameListInfo(getData(), idx));
	                                    rm.done();
	                                }
	                            });
                	} else {
                		super.handleError();
                	}
                }
            }); 
    }

	@Override
	public void getArguments(final IFrameDMContext frameDmc, final DataRequestMonitor<IVariableDMContext[]> rm) {
        final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);
	    if (execDmc == null) { 
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "No execution context found in " + frameDmc, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        
        // If requested frame is the top stack frame, try to retrieve it from 
        // the stopped event data.
        if (frameDmc.getLevel() == 0 && 
            fCachedStoppedEvent != null && 
            fCachedStoppedEvent.getFrame() != null &&
            execDmc.equals(fCachedStoppedEvent.getDMContext()) && 
            fCachedStoppedEvent.getFrame().getArgs() != null) 
        {
            rm.setData(makeVariableDMCs(
                frameDmc, MIVariableDMC.Type.ARGUMENT, fCachedStoppedEvent.getFrame().getArgs())); 
            rm.done();
            return;
        }
        
        // If not, retrieve the full list of frame data.  Although we only need one frame
        // for this call, it will be stored the cache and made available for other calls.
        fMICommandCache.execute(
            // We don't actually need to ask for the values in this case, but since
        	// we will ask for them right after, it is more efficient to ask for them now
            // so as to cache the result.  If the command fails, then we will ask for
        	// the result without the values
       		// Don't ask for value when we are visualizing trace data, since some
       		// data will not be there, and the command will fail
       		fCommandFactory.createMIStackListArguments(execDmc, true),
            new DataRequestMonitor<MIStackListArgumentsInfo>(getExecutor(), rm) { 
                @Override
                protected void handleSuccess() {
                    // Find the index to the correct MI frame object.
                    // Note: this is a short-cut, but it won't work once we implement retrieving
                    // partial lists of stack frames.
                    int idx = frameDmc.getLevel();
                    if (idx == -1 || idx >= getData().getMIFrames().length) {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid frame " + frameDmc, null));  //$NON-NLS-1$
                        rm.done();
                        return;
                    }
                    
                    // Create the variable array out of MIArg array.
                    MIArg[] args = getData().getMIFrames()[idx].getArgs();
                    if (args == null) args = new MIArg[0]; 
                    rm.setData(makeVariableDMCs(frameDmc, MIVariableDMC.Type.ARGUMENT, args));
                    rm.done();
                }
                @Override
                protected void handleError() {
                	// If the command fails it can be because we asked for values.
                	// This can happen with uninitialized values and pretty printers (bug 307614).
                	// Since asking for values was simply an optimization
                	// to store the command in the cache, let's retry the command without asking for values.
                    fMICommandCache.execute(
                        	fCommandFactory.createMIStackListArguments(execDmc, false),
                            new DataRequestMonitor<MIStackListArgumentsInfo>(getExecutor(), rm) { 
                                @Override
                                protected void handleSuccess() {
                                    // Find the index to the correct MI frame object.
                                    // Note: this is a short-cut, but it won't work once we implement retrieving
                                    // partial lists of stack frames.
                                    int idx = frameDmc.getLevel();
                                    if (idx == -1 || idx >= getData().getMIFrames().length) {
                                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid frame " + frameDmc, null));  //$NON-NLS-1$
                                        rm.done();
                                        return;
                                    }
                                    
                                    // Create the variable array out of MIArg array.
                                    MIArg[] args = getData().getMIFrames()[idx].getArgs();
                                    if (args == null) args = new MIArg[0]; 
                                    rm.setData(makeVariableDMCs(frameDmc, MIVariableDMC.Type.ARGUMENT, args));
                                    rm.done();
                                }
                            }); 
                }
            }); 
    }
	
	@Override
    public void getVariableData(IVariableDMContext variableDmc, final DataRequestMonitor<IVariableDMData> rm) {
        if (!(variableDmc instanceof MIVariableDMC)) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context type " + variableDmc, null)); //$NON-NLS-1$
            rm.done();
            return;            
        }
        final MIVariableDMC miVariableDmc = (MIVariableDMC)variableDmc;
        
        // Extract the frame DMC from the variable DMC.
        final MIFrameDMC frameDmc = DMContexts.getAncestorOfType(variableDmc, MIFrameDMC.class);
        if (frameDmc == null) { 
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "No frame context found in " + variableDmc, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);
        if (execDmc == null) { 
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "No execution context found in " + frameDmc, null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        /**
         * Same as with frame objects, this is a base class for the IVariableDMData object that uses an MIArg object to 
         * provide the data.  Sub-classes must supply the MIArg object.
         */
    	class VariableData implements IVariableDMData {
    		private MIArg dsfMIArg;
    		VariableData(MIArg arg){
    			dsfMIArg = arg;
    		}
    		@Override
    		public String getName() { return dsfMIArg.getName(); }
    		@Override
    		public String getValue() { return dsfMIArg.getValue(); }
    		@Override
    		public String toString() { return dsfMIArg.toString(); }	
    	}    	

        // Check if the stopped event can be used to extract the variable value. 
        if (execDmc != null && miVariableDmc.fType == MIVariableDMC.Type.ARGUMENT &&
            frameDmc.fLevel == 0 && fCachedStoppedEvent != null && fCachedStoppedEvent.getFrame() != null &&
            execDmc.equals(fCachedStoppedEvent.getDMContext()) && 
            fCachedStoppedEvent.getFrame().getArgs() != null) 
        {
            if (miVariableDmc.fIndex >= fCachedStoppedEvent.getFrame().getArgs().length) {
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Invalid variable " + miVariableDmc, null));  //$NON-NLS-1$
                rm.done();
                return;
            }

            rm.setData(new VariableData(fCachedStoppedEvent.getFrame().getArgs()[miVariableDmc.fIndex]));
            rm.done();
            return;
        }

        if (miVariableDmc.fType == MIVariableDMC.Type.ARGUMENT){
	        fMICommandCache.execute(
	            // Don't ask for value when we are visualizing trace data, since some
	            // data will not be there, and the command will fail
	    	    fCommandFactory.createMIStackListArguments(execDmc, true),
	            new DataRequestMonitor<MIStackListArgumentsInfo>(getExecutor(), rm) { 
	                @Override
	                protected void handleSuccess() {
	                    // Find the correct frame and argument
	                    if ( frameDmc.fLevel >= getData().getMIFrames().length ||
	                        miVariableDmc.fIndex >= getData().getMIFrames()[frameDmc.fLevel].getArgs().length )
	                    {
	                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid variable " + miVariableDmc, null));  //$NON-NLS-1$
	                        rm.done();
	                        return;
	                    }
	                    
	                    // Create the data object.
	                    rm.setData(new VariableData(getData().getMIFrames()[frameDmc.fLevel].getArgs()[miVariableDmc.fIndex]));
	                    rm.done();
	                }
	                @Override
	                protected void handleError() {
	                	// Unable to get the values.  This can happen with uninitialized values and pretty printers (bug 307614)
	                	// Let's try to ask for the arguments without their values, which is better than nothing
	                	fMICommandCache.execute(
	                			fCommandFactory.createMIStackListArguments(execDmc, false),
	                			new DataRequestMonitor<MIStackListArgumentsInfo>(getExecutor(), rm) { 
	                				@Override
	                				protected void handleSuccess() {
	                					// Find the correct frame and argument
	                					if ( frameDmc.fLevel >= getData().getMIFrames().length ||
	                							miVariableDmc.fIndex >= getData().getMIFrames()[frameDmc.fLevel].getArgs().length )
	                					{
	                						rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid variable " + miVariableDmc, null));  //$NON-NLS-1$
	                						rm.done();
	                						return;
	                					}

	                					// Create the data object.
	                					rm.setData(new VariableData(getData().getMIFrames()[frameDmc.fLevel].getArgs()[miVariableDmc.fIndex]));
	                					rm.done();
	                				}
	                			});	
	                }
	        	});
        }//if
        if (miVariableDmc.fType == MIVariableDMC.Type.LOCAL){
            fMICommandCache.execute(
                	// Don't ask for value when we are visualizing trace data, since some
                	// data will not be there, and the command will fail
            		fCommandFactory.createMIStackListLocals(frameDmc, !fTraceVisualization),
                    new DataRequestMonitor<MIStackListLocalsInfo>(getExecutor(), rm) { 
                        @Override
                        protected void handleSuccess() {
   		                    
		                    // Create the data object.
		                    MIArg[] locals = getData().getLocals();
		                    if (locals.length > miVariableDmc.fIndex) {
		                    	rm.setData(new VariableData(locals[miVariableDmc.fIndex]));
		                    } else {
		                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid variable " + miVariableDmc, null));  //$NON-NLS-1$
		                    }
		                    rm.done();
                        }
                        @Override
                        protected void handleError() {
                        	// Unable to get the value.  This can happen with uninitialized values and pretty printers (bug 307614).
                        	// Let's try to ask for the variables without their values, which is better than nothing
                            fMICommandCache.execute(
                            		fCommandFactory.createMIStackListLocals(frameDmc, false),
                                    new DataRequestMonitor<MIStackListLocalsInfo>(getExecutor(), rm) { 
                                        @Override
                                        protected void handleSuccess() {
                   		                    
                		                    // Create the data object.
                		                    MIArg[] locals = getData().getLocals();
                		                    if (locals.length > miVariableDmc.fIndex) {
                		                    	rm.setData(new VariableData(locals[miVariableDmc.fIndex]));
                		                    } else {
                		                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid variable " + miVariableDmc, null));  //$NON-NLS-1$
                		                    }
                		                    rm.done();
                                        }
                            		});
                        }
                    });
        }//if   

    }

    private MIVariableDMC[] makeVariableDMCs(IFrameDMContext frame, MIVariableDMC.Type type, MIArg[] miArgs) {
    	// Use LinkedHashMap in order to keep the original ordering.
    	// We don't currently support variables with the same name in the same frame,
    	// so we only keep the first one.
    	// Bug 327621 and 328573
    	Map<String, MIVariableDMC> variableNames = new LinkedHashMap<String, MIVariableDMC>();
    	
    	for (int i = 0; i < miArgs.length; i++) {
    		String name = miArgs[i].getName();
    		MIVariableDMC var = variableNames.get(name);
    		
    		if (var == null) {
    			variableNames.put(name, new MIVariableDMC(this, frame, type, i));
    		}
    	}

    	return variableNames.values().toArray(new MIVariableDMC[0]);
    }

    private int findFrameIndex(MIFrame[] frames, int level) {
        for (int idx = 0; idx < frames.length; idx++) {
            if (frames[idx].getLevel() == level) {
                return idx;
            }
        }
        return -1;
    }
    
    
	@Override
    public void getLocals(final IFrameDMContext frameDmc, final DataRequestMonitor<IVariableDMContext[]> rm) {

        final List<IVariableDMContext> localsList = new ArrayList<IVariableDMContext>();
        
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                rm.setData( localsList.toArray(new IVariableDMContext[localsList.size()]) );
                rm.done();
            }
        };
        countingRm.setDoneCount(2);
        
        getArguments(
            frameDmc,
            new DataRequestMonitor<IVariableDMContext[]>(getExecutor(), countingRm) { 
                @Override
                protected void handleSuccess() {
                    localsList.addAll( Arrays.asList(getData()) );
                    countingRm.done();
                }
            }); 
        
	    fMICommandCache.execute(
	            // We don't actually need to ask for the values in this case, but since
	        	// we will ask for them right after, it is more efficient to ask for them now
	            // so as to cache the result.  If the command fails, then we will ask for
	        	// the result without the values
	        	// Don't ask for value when we are visualizing trace data, since some
	        	// data will not be there, and the command will fail
	    		fCommandFactory.createMIStackListLocals(frameDmc, !fTraceVisualization),
                new DataRequestMonitor<MIStackListLocalsInfo>(getExecutor(), countingRm) { 
                    @Override
                    protected void handleSuccess() {
                        localsList.addAll( Arrays.asList(
                            makeVariableDMCs(frameDmc, MIVariableDMC.Type.LOCAL, getData().getLocals())) );
                        countingRm.done();
                    }
                    @Override
                    protected void handleError() {
                    	// If the command fails it can be because we asked for values.
                    	// This can happen with uninitialized values and pretty printers (bug 307614).
                    	// Since asking for values was simply an optimization
                    	// to store the command in the cache, let's retry the command without asking for values.
                	    fMICommandCache.execute(
                	    		fCommandFactory.createMIStackListLocals(frameDmc, false),
                                new DataRequestMonitor<MIStackListLocalsInfo>(getExecutor(), countingRm) { 
                                    @Override
                                    protected void handleSuccess() {
                                        localsList.addAll( Arrays.asList(
                                            makeVariableDMCs(frameDmc, MIVariableDMC.Type.LOCAL, getData().getLocals())) );
                                        countingRm.done();
                                    }
                                }); 
                    }
                }); 
    }

	@Override
    public void getStackDepth(final IDMContext dmc, final int maxDepth, final DataRequestMonitor<Integer> rm) {
        final IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(dmc, IMIExecutionDMContext.class);
	    if (execDmc != null) {
	    	// Make sure the thread is stopped
	    	if (!fTraceVisualization && !fRunControl.isSuspended(execDmc)) {
	    		rm.setData(0);
	    		rm.done();
	    		return;
	    	}

	    	// Check our internal cache first because different commands can
	    	// still be re-used.
	    	StackDepthInfo cachedDepth = fStackDepthCache.get(execDmc.getThreadId());
	    	if (cachedDepth != null) {
	    	    if (cachedDepth.maxDepthRequested == 0 || 
	    		    (maxDepth != 0 && cachedDepth.maxDepthRequested >= maxDepth))
	    	    {
	    	        rm.setData(cachedDepth.returnedDepth);
	    	        rm.done();
	    	        return;
	    		}
	    	}
	    	
	    	ICommand<MIStackInfoDepthInfo> depthCommand = null;
	    	if (maxDepth > 0) depthCommand = fCommandFactory.createMIStackInfoDepth(execDmc, maxDepth);
	    	else depthCommand = fCommandFactory.createMIStackInfoDepth(execDmc);

	    	fMICommandCache.execute(
	    			depthCommand,
	    			new DataRequestMonitor<MIStackInfoDepthInfo>(getExecutor(), rm) { 
	    				@Override
	    				protected void handleSuccess() {
	    					// Store result in our internal cache
	    					fStackDepthCache.put(execDmc.getThreadId(), new StackDepthInfo(maxDepth, getData().getDepth()));
	    					
	    					rm.setData(getData().getDepth());
	    					rm.done();
	    				}
	    				@Override
	    				protected void handleError() {
	    					if (fTraceVisualization) {
	    				    	// when visualizing trace data with GDB 7.2, the command
	    				    	// -stack-info-depth will return an error if we ask for any level
	    				    	// that GDB does not know about.  We would have to iteratively
	    				    	// try different depths until we found the deepest that succeeds.
	    				    	// That is too much of a hack, especially since GDB 7.3 answers correctly.
	    				    	// For 7.2, we can safely say we have one stack
	    				    	// frame, which is going to be the case for 95% of the cases.
	    				    	// To have more stack frames, the user would have to have collected
	    				    	// the registers and enough stack memory for GDB to build another frame.
	    						rm.setData(1);
	    						rm.done();	    				    	
	    					} else {
								// We're seeing gdb in some cases fail when it's
								// being asked for the stack depth or stack
								// frames, but the same command succeeds if
								// the request is limited to one frame. So try
								// again with a limit of 1. It's better to show
								// just one frame than none at all
	    						if (maxDepth != 1) {
	    							getStackDepth(dmc, 1, rm);
	    						}
	    						else {
		    						super.handleError();
	    						}
	    					}
	    				}
	    			});
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler 
    public void eventDispatched(IResumedDMEvent e) {
    	fMICommandCache.setContextAvailable(e.getDMContext(), false);
        if (e.getReason() != StateChangeReason.STEP) {
            fCachedStoppedEvent = null;
            fMICommandCache.reset();
            fStackDepthCache.clear();
        }
    }
    
    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     * @since 1.1
     */
    @DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
    	fMICommandCache.setContextAvailable(e.getDMContext(), true);
        fMICommandCache.reset();
        fStackDepthCache.clear();
    }
    

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     * @since 1.1
     */
    @DsfServiceEventHandler 
    public void eventDispatched(IMIDMEvent e) {
    	if (e.getMIEvent() instanceof MIStoppedEvent) {
    		fCachedStoppedEvent = (MIStoppedEvent)e.getMIEvent();
    	}
    }

    /** @since 3.0 */
    @DsfServiceEventHandler
    public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
    	if (e.isVisualizationModeEnabled()) {
    		fTraceVisualization = true;
    	} else {
    		fTraceVisualization = false;
    		fCachedStoppedEvent = null;
    	}
    }
    
    /**
     * {@inheritDoc}
     * @since 1.1
     */
	@Override
	public void flushCache(IDMContext context) {
        fMICommandCache.reset(context);
       	fStackDepthCache.clear(context);
       	fCachedStoppedEvent = null;
	}

}
