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

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;


@ThreadSafeAndProhibitedFromDsfExecutor("")
public interface IBreakpointAttributeTranslator {
    
    public void initialize(BreakpointsMediator mediator);
    
    public void dispose();
    
    public List<Map<String, Object>> getBreakpointAttributes(IBreakpoint breakpoint, boolean bpManagerEnabled)  throws CoreException;
    
    public boolean canUpdateAttributes(IBreakpointDMContext bp, Map<String, Object> delta);

    public boolean supportsBreakpoint(IBreakpoint bp);

    public void updateBreakpointStatus(IBreakpoint bp);
}