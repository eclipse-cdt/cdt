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
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
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
 * This service keeps synchronized the CDT Debug View selection and GDB's 
 * internal focus. 
 *
 * To keep the Debug View selection synchronized to CDT's selection, the service keeps 
 * track of what is the current GDB focus, by listening to the GDB MI notification 
 * "=thread-selected". When this notification is received, the service orders a change 
 * to CDT's Debug View selection to match, by sending an IGDBFocusChangedEvent. 
 * 
 * To keep GDB's focus synchronized to the Debug View selections, the UI listens to 
 * platform 'Debug Selection changed' events, and then uses this service, to order GDB 
 * to change focus to match the selection. 
 * 
 * Note: the mapping between the DV selection and GDB focus is not 1 to 1; there can
 * be multiple debug sessions at one time, all shown in the DV. There is however a single
 * effective DV selection. On the other end, each debug session has a dedicated instance
 * of GDB, having its own unique focus, at any given time. Also not all DV selections map 
 * to a valid GDB focus. 
 * 
 * @since 5.1
 */
public class GDBSynchronizer extends AbstractDsfService implements IGDBSynchronizer, IEventListener
{
	private class GDBFocusChangedEvent extends AbstractDMEvent<IExecutionDMContext> 
	implements IGDBFocusChangedEvent 
	{
		public GDBFocusChangedEvent(IExecutionDMContext ctx) {
			super(ctx);
		}
	}

	private IExecutionDMContext fCurrentThreadCtx;	
	private IFrameDMContext fCurrentStackFrameCtx;
	
	private ICommandControlService fCommandControl;
	private IStack stackService;
	private IGDBProcesses fProcesses;
	private IGDBControl fGdbcontrol;
	private CommandFactory fCommandFactory;
	
