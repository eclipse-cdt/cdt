package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.model.IDataModelService;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Breakpoint service provides access to information about breakpoints that 
 * are planted on a target.  It's the reposnsibility of the debugger 
 * implementation to retrieve static breakpoint data from platform APIs and to
 * install them on the target. 
 */
public interface IBreakpoints extends IDataModelService {
    /**
     * Data Model Context for a breakpoint objects.  Breakpoints service only
     * with respect to running processes/threads, therefore the breakpoint 
     * context will always have an IExecutionDMC as one of its ancestors.
     */
    public interface IBreakpointDMC extends IDataModelContext<BreakpointData> {}
    
    /** Indicates that the state of given breakpoint has changed. */
    public interface BreakpointChangedEvent extends IDataModelEvent<IBreakpointDMC> {}
    
    /** Indicates that given breakpoint was hit, by the given execution context. */
    public interface BreakpointHitEvent extends BreakpointChangedEvent {
        IRunControl.IExecutionDMC getExecutionDMC();
    }
    
    /** Common breakpoint state data. */
    public interface BreakpointData extends IDataModelData {
        /** Installed status values of a breakpoint. */
        public enum Status { FILTERED_OUT, INSTALLED, FAILED_TO_INSTALL, PENDING_INSTALL, PENDING_UNINSTALL }
        
        /** Returns the corresponding platform debug model breakpoint object. */
        IBreakpoint getBreakpoint();
        
        /** Returns current installed status of this breakpoint */
        Status getStatus();
        
        /** 
         * Returns error message (if any) with respect to current status of 
         * the breakpoint. 
         */
        String getErrorMessage();

        /** 
         * Returns a warning message (if any) with respect to current status of 
         * the breakpoint. 
         */
        String getWarningMessage();
    }

    /**
     * Retrieves all breakpoints for given execution context.
     */
    void getBreakpoints(IRunControl.IExecutionDMC execCtx, GetDataDone<IBreakpointDMC[]> done);
    
    /**
     * Retrieves all breakpoints for given platform breakpoint object.
     */
    void getBreakpoints(IBreakpoint bp, GetDataDone<IBreakpointDMC[]> done);
}
