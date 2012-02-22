/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - Converted into a command
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Presents a custom properties dialog to configure the attibutes of a C/C++ breakpoint.
 */
public class CBreakpointPropertiesHandler extends AbstractHandler {

	/**
	 * Constructor for CBreakpointPropertiesAction.
	 */
	public CBreakpointPropertiesHandler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        final ICBreakpoint bp = getBreakpoint(event.getApplicationContext());
	    
	    if (part != null && bp != null) {
	        CDebugUIUtils.editBreakpointProperties(part, bp);
        }
	    
	    return null;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
	    setBaseEnabled( getBreakpoint(evaluationContext) != null );
	}
	
	private ICBreakpoint getBreakpoint(Object evaluationContext) {
	    if (evaluationContext instanceof IEvaluationContext) {
	        Object s = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
	        if (s instanceof IStructuredSelection) {
	            IStructuredSelection ss = (IStructuredSelection)s;
	            if (ss.size() == 1) {
    	            return (ICBreakpoint)DebugPlugin.getAdapter(ss.getFirstElement(), ICBreakpoint.class);
	            }
	        }
	    }
	    return null;
	}
}