	// default initial values
	private static final String THREAD_ID_DEFAULT = "1"; //$NON-NLS-1$
	private static final String STACKFRAME_ID_DEFAULT = "0"; //$NON-NLS-1$
	private static boolean SYNC_ENABLED = true;

		
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
		// TODO: the session may not be ready when this is first called, which results
		// in invalid initial contexts. We will however fall back on our feet when a 
		// selection is first made in the DV.
	    fCurrentThreadCtx = createExecContextFromThreadId(THREAD_ID_DEFAULT);
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
	public void setFocus(final Object[] focus) {
		assert focus != null;

		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				Object elem = focus[0];
				// new selection is a frame?
				if (elem instanceof IFrameDMContext) {
					// set as new current frame
					setFrameFocus((IFrameDMContext)elem,  new ImmediateRequestMonitor() {} );
				}
				// new selection is a thread?
				else if (elem instanceof IExecutionDMContext) {
					// set as new thread
					setThreadFocus((IExecutionDMContext)elem, new ImmediateRequestMonitor() {
						@Override
						protected void handleCompleted() {
							// do we already have a valid frame selection, for that new selected thread? 
							if (!getThreadFromFrame(fCurrentStackFrameCtx).equals(fCurrentThreadCtx)) 
							{
								// no, then select default frame level, for the new thread
								setFrameFocus(createFrameContext((IExecutionDMContext)elem, STACKFRAME_ID_DEFAULT), 
										new ImmediateRequestMonitor() {});
							}
						}
					} );
				}
				else {
					assert false;
				}
			}});
	}
	
	protected void setThreadFocus(final IExecutionDMContext newThread, ImmediateRequestMonitor rm) {
		if (!(newThread instanceof IMIExecutionDMContext)) {
			return;
		}
		
		final IExecutionDMContext oldThread = fCurrentThreadCtx;
		if (!newThread.equals(oldThread)) {
			if (SYNC_ENABLED) {
				// Create a mi-thread-select and send the command
				ICommand<MIInfo> command =  fCommandFactory.createMIThreadSelect(newThread, getThreadIdFromContext(newThread));
				fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo> (rm) {
					@Override
					protected void handleCompleted() {
						if (isSuccess()) {
							fCurrentThreadCtx = newThread;
						}
						rm.done();
					}
				});
			}
			else {
				fCurrentThreadCtx = newThread;
				rm.done();
			}
		}
	}

	protected void setFrameFocus(final IFrameDMContext newFrame, ImmediateRequestMonitor rm) {
		// before switch
		final IFrameDMContext oldFrame = fCurrentStackFrameCtx;
		
		// a stack frame was selected. If it was required, we already switched the thread, now take
		// care of the frame
		if (!newFrame.equals(oldFrame)) {
			if (SYNC_ENABLED && isThreadSuspended(getThreadFromFrame(newFrame))) {
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
						}
						rm.done();
					}
				});
			}
			else {
				fCurrentStackFrameCtx = newFrame;
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
    				// when a thread is running, its frame will be absent from =thread-selected.
    				// upon resume, frame 0 will be in focus
    				frameLevel = frameLevel != null ? frameLevel : STACKFRAME_ID_DEFAULT;
    				
    				// update current thread and frame
    				fCurrentThreadCtx = createExecContextFromThreadId(tid);
    				fCurrentStackFrameCtx = createFrameContext(fCurrentThreadCtx, frameLevel);
    				
    				createAndDispatchGDBFocusChangedEvent();
    			}
    		}
    	}
	}

	protected void createAndDispatchGDBFocusChangedEvent() {
		if (SYNC_ENABLED) {
			fCommandControl.getSession().dispatchEvent(new GDBFocusChangedEvent(fCurrentThreadCtx),
					fCommandControl.getProperties());
		}
	}

	/**
	 * Creates an execution context from a thread id
	 * @param tid thread id 
	 */
	@Override
	public IExecutionDMContext createExecContextFromThreadId(String tid) {
		assert tid != null;
		if (tid == null) {return null;}
		
		IContainerDMContext parentContainer = 
				fProcesses.createContainerContextFromThreadId(fCommandControl.getContext(), tid);
		IProcessDMContext processDmc = 	DMContexts.getAncestorOfType(parentContainer, IProcessDMContext.class);
		IThreadDMContext threadDmc = fProcesses.createThreadContext(processDmc, tid);
		return fProcesses.createExecutionContext(parentContainer, threadDmc, tid);
	}
	
	private IFrameDMContext createFrameContext(IExecutionDMContext execCtx, String frameNum) {
		assert execCtx != null && frameNum != null;
		if (execCtx == null || frameNum == null) {return null;}
		
		int intFrameNum = 0;
		try {
			intFrameNum = Integer.parseInt(frameNum);
		}
		catch (NumberFormatException e){
			GdbPlugin.log(e);
		}
		return ((MIStack)(stackService)).createFrameDMContext(execCtx, intFrameNum);
	}
	
	@Override
	public void restoreDVSelectionFromFocus() {
		// get debug view to select this session's current thread/frame
		createAndDispatchGDBFocusChangedEvent();
	}

	// convenience method that returns the current thread id
	protected String getCurrentThreadId() {
		if (fCurrentThreadCtx != null && fCurrentThreadCtx instanceof IMIExecutionDMContext) {
			return ((IMIExecutionDMContext)fCurrentThreadCtx).getThreadId();
		}
		return null;
	}

	// convenience method that returns the thread id from a thread context
	protected String getThreadIdFromContext(IExecutionDMContext threadCtx) {
		assert threadCtx != null;
		if (threadCtx != null && threadCtx instanceof IMIExecutionDMContext) {
			return ((IMIExecutionDMContext)threadCtx).getThreadId();
		}
		return null;
	}

	@Override
	public Object[] getFocus() {
		return new Object[] { fCurrentThreadCtx, fCurrentStackFrameCtx };
	}
	
	protected boolean isThreadSuspended(IExecutionDMContext ctx) {
		assert ctx != null;
		IRunControl runControl = getServicesTracker().getService(IRunControl.class);
		return runControl.isSuspended(ctx); 
	}
	
	private IExecutionDMContext getThreadFromFrame(IFrameDMContext frame) {
		return DMContexts.getAncestorOfType(frame, IExecutionDMContext.class);
	}
	
}
