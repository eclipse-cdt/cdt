/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2.BreakpointEventType;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2.ITargetBreakpointInfo;
import org.eclipse.cdt.dsf.debug.service.IBreakpointAttributeTranslator2;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.breakpoints.PDALineBreakpoint;
import org.eclipse.cdt.examples.dsf.pda.breakpoints.PDAWatchpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Translator between {@link PDALineBreakpoint} object attributes and 
 * attributes used by the {@link PDABreakpoints} service.  
 * <p>
 * The attribute translator is used by the standard {@link BreakpointsMediator} 
 * service to map between platform breakpoint attributes and target-side DSF
 * breakpoint attributes.  Thus, this object encapsulates the model-specific
 * functionality of synchronizing target side and IDE-side breakpoint objects.  
 * </p>
 */
public class PDABreakpointAttributeTranslator implements IBreakpointAttributeTranslator2 {

    // Arrays of common attributes between the two breakpoint types.  These 
    // attributes can be copied directly without translation.
    private static final String[] fgPDALineBreakpointAttributes = {
        IBreakpoint.ENABLED,
        IMarker.LINE_NUMBER,
    };
    private static final String[] fgPDAWatchpointAttributes = {
        IBreakpoint.ENABLED,
        PDAWatchpoint.FUNCTION_NAME,
        PDAWatchpoint.VAR_NAME,
        PDAWatchpoint.ACCESS,
        PDAWatchpoint.MODIFICATION
    };

    private static final String[] fgCDTWatchpointAttributes = {
        IBreakpoint.ENABLED,
        PDAWatchpoint.FUNCTION_NAME,
    };

    // PDA breakpoints translator doesn't keep any state and it doesn't 
    // need to initialize or clean up.
    public void initialize(BreakpointsMediator2 mediator) {
    }


    public void dispose() {
    }

    private List<Map<String, Object>> getBreakpointAttributes(IBreakpoint bp, boolean bpManagerEnabled) 
        throws CoreException 
    {
        if (bp instanceof ICLineBreakpoint) { 
            return getCBreakpointAttributes((ICLineBreakpoint)bp, bpManagerEnabled);
        } else if (bp instanceof ICWatchpoint) {
            return getCWatchpointAttributes((ICWatchpoint)bp, bpManagerEnabled);
        } else {
            return getPDABreakpointAttributes(bp, bpManagerEnabled);
        }
    }

    private List<Map<String, Object>> getCBreakpointAttributes(ICLineBreakpoint bp, boolean bpManagerEnabled) 
        throws CoreException 
    {
        Map<String, Object> attrs = new HashMap<String, Object>(); 

        // Check that the marker exists and retrieve its attributes.  
        // Due to accepted race conditions, the breakpiont marker may become null 
        // while this method is being invoked.  In this case throw an exception
        // and let the caller handle it.
        IMarker marker = bp.getMarker();
        if (marker == null || !marker.exists()) {
            throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Breakpoint marker does not exist", null)); 
        }
        // Suppress cast warning: platform is still on Java 1.3
        Map<String, Object> platformBpAttrs = marker.getAttributes();

        // Copy breakpoint attributes.
        attrs.put(PDABreakpoints.ATTR_BREAKPOINT_TYPE, PDABreakpoints.PDA_LINE_BREAKPOINT);
        attrs.put(PDABreakpoints.ATTR_PROGRAM_PATH, bp.getFileName());

        copyAttributes(platformBpAttrs, attrs, fgPDALineBreakpointAttributes);

        // If the breakpoint manager is disabled, override the enabled attribute.
        if (!bpManagerEnabled) {
            attrs.put(IBreakpoint.ENABLED, false);
        }

        // The breakpoint mediator allows for multiple target-side breakpoints 
        // to be created for each IDE breakpoint.  Although in case of PDA this 
        // feature is never used, we still have to return a list of attributes.
        List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>(1);
        retVal.add(attrs);
        return retVal;        
    }

