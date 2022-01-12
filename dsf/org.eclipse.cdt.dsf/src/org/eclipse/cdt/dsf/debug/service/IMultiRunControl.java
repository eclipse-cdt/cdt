/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * This interface provides the ability to perform run control operations on multiple contexts.
 *
 * @see org.eclipse.cdt.dsf.debug.service.IRunControl
 *
 * @since 2.3
 */
public interface IMultiRunControl extends IRunControl {
	/**
	 * Check if at least one of the specified contexts can be resumed
	 * @param context List of execution contexts that want to be resumed
	 * @param rm Request monitor returning:
	 *             true if at least one of the specified contexts can be resumed
	 *             false if none of the specified contexts can be resumed
	 */
	void canResumeSome(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if all of the specified contexts can be resumed
	 * @param context List of execution contexts that want to be resumed
	 * @param rm Request monitor returning:
	 *             true if all of the specified contexts can be resumed
	 *             false if any of the specified contexts cannot be resumed
	 */
	void canResumeAll(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if at least one of the specified contexts can be suspended
	 * @param context List of execution contexts that want to be suspended
	 * @param rm Request monitor returning:
	 *             true if at least one of the specified contexts can be suspended
	 *             false if none of the specified contexts can be suspended
	 */
	void canSuspendSome(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if all of the specified contexts can be suspended
	 * @param context List of execution contexts that want to be suspended
	 * @param rm Request monitor returning:
	 *             true if all of the specified contexts can be suspended
	 *             false if any of the specified contexts cannot be suspended
	 */
	void canSuspendAll(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if any of the specified contexts is suspended.
	 * @param context List of execution contexts that are to be checked for being suspended
	 * @param rm Request monitor returning:
	 *           true if any of the specified contexts is suspended, false otherwise
	 */
	void isSuspendedSome(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if all of the specified contexts are suspended.
	 * @param context List of execution contexts that are to be checked for being suspended
	 * @param rm Request monitor returning:
	 *           true if all of the specified contexts are suspended, false otherwise
	 */
	void isSuspendedAll(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if any of the specified contexts can be stepped using stepType
	 * @param context List of execution contexts that want to be stepped
	 * @param stepStype The type of step to be used.
	 * @param rm Request monitor returning:
	 *             true if any of the specified contexts can be stepped
	 *             false if none of the specified contexts can be stepped
	 */
	void canStepSome(IExecutionDMContext[] contexts, StepType stepType, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if all of the specified contexts can be stepped using stepType
	 * @param context List of execution contexts that want to be stepped
	 * @param stepStype The type of step to be used.
	 * @param rm Request monitor returning:
	 *             true if all of the specified contexts can be stepped
	 *             false if any of the specified contexts cannot be stepped
	 */
	void canStepAll(IExecutionDMContext[] contexts, StepType stepType, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if any of the specified contexts is currently stepping.
	 * @param context List of execution contexts that are to be checked for stepping
	 * @param rm Request monitor returning:
	 *           true if any of the specified contexts is stepping, false otherwise
	 */
	void isSteppingSome(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Check if all of the specified contexts are currently stepping.
	 * @param context List of execution contexts that are to be checked for stepping
	 * @param rm Request monitor returning:
	 *           true if all of the specified contexts are stepping, false otherwise
	 */
	void isSteppingAll(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Request that all specified contexts be resumed.  Only contexts that are in a
	 * state that can be resumed will be affected, others will be ignored.
	 * @param context List of execution contexts that are to be resumed
	 */
	void resume(IExecutionDMContext[] contexts, RequestMonitor rm);

	/**
	 * Request that all specified contexts be suspended.  Only contexts that are in a
	 * state that can be suspended will be affected, others will be ignored.
	 * @param context List of execution contexts that are to be suspended
	 */
	void suspend(IExecutionDMContext[] contexts, RequestMonitor rm);

	/**
	 * Request that all specified context be stepped using stepType.  Only contexts
	 * that are in a state that can be stepped will be affected, others will be ignored.
	 * @param context List of execution contexts that are to be stepped
	 * @param stepStype The type of step to be used.
	 */
	void step(IExecutionDMContext[] contexts, StepType stepType, RequestMonitor rm);
}