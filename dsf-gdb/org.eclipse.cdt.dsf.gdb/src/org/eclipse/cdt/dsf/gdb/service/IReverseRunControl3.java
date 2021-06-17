/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/** @since 6.5 */
public interface IReverseRunControl3 extends IReverseRunControl2 {

	/**
	 * Perform a functions call history operation on the specified context.
	 * @since 6.5
	 * @param context The thread or process on which the reverse operation will apply
	 *
	 */
	void functionsCallHistory(IExecutionDMContext context, RequestMonitor requestMonitor);
}