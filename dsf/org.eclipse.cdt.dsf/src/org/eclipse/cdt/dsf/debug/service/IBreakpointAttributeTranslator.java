/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Breakpoint attribute translator is used by the {@link BreakpointsMediator} 
 * to map IDE breakpoint attributes to debugger breakpoint attributes.  
 * <p>
 * Note: The attribute translator is expected to access IDE breakpoint manager
 * objects which are synchronized using the resource system.  Therefore all the 
 * translator methods are called using background threads.  When the attribute
 * translator needs to access DSF services, it needs to schedule a runnable using 
 * the DSF session executable.
 * 
 * @see BreakpointMediator
 * @since 1.0
 */
@ThreadSafeAndProhibitedFromDsfExecutor("")
public interface IBreakpointAttributeTranslator {
    
    /**
     * Initializes the translator.  This method is called by the breakpoint 
     * mediator service, when the mediator service is initialized. 
     */
    @ConfinedToDsfExecutor("")
   public void initialize(BreakpointsMediator mediator);

    /**
     * Disposes the translator.  Also called by the mediator upon service 
     * shutdown.
     */
    @ConfinedToDsfExecutor("")
    public void dispose();

    /**
     * Returns whether the given IDE breakpoint is supported by this debugger.
     */
    public boolean supportsBreakpoint(IBreakpoint bp);

    /**
     * Returns the target breakpoint attributes for the given IDE breakpoint. 
     */
    public List<Map<String, Object>> getBreakpointAttributes(IBreakpoint breakpoint, boolean bpManagerEnabled)  throws CoreException;
    
    /**
     * Based on the given change in IDE breakpoint attributes, this method returns 
     * whether the given target breakpoint can be modified using 
     * {@link IBreakpoints#updateBreakpoint(IBreakpointDMContext, Map, org.eclipse.cdt.dsf.concurrent.RequestMonitor)} 
     * method.
     */
    public boolean canUpdateAttributes(IBreakpointDMContext bp, Map<String, Object> delta);

    /**
     * Notifies the translator to update the given IDE breakpoint's status.
     */
    public void updateBreakpointStatus(IBreakpoint bp);
}