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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Test breakpoint.
 */
public class DsfTestBreakpoint extends Breakpoint {
    
    public static final String DSF_TEST_BREAKPOINT_MODEL_ID = "dsfTest"; 
    public static final String ATTR_IDE_PREFIX = DSF_TEST_BREAKPOINT_MODEL_ID + ".ide.";
    
    public static final String ATTR_ID = ATTR_IDE_PREFIX + "id"; 
    public static final String ATTR_NUM_TARGET_BREAKPOINTS = ATTR_IDE_PREFIX + "numTargetBreakpoints"; 
    public static final String ATTR_TRANSLATED = ATTR_IDE_PREFIX + "translated"; 
    public static final String ATTR_UNTRANSLATED = ATTR_IDE_PREFIX + "untranslated"; 
    public static final String ATTR_UPDATABLE = ATTR_IDE_PREFIX + "updatable"; 

    public static int fgIdCounter = 0;

    public DsfTestBreakpoint() throws CoreException {
        this(true, 1, "", "", "");
    }

    public DsfTestBreakpoint(final boolean enabled, final int numTargetBPs, final String translated, 
        final String untranslated, final String updatable) 
        throws CoreException 
    {
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot();
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IMarker marker = resource.createMarker("org.eclipse.cdt.tests.dsf.markerType.breakpoint");
                setMarker(marker);
                marker.setAttribute(IBreakpoint.ENABLED, enabled);
                marker.setAttribute(DsfTestBreakpoint.ATTR_ID, fgIdCounter++);                
                marker.setAttribute(DsfTestBreakpoint.ATTR_NUM_TARGET_BREAKPOINTS, numTargetBPs);
                marker.setAttribute(DsfTestBreakpoint.ATTR_TRANSLATED, translated);
                marker.setAttribute(DsfTestBreakpoint.ATTR_UNTRANSLATED, untranslated);
                marker.setAttribute(DsfTestBreakpoint.ATTR_UPDATABLE, updatable);
            }
        };
        run(getMarkerRule(resource), runnable);
        DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
    }
    
    public Integer getID() throws CoreException {
        return (Integer)ensureMarker().getAttribute(ATTR_ID);
    }
    
    public String getModelIdentifier() {
        return DSF_TEST_BREAKPOINT_MODEL_ID;
    }
    
    
}
