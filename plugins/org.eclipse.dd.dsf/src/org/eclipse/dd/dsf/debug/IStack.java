package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * Stack service provides access to stack information for a 
 * given execution context.
 */
public interface IStack extends IDataModelService {

    /**
     * Context for a specific stack frame.  Besides allowing access to stack
     * frame data, this context is used by other services that require a stack
     * frame for evaluation.  
     */
    public interface IFrameDMC extends IDataModelContext<IFrameData> {}

    /**
     * Stack frame information. 
     */
    public interface IFrameData extends IDataModelData {
        IAddress getAddress();
        String getFile();
        int getLine();
        int getColumn();
    }
    
    /**
     * Variable context.  This context only provides access to limited 
     * expression information.  For displaying complete information, 
     * Expressions service should be used.
     */
    public interface IVariableDMC extends IDataModelContext<VariableData> {}

    /** 
     * Stack frame variable information.
     */
    public interface VariableData extends IDataModelData {
        String getName();
        String getTypeName();
        String getValue();
    }

    /**
     * Retrieves list of stack frames for the given execution context.  Request
     * will fail if the stack frame data is not available.
     */
    void getFrames(IRunControl.IExecutionDMC execContext, GetDataDone<IFrameDMC[]> done);
    
    /**
     * Retrieves variables which were arguments to the stack frame's function.
     */
    void getArguments(IFrameDMC frameCtx, GetDataDone<IVariableDMC[]> done);
    
    /**
     * Retrieves variables local to the stack frame.
     */
    void getLocals(IFrameDMC frameCtx, GetDataDone<IVariableDMC[]> done);
}
