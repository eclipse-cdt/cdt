/*******************************************************************************
 * Copyright (c) 2010, 2011 Texas Instruments, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dobrin Alexiev (Texas Instruments) - initial API and implementation (bug 240208)
********************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.provisional.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * EXPERIMENTAL. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 *
 * Interface for translating one model of execution context hierarchy to another.
 * As the grouping feature is added incrementally this interface will be defined properly.
 *
 * The reason this interface is proposed at the DSF level is to accommodate requirements from
 * multiple DSF debuggers.
 *
 * @since 2.2
 * @experimental
 */
public interface IExecutionContextTranslator extends IDsfService {

	/**
	 * returns true if all DM contexts can be grouped into one container.
	 *
	 * @param context
	 * @param rm
	 */
	void canGroup(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * returns true if all DM contexts can be ungrouped.
	 *
	 * @param context
	 * @param rm
	 */
	void canUngroup(IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);

	/**
	 * Groups the specified execution contexts.
	 *
	 * @param context
	 * @param requestMonitor
	 */
	void group(IExecutionDMContext[] contexts, RequestMonitor requestMonitor);

	/**
	 * Ungroups the specified execution contexts.
	 *
	 * @param context
	 * @param requestMonitor
	 */
	void ungroup(IExecutionDMContext[] contexts, RequestMonitor requestMonitor);
}
