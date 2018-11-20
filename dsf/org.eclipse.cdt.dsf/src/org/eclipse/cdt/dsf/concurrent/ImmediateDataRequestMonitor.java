/*******************************************************************************
 * Copyright (c) 2011 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera) - initial API and implementation (Bug 365966)
 *******************************************************************************/

package org.eclipse.cdt.dsf.concurrent;

/**
 * Convenience extension of DataRequestMonitor that uses the ImmediateExecutor.
 * The handleCompleted() method is immediately executed in same thread as done() call.
 * @since 2.3
 */
public class ImmediateDataRequestMonitor<V> extends DataRequestMonitor<V> {
	/**
	 * Constructor without a parent monitor and using ImmediateExecutor.
	 */
	public ImmediateDataRequestMonitor() {
		super(ImmediateExecutor.getInstance(), null);
	}

	/**
	 * Constructor with an optional parent monitor and using ImmediateExecutor.
	 */
	public ImmediateDataRequestMonitor(RequestMonitor parentMonitor) {
		super(ImmediateExecutor.getInstance(), parentMonitor);
	}
}
