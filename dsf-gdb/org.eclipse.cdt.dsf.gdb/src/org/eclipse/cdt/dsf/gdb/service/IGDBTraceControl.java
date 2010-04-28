/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * The TraceControl service provides access to the debugger Tracing functionality.
 * It is used to do such things as start and stop tracing.
 * 
 * As this functionality is very new to GDB itself, this interface is likely 
 * to change a little in the next release of CDT.
 * 
 * @since 3.0
 */
public interface IGDBTraceControl extends IDsfService {

    /**
     * Marker interface for a context on which trace operations can be performed
     */
    public interface ITraceTargetDMContext extends IDMContext {}
    
    /**
     * Specific Trace Record context.  It describes tracing data.
     */
    @Immutable
    public interface ITraceRecordDMContext extends IDMContext {}
    
    /**
     * This is the model data interface that corresponds to ITraceRecordDMContext.
     * The content of the data is backend-specific and therefore is not specified here.
     */
    public interface ITraceRecordDMData extends IDMData {
    }

    /**
	 * Trace events
	 */
    public interface ITracingSupportedChangeDMEvent extends IDMEvent<ITraceTargetDMContext> {
    	boolean isTracingSupported();
    }
    public interface ITracingStartedDMEvent extends IDMEvent<ITraceTargetDMContext> {}
    public interface ITracingStoppedDMEvent extends IDMEvent<ITraceTargetDMContext> {}
    public interface ITraceRecordSelectedChangedDMEvent extends IDMEvent<ITraceRecordDMContext> {}

    /**
     * Returns whether tracing can be started on the specified trace target
     */
    public void canStartTracing(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm);
    /**
     * Start tracing on the specified trace target
     */
    public void startTracing(ITraceTargetDMContext context, RequestMonitor rm);
    /**
     * Returns whether tracing can be stopped on the specified trace target
     */
    public void canStopTracing(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm);
    /**
     * Stop tracing on the specified trace target
     */
    public void stopTracing(ITraceTargetDMContext context, RequestMonitor rm);
    /**
     * Returns true if tracing is ongoing.
     */
    public void isTracing(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm);
    
    /**
     * Returns true if trace data can be saved to file
     */
    public void canSaveTraceData(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm);

    /**
     * Save trace data (all trace records) to the specified file in a format suitable for {@link loadTraceData}
     * If 'remoteSave' is true, the storage will be done on the target.
     */
    public void saveTraceData(ITraceTargetDMContext context,
    						  String file, boolean remoteSave,
    						  RequestMonitor rm);

    /**
     *  Returns true if trace data can be loaded from a file
     */
    public void canLoadTraceData(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm);

    /**
     * Load trace data (all trace records) from the specified file.  A file created from a call to 
     * {@link saveTraceData} should have the correct format to be loaded by this call.
     */
    public void loadTraceData(ITraceTargetDMContext context,
    						  String file,
    						  RequestMonitor rm);

    
    /**
     * Request that the backend use the specified trace record.
     */
    public void selectTraceRecord(ITraceRecordDMContext context, RequestMonitor rm);
    
    public void getTraceRecordData(ITraceRecordDMContext context, DataRequestMonitor<ITraceRecordDMData> rm);

    /////////////////////////////////////////////////
    // GDB specific part
    /////////////////////////////////////////////////
    
	public static enum STOP_REASON_ENUM { REQUEST, PASSCOUNT, OVERFLOW, DISCONNECTION, ERROR, UNKNOWN };

    public interface ITraceStatusDMData extends IDMData {
    	boolean isTracingSupported();
    	boolean isTracingActive();
    	int     getNumberOfCollectedFrame();
    	int     getTotalBufferSize();
    	int     getFreeBufferSize();
		
    	STOP_REASON_ENUM getStopReason();

		/**
		 * Returns the id of the tracepoint that caused the stop.
		 * Should be null if getStopReason is null
		 */
		Integer getStoppingTracepoint();
    }
    
    public interface ITraceVariableDMData extends IDMData {
    	String getName();
    	String getValue();
    	String getInitialValue();
    }
    
    /**
     * Request the tracing status of the specified trace target
     */
    public void getTraceStatus(ITraceTargetDMContext context, DataRequestMonitor<ITraceStatusDMData> rm);
    
    /**
     * Create a new trace state variable with an optional value
     */
    public void createTraceVariable(ITraceTargetDMContext context, 
    								String varName,
    								String varValue, 
    								RequestMonitor rm);

    /**
     * Get a list of all trace state variables and their values
     */
    public void getTraceVariables(ITraceTargetDMContext context, DataRequestMonitor<ITraceVariableDMData[]> rm);

	public ITraceRecordDMContext createTraceRecordContext(ITraceTargetDMContext ctx, int index);
	public void getCurrentTraceRecordContext(ITraceTargetDMContext context, DataRequestMonitor<ITraceRecordDMContext> drm);
	public ITraceRecordDMContext createNextRecordContext(ITraceRecordDMContext ctx);
	public ITraceRecordDMContext createPrevRecordContext(ITraceRecordDMContext ctx);
}
