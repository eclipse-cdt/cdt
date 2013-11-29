/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dmitry Kozlov (Mentor Graphics) - initial API and implementation (Bug 390827)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * Trace status related enhancements to API
 * @since 4.2
 */
public interface IGDBTraceControl2 extends IGDBTraceControl {
	public interface ITraceStatusDMData2 extends ITraceStatusDMData {
		/** Get trace user name as reported by user field of trace-status command */
		String  getUserName();
		/** Get trace notes as reported by notes field of trace-status command */
		String  getNotes();
		/** Get trace start time as reported by start-time field of trace-status command */
		String  getStartTime();
		/** Get trace stop time as reported by stop-time field of trace-status command */
		String  getStopTime();
		/** Is tracing performed from offline data */
		boolean isOfflineTracing();
		/** Is tracing will continue after GDB disconnect */
		boolean isDisconnectedTracing();
		/** Is circular buffer used for record tracing data */
		boolean isCircularBuffer();
		/** Get number of created frames as reported by frames-created field of trace-status command */
		int getNumberOfCreatedFrames();
	}

	/*
	 * Note this is ugly hack to put these two methods here caused by architecture of GDBTraceControl_7_2 implementation, 
	 * which keeps information about current trace record in two separate static fields: 
	 * fCurrentRecordDmc and fTracepointIndexForTraceRecord whose are updated independently from from trace status information.
	 */
	/** Get current trace frame number */
	public String getCurrentTraceFrame();
	/** Get tracepoint index for current trace record */
	public int getTracepointIndexForCurrentTraceRecord();

	/** Set circular trace buffer to on/off */
	public void setCircularTraceBuffer(ITraceTargetDMContext context, boolean useCircularBuffer, RequestMonitor rm);
	/** Set to continue tracing on GDB disconnect to on/off */
	public void setDisconnectedTracing(ITraceTargetDMContext context, boolean disconnectedTracing, RequestMonitor rm);
	/** Set trace user name */
	public void setTraceUser(ITraceTargetDMContext context, String userName, RequestMonitor rm);
	/** Set trace notes */
	public void setTraceNotes(ITraceTargetDMContext context, String note, RequestMonitor rm);
}
