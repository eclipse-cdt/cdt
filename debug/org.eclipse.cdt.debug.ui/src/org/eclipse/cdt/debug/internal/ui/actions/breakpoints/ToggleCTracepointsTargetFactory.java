/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggle tracepoints target factory for the CEditor.
 * We use a separate factory so that we can control it through an action set.
 * 
 * @since 6.1
 */
public class ToggleCTracepointsTargetFactory implements IToggleBreakpointsTargetFactory {
    
    public static String TOGGLE_C_TRACEPOINT_TARGET_ID = CDebugUIPlugin.getUniqueIdentifier() + ".toggleCTracepointTarget"; //$NON-NLS-1$
    
    private static Set<String> TOGGLE_TARGET_IDS = new HashSet<String>(1);
    static {
        TOGGLE_TARGET_IDS.add(TOGGLE_C_TRACEPOINT_TARGET_ID);
    }
    
    private ToggleTracepointAdapter fCToggleTracepointTarget = new ToggleTracepointAdapter();
    
    @Override
	public IToggleBreakpointsTarget createToggleTarget(String targetID) {
        if (TOGGLE_C_TRACEPOINT_TARGET_ID.equals(targetID)) {
            return fCToggleTracepointTarget;
        }
        return null;
    }
    
    @Override
	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
        return null;
    }
    
    @Override
	public String getToggleTargetDescription(String targetID) {
        if (TOGGLE_C_TRACEPOINT_TARGET_ID.equals(targetID)) {
            return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CTracepointDescription"); //$NON-NLS-1$
        }
        return null;
    }
    
    @Override
	public String getToggleTargetName(String targetID) {
        if (TOGGLE_C_TRACEPOINT_TARGET_ID.equals(targetID)) {
            return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CTracepointName"); //$NON-NLS-1$
        }
        return null;
    }
    
    @Override
    public Set<?> getToggleTargets(IWorkbenchPart part, ISelection selection) {
        return TOGGLE_TARGET_IDS;
    }
}
