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
package org.eclipse.cdt.tests.dsf.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2;
import org.eclipse.cdt.dsf.debug.service.IBreakpointAttributeTranslator2;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2.BreakpointEventType;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2.ITargetBreakpointInfo;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * 
 */
public class DsfTestBreakpointAttributeTranslator2 implements IBreakpointAttributeTranslator2 {

    public void initialize(BreakpointsMediator2 mediator) {
    }

    public void dispose() {
    }

    public boolean supportsBreakpoint(IBreakpoint bp) {
        return DsfTestBreakpoint.DSF_TEST_BREAKPOINT_MODEL_ID.equals(bp.getModelIdentifier());
    }

    public void resolveBreakpoint(IBreakpointsTargetDMContext context, IBreakpoint breakpoint,
        Map<String, Object> bpAttrs, DataRequestMonitor<List<Map<String, Object>>> drm) 
    {
        Integer num = (Integer)bpAttrs.get(DsfTestBreakpoint.ATTR_NUM_TARGET_BREAKPOINTS);
        if (num == null) {
            num = new Integer(1); 
        }
        List<Map<String, Object>> subBpsAttrs = new ArrayList<Map<String, Object>>(num);
        for (int i = 0; i < num; i++) {
            Map<String, Object> subBpAttr = new HashMap<String, Object>(bpAttrs);
            subBpAttr.put(DsfTestBreakpoints.ATTR_SUB_ID, i);
            subBpsAttrs.add(subBpAttr);
        }
        drm.setData(subBpsAttrs);
        drm.done();
    }

    public Map<String, Object> getAllBreakpointAttributes(IBreakpoint platformBP, boolean bpManagerEnabled)
        throws CoreException 
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> platformBPAttr = platformBP.getMarker().getAttributes();
        if (!bpManagerEnabled) {
            platformBPAttr.put(IBreakpoint.ENABLED, Boolean.FALSE);
        }

        return convertAttributes(platformBPAttr);
    }

    public Map<String, Object> convertAttributes(Map<String, Object> platformBPAttr) {
        Map<String, Object> debugAttrs = new HashMap<String, Object>(platformBPAttr.size());
        for (Map.Entry<String, Object> entry : platformBPAttr.entrySet()) {
            if ( DsfTestBreakpoint.ATTR_TRANSLATED.equals(entry.getKey())) {
                debugAttrs.put(DsfTestBreakpoints.ATTR_TRANSLATED, entry.getValue());
            } 
            else if ( IBreakpoint.ENABLED.equals(entry.getKey())) {
                debugAttrs.put(DsfTestBreakpoints.ATTR_ENABLED, entry.getValue());
            } else {
                debugAttrs.put(entry.getKey(), entry.getValue());
            }
        }
        return debugAttrs;
    }

    public void updateBreakpointsStatus(
        Map<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>> bpsInfo,
        BreakpointEventType eventType) 
    {
        
    }

    public boolean canUpdateAttributes(IBreakpoint bp, IBreakpointsTargetDMContext context,
        Map<String, Object> attributes) 
    {
        for (String attribute : attributes.keySet()) {
            if (!DsfTestBreakpoint.ATTR_UPDATABLE.equals(attribute)) {
                return false;
            }
        }
        return true;
    }

}
