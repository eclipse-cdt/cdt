/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Dmitry Kozlov (Mentor Graphics) - Enhance trace status (Bug 390827)
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
 * @since 3.0
 */
public interface IGDBTraceControl extends IDsfService {

	/**
	 * Marker interface for a context on which trace operations can be performed
	 */
	public interface ITraceTargetDMContext extends IDMContext {
	}

	/**
	 * Specific Trace Record context.  It describes tracing data.
	 */
	@Immutable
	public interface ITraceRecordDMContext extends IDMContext {
		/**
		 * Returns the GDB id to the trace record.  Can return null
		 * if the context does not point to a valid trace record.
		 * @since 4.0
		 */
		String getRecordId();
	}

	/**
	 * This is the model data interface that corresponds to ITraceRecordDMContext.
	 */
	public interface ITraceRecordDMData extends IDMData {
		/**
		 * Return the content of the trace record in the form of a string
		 * @since 4.0
		 */
		String getContent();

		/**
		 * Return the timestamp of the trace record. Can return null.
		 * @since 4.0
		 */
		String getTimestamp();

		/**
		 * Return the GDB tracepoint number
		 * @since 4.0
		 */
		String getTracepointNumber();

		/**
		 * Returns the GDB id to the trace record
		 * @since 4.0
		 */
		String getRecordId();
	}

	/**
	 * Trace events
	 */
	public interface ITracingSupportedChangeDMEvent extends IDMEvent<ITraceTargetDMContext> {
		boolean isTracingSupported();
	}

	public interface ITracingStartedDMEvent extends IDMEvent<ITraceTargetDMContext> {
	}

	public interface ITracingStoppedDMEvent extends IDMEvent<ITraceTargetDMContext> {
	}

	public interface ITraceRecordSelectedChangedDMEvent extends IDMEvent<ITraceRecordDMContext> {
		boolean isVisualizationModeEnabled();
	}

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
	public void saveTraceData(ITraceTargetDMContext context, String file, boolean remoteSave, RequestMonitor rm);

	/**
	 *  Returns true if trace data can be loaded from a file
	 */
	public void canLoadTraceData(ITraceTargetDMContext context, DataRequestMonitor<Boolean> rm);

	/**
	 * Load trace data (all trace records) from the specified file.  A file created from a call to
	 * {@link saveTraceData} should have the correct format to be loaded by this call.
	 */
	public void loadTraceData(ITraceTargetDMContext context, String file, RequestMonitor rm);

	/**
	 * Request that the backend use the specified trace record.
	 */
	public void selectTraceRecord(ITraceRecordDMContext context, RequestMonitor rm);

	/**
	 * Get the data associated to current GDB tracepoint record
	 */
	public void getTraceRecordData(ITraceRecordDMContext context, DataRequestMonitor<ITraceRecordDMData> rm);

	/////////////////////////////////////////////////
	// GDB specific part
	/////////////////////////////////////////////////

	public static enum STOP_REASON_ENUM {
		REQUEST, PASSCOUNT, OVERFLOW, DISCONNECTION, ERROR, UNKNOWN
	}

	public interface ITraceStatusDMData extends IDMData {
		boolean isTracingSupported();

		boolean isTracingActive();

		int getNumberOfCollectedFrame();

		int getTotalBufferSize();

		int getFreeBufferSize();

		STOP_REASON_ENUM getStopReason();

		/**
		 * Returns the id of the tracepoint that caused the stop.
		 * Should be null if getStopReason() is null
		 */
		Integer getStoppingTracepoint();
	}

	/** @since 4.4 */
	public interface ITraceStatusDMData2 extends ITraceStatusDMData {
		/**
		 * Returns the user-name of the user that started or stopped a trace.  Returns an
		 * empty string if no user-name is available.
		 */
		String getUserName();

		/**
		 * Returns the traces notes related to a started or stopped trace.  Returns an
		 * empty string if no notes are defined.
		 */
		String getNotes();

		/**
		 * Returns the start-time of an on-going trace.
		 * Returns an empty string if no start-time is available or if no trace was started.
		 */
		String getStartTime();

		/**
		 * Returns the stop-time of the last trace experiment.
		 * Returns an empty string if no stop-time is available, if a trace is currently
		 * running or if no trace was ever started.
		 */
		String getStopTime();

		/**
		 * Returns true if trace visualization is done from a trace file
		 * as compared to one from an ongoing execution.
		 */
		boolean isTracingFromFile();

		/**
		 * Returns true if an ongoing tracing experiment will continue after
		 * GDB disconnects from the target.
		 */
		boolean isDisconnectedTracingEnabled();

		/**
		 * Returns true if the buffer being used or to be used to record
		 * the trace data is a circular buffer (overwriting/flight-recorder), or not.
		 */
		boolean isCircularBuffer();

		/**
		 * Returns the number of created frames of the current trace experiment.
		 */
		int getNumberOfCreatedFrames();

		/**
		 * Returns the error description if the trace was stopped due to an error (getStopReason() returns ERROR).
		 * Returns null if the trace is not stopped, or if it is not stopped by an ERROR.
		 * Can return an empty string in other cases if no description is available.
		 */
		String getStopErrorDescription();

		/**
		 * Returns the trace file path when isTracingFromFile() is true.  Can return
		 * an empty string if the file path is not available.
		 * Should return null if isTracingFromFile() is false;
		 */
		String getTraceFile();

		/**
		 * If a trace frame is currently being examined, this method will return
		 * its id. Returns null if no trace frame is in focus.
		 */
		String getCurrentTraceFrameId();

		/**
		 * If a trace frame is currently being examined, this method will return
		 * the GDB tracepoint number that triggered the trace record in focus.
		 * Returns null if no trace frame is in focus (if getCurrentTraceFrameId() == null).
		 */
		Integer getTracepointNumberForCurrentTraceFrame();
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
	public void createTraceVariable(ITraceTargetDMContext context, String varName, String varValue, RequestMonitor rm);

	/**
	 * Get a list of all trace state variables and their values
	 */
	public void getTraceVariables(ITraceTargetDMContext context, DataRequestMonitor<ITraceVariableDMData[]> rm);

	/** @since 4.0 */
	public ITraceRecordDMContext createTraceRecordContext(ITraceTargetDMContext ctx, String recordId);

	public void getCurrentTraceRecordContext(ITraceTargetDMContext context,
			DataRequestMonitor<ITraceRecordDMContext> drm);

	public ITraceRecordDMContext createNextRecordContext(ITraceRecordDMContext ctx);

	public ITraceRecordDMContext createPrevRecordContext(ITraceRecordDMContext ctx);
}