    private List<Map<String, Object>> getCWatchpointAttributes(ICWatchpoint bp, boolean bpManagerEnabled) 
        throws CoreException 
    {
        Map<String, Object> attrs = new HashMap<String, Object>(); 

        // Check that the marker exists and retrieve its attributes.  
        // Due to accepted race conditions, the breakpiont marker may become null 
        // while this method is being invoked.  In this case throw an exception
        // and let the caller handle it.
        IMarker marker = bp.getMarker();
        if (marker == null || !marker.exists()) {
            throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Breakpoint marker does not exist", null)); 
        }
        // Suppress cast warning: platform is still on Java 1.3
        Map<String, Object> platformBpAttrs = marker.getAttributes();

        // Copy breakpoint attributes.
        attrs.put(PDABreakpoints.ATTR_BREAKPOINT_TYPE, PDABreakpoints.PDA_WATCHPOINT);
        attrs.put(PDABreakpoints.ATTR_BREAKPOINT_TYPE, PDABreakpoints.PDA_WATCHPOINT);
        attrs.put(PDAWatchpoint.VAR_NAME, platformBpAttrs.get(ICWatchpoint.EXPRESSION));
        attrs.put(PDAWatchpoint.ACCESS, platformBpAttrs.get(ICWatchpoint.READ));
        attrs.put(PDAWatchpoint.MODIFICATION, platformBpAttrs.get(ICWatchpoint.WRITE));

        copyAttributes(platformBpAttrs, attrs, fgCDTWatchpointAttributes);

        // If the breakpoint manager is disabled, override the enabled attribute.
        if (!bpManagerEnabled) {
            attrs.put(IBreakpoint.ENABLED, false);
        }

        // The breakpoint mediator allows for multiple target-side breakpoints 
        // to be created for each IDE breakpoint.  Although in case of PDA this 
        // feature is never used, we still have to return a list of attributes.
        List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>(1);
        retVal.add(attrs);
        return retVal;        
    }

    private List<Map<String, Object>> getPDABreakpointAttributes(IBreakpoint bp, boolean bpManagerEnabled) 
        throws CoreException 
    {
        Map<String, Object> attrs = new HashMap<String, Object>(); 

        // Check that the marker exists and retrieve its attributes.  
        // Due to accepted race conditions, the breakpiont marker may become null 
        // while this method is being invoked.  In this case throw an exception
        // and let the caller handle it.
        IMarker marker = bp.getMarker();
        if (marker == null || !marker.exists()) {
            throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Breakpoint marker does not exist", null)); 
        }
        // Suppress cast warning: platform is still on Java 1.3
        Map<String, Object> platformBpAttrs = marker.getAttributes();

        // Copy breakpoint attributes.
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

        // The breakpoint mediator allows for multiple target-side breakpoints 
        // to be created for each IDE breakpoint.  Although in case of PDA this 
        // feature is never used, we still have to return a list of attributes.
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

    public boolean canUpdateAttributes(IBreakpoint bp, IBreakpointsTargetDMContext context, 
        Map<String, Object> attributes) 
    {
        // PDA debugger only allows updating of the action property of the watchpoint.
        // All other breakpoint updates will require a re-installation.
        if (bp instanceof PDAWatchpoint) {
            Map<String, Object> deltaCopy = new HashMap<String, Object>(attributes);
            deltaCopy.remove(PDAWatchpoint.ACCESS);
            deltaCopy.remove(PDAWatchpoint.MODIFICATION);
            return !deltaCopy.isEmpty();
        }
        return false;
    }

    public boolean supportsBreakpoint(IBreakpoint bp) {
        return bp.getModelIdentifier().equals(PDAPlugin.ID_PDA_DEBUG_MODEL) ||
            bp instanceof ICLineBreakpoint || bp instanceof ICWatchpoint;
    }

    public void updateBreakpointsStatus(
        Map<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>> bpsInfo,
        BreakpointEventType eventType) 
    {
        for (IBreakpoint bp : bpsInfo.keySet()) {
            if (!(bp instanceof ICLineBreakpoint)) {
                continue;
            }
            ICLineBreakpoint cbp = (ICLineBreakpoint)bp;
            try {
                if (eventType == BreakpointEventType.ADDED) {
                    cbp.incrementInstallCount();
// Testing for Bug 360280 - [breakpoints] Reposition breakpoints when planted on invalid line                    
//                    if (cbp instanceof ICLineBreakpoint2) {
//                        ICLineBreakpoint2 lbp2 = (ICLineBreakpoint2)cbp;
//                        lbp2.setInstalledLineNumber(lbp2.getRequestedLine() + 1);
//                    }
                } else if (eventType == BreakpointEventType.REMOVED) {
                    cbp.decrementInstallCount();
                }
            } catch (CoreException e) {}
        }
    }

    public void resolveBreakpoint(IBreakpointsTargetDMContext context, IBreakpoint breakpoint,
        Map<String, Object> bpAttributes, DataRequestMonitor<List<Map<String, Object>>> drm) 
    {
        try {
            drm.setData( getBreakpointAttributes(breakpoint, true) );
        } catch (CoreException e) {
            drm.setStatus(e.getStatus());
        }
        drm.done();
    }

    public Map<String, Object> getAllBreakpointAttributes(IBreakpoint platformBP, boolean bpManagerEnabled)
        throws CoreException 
    {
        IMarker marker = platformBP.getMarker(); 
        if (marker == null) {
            throw new DebugException(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, "Null marker for breakpoint: " + platformBP));
        }
        return marker.getAttributes();
    }

    public Map<String, Object> convertAttributes(Map<String, Object> platformBPAttr) {
        return platformBPAttr;
    }

    
}
