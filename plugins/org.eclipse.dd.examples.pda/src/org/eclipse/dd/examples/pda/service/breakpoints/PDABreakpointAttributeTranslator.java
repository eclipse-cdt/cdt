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
package org.eclipse.dd.examples.pda.service.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.debug.service.BreakpointsMediator;
import org.eclipse.dd.dsf.debug.service.IBreakpointAttributeTranslator;
import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.breakpoints.PDALineBreakpoint;
import org.eclipse.dd.examples.pda.breakpoints.PDAWatchpoint;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * 
 */
public class PDABreakpointAttributeTranslator implements IBreakpointAttributeTranslator {

    private static final String[] fgPDALineBreakpointAttributes = {
        IMarker.LINE_NUMBER,
    };

    private static final String[] fgPDAWatchpointAttributes = {
        PDAWatchpoint.FUNCTION_NAME,
        PDAWatchpoint.VAR_NAME,
        PDAWatchpoint.ACCESS,
        PDAWatchpoint.MODIFICATION
    };

    
    public void initialize(BreakpointsMediator mediator) {
    }

    public void dispose() {
    }

    public List<Map<String, Object>> getBreakpointAttributes(IBreakpoint bp, boolean bpManagerEnabled) 
        throws CoreException 
    {
        Map<String, Object> attrs = new HashMap<String, Object>(); 

        IMarker marker = bp.getMarker();
        if (marker == null || !marker.exists()) {
            throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Breakpoint marker does not exist", null)); 
        }

        // Suppress cast warning: platform is still on Java 1.3
        @SuppressWarnings("unchecked")
        Map<String, Object> platformBpAttrs = marker.getAttributes();

        if (bp instanceof PDAWatchpoint) {
            attrs.put(PDABreakpoints.ATTR_BREAKPOINT_TYPE, PDABreakpoints.PDA_WATCHPOINT);
        
            copyAttributes(platformBpAttrs, attrs, fgPDAWatchpointAttributes);
        } else if (bp instanceof PDALineBreakpoint) {
            attrs.put(PDABreakpoints.ATTR_BREAKPOINT_TYPE, PDABreakpoints.PDA_LINE_BREAKPOINT);
            attrs.put(PDABreakpoints.ATTR_PROGRAM_PATH, marker.getResource().getFullPath().toString()); 
            
            copyAttributes(platformBpAttrs, attrs, fgPDALineBreakpointAttributes);
        }
        
        // If the breakpoint manager is disabled, override the enabled attribute.
        if (!bpManagerEnabled) {
            attrs.put(IBreakpoint.ENABLED, false);
        }

        List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>(1);
        retVal.add(attrs);
        return retVal;
    }

    private void copyAttributes(Map<String, Object> srcMap, Map<String, Object> destMap, String[] attrs) {
        for (String attr : attrs) {
            if (srcMap.containsKey(attr)) {
                destMap.put(attr, srcMap.get(attr));
            }
        }
    }
    
    public boolean canUpdateAttributes(IBreakpointDMContext bp, Map<String, Object> delta) {
        // PDA debugger only allows updating of the action property of the watchpoint.
        // All other breakpoint updates will require a reinstallation.
        if (bp instanceof PDAWatchpoint) {
            Map<String, Object> deltaCopy = new HashMap<String, Object>(delta);
            deltaCopy.remove(PDAWatchpoint.ACCESS);
            deltaCopy.remove(PDAWatchpoint.MODIFICATION);
            return !deltaCopy.isEmpty();
        }
        return false;
    }

    public boolean supportsBreakpoint(IBreakpoint bp) {
        return bp.getModelIdentifier().equals(PDAPlugin.ID_PDA_DEBUG_MODEL);
    }

    public void updateBreakpointStatus(IBreakpoint bp) {
        // PDA breakpoints do not support status reporting
    }

}
