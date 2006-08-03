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
package org.eclipse.dd.dsf.concurrent;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.DsfPlugin;

/**
 * Base class for Riverbed service method-completion callbacks.  By default
 * all callbacks that indicate a complition of a method contain the status
 * of the result.   
 * <br>NOTE: Access to the status data is not synchronized, so 
 * clients have to make sure that access to this object is thread safe if 
 * it's used outside of the caller's dispatch thread.   
 */
abstract public class Done extends DsfRunnable {
    private IStatus fStatus = Status.OK_STATUS;
 
    /** Sets the status of the called method. */
    public void setStatus(IStatus status) { fStatus = status; }
    
    /** Returns the status of the completed method. */
    public IStatus getStatus() { return fStatus; }
    
    /**
     * Convenience method for setting the status using a status object of a 
     * sub-command.
     * @param pluginId plugin id of the invoked method 
     * @param code status code
     * @param message message to include
     * @param subStatus status object to base the Done status on
     */
    public void setErrorStatus(String pluginId, int code, String message, final IStatus subStatus) {
        MultiStatus status = new MultiStatus(pluginId, code, message, null);
        status.merge(subStatus);
        fStatus = status;
    }

    /**
     * Convenience method which checks for error in done, and propagates it
     * to caller's client done.  
     * @return Returns true if there was an error that was propagated and 
     * the caller can stop processing result.
     */
    protected boolean propagateErrorToClient(DsfExecutor executor, Done clientDone, int code, String message) {
        if (clientDone.getStatus().getSeverity() == IStatus.CANCEL) {
            return true;
        }
        if (!getStatus().isOK()) {
            clientDone.setErrorStatus(DsfPlugin.PLUGIN_ID, code, message, getStatus());
            executor.execute(clientDone);
            return true;
        } 
        return false;
    }
}
