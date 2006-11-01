/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.service.IDsfService;


/**
 * Service representing an external process that is part of the debugger back
 * end implementation.  Having this service allows UI and other clients 
 * access to the java.lang.Process object for monitoring.  E.g. for displaying
 * in Debug view and channeling I/O to the console view.
 */
public interface IBackEndProcess extends IDsfService {
    /**
     * Optional property identifying the process among other services.
     * Since there could be multiple instances of this service running at the
     * same time, a service property is needed to allow clients to distinguish
     * between them. 
     */
    static final String PROCESS_ID = "org.eclipse.dsdp.DSF.debug.BackendProcess.PROCESS_ID";
    
    /**
     * Event indicating that the back end process has terminated.
     */
    public interface IExitedEvent {}

    /**
     * Returns the instance of the java process object representing the back
     * end process.
     * @return
     */
    Process getProcess();
    
    /** 
     * Returns true if back-end process has exited.
     */
    boolean isExited();
}
