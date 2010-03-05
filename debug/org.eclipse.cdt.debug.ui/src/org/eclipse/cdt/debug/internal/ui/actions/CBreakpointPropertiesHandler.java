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
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.internal.ui.CBreakpointContext;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PropertyDialogAction;
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

	public Object execute(ExecutionEvent event) throws ExecutionException {
	    IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        ICBreakpoint bp = getBreakpoint(event.getApplicationContext());
	    
	    if (part != null && bp != null) {
	        ISelection debugContext = DebugUITools.getDebugContextManager().
	            getContextService(part.getSite().getWorkbenchWindow()).getActiveContext();

	        final CBreakpointContext bpContext = new CBreakpointContext(bp, debugContext);
	        
            PropertyDialogAction propertyAction = new PropertyDialogAction( part.getSite(), new ISelectionProvider() {
    
                public void addSelectionChangedListener( ISelectionChangedListener listener ) {
                }
    
                public ISelection getSelection() {
                    return new StructuredSelection( bpContext );
                }
    
                public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
                }
    
                public void setSelection( ISelection selection ) {
                    assert false; // Not supported
                }
            } );
            propertyAction.run();
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
