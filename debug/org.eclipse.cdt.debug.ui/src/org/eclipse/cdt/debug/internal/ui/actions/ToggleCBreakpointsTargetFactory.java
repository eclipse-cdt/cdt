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

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 */
public class ToggleCBreakpointsTargetFactory implements IToggleBreakpointsTargetFactory {
    
    public static String TOGGLE_C_BREAKPOINT_TARGET_ID = CDebugUIPlugin.getUniqueIdentifier() + ".toggleCBreakpointTarget"; //$NON-NLS-1$
    
    private static Set<String> TOGGLE_TARGET_IDS = new HashSet<String>(1);
    static {
        TOGGLE_TARGET_IDS.add(TOGGLE_C_BREAKPOINT_TARGET_ID);
    }
    
    private ToggleBreakpointAdapter fCToggleBreakpointTarget = new ToggleBreakpointAdapter();
    
    @Override
	public IToggleBreakpointsTarget createToggleTarget(String targetID) {
        if (TOGGLE_C_BREAKPOINT_TARGET_ID.equals(targetID)) {
            return fCToggleBreakpointTarget;
        }
        return null;
    }
    
    @Override
	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
        // Return the debug context as a default if the currently selected context
        // is a CDT element.  Otherwise return null.
        Object element = getDebugContext(part).getFirstElement();
        if (element instanceof IAdaptable) {
            IDebugModelProvider modelProvider = 
                (IDebugModelProvider)((IAdaptable)element).getAdapter(IDebugModelProvider.class);
            if (modelProvider != null) {
                String[] models = modelProvider.getModelIdentifiers();
                for (String model : models) {
                    if (CDIDebugModel.getPluginIdentifier().equals(model) ||
                        ICBreakpoint.C_BREAKPOINTS_DEBUG_MODEL_ID.equals(model)) 
                    {
                        return TOGGLE_C_BREAKPOINT_TARGET_ID;
                    }
                }
            } else if (element instanceof IDebugElement) {
                if (CDIDebugModel.getPluginIdentifier().equals(((IDebugElement)element).getModelIdentifier()) ) {
                    return TOGGLE_C_BREAKPOINT_TARGET_ID;
                }
            }
        }
        return null;
    }
    
    @Override
	public String getToggleTargetDescription(String targetID) {
        return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CBreakpointDescription"); //$NON-NLS-1$
    }
    
    @Override
	public String getToggleTargetName(String targetID) {
        return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CBreakpointName"); //$NON-NLS-1$
    }
    
    @Override
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
