/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Nokia - create and use backend service. 
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Service for controlling the back end process.
 * @since 1.1
 */
public interface IMIBackend extends IDsfService {

    public enum State { NOT_INITIALIZED, STARTED, TERMINATED };

	/**
	 * Event indicating that the back end process has started or terminated.
	 */
    @Immutable
    public static class BackendStateChangedEvent {
        final private String fSessionId;
        final private String fBackendId;
        final private State fState;
        
        public BackendStateChangedEvent(String sessionId, String backendId, State state) {
            fSessionId = sessionId;
            fBackendId = backendId;
            fState = state;
        }
        
        public String getSessionId() {
            return fSessionId;
        }
        
        public String getBackendId() {
            return fBackendId;
        }
        
        public State getState() {
            return fState;
        }
    }

    /**
     * Returns the identifier of this backend service.  It can be used 
     * to distinguish between multiple instances of this service in a 
     * single session.   
     */
    public String getId();
    
    /**
     * Requests that the backend be immediately terminated.
     */
    public void destroy();

    /**
     * Returns the current state of the backed.
     * @return
     */
    public State getState();

    /**
     * Returns the exit code of the backend.  Returns <code>-1</code> if 
     * the backend exit code is not available.
     * @return
     */
    public int getExitCode();

    /**
     * Returns the backend command stream. 
     */
    public InputStream getMIInputStream();

    /**
     * Returns the backend result and event stream.
     * @return
     */
    public OutputStream getMIOutputStream();
}
