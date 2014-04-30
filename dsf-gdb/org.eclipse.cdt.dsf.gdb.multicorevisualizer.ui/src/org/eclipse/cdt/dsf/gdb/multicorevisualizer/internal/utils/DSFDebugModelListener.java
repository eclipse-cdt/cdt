/*******************************************************************************
 * Copyright (c) 2012, 2014 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *     Marc Dumais (Ericsson) - Add CPU/core load information to the multicore visualizer (Bug 396268)
 *     Xavier Raynaud (Kalray) - Bug 431935
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;


import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.ILoadInfo;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;

/** Interface for classes that interact with DSFDebugModel.
 * 
 *  An instance of this interface is passed as
 *  an argument to a method of DSFDebugModel, and
 *  the corresponding callback on this interface is
 *  invoked when the method completes.
 *  
 *  The "arg" argument is the value (if any) passed
 *  through the corresponding "arg" argument of the
 *  DSFDebugModel method.
 *  
 *  TODO: we intend to refactor this API and make this
 *  a base class rather than an instance.
 */
public interface DSFDebugModelListener {
	
	/** Invoked when getCPUs() request completes. */
	public void getCPUsDone(ICPUDMContext[] cpuContexts,
							Object arg);

	/** Invoked when getCores() request completes. */
	public void getCoresDone(ICPUDMContext cpuContext,
							 ICoreDMContext[] coreContexts,
							 Object arg);

	/** Invoked when getThreads() request completes. */
	public void getThreadsDone(ICPUDMContext cpuContext,
			 				   ICoreDMContext coreContext,
							   IDMContext[] threadContexts,
							   Object arg);

	/** Invoked when getThreadDataState() request completes. */
	public void getThreadDataDone(ICPUDMContext cpuContext,
			ICoreDMContext coreContext,
			IMIExecutionDMContext threadContext,
			IThreadDMData data,
			Object arg);

	/** Invoked when getThreadExecutionState() request completes. */
	public void getThreadExecutionStateDone(ICPUDMContext cpuContext,
			 				                ICoreDMContext coreContext,
							                IMIExecutionDMContext threadContext,
							                IThreadDMData threadData,
							                IFrameDMData frame,
							                VisualizerExecutionState state,
							                Object arg);
	
	/**
	 * Invoked when getLoad() request completes.
	 * @since 1.1
	 */
	public void getLoadDone(IDMContext context, ILoadInfo loads, Object arg);

	/**
	 * Invoked when the load timer triggers. Updates the loads 
	 * for a given DSF session.
	 */
	public void updateLoads(DSFSessionState session);

	/** Invoked when a DSF session terminates */
	public void disposeDataSource(String id);
}
