/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

/**
 * @since 5.1
 */
public class GDBSynchronizer extends AbstractDsfService implements IGDBSynchronizer, IEventListener
{

	/**
	 * This holds whether the synchronization is to be enforced. The service will keep track of the 
	 * selections regardless, but will not enforce synchronization when sync is disabled. Static because
	 * we want all instances of the service (sessions) to be in the same state w/r to synchronization.
	 */
	private static boolean fSyncEnabled = true;
	
	/** 
     * This event indicates that GDB has switched its current thread and/or frame,
     *  as a result of an event not triggered by CDT - for example a console command typed
     *  by the user. 
     */
	private class ThreadFrameSwitchedEvent extends AbstractDMEvent<IExecutionDMContext> implements IThreadFrameSwitchedEvent 
	{
		IFrameDMContext fFrameCtx;
		public ThreadFrameSwitchedEvent(IExecutionDMContext threadCtx, IFrameDMContext frameCtx) {
			super(threadCtx);
			fFrameCtx = frameCtx;
		}

		@Override
		public IFrameDMContext getCurrentFrameContext() {
			return fFrameCtx;
		}
	}
	
	/**
     * A generic event that causes a refresh of the VMNode corresponding to IDMContext parameter. 
     */
	private class RefreshElementEvent extends AbstractDMEvent<IDMContext> implements IRefreshElementEvent {
		public RefreshElementEvent(IDMContext context) {
			super(context);
		}
	}

	private IExecutionDMContext fCurrentThreadCtx;	
	private IFrameDMContext fCurrentStackFrameCtx;

	// default initial values for tid and stackframe number
	private static final String THREAD_ID_DEFAULT = "1"; //$NON-NLS-1$
	private static final String STACKFRAME_ID_DEFAULT = "0"; //$NON-NLS-1$
	
	private ICommandControlService fCommandControl;
	private IStack stackService;
	private IGDBProcesses fProcesses;
	private IGDBControl fGdbcontrol;
	private CommandFactory fCommandFactory;
	
