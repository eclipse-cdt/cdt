/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.service;

import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * This service keeps synchronized the CDT debug view selection and GDB's 
 * internal focus - GDB's current thread, stack frame, and (implicitly) inferior.
 * 
 * @since 5.2
 */
public interface IGDBSynchronizer extends IDsfService {
	/**
	 * Returns an array of contexts, representing the current synchronized focus
	 */
	Object[] getFocus();
	
	/**  
	 * Sets the service's current focus and propagate it to the GDB corresponding to this 
	 * service's instance, when appropriate.
	 * 
	 * @param  focus An array of objects, each a context representing a focus'ed element
	 * 		   from the Debug View
	 */
	void setFocus(Object[] focus);
	
	/** 
	 * The service sends this event to indicate that GDB has changed its focus, as a 
	 * result of an event not triggered by CDT. For example a console command typed by 
	 * the user. 
	 * Note: the full focus might not be reflected in the included context. The service
	 * can be queried to get the complete picture.
     */
    interface IGDBFocusChangedEvent extends IDMEvent<IExecutionDMContext> {}
    
    /**
     * This tells the synchronizer that the session, corresponding to this service's 
     * instance, should have its DV selection restored, according to its current focus. 
     * This can be called, for example, when a specific GDB console has become active, 
     * so that its focus becomes selected in the DV.
     * 
     * Note: the new active session is implied by the instance of the service being used
     */
    void restoreDVSelectionFromFocus();
    
    /**
	 * Creates an execution context from a thread id
	 * 
	 * @param tid The thread id on which the execution context is based
	 */
    IExecutionDMContext createExecContextFromThreadId(String tid);
    
    /**
     * Enables or disables the propagation of the synchronization of the DV selection
     * and GDB focus. This setting applies to all instances of this 
     * service, i.e. all debug sessions share the value set here.
     */
    void setSyncEnabled(boolean enable);
    
    /** 
     * 
     * @return whether the propagation of the synchronization of the DV selection
     * and GDB focus, is enabled or not.
     */
    boolean isSyncEnabled();
 
}
