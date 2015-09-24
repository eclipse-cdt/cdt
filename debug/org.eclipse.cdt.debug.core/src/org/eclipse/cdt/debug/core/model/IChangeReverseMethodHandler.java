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
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.commands.IDebugCommandHandler;

/**
 * Handler interface for the reverse debug change trace method command
 *
 * @since 7.8
 */
public interface IChangeReverseMethodHandler extends IReverseToggleHandler, IDebugCommandHandler {
    interface IReverseTraceMethod {};

    void setTraceMethod(IReverseTraceMethod traceMethod);

   /**
    * get the trace method
    * @return FullTrace, BranchTrace, ProcessorTrace
    *
    */
    IReverseTraceMethod getTraceMethod(Object context);
}
