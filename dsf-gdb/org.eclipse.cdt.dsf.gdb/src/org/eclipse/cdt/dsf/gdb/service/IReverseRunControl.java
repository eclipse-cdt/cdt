/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson and others.
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
 *     Marc Khouzam (Ericsson) - Added IReverseModeChangedDMEvent (Bug 399163)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * This interface provides access to controlling and monitoring the reverse execution
 * state of a process being debugged.
 *
 * @since 2.0
 */
public interface IReverseRunControl {

	/**
	 * Indicates that the enablement of reverse debugging has changed.
	 *
	 * @since 4.2
	 */
	interface IReverseModeChangedDMEvent extends IDMEvent<ICommandControlDMContext> {
		/**
		 * @return the new state of reverse mode.
		 */
		boolean isReverseModeEnabled();
	}

	/**
	 * Establish if a reverse-resume operation is allowed on the specified context.
	 *
	 * @param context The thread or process on which the reverse operation will apply
	 * @param rm Will contain the result of the operation, true or false, not null.
	 */
	void canReverseResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm);

	/**
	 * Perform a reverse-resume operation on the specified context.
	 *
	 * @param context The thread or process on which the reverse operation will apply
	 */
	void reverseResume(IExecutionDMContext context, RequestMonitor requestMonitor);

	/**
	 * Returns whether a reverse-step operation is on-going for the specified context.
	 *
	 * @param context The thread or process on which the reverse operation will apply
	 * @return True if a reverse-steop operation is on-going, false otherwise.
	 */
	boolean isReverseStepping(IExecutionDMContext context);

	/**
	 * Establish if a reverse-step operation is allowed on the specified context.
	 *
	 * @param context The thread or process on which the reverse operation will apply
	 * @param rm Will contain the result of the operation, true or false, not null.
	 */
	void canReverseStep(IExecutionDMContext context, StepType stepType, DataRequestMonitor<Boolean> rm);

	/**
	 * Perform a reverse-step operation on the specified context with the specified step type.
	 *
	 * @param context The thread or process on which the reverse operation will apply
	 * @param stepType The step type to be used for the operation
	 */
	void reverseStep(IExecutionDMContext context, StepType stepType, RequestMonitor requestMonitor);

	/**
	 * Establish if it is possible to enable reverse debugging.
	 *
	 * @param rm Will contain the result of the operation, true or false, not null.
	 */
	void canEnableReverseMode(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm);

	/**
	 * Establish if reverse debugging is enabled.
	 *
	 * @param rm Will contain the result of the operation, true or false, not null.
	 */
	void isReverseModeEnabled(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm);

	/**
	 * Enable or disable reverse debugging based on the enable parameter.
	 *
	 * @param enable True if reverse debugging should enabled, false for disabled.
	 */
	void enableReverseMode(ICommandControlDMContext context, boolean enable, RequestMonitor rm);
}
