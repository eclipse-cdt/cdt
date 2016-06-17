/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
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
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
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
 * @since 5.2
 */
public class GDBSynchronizer extends AbstractDsfService implements IGDBSynchronizer, IEventListener
{
	private IMIExecutionDMContext fCurrentThreadCtx;	
	private IFrameDMContext fCurrentStackFrameCtx;
	
	private ICommandControlService fCommandControl;
	private MIStack fStackService;
	private IGDBProcesses fProcesses;
	private IGDBControl fGdbcontrol;
	private CommandFactory fCommandFactory;
	
	// default initial values
	private static final String THREAD_ID_DEFAULT = "1"; //$NON-NLS-1$
	private static final String STACKFRAME_ID_DEFAULT = "0"; //$NON-NLS-1$
		
	public GDBSynchronizer(DsfSession session) {
		super(session);
	}
	
	private class GDBFocusChangedEvent extends AbstractDMEvent<IDMContext> 
	implements IGDBFocusChangedEvent 
	{
		public GDBFocusChangedEvent(IDMContext ctx) {
			super(ctx);
		}
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
		fStackService = getServicesTracker().getService(MIStack.class);
		fGdbcontrol = getServicesTracker().getService(IGDBControl.class);
		fCommandFactory = fGdbcontrol.getCommandFactory();

		register(new String[] { IGDBSynchronizer.class.getName()},
				new Hashtable<String, String>());

		fCommandControl.addEventListener(this);
		getSession().addServiceEventListener(this, null);
		
	    // set some sane initial values.
		// note: the session may not be ready when this is first called, which results
		// in invalid initial contexts. These values will be updated when the session has
		// finished launching. See updateContexts() below.
	    fCurrentThreadCtx = (IMIExecutionDMContext) createExecContextFromThreadId(THREAD_ID_DEFAULT);
	    fCurrentStackFrameCtx = createFrameContext(fCurrentThreadCtx, STACKFRAME_ID_DEFAULT);
	    
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		fCommandControl.removeEventListener(this);
		getSession().removeServiceEventListener(this);
		
		unregister();
		super.shutdown(requestMonitor);
	}
	
	
	@Override
	public void setFocus(final IDMContext[] focus, RequestMonitor rm) {
		assert focus != null;
		IDMContext elem = focus[0];

		// As of GDB 7.12, we know that there are still some sync problems, like MI
		// commands with "--thread" and "--frame" parameters having the side-
		// effect of changing GDB's internal selection. So we play it safe here by
		// always asking GDB to set the focus of both the thread and stack frame, even 
		// when we think GDB already has the same focus as us - this way we can fall back 
		// on our feet quicker if GDB's focus is changed without us being notified.
		IFrameDMContext frameCtx = null;
		IMIExecutionDMContext threadCtx = null;

		// new selection is a frame?
		if (elem instanceof IFrameDMContext) {
			frameCtx = (IFrameDMContext)elem;
			threadCtx = getThreadFromFrame(frameCtx);
		}
		// new selection is a thread?
		else if (elem instanceof IMIExecutionDMContext) {
			threadCtx = (IMIExecutionDMContext)elem; 
			frameCtx = createFrameContext((IExecutionDMContext)elem, STACKFRAME_ID_DEFAULT);
		}
		else {
			assert false;
			return;
		}

		final IFrameDMContext finalFrameCtx = frameCtx;
		final IMIExecutionDMContext finalThreadCtx = threadCtx;
		// proceed to set thread and frame focus
		setThreadFocus(threadCtx, new ImmediateRequestMonitor(rm) {
			// if setting the thread fails we try setting the frame which will include the --thread flag.
			// This can be seen as a second attempt to select the thread and a first attempt to select the 
			// frame, however the selection of the frame will only succeed if the thread is stopped.
			@Override
			protected void handleCompleted() {
				setFrameFocus(finalFrameCtx, new ImmediateRequestMonitor(rm) {
					@Override
					public void handleCompleted() {
						// update current frame/thread, no matter if setThreadFocus() 
						// and setFrameFocus() succeeded or not
						fCurrentStackFrameCtx = finalFrameCtx;
						fCurrentThreadCtx = finalThreadCtx;
						rm.done();
					}
				});
			}
		} );
	}
	
