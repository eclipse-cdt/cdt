/*******************************************************************************
 * Copyright (c) 2015 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * Describes the methods to query data from a target system under debug
 */
public interface IDSFTargetDataProxy {

	/** Requests list of CPUs. */
	public void getCPUs(DSFSessionState sessionState, DataRequestMonitor<ICPUDMContext[]> rm);

	/** Requests list of Cores. */
	public void getCores(DSFSessionState sessionState, DataRequestMonitor<ICoreDMContext[]> rm);

	/** Requests list of Cores */
	public void getCores(DSFSessionState sessionState, ICPUDMContext cpuContext,
			DataRequestMonitor<ICoreDMContext[]> rm);

	/** Requests list of Threads Related to the specified CPU and Core */
	public void getThreads(DSFSessionState sessionState, ICPUDMContext cpuContext, ICoreDMContext coreContext,
			DataRequestMonitor<IDMContext[]> rm);

	/** Requests data for the thread associated to the give execution context */
	public void getThreadData(DSFSessionState sessionState, ICPUDMContext cpuContext, ICoreDMContext coreContext,
			IMIExecutionDMContext execContext, DataRequestMonitor<IThreadDMData> rm);

	/** Requests frame data for a given thread */
	public void getTopFrameData(DSFSessionState sessionState, IMIExecutionDMContext execContext,
			DataRequestMonitor<IFrameDMData> rm);

	/** Requests execution state of a thread. */
	public void getThreadExecutionState(DSFSessionState sessionState, ICPUDMContext cpuContext,
			ICoreDMContext coreContext, IMIExecutionDMContext execContext, IThreadDMData threadData,
			DataRequestMonitor<VisualizerExecutionState> rm);

	/** Request load information for a single CPU or core */
	public void getLoad(DSFSessionState sessionState, IDMContext context, DataRequestMonitor<ILoadInfo> rm);
}