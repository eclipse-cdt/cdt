package org.eclipse.dd.dsf.debug;

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
    static final String PROCESS_ID = "org.eclipse.dsdp.riverbed.debug.BackendProcess.PROCESS_ID";
    
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
