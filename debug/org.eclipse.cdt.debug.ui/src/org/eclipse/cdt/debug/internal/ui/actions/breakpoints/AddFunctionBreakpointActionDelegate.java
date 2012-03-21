/*******************************************************************************
 * Copyright (c) 2004, 2007-7 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
*******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints; 

import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * A delegate for the "Add Function Breakpoint" action.
 */
public class AddFunctionBreakpointActionDelegate extends ActionDelegate implements IViewActionDelegate {

	private IViewPart fView;
	private ISelection fSelection;
	private ToggleBreakpointAdapter fDefaultToggleTarget = new ToggleBreakpointAdapter();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init( IViewPart view ) {
		setView( view );
	}

	private void setView(IViewPart view) {
		fView = view;
	}

	protected IViewPart getView() {
		return fView;
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	    fSelection = selection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run( IAction action ) {
	    IToggleBreakpointsTarget toggleTarget = DebugUITools.getToggleBreakpointsTargetManager().getToggleBreakpointsTarget(fView, fSelection);
	    IToggleBreakpointsTargetCExtension cToggleTarget = null;
	    if (toggleTarget instanceof IToggleBreakpointsTargetCExtension) {
	        cToggleTarget = (IToggleBreakpointsTargetCExtension)toggleTarget;
	    } else { 
	        cToggleTarget = fDefaultToggleTarget;
	    }
	        
        try {
            cToggleTarget.createFunctionBreakpointInteractive(fView, fSelection);
        } catch (CoreException e) {
            CDebugUIPlugin.errorDialog( ActionMessages.getString( "AddWatchpointActionDelegate1.0" ), e ); //$NON-NLS-1$
        }
	}
}
