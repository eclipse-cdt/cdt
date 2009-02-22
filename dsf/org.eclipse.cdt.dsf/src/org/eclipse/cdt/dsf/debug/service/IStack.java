/*******************************************************************************
 * Copyright (c) 2006,, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMService;

/**
 * Stack service provides access to stack information for a 
 * given execution context.
 */
public interface IStack extends IDMService {

    /**
     * Context for a specific stack frame.  Besides allowing access to stack
     * frame data, this context is used by other services that require a stack
     * frame for evaluation.  
     */
    public interface IFrameDMContext extends IDMContext {
        int getLevel();
    }

    /**
     * Stack frame information. 
     */
    public interface IFrameDMData extends IDMData {
        IAddress getAddress();
        String getFile();
        String getFunction();
        int getLine();
        int getColumn();
        /**
         * @since 2.0
         */
		String getModule();
    }
    
    /**
     * Variable context.  This context only provides access to limited 
     * expression information.  For displaying complete information, 
     * Expressions service should be used.
     */
    public interface IVariableDMContext extends IDMContext {}

    /** 
     * Stack frame variable information.
     */
    public interface IVariableDMData extends IDMData {
        String getName();
        String getValue();
    }

    /**
     * Retrieves stack frame data for given context.
     * @param frameDmc Context to retrieve data for.
     * @param rm Request completion monitor.
     */    
    public void getFrameData(final IFrameDMContext frameDmc, DataRequestMonitor<IFrameDMData> rm);

    /**
     * Retrieves stack frame variable data for given context.
     * @param variableDmc Context to retrieve data for.
     * @param rm Request completion monitor.
     */    
    public void getVariableData(IVariableDMContext variableDmc, DataRequestMonitor<IVariableDMData> rm);
    
    /**
     * Retrieves list of stack frames for the given execution context.  Request
     * will fail if the stack frame data is not available.
     */
    void getFrames(IDMContext execContext, DataRequestMonitor<IFrameDMContext[]> rm);
    
    /**
     * Retrieves the top stack frame for the given execution context.  
     * Retrieving just the top frame DMC and corresponding data can be much 
     * more efficient than just retrieving the whole stack, before the data
     * is often included in the stopped event.  Also for some UI functionality, 
     * such as setpping, only top stack frame is often needed. 
     * @param execContext
     * @param rm
     */
    void getTopFrame(IDMContext execContext, DataRequestMonitor<IFrameDMContext> rm);
    
    /**
     * Retrieves variables which were arguments to the stack frame's function.
     */
    void getArguments(IFrameDMContext frameCtx, DataRequestMonitor<IVariableDMContext[]> rm);
    
    /**
     * Retrieves variables local to the stack frame, including arguments.
     */
    void getLocals(IFrameDMContext frameCtx, DataRequestMonitor<IVariableDMContext[]> rm);
    
    /**
     * Retrieves the number of stack frames available for the given context..
     * @param dmc Context to retrieve data for.
     * @param The maximum depth of stack to calculate.  Should be 0 to calculate 
     * depth with no limit.
     * @param rm Callback 
     */
    void getStackDepth(IDMContext dmc, int maxDepth, DataRequestMonitor<Integer> rm);

        
}
