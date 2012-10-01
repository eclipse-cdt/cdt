/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dmitry Kozlov (Mentor) - initial API and implementation (Bug 390827)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * Trace status related enhancements to API
 * @since 4.2
 */
public interface IGDBTraceControl2 extends IGDBTraceControl {
	public interface ITraceStatusDMData2 extends ITraceStatusDMData {
    	String  getUserName();
    	String  getStartNotes();
    	String  getStartTime();
    	String  getStopNotes();
		String  getStopTime();
    	boolean isOfflineTracing();
    	boolean isCircularBuffer();    	
		String getCurrentTraceFrame();
		int getTracepointIndexForCurrentTraceRecord();
	}
	
	public void setCircularTraceBuffer(ITraceTargetDMContext context, boolean useCircularBuffer, RequestMonitor rm);
	public void setTraceUser(ITraceTargetDMContext context, String userName, RequestMonitor rm);
	public void setTraceNotes(ITraceTargetDMContext context, String note, RequestMonitor rm);
	public void setTraceStopNotes(ITraceTargetDMContext context, String note, RequestMonitor rm);
}
