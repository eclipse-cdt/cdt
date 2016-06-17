/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
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
	public void setGDBSelection(Object[] selection) {
		assert selection != null;
		for (Object s : selection) {
			if (s instanceof IExecutionDMContext) {
				setCurrentGDBThread((IExecutionDMContext)s);
			}
			else if (s instanceof IFrameDMContext) {
				setCurrentGDBStackFrame((IFrameDMContext)s);
			}
			else {
				assert false;
			}
		}
	}
	
	protected void setCurrentGDBThread(IExecutionDMContext execCtx) {
		if (!(execCtx instanceof IMIExecutionDMContext)) {
			return;
		}
		
		if (!execCtx.equals(fCurrentThreadCtx)) {
			fCurrentThreadCtx = execCtx;
			if (isSyncEnabled()) {
				// have GDB change its current selected thread
				this.getSession().getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						// Create a mi-thread-select and send the command
						ICommand<MIInfo> command =  fCommandFactory.createMIThreadSelect(execCtx, getCurrentThreadId());
						fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo> () {});
					}
				});
			}
		}
	}

	protected void setCurrentGDBStackFrame(IFrameDMContext frameCtx) {
		// a stack frame was selected. If it was required, we already switched the thread above, now take
		// care of the stack frame
		if (!frameCtx.equals(fCurrentStackFrameCtx)) {
			fCurrentStackFrameCtx = frameCtx;
			if (isSyncEnabled()) {
				// have GDB change its current selected frame
				this.getSession().getExecutor().execute(new DsfRunnable() {
					@Override
					public void run() {
						// Create a mi-stack-select-frame and send the command
						ICommand<MIInfo> command = fCommandFactory.createMIStackSelectFrame(frameCtx, fCurrentStackFrameCtx.getLevel());
						fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo>() {
						});
					}
				});
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
    				fCurrentThreadCtx = createExecContext(tid);
    				fCurrentStackFrameCtx = createFrameContext(fCurrentThreadCtx, frameLevel);
    				
    				createAndDispatchThreadSwitchedEvent();
    			}
    		}
    	}
	}

	protected void createAndDispatchThreadSwitchedEvent() {
		if (isSyncEnabled()) {
			// create DSF event and dispatch
			fCommandControl.getSession().dispatchEvent(new ThreadFrameSwitchedEvent(fCurrentThreadCtx, fCurrentStackFrameCtx), 
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
		createAndDispatchThreadSwitchedEvent();
	}


	@Override
	public void setSyncEnabled(boolean enabled) {
		fSyncEnabled = enabled;
	}
	
	protected boolean isSyncEnabled() {
		return fSyncEnabled;
	}

	// convenience method that returns the current thread id
	protected String getCurrentThreadId() {
		if (fCurrentThreadCtx != null && fCurrentThreadCtx instanceof IMIExecutionDMContext) {
			return ((IMIExecutionDMContext)fCurrentThreadCtx).getThreadId();
		}
		return null;
	}
	// convenience method that returns the current stack frame id
	protected String getCurrentStackFrameId() {
		if (fCurrentStackFrameCtx != null) {
			return String.valueOf(fCurrentStackFrameCtx.getLevel());
		}
		return null;
	}

	@Override
	public Object[] getSelection() {
		return new Object[] { fCurrentThreadCtx, fCurrentStackFrameCtx };
	}
	
}
