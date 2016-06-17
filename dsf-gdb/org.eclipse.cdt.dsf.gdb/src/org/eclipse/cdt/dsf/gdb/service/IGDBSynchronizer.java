/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
 * a IThreadFrameSwitchedEvent DSF event. 
 * 
 * To keep GDB internally synchronized to Debug View selections, the UI listens to 
 * platform 'Debug Selection changed' events, and then uses this service, to order GDB 
 * to keep in sync. 
 * 
 * @since 5.1
 */
public interface IGDBSynchronizer extends IDsfService {
	/**
	 * Returns an array of contexts, representing the current selection
	 */
	Object[] getSelection();
	
	/**  
	 * Sets the service's current selection. If synchronization is enabled, a best 
	 * effort attempt will be made to propagate the selection to GDB.
	 * 
	 * @param  selection an array of objects, each a context representing a selection
	 */
	void setGDBSelection(Object[] selection);
	
	/** 
     * This event interface indicates that GDB has switched its current thread and/or frame,
     *  as a result of an event not triggered by CDT - for example a console command typed
     *  by the user. 
     */
    interface IThreadFrameSwitchedEvent extends IDMEvent<IExecutionDMContext> {
    	IFrameDMContext getCurrentFrameContext();
    }
    
    /**
     * This tells the synchronizer that the GDB console, that corresponds to this instance of the 
     * service, has been made active. This can happen when the user switches the current debug 
     * console, for example. 
     * If synchronization is enabled, the service will request that the debug view sets its 
     * selection to reflect the session's (i.e. GDB's) currently selected thread and stack frame.  
     * 
     * Note: the new active session is implied by the instance of the service being used
     */
    void consoleActivated();
    
    /**
     * This permits enabling or disabling the synchronization, between the Debug View and GDB.
     * When the synchronization is disabled, the service will still keep track of the state of 
     * both participants, but will not attempt to keep them synchronized together.
     * 
     * @param enabled whether the synchronization should be enabled or not
     */
    void setSyncEnabled(boolean enabled);
}
