package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * This service helps keeping in sync the CDT debug view selection and 
 * GDB's internal selection - thread, stack frame, and (implicitly) inferior.
 *  
 * This service attempts to keep track of what is the current GDB internal selection, 
 * by listening to related GDB MI Events (=thread-selected).
 * When GDB's internal selection is changed by an external force (i.e. not through an
 * MI command) this service changes the CDT Debug View selection to match, by sending 
 * a IThreadSwitchedEvent DSF event. 
 * 
 * To keep GDB internally synchronized to Debug View selection, this service 
 * works in collaboration with the console view (GDB >= 7.12). The console listens to 
 * platform 'Debug Selection changed' events, and then uses this service, to order GDB 
 * to keep in sync. 
 * 
 * @since 5.1
 */
public interface IGDBSynchronizer extends IDsfService {
	
	String getCurrentThreadId();
	String getCurrentStackFrameId();
	
	/**  
	 * Best effort attempt to change the current internal GDB thread. Calling this method
	 * with the thread already selected has no effect. 
	 */
	void setCurrentGDBThread(IExecutionDMContext ctx);
	/**
	 * Best effort attempt to change the current internal GDB stack frame, of the current thread. 
	 * Calling this method with the stack frame already selected has no effect. 
	 */
	void setCurrentGDBStackFrame(IFrameDMContext ctx);
	
	/** 
     * This event indicates that gdb has switched its current thread, as a result 
     * of an event not triggered by CDT - For example a console command. 
     */
    interface IThreadSwitchedEvent extends IDMEvent<IExecutionDMContext> {}
    
    /** 
     * This event indicates that gdb has switched its current stack frame, as a result 
     * of an event not triggered by CDT - For example a console command.
     */
    interface IStackFrameSwitchedEvent extends IDMEvent<IFrameDMContext> {}
    
}
