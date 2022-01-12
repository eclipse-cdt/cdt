/*******************************************************************************
 * Copyright (c) 2013, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dmitry Kozlov (Mentor Graphics) - initial API and implementation (Bug 390827)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * Enhancements to the trace control functionality, which allow to enable new settings.
 * @since 4.4
 */
public interface IGDBTraceControl2 extends IGDBTraceControl {

	/**
	 * Stops the visualization of trace data.
	 */
	public void stopTraceVisualization(ITraceTargetDMContext context, RequestMonitor rm);

	/**
	 * Enables/disables the user of a circular trace buffer to collect trace data.
	 */
	public void setCircularTraceBuffer(ITraceTargetDMContext context, boolean useCircularBuffer, RequestMonitor rm);

	/**
	 * Enables/disables disconnected tracing.  When this flag is enabled, an ongoing tracing experiment will
	 * continue even if GDB disconnects from the target.
	 */
	public void setDisconnectedTracing(ITraceTargetDMContext context, boolean disconnectedTracing, RequestMonitor rm);

	/**
	 * Sets the name of the user that is performing tracing operations.
	 * This name will be persisted during a disconnected tracing experiment.
	 */
	public void setTraceUser(ITraceTargetDMContext context, String userName, RequestMonitor rm);

	/**
	 * Sets some information about the tracing experiment.
	 * This information will be persisted during a disconnected tracing experiment.
	 */
	public void setTraceNotes(ITraceTargetDMContext context, String notes, RequestMonitor rm);
}
