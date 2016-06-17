package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

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

	public class ThreadSwitchedEvent extends AbstractDMEvent<IExecutionDMContext> 
	implements IThreadSwitchedEvent 
	{
		public ThreadSwitchedEvent(IExecutionDMContext context) {
			super(context);
		}
	}
	
	public class StackFrameSwitchedEvent extends AbstractDMEvent<IFrameDMContext> 
	implements IStackFrameSwitchedEvent 
	{
		public StackFrameSwitchedEvent(IFrameDMContext context) {
			super(context);
		}
	}

	private ICommandControlService fCommandControl;
	private IStack stackService;
	private IGDBProcesses fProcesses;
	private IGDBControl fGdbcontrol;
	private CommandFactory fCommandFactory;

	private String fCurrentThreadId;
	private String fCurrentStackFrameId;
	
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
		
	    // sane defaults
	    fCurrentThreadId = "1"; //$NON-NLS-1$
	    fCurrentStackFrameId = "0"; //$NON-NLS-1$
	    
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();		
		fCommandControl.removeEventListener(this);
		
		super.shutdown(requestMonitor);
	}
	
	@Override
	public String getCurrentThreadId() {
		return fCurrentThreadId;
	}

	@Override
	public String getCurrentStackFrameId() {
		return fCurrentStackFrameId;
	}
	
	@Override
	public void setCurrentGDBThread(IExecutionDMContext ctx) {
		String id;
		if (ctx instanceof IMIExecutionDMContext) {
			id = ((IMIExecutionDMContext)ctx).getThreadId();
		}
		else {
			return;
		}
		
		if (!id.equals(fCurrentThreadId)) {
			fCurrentThreadId = id;
			// have GDB change its current selected thread
			this.getSession().getExecutor().execute(new Runnable() {
				@Override
				public void run() {
					// Create a mi-thread-select and send the command
					ICommand<MIInfo> command =  fCommandFactory.createMIThreadSelect(ctx, fCurrentThreadId);
					fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo> () {});
				}
			});
		}
	}


	@Override
	public void setCurrentGDBStackFrame(IFrameDMContext ctx) {
		// a stack frame was selected. If it was required, we already switched the thread above, now take
		// care of the stack frame
		int frameLevel = ctx.getLevel();
		String frameId = String.valueOf(frameLevel);
		if (!frameId.equals(fCurrentStackFrameId)) {
			fCurrentStackFrameId = frameId;
			
			// have GDB change its current selected frame
			this.getSession().getExecutor().execute(new Runnable() {
				@Override
				public void run() {
					// Create a mi-stack-select-frame and send the command
					try {
						ICommand<MIInfo> command =  fCommandFactory.createMIStackSelectFrame(ctx, ctx.getLevel());
						fCommandControl.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo> () {});
					}
					catch (NumberFormatException e) {

					}
				}
			});
		}
	}
	
	/**  
	 * Parses gdb output for signs that there was an inferior, thread or stack frame switch.
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
    				// when a thread is suspended, its frameLevel will be undefined (null here). 
    				frameLevel = frameLevel != null ? frameLevel : "0";  //$NON-NLS-1$
    				
    				// thread stayed the same and frame changed 
    				if (tid.equals(fCurrentThreadId) && !(frameLevel.equals(fCurrentStackFrameId))) {
    					createAndDispatchStackFrameSwitchedEvent(tid, frameLevel);
    				}
    				// note: ATM GDB can generates the =thread-selected event even if in some cases 
    				//       the thread has not changed (i.e. same thread re-selected). We could 
    				//       ignore that case, but OTOH our perception of what's the current thread
    				//       might be wrong, in which case we would fail to switch, when we should. 
    				else {
    					createAndDispatchThreadSwitchedEvent(tid);
    				}
    				fCurrentThreadId = tid;
    				fCurrentStackFrameId = frameLevel;
    			}
    		}
    	}
	}

	protected void createAndDispatchThreadSwitchedEvent(String tid) {
		IContainerDMContext parentContainer = fProcesses.createContainerContextFromThreadId(fCommandControl.getContext(), tid);
		IProcessDMContext processDmc = DMContexts.getAncestorOfType(parentContainer, IProcessDMContext.class);
		IThreadDMContext threadDmc = fProcesses.createThreadContext(processDmc, tid);
		IExecutionDMContext execDmc = fProcesses.createExecutionContext(parentContainer, threadDmc, tid);
		
		// create DSF event and dispatch
		AbstractDMEvent<IExecutionDMContext> event = null;
			event = new ThreadSwitchedEvent(execDmc); 
		
    	fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
	}
	
	protected void createAndDispatchStackFrameSwitchedEvent(String tid, String frameId) {
		IContainerDMContext parentContainer = fProcesses.createContainerContextFromThreadId(fCommandControl.getContext(), tid);
		IProcessDMContext processDmc = DMContexts.getAncestorOfType(parentContainer, IProcessDMContext.class);
		IThreadDMContext threadDmc = fProcesses.createThreadContext(processDmc, tid);
		IExecutionDMContext execDmc = fProcesses.createExecutionContext(parentContainer, threadDmc, tid);
		// createFrameDMContext() is not part of the IStack interface... 
		IFrameDMContext frameDmc = ((MIStack)(stackService)).createFrameDMContext(execDmc, Integer.parseInt(frameId));
		
		// create DSF event and dispatch
		AbstractDMEvent<IFrameDMContext> event = null;
			event = new StackFrameSwitchedEvent(frameDmc); 
		
    	fCommandControl.getSession().dispatchEvent(event, fCommandControl.getProperties());
	}

}
