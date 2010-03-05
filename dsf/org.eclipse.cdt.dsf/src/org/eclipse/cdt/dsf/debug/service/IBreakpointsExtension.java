/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;

/**
 * Extension to the Breakpoints service which adds support for corollating 
 * breakpoints and execution contexts.
 * @since 2.1
 */
public interface IBreakpointsExtension extends IBreakpoints {

    /**
     * Event indicating that a given thread or container was suspended
     * by the given breakpoint(s).
     */
    public interface IBreakpointHitDMEvent extends ISuspendedDMEvent {
        
        /**
         * Returs the breakpoints that suspended the thread.
         */
        IBreakpointDMContext[] getBreakpoints();
    }

    /**
     * If a given execution context was suspended due hitting a breakpoint, 
     * this method should return the breakpoints which caused the thread or 
     * container to suspend.
     * 
     * @param ctx Thread or container to get breakpoints for.
     * @param rm Breakpoints that the thread suspended on.
     */
    public void getExecutionContextBreakpoints(IExecutionDMContext ctx, DataRequestMonitor<IBreakpointDMContext[]> rm);
}
