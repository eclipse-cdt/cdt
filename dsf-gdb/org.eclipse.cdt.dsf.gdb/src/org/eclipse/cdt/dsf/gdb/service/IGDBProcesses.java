/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Ericsson - added support for core-awareness
 *     Marc Khouzam (Ericsson) - Support for exited processes in the debug view (bug 407340)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public interface IGDBProcesses extends IMIProcesses {

	/**
	 * This interface extends the DSF ThreadDMData to provide
	 * the cores on which a process or a thread is located as well
	 * as the owner of the process.
	 *
	 * @since 4.0
	 */
	interface IGdbThreadDMData extends IThreadDMData {
		/**
		 * @return The list of identifiers of the cores on which the thread
		 *         or process is currently located.  A thread will typically
		 *         be located on a single core at a time, while a process will
		 *         be located on all cores on which one of the process' threads
		 *         is located.  Returns null if the information is not available.
		 */
		String[] getCores();

		/**
		 * @return The owner of the process, usually a user ID.  Returns null if the
		 *         information is not available.  For threads, this method can return
		 *         null or the owner of the parent process, if available.
		 */
		String getOwner();
	}

	/**
	 * This interface extends the {@link IGdbThreadDMData} to provide a description
	 * for a process or thread.
	 *
	 * @since 5.6
	 */
	public interface IGdbThreadDMData2 extends IGdbThreadDMData {

		/**
		 * @return The description for this process or thread. Usually
		 *         the program and its arguments.
		 */
		String getDescription();
	}

	/**
	 * This interface describes an exited thread/process.
	 *
	 * @since 4.7
	 */
	interface IGdbThreadExitedDMData extends IThreadDMData {
		/**
		 * @return The exit code of this process.
		 *         Returns null if the exit code is not known.
		 */
		Integer getExitCode();
	}

	/**
	 * Indicates that a process or thread is no longer being tracked by
	 * the session.  This event usually refers to exited elements that
	 * were still being shown to the user but that have now been removed.
	 * @since 4.7
	 */
	interface IThreadRemovedDMEvent extends IDMEvent<IThreadDMContext> {
	}

	/**
	 * Get a list of all execution contexts belonging to a container.  This call is synchronous,
	 * unlike the call to getProcessesBeingDebugged().  However, some services may not be able
	 * to fulfill this request synchronously and will have to rely on getProcessesBeingDebugged().
	 *
	 * @param containerDmc The container for which we want to get the execution contexts
	 */
	IMIExecutionDMContext[] getExecutionContexts(IMIContainerDMContext containerDmc);

	/**
	 * Returns whether the specified process can be restarted.
	 *
	 * @param containerDmc The process that should be restarted
	 * @param rm The requestMonitor that returns if a restart is allowed on the specified process.
	 *
	 * @since 4.0
	 */
	void canRestart(IContainerDMContext containerDmc, DataRequestMonitor<Boolean> rm);

	/**
	 * Request that the specified process be restarted.
	 *
	 * @param containerDmc The process that should be restarted
	 * @param attributes Different attributes that affect the restart operation.  This is
	 *                   usually the launch configuration attributes
	 * @param rm The requetMonitor that indicates that the restart request has been completed.  It will
	 *           contain the IContainerDMContext fully filled with the data of the restarted process.
	 *
	 * @since 4.0
	 */
	void restart(IContainerDMContext containerDmc, Map<String, Object> attributes,
			DataRequestMonitor<IContainerDMContext> rm);

	/**
	 * Request that the specified process be started.
	 *
	 * @param containerDmc The process that should be started.
	 * @param attributes Different attributes that affect the start operation.  This is
	 *                   usually the launch configuration attributes
	 * @param rm The requestMonitor that indicates that the start request has been completed.  It will
	 *           contain the IContainerDMContext fully filled with the data of the newly started process.
	 *
	 * @since 4.0
	 */
	void start(IContainerDMContext containerDmc, Map<String, Object> attributes,
			DataRequestMonitor<IContainerDMContext> rm);

	/**
	 * Attaches debugger to the given process.
	 * When attaching to a process, a debugging context can now be used to characterize the process.
	 * This method can optionally choose to return this IDMContext inside the DataRequestMonitor.
	 * This can be useful for backends that do not have the ability to obtain the different
	 * debugging IDMContexts through {@link #getProcessesBeingDebugged(IDMContext, DataRequestMonitor)
	 *
	 * @param file Binary to use for the process.
	 * @since 4.0
	 */
	void attachDebuggerToProcess(IProcessDMContext procCtx, String file, DataRequestMonitor<IDMContext> rm);

	/**
	 * Adds a process representing the inferior to the launch.  An I/O console will be created if necessary.
	 *
	 * @param containerDmc The inferior for which a a process will be added to the launch.
	 * @param label The name to use for the console if created.
	 * @param pty The PTY to be used by the console for I/O
	 * @param rm The requestMonitor that indicates that the request has been completed.
	 *
	 * @since 5.2
	 */
	default void addInferiorToLaunch(IContainerDMContext containerDmc, String label, PTY pty, RequestMonitor rm) {
		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Not supported", //$NON-NLS-1$
				null));
	}
}
