/*******************************************************************************
 * Copyright (c) 2015 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 459114 - override construction of the data model
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.ILoadInfo;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;

/**
 * Describes the methods to query a data from a target system under debug
 */
public interface IDSFTargetDataProxy {

	/** Requests list of CPUs. */
	public abstract void getCPUs(DSFSessionState sessionState, DataRequestMonitor<ICPUDMContext[]> rm);

	/** Requests list of Cores. */
	public abstract void getCores(DSFSessionState sessionState, DataRequestMonitor<ICoreDMContext[]> rm);

	/** Requests list of Cores */
	public abstract void getCores(DSFSessionState sessionState, ICPUDMContext cpuContext, DataRequestMonitor<ICoreDMContext[]> rm);

	/** Requests list of Threads.*/
	public abstract void getThreads(DSFSessionState sessionState, ICPUDMContext cpuContext, ICoreDMContext coreContext, DataRequestMonitor<IDMContext[]> rm);

	/** Requests data of a thread.*/
	public abstract void getThreadData(DSFSessionState sessionState, ICPUDMContext cpuContext, ICoreDMContext coreContext, IMIExecutionDMContext execContext,
			DataRequestMonitor<IThreadDMData> rm);

	/** Request data frame data for a given thread */
	public abstract void getFrameData(DSFSessionState sessionState, ICPUDMContext cpuContext, ICoreDMContext coreContext,
			IMIExecutionDMContext execContext, IThreadDMData threadData, DataRequestMonitor<IFrameDMData> rm);
	
	/** Requests execution state of a thread. */
	public abstract void getThreadExecutionState(DSFSessionState sessionState, ICPUDMContext cpuContext, ICoreDMContext coreContext,
			IMIExecutionDMContext execContext, IThreadDMData threadData, DataRequestMonitor<VisualizerExecutionState> rm);

	/** Request load information for a single CPU or core */
	public abstract void getLoad(DSFSessionState sessionState, IDMContext context, DataRequestMonitor<ILoadInfo> rm);
}