	public GDBSynchronizer(DsfSession session) {
		super(session);
	}
	
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(RequestMonitor requestMonitor) {
		// obtain reference to a few needed services		
		fCommandControl = getServicesTracker().getService(ICommandControlService.class);
		fProcesses = getServicesTracker().getService(IGDBProcesses.class);
		stackService = getServicesTracker().getService(IStack.class);
		fGdbcontrol = getServicesTracker().getService(IGDBControl.class);
		fCommandFactory = fGdbcontrol.getCommandFactory();

		register(new String[] { IGDBSynchronizer.class.getName()},
				new Hashtable<String, String>());

		fCommandControl.addEventListener(this);
		
	    // set some sane initial values
	    fCurrentThreadCtx = createExecContext(THREAD_ID_DEFAULT);
	    fCurrentStackFrameCtx = createFrameContext(fCurrentThreadCtx, STACKFRAME_ID_DEFAULT);
	    
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {		
		fCommandControl.removeEventListener(this);
		
		super.shutdown(requestMonitor);
		unregister();
	}
	
	@Override
	public void setGDBSelection(final Object[] selection) {
		assert selection != null;
		
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				Object s = selection[0];
				// new selection is a frame?
				if (s instanceof IFrameDMContext) {
					// set as new current frame
					setCurrentGDBStackFrame((IFrameDMContext)s,  new ImmediateRequestMonitor() {} );
				}
				// new selection is a thread?
				else if (s instanceof IExecutionDMContext) {
					// set as new thread
					setCurrentGDBThread((IExecutionDMContext)s, new ImmediateRequestMonitor() {
						@Override
						protected void handleCompleted() {
							// do we already have a valid frame selection, for that new selected thread? 
							if (!getThreadFromFrame(fCurrentStackFrameCtx).equals(fCurrentThreadCtx)) 
							{
								// no, then select default frame level, for new thread
								setCurrentGDBStackFrame(createFrameContext((IExecutionDMContext)s, STACKFRAME_ID_DEFAULT), new ImmediateRequestMonitor() {});
							}
						}
					} );
				}
				else {
					assert false;
				}
				
			}});
	}
	
	protected void setCurrentGDBThread(final IExecutionDMContext newThread, ImmediateRequestMonitor rm) {
		if (!(newThread instanceof IMIExecutionDMContext)) {
			return;
		}
		
		final IExecutionDMContext oldThread = fCurrentThreadCtx;
		final IFrameDMContext oldFrame = fCurrentStackFrameCtx;
		
		if (!newThread.equals(oldThread)) {
			if (isSyncEnabled()) {
				// Create a mi-thread-select and send the command
				ICommand<MIInfo> command =  fCommandFactory.createMIThreadSelect(newThread, getThreadIdFromContext(newThread));
				fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo> (rm) {
					@Override
					protected void handleCompleted() {
						if (isSuccess()) {
							fCurrentThreadCtx = newThread;
							fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(oldThread), fCommandControl.getProperties());
							fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(fCurrentThreadCtx), fCommandControl.getProperties());
						}
						rm.done();
					}
				});
			}
			else {
				fCurrentThreadCtx = newThread;
				fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(oldThread), fCommandControl.getProperties());
				fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(fCurrentThreadCtx), fCommandControl.getProperties());
				rm.done();
			}
		}
	}

	protected void setCurrentGDBStackFrame(final IFrameDMContext newFrame, ImmediateRequestMonitor rm) {
		// before switch
		final IFrameDMContext oldFrame = fCurrentStackFrameCtx;
		final IExecutionDMContext oldThread = fCurrentThreadCtx;
		
		// a stack frame was selected. If it was required, we already switched the thread, now take
		// care of the frame
		if (!newFrame.equals(oldFrame)) {
			if (isSyncEnabled() && 	isThreadSuspended(getThreadFromFrame(newFrame))) {
				// Create a mi-stack-select-frame and send the command
				ICommand<MIInfo> command = fCommandFactory.createMIStackSelectFrame(newFrame, newFrame.getLevel());
				fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo>(rm) {
					@Override
					protected void handleCompleted() {
						if (isSuccess()) {
							fCurrentStackFrameCtx = newFrame;
							// note: CDT also switches the thread when doing a 
							// createMIStackSelectFrame
							fCurrentThreadCtx = getThreadFromFrame(fCurrentStackFrameCtx);
							
							// notify that the selected GDB frame has changed - one notification to update the highlight 
							// of the previously selected thread/frame, and another one for the newly selected ones 
							fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(oldFrame), fCommandControl.getProperties());
							fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(fCurrentStackFrameCtx), fCommandControl.getProperties());
							
							// implicit thread switch?
							if (!oldThread.equals(fCurrentThreadCtx)) {
								fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(oldThread), fCommandControl.getProperties());
								fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(fCurrentThreadCtx), fCommandControl.getProperties());
							}
						}
						rm.done();
					}
				});
			}
			else {
				fCurrentStackFrameCtx = newFrame;
				// notify that the selected GDB frame has changed - one notification to update the highlight 
				// of the previously selected thread/frame, and another one for the newly selected ones 
				fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(oldFrame), fCommandControl.getProperties());
				rm.done();
			}
		}
	}
	
	/**  
	 * Parses gdb output for the =thread-selected notification.
	 * When this is detected, generate a DSF event to notify listeners
	 * 
	 * example :
     * =thread-selected,id="7",frame={level="0",addr="0x000000000041eab0",func="main",args=[]}
	 */
	@Override
	public void eventReceived(Object output) {
		for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
    		if (oobr instanceof MINotifyAsyncOutput) {
    			MINotifyAsyncOutput out = (MINotifyAsyncOutput) oobr;
    			String miEvent = out.getAsyncClass();
    			if ("thread-selected".equals(miEvent)) { //$NON-NLS-1$
    				// extract tid
    				MIResult[] results = out.getMIResults();
    				String tid = null;
    				String frameLevel = null;
    				for (int i = 0; i < results.length; i++) {
    					String var = results[i].getVariable();
    					MIValue val = results[i].getMIValue();
    					
    					if (var.equals("frame") && val instanceof MITuple) { //$NON-NLS-1$
    						// dig deeper to get the frame level
    						MIResult[] res = ((MITuple)val).getMIResults();
    						
    						for (int j = 0; j < res.length; j++) {
    							var = res[j].getVariable();
    	    					val = res[j].getMIValue();
    	    					
    	    					if (var.equals("level")) { //$NON-NLS-1$
    	    						if (val instanceof MIConst) {
    	    							frameLevel = ((MIConst) val).getString();
    	    						}
    	    					}
    						}
    					}
    					else {
    						if (var.equals("id")) { //$NON-NLS-1$
    							if (val instanceof MIConst) {
    								tid = ((MIConst) val).getString();
    							}
    						}
    					}
    				}
    				
    				// tid should never be null
    				assert (tid != null);
    				if (tid == null) {
    					return;
    				}
    				// when a thread is suspended, its frame will be absent from =thread-selected. 
    				frameLevel = frameLevel != null ? frameLevel : STACKFRAME_ID_DEFAULT;
    				
    				// update current thread and SF
    				switchSelection(tid, frameLevel);
    			}
    		}
    	}
	}

	
	protected void switchSelection(String newTid, String newFrameLevel) {
		IFrameDMContext oldFrame = fCurrentStackFrameCtx;
		IExecutionDMContext oldThread = fCurrentThreadCtx;
		
		// update current thread and SF
		fCurrentThreadCtx = createExecContext(newTid);
		fCurrentStackFrameCtx = createFrameContext(fCurrentThreadCtx, newFrameLevel);
		
		createAndDispatchThreadFrameSwitchedEvent();

		createAndDispatchRefreshElementEvent(oldFrame);
		createAndDispatchRefreshElementEvent(fCurrentStackFrameCtx);
		if (!oldThread.equals(fCurrentThreadCtx)) {
			createAndDispatchRefreshElementEvent(oldThread);
			createAndDispatchRefreshElementEvent(fCurrentThreadCtx);
		}
	}
	
	
	protected void createAndDispatchThreadFrameSwitchedEvent() {
		if (isSyncEnabled()) {
			// create DSF event and dispatch
			fCommandControl.getSession().dispatchEvent(new ThreadFrameSwitchedEvent(fCurrentThreadCtx, fCurrentStackFrameCtx), 
					fCommandControl.getProperties());
		}
	}

	protected void createAndDispatchRefreshElementEvent(IDMContext ctx) {
		if (isSyncEnabled()) {
			// create DSF event and dispatch
			fCommandControl.getSession().dispatchEvent(new RefreshElementEvent(ctx), 
					fCommandControl.getProperties());
		}
	}
	
	private IExecutionDMContext createExecContext(String tid) {
		assert tid != null;
		IContainerDMContext parentContainer = 
				fProcesses.createContainerContextFromThreadId(fCommandControl.getContext(), tid);
		IProcessDMContext processDmc = 	DMContexts.getAncestorOfType(parentContainer, IProcessDMContext.class);
		IThreadDMContext threadDmc = fProcesses.createThreadContext(processDmc, tid);
		return fProcesses.createExecutionContext(parentContainer, threadDmc, tid);
	}
	
	private IFrameDMContext createFrameContext(IExecutionDMContext execCtx, String frameNum) {
		assert execCtx != null && frameNum != null;
		return ((MIStack)(stackService)).createFrameDMContext(execCtx, Integer.parseInt(frameNum));
	}
	
	
	@Override
	public void consoleActivated() {
		// get debug view to select this session's current thread/frame
		createAndDispatchThreadFrameSwitchedEvent();
	}


	@Override
	public void setSyncEnabled(boolean enabled) {
		fSyncEnabled = enabled;
	}
	
	@Override
	public boolean isSyncEnabled() {
		return fSyncEnabled;
	}

	// convenience method that returns the thread id from a thread context
	protected String getThreadIdFromContext(IExecutionDMContext threadCtx) {
		if (threadCtx != null && threadCtx instanceof IMIExecutionDMContext) {
			return ((IMIExecutionDMContext)threadCtx).getThreadId();
		}
		return null;
	}

	@Override
	public Object[] getSelection() {
		if (isSyncEnabled()) {
			return new Object[] { fCurrentThreadCtx, fCurrentStackFrameCtx };
		}
		else {
			return new Object[] { };
		}
	}
	
	protected boolean isThreadSuspended(IExecutionDMContext ctx) {
		IRunControl runControl = getServicesTracker().getService(IRunControl.class);
		return runControl.isSuspended(ctx); 
	}
	
	protected IExecutionDMContext getThreadFromFrame(IFrameDMContext frame) {
		return DMContexts.getAncestorOfType(frame, IExecutionDMContext.class);
	}
}
