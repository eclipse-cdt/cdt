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
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
	/** This service's opinion of what is the current GDB focus - it can be 
	 * a thread or stack frame context */
	private IDMContext fCurrentGDBFocus;
	
	private IStack fStackService;
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
		fProcesses = getServicesTracker().getService(IGDBProcesses.class);
		fStackService = getServicesTracker().getService(IStack.class);
		fGdbcontrol = getServicesTracker().getService(IGDBControl.class);
		fCommandFactory = fGdbcontrol.getCommandFactory();

		register(new String[] { IGDBSynchronizer.class.getName()},
				new Hashtable<String, String>());

		fGdbcontrol.addEventListener(this);
		getSession().addServiceEventListener(this, null);
		
	    // set a sane initial value for current GDB focus. 
		// This value will be updated when the session has finished launching. 
		// See updateContexts() below.
		fCurrentGDBFocus = createThreadContextFromThreadId(THREAD_ID_DEFAULT);
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		fGdbcontrol.removeEventListener(this);
		getSession().removeServiceEventListener(this);
		
		unregister();
		super.shutdown(requestMonitor);
	}
	
	
	@Override
	public void setFocus(final IDMContext[] focus, RequestMonitor rm) {
		assert focus != null;
		// new Debug View thread or stack frame selection
		IDMContext elem = focus[0];

		// new selection is a frame?
		if (elem instanceof IFrameDMContext) {
			final IFrameDMContext finalFrameCtx = (IFrameDMContext)elem; 
			
			setFrameFocus(finalFrameCtx, new ImmediateRequestMonitor(rm) {
				@Override
				public void handleCompleted() {
					// update current focus, no matter if the frame-select command
					// sent to gdb succeeded or not
					fCurrentGDBFocus = finalFrameCtx;
					rm.done();
				}
			});
		}
		// new selection is a thread?
		else if (elem instanceof IMIExecutionDMContext) {
			final IMIExecutionDMContext finalThreadCtx = (IMIExecutionDMContext)elem;
			
			setThreadFocus(finalThreadCtx, new ImmediateRequestMonitor(rm) {
				@Override
				protected void handleCompleted() {
					// update current focus, no matter if the thread-select command
					// sent to gdb succeeded or not
					fCurrentGDBFocus = finalThreadCtx;
					rm.done();
				}
			});
		}
		else {
			assert false;
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INVALID_HANDLE, "Invalid context to set focus to", null)); //$NON-NLS-1$);
			return;
		}
	}
	
	protected void setThreadFocus(final IMIExecutionDMContext newThread, RequestMonitor rm) {
		if (newThread == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INVALID_HANDLE, "GdbSynchronizer unable to resolve thread context for the selected element", null)); //$NON-NLS-1$
			return;
		}
		
		// Create a mi-thread-select and send the command
		ICommand<MIInfo> command = fCommandFactory.createMIThreadSelect(fGdbcontrol.getContext(), getThreadIdFromContext(newThread));
		fGdbcontrol.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo> (rm) {});
	}

	protected void setFrameFocus(final IFrameDMContext newFrame, RequestMonitor rm) {
		if (newFrame == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INVALID_HANDLE, "GdbSynchronizer unable to resolve frame context for the selected element", null)); //$NON-NLS-1$
			return;
		}
		
		// a stack frame was selected. If it was required, we already switched the thread, now take
		// care of the frame
		if (isThreadSuspended(getThreadFromFrame(newFrame))) {
			// Create a mi-stack-select-frame and send the command
			ICommand<MIInfo> command = fCommandFactory.createMIStackSelectFrame(getThreadFromFrame(newFrame), newFrame.getLevel());
			fGdbcontrol.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo>(rm) {});
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
		if (runControl != null) {
			return runControl.isSuspended(ctx);
		}
		else {
			return false;
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

    				// update current focus
    				if (frameLevel == null) {
    					// thread running - current focus is a thread
    					fCurrentGDBFocus = createThreadContextFromThreadId(tid);;
    				}
    				else {
    					// thread suspended - current focus is a stack frame
    					fCurrentGDBFocus = createFrameContext(createThreadContextFromThreadId(tid), frameLevel);
    				}
    				
    				createAndDispatchGDBFocusChangedEvent();
    			}
    		}
    	}
	}

	private void createAndDispatchGDBFocusChangedEvent() {
		assert fCurrentGDBFocus != null;

		fGdbcontrol.getSession().dispatchEvent(new GDBFocusChangedEvent(fCurrentGDBFocus),
				fGdbcontrol.getProperties());
	}

    /**
	 * Creates an execution context from a thread id
	 * 
	 * @param tid The thread id on which the execution context is based
	 */
	private IMIExecutionDMContext createThreadContextFromThreadId(String tid) {
		assert tid != null;
		
		IContainerDMContext parentContainer = 
				fProcesses.createContainerContextFromThreadId(fGdbcontrol.getContext(), tid);
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
		return new IDMContext[] { fCurrentGDBFocus };
	}
	
	@DsfServiceEventHandler
	public void updateContexts(DataModelInitializedEvent event) {
		// the debug session has finished launching - update the current focus
		// to something sane. i.e. thread1 or thread1->frame0

		IMIExecutionDMContext threadCtx = createThreadContextFromThreadId(THREAD_ID_DEFAULT);
	    
	    if (!isThreadSuspended(threadCtx)) {
	    	fCurrentGDBFocus = threadCtx;
	    }
	    else {
	    	fCurrentGDBFocus = createFrameContext(threadCtx, STACKFRAME_ID_DEFAULT);
	    }
	}
}
