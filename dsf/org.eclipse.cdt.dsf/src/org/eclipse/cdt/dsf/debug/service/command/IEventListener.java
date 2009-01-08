package org.eclipse.cdt.dsf.debug.service.command;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;

/**
 * Synchronous listener for events issued from the debugger.  All 
 * registered listeners will be called in the same dispatch cycle.
 */

@ConfinedToDsfExecutor("")
public interface IEventListener {
    /**
     * Notifies that the given asynchronous output was received from the 
     * debugger.
     * @param output output that was received from the debugger.  Format
     * of the output data is debugger specific.
     */
    public void eventReceived(Object output);
}
