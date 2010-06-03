/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
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
 * Toggle breakpoints target factory for disassembly parts.
 *
 * @since 2.1
 */
public class ToggleBreakpointsTargetFactory implements IToggleBreakpointsTargetFactory {

	/**
	 * Toggle breakpoint target-id for normal C breakpoints.
	 * Note: The id must be the same as in <code>ToggleCBreakpointsTargetFactory</code>
	 *       which is used for the editor.  We need the id to be the same so that when
	 *       the user goes from editor to DSF disassembly view, the choice of breakpoint
	 *       targets looks the same and is remembered.
	 *       To use the same id though, we must be careful not to have the two factories
	 *       return the same id for the same part, or else it may confuse things.
	 *       This is why this factory only returns this id for the DSF disassembly part,
	 *       leaving <code>ToggleCBreakpointsTargetFactory</code> to return the same id
	 *       for the editor.
	 */
	public static final String TOGGLE_C_BREAKPOINT_TARGET_ID = CDebugUIPlugin.PLUGIN_ID + ".toggleCBreakpointTarget"; //$NON-NLS-1$
	
	private static final Set<String> TOGGLE_TARGET_IDS = new HashSet<String>(1);
	static {
		TOGGLE_TARGET_IDS.add(TOGGLE_C_BREAKPOINT_TARGET_ID);
	}

	private static final IToggleBreakpointsTarget fgDisassemblyToggleBreakpointsTarget = new DisassemblyToggleBreakpointsTarget();

	public ToggleBreakpointsTargetFactory() {
	}

	public IToggleBreakpointsTarget createToggleTarget(String targetID) {
		if (TOGGLE_C_BREAKPOINT_TARGET_ID.equals(targetID)) {
			return fgDisassemblyToggleBreakpointsTarget;
		}
		return null;
	}

	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IDisassemblyPart) {
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
		}
		return null;
	}

	public String getToggleTargetDescription(String targetID) {
		return Messages.ToggleBreakpointsTargetFactory_description;
	}

	public String getToggleTargetName(String targetID) {
		return Messages.ToggleBreakpointsTargetFactory_name;
	}

	public Set<String> getToggleTargets(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IDisassemblyPart) {
			return TOGGLE_TARGET_IDS;
		}
		return Collections.emptySet();
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
