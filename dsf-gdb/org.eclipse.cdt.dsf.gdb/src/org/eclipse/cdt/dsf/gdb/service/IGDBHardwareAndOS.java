/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * <strong>EXPERIMENTAL</strong>.  This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * 
 * The IGDBHardware service provides access to information about the target
 * hardware, such as the number of cores.
 * 
 * @since 4.1
 */
public interface IGDBHardwareAndOS extends IDsfService {

    /**
     * The physical target that has CPUs and Cores.
     */
    public interface IHardwareTargetDMContext extends IDMContext {}

    /**
     * A physical container of cores.
     */
    public interface ICPUDMContext extends IDMContext {
    	String getId();
    }

    /**
     * A logical core.  Without SMT (Simultaneous Multi-Threading),
     * a logical core is a physical core.  However, with SMT, each
     * physical core will have two logical cores.  
     * This context represents each logical core.   
     */
    public interface ICoreDMContext extends IDMContext {
    	String getId();
    }
    
    /**
     * Model data interface corresponding to ICPUDMContext.
     */
    public interface ICPUDMData extends IDMData {
    	/** Number of cores contained in this CPU */
    	int getNumCores();
    }

    /**
     * Model data interface corresponding to ICoreDMContext.
     */
    public interface ICoreDMData extends IDMData {
    	/**
    	 * The physical id of the core.  Multiple cores can have
    	 * the same physical id in the case of Simultaneous Multi-Threading.
    	 */
    	String getPhysicalId();
    }

    //
    // Events, e.g., a core halting, starting, etc
    //
    
    /**
     * Returns an array of CPUs, based on the specified context.
     * 
     * @param context The context to which this method applies.
     */
    public void getCPUs(IHardwareTargetDMContext context, DataRequestMonitor<ICPUDMContext[]> rm);

    /**
     * Returns an array of cores, based on the specified context.
     * 
     * @param context The context to which this method applies.
     *                For an IHardwareTargetDMContext, all cores will be returned;
     *                for an ICPUDMContext, the cores on that CPU will be returned.
     */
    public void getCores(IDMContext context, DataRequestMonitor<ICoreDMContext[]> rm);
    
    /**
     * Retrieves data for a given ICPUDMContext or ICoreDMContext context.
     */
    public void getExecutionData(IDMContext dmc, DataRequestMonitor<IDMData> rm);
    
    /**
     * Create a CPU context.
     * 
     * @param targetDmc The parent context of this context
     * @param CPUId The id of the CPU
     */
    public ICPUDMContext createCPUContext(IHardwareTargetDMContext targetDmc, String CPUId);

    /**
     * Create a core context.
     * 
     * @param cpuDmc The parent CPU context of this context
     * @param coreId The id of the core
     */
    public ICoreDMContext createCoreContext(ICPUDMContext cpuDmc, String coreId);

}
