/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * Presents a custom properties dialog to configure the attibutes of a C/C++ breakpoint.
 */
public class CBreakpointPropertiesAction implements IObjectActionDelegate {

	private IWorkbenchPart fPart;

	private ICBreakpoint fBreakpoint;

	/**
	 * Constructor for CBreakpointPropertiesAction.
	 */
	public CBreakpointPropertiesAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
		fPart = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		PropertyDialogAction propertyAction = new PropertyDialogAction( CDebugUIPlugin.getActiveWorkbenchShell(), new ISelectionProvider() {

			public void addSelectionChangedListener( ISelectionChangedListener listener ) {
			}

			public ISelection getSelection() {
				return new StructuredSelection( getBreakpoint() );
			}

			public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
			}

			public void setSelection( ISelection selection ) {
			}
		} );
		propertyAction.run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.isEmpty() || ss.size() > 1 ) {
				return;
			}
			Object element = ss.getFirstElement();
			if ( element instanceof ICBreakpoint ) {
				setBreakpoint( (ICBreakpoint)element );
			}
		}
	}

	protected IWorkbenchPart getActivePart() {
		return fPart;
	}

	protected ICBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint( ICBreakpoint breakpoint ) {
		fBreakpoint = breakpoint;
	}
}