	protected void setThreadFocus(final IMIExecutionDMContext newThread, RequestMonitor rm) {
		if (!(newThread != null)) {
			GdbPlugin.logErrorMessage("GdbSynchronizer unable to resolve thread context for the selected element"); //$NON-NLS-1$
			rm.done();
			return;
		}
		
		ICommandControlDMContext cmd = DMContexts.getAncestorOfType(newThread, ICommandControlDMContext.class);

		// Create a mi-thread-select and send the command
		ICommand<MIInfo> command = fCommandFactory.createMIThreadSelect(cmd, getThreadIdFromContext(newThread));
		fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo> (rm) {});
	}

	protected void setFrameFocus(final IFrameDMContext newFrame, RequestMonitor rm) {
		if (newFrame == null) {
			GdbPlugin.logErrorMessage("GdbSynchronizer unable to resolve frame context for the selected element"); //$NON-NLS-1$
			rm.done();
			return;
		}
		
		// a stack frame was selected. If it was required, we already switched the thread, now take
		// care of the frame
		if (isThreadSuspended(getThreadFromFrame(newFrame))) {
			// Create a mi-stack-select-frame and send the command
			ICommand<MIInfo> command = fCommandFactory.createMIStackSelectFrame(getThreadFromFrame(newFrame), newFrame.getLevel());
			fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo>(rm) {});
		}
		else {
			rm.done();
		}
	}
	
	private IMIExecutionDMContext getThreadFromFrame(IFrameDMContext frame) {
		return DMContexts.getAncestorOfType(frame, IMIExecutionDMContext.class);
	}
	
	// convenience method that returns the thread id from a thread context
	private String getThreadIdFromContext(IMIExecutionDMContext threadCtx) {
		assert threadCtx != null;
		if (threadCtx != null) {
			return threadCtx.getThreadId();
		}
		return null;
	}

	private boolean isThreadSuspended(IExecutionDMContext ctx) {
		assert ctx != null;
		IRunControl runControl = getServicesTracker().getService(IRunControl.class);
		return runControl.isSuspended(ctx); 
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

	private void createAndDispatchGDBFocusChangedEvent() {
		assert fCurrentThreadCtx != null;

		fCommandControl.getSession().dispatchEvent(new GDBFocusChangedEvent(fCurrentThreadCtx),
				fCommandControl.getProperties());
	}

    /**
	 * Creates an execution context from a thread id
	 * 
	 * @param tid The thread id on which the execution context is based
	 */
	private IMIExecutionDMContext createExecContextFromThreadId(String tid) {
		assert tid != null;
		
		IContainerDMContext parentContainer = 
				fProcesses.createContainerContextFromThreadId(fCommandControl.getContext(), tid);
		IProcessDMContext processDmc = 	DMContexts.getAncestorOfType(parentContainer, IProcessDMContext.class);
		IThreadDMContext threadDmc = fProcesses.createThreadContext(processDmc, tid);
		return fProcesses.createExecutionContext(parentContainer, threadDmc, tid);
	}
	
	private IFrameDMContext createFrameContext(IExecutionDMContext execCtx, String frameNum) {
		assert execCtx != null && frameNum != null;
		if (execCtx == null || frameNum == null) {
			return null;
		}
		
		int intFrameNum = 0;
		try {
			intFrameNum = Integer.parseInt(frameNum);
		}
		catch (NumberFormatException e){
			GdbPlugin.log(e);
		}
		return fStackService.createFrameDMContext(execCtx, intFrameNum);
	}
	
	@Override
	public void sessionSelected() {
		// get debug view to select this session's current thread/frame
		createAndDispatchGDBFocusChangedEvent();
	}

	@Override
	public IDMContext[] getFocus() {
		return new IDMContext[] { fCurrentThreadCtx, fCurrentStackFrameCtx };
	}
	
	
	@DsfServiceEventHandler
	public void updateContexts(DataModelInitializedEvent event) {
		// the debug session has finished launching - 
		// update the current thread and stack frame contexts
		fCurrentThreadCtx = createExecContextFromThreadId(THREAD_ID_DEFAULT);
	    fCurrentStackFrameCtx = createFrameContext(fCurrentThreadCtx, STACKFRAME_ID_DEFAULT);
	}
}
