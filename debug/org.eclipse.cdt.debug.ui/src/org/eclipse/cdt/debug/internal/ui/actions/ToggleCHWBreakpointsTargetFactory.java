/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Copied and modified from ToggleCBreakpointsTargetFactory
 * @author thloh
 */
public class ToggleCHWBreakpointsTargetFactory implements IToggleBreakpointsTargetFactory {
    
    public static String TOGGLE_C_HWBREAKPOINT_TARGET_ID = CDebugUIPlugin.getUniqueIdentifier() + ".toggleCHWBreakpointTarget"; //$NON-NLS-1$
    
    private static Set<String> TOGGLE_TARGET_IDS = new HashSet<String>(1);
    static {
    	TOGGLE_TARGET_IDS.add(TOGGLE_C_HWBREAKPOINT_TARGET_ID);
    }
    
    private ToggleHWBreakpointAdapter fCToggleHWBreakpointTarget = new ToggleHWBreakpointAdapter();
    
    public IToggleBreakpointsTarget createToggleTarget(String targetID) {
        
        if (TOGGLE_C_HWBREAKPOINT_TARGET_ID.equals(targetID)) {
        	return fCToggleHWBreakpointTarget;
        }
        return null;
    }
    
    public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
    	// we have no preference
    	return null;
    }
    
    public String getToggleTargetDescription(String targetID) {
    		return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CHWBreakpointDescription"); //$NON-NLS-1$
    }
    
    public String getToggleTargetName(String targetID) {
    	return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CHWBreakpointName"); //$NON-NLS-1$
    }
    
    @SuppressWarnings("unchecked")
    public Set getToggleTargets(IWorkbenchPart part, ISelection selection) {
        return TOGGLE_TARGET_IDS;
    }
    
    private IStructuredSelection getDebugContext(IWorkbenchPart part) {
        ISelection selection = DebugUITools.getDebugContextManager().
            getContextService(part.getSite().getWorkbenchWindow()).getActiveContext();
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection)selection;
        } 
        return StructuredSelection.EMPTY;
    }

}
