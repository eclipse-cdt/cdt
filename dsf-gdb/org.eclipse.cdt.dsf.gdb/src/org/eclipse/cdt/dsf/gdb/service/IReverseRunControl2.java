/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * @since 4.8
 */
public interface IReverseRunControl2 extends IReverseRunControl {

    /**
     * Get the reverse debugging trace method.
     *
     * @param rm Will contain the result of the operation, true or false, not null.
     * @since 5.0
     */
    void getReverseTraceMethod(ICommandControlDMContext context, DataRequestMonitor<String> rm);

    /**
     * Change reverse debugging trace method based on the method parameter.
     *
     * @param set the reverse debugging trace method to Full Trace, Branch Trace or Processor Trace
     * @since 5.0
     */
    void enableReverseMode(ICommandControlDMContext context, String traceMethod, RequestMonitor rm);
}
