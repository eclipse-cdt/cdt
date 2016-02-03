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
import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseTraceMethod;

/** @since 5.0 */
public interface IReverseRunControl2 extends IReverseRunControl {

    /**
     * Get the reverse debugging trace method.
     *
     * @param rm Will contain the result of the operation, true or false, not null.
     */
    void getReverseTraceMethod(ICommandControlDMContext context, DataRequestMonitor<ReverseTraceMethod> rm);

    /**
     * Change reverse debugging trace method based on the method parameter.
     *
     * @param set the reverse debugging trace method to Full Trace, Branch Trace or Processor Trace
     */
    void enableReverseMode(ICommandControlDMContext context, ReverseTraceMethod traceMethod, RequestMonitor rm);
